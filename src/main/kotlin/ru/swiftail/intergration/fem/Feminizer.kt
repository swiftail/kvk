package ru.swiftail.intergration.fem

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.result.map
import javax.inject.Singleton

@Singleton
class Feminizer(private val fuelManager: FuelManager) {

    private fun convertWords(words: String): String {
        var string = words
        for ((fem_w, rep) in priorityWords) {
            string = string
                .replace(Regex("(^|\\s)+$fem_w", RegexOption.IGNORE_CASE), "$1$rep")
                .capitalize()
        }
        return string
    }

    private fun makeFeminitives(word: String, gap: Boolean): List<String> {

        val gs = if(gap) "_" else ""

        fun regexLen(r: String): Int {
            return r.replace(Regex("\\[[^\\[\\]]*\\]"), "x").length
        }

        fun ending(tuple: Array<Any>): Regex {
            return Regex("^.*" + tuple.first() + "$", RegexOption.IGNORE_CASE)
        }

        fun offset(tuple: Array<Any>): Int {
            return tuple[1] as Int
        }

        fun constructFeminitive(stem: String, ending: String): String {
            return stem + gs + ending;
        }

        if (word.length < 3) return listOf(word)

        if (word in priorityWords) {
            val wrd = priorityWords[word] ?: error("Peepee poopoo")
            return listOf(wrd)
        }

        var stem = ""
        val currentEndings = mutableListOf(
            word.takeLast(4),
            word.takeLast(3),
            word.takeLast(2)
        )
        val feminitives = mutableListOf<String>()

        var found = false

        currentEndings.forEach { currentEnding ->
            if (!found) {
                for ((key, ends) in endings) {
                    ends.forEach { end ->

                        if (
                            regexLen(end[0] as String) == currentEnding.length
                            && ending(end).matches(currentEnding)
                        ) {

                            stem =
                                if (offset(end) == 0)
                                    word
                            else
                                    word.dropLast(offset(end))

                            val rule = key.split("+")

                            var prefix = ""
                            var allFemEndings = ""

                            if(rule.size > 1) {
                                prefix = rule[0]
                                allFemEndings = rule[1]
                            } else {
                                prefix = ""
                                allFemEndings = key
                            }

                            allFemEndings.split("|").forEach { e ->
                                feminitives.add(constructFeminitive(stem + prefix, e))
                            }

                            found = true

                        }

                    }
                }
            }
        }

        return if(feminitives.isEmpty())
            listOf(word)
        else
            feminitives
    }

    private suspend fun getWictionary(word: String, gap: Boolean): String? {

        val wikiPage = fuelManager
            .get("https://ru.wiktionary.org/w/index.php?action=raw&title=$word")
            .awaitStringResult(Charsets.UTF_8)

        val definition = wikiPage.map { page ->
            val wiki = page.split("\n")
            var definition = ""

            for ((n, line) in wiki.withIndex()) {
                if (line == "==== Значение ====") {

                    try {
                        var definitionLine = n + 1

                        if (wiki[n + 1].trim().isEmpty()
                            || wiki[n + 1].trim().matches(Regex("^\\{\\{[^\\}]*\\}\\}$"))
                        ) {
                            definitionLine = n + 2
                        }

                        definition = wiki[definitionLine]
                            .replace(Regex("^# ?"), "")
                            .replace(Regex("\\[{2}([^\\]|]*)\\]{2}"), "$1")
                            .replace(Regex("\\[{2}[^|]*\\|([^\\]]*)\\]{2}"), "$1")
                            .replace(Regex("\\{{2}[а-яА-Я]+\\.[^\\{\\}]*\\|[^\\{\\}]*\\}{2}\\s*"), "")
                            .replace(Regex("\\{{2}[а-яА-Я]+\\.[^\\{\\}]*\\|[^\\{\\}]*\\}{2}\\s*"), "")
                            .replace(Regex("\\{\\{-\\}\\}"), "")
                            .replace(Regex("\\{{2}помета\\s*\\|[^\\}]+\\}{2}"), "")
                            .replace(Regex("\\{{2}(действие)\\s*\\|([^#|]+)([#|].*lang=\\w{2})?\\}{2}"), "$1 «$2»")
                            .replace(Regex("\\{{2}(пример|семантика)(\\}{2}|\\s*\\|.*$)"), "")
                            .replace(Regex("\\{{2}[^\\{\\}]*\\|(lang=\\w{2})?"), "")
                            .replace(Regex("\\{{2}([^\\]|]*)\\}{2}"), "$1")
                            .replace(Regex("'''([^']*)'''"), "«$1»")
                            .replace(Regex("<!--[^>]*-->"), "")
                            .replace(Regex("\\}{2}"), "")
                            .replace(Regex("\\[[0-9]\\{1,\\}\\]"), "")
                            .replace(Regex("^\\s*[,;]\\s*"), "")
                            .replace(Regex("\\{{2}[^\\}]*$"), "")
                            .replace(Regex("(<([^>]+)>)"), "")
                            .replace(Regex("&nbsp;"), " ")
                            .replace(Regex(" ?$"), ".")
                            .replace(Regex("\\.+"), ".")
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }

                    break
                }
            }

            definition
        }.fold({ s -> s }, { null }) ?: return null

        val tokens = Regex("[\\wа-яА-Яё]+|\\d+| +|[.;,]|[^ \\w\\d\\t.;,]+")
            .findAll(definition)

        return convertWords(tokens.joinToString("") { makeFeminitives(it.value, gap)[0] })
    }


    suspend fun transform(word: String, gap: Boolean): String {

        val wictionary = getWictionary(word, gap) ?: "я не знаю что это"
        val feminitives = makeFeminitives(word, gap)

        return if(feminitives.first() == word) {
            "Это слово и так норм"
        } else {
            "\ud83d\ude0f ${feminitives.joinToString()}\n\u2049 $wictionary"
        }

    }

}
