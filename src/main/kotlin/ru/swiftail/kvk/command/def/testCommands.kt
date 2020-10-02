package ru.swiftail.kvk.command.def

import com.github.kittinunf.result.Result
import kotlinx.coroutines.delay
import org.apache.commons.lang3.time.DurationFormatUtils
import ru.swiftail.kvk.command.api.command.args.GenericArguments
import ru.swiftail.kvk.command.api.command.spec.CommandSpec
import ru.swiftail.kvk.reflection.Command
import ru.swiftail.intergration.cataas.CataasApi
import ru.swiftail.intergration.fem.Feminizer
import ru.swiftail.intergration.meow.MeowApi
import ru.swiftail.intergration.meow.MeowType.*
import ru.swiftail.kvk.vk.api.file.ExtensionResolverStrategy
import ru.swiftail.kvk.vk.api.file.WebFileProvider
import java.awt.Color
import java.io.FileNotFoundException
import java.net.URI
import java.time.Duration
import kotlin.math.max
import kotlin.math.min

@Command(["echo"])
val testCommand = CommandSpec
    .builder()
    .description("Эхо")
    .arguments(GenericArguments.remainingJoinedStrings("message"))
    .executor { src, args ->
        src.reply(args.requireOne<String>("message")).await()
    }
    .build()

@Command(["dice"])
val diceCommand = CommandSpec
    .builder()
    .description("ЫЫЫ")
    .arguments(
        GenericArguments.integer("to"),
        GenericArguments.optional(GenericArguments.integer("from"), 0)
    )
    .executor { src, args ->
        val a1 = args.getOne<Int>("to").get()
        val a2 = args.getOne<Int>("from").get()

        val from = min(a1, a2)
        val to = max(a1, a2)

        src.reply("($from,$to) => ${(from..to).random()}").await()
    }
    .build()

@Command(["randomcat"])
val randomCatCommand = CommandSpec
    .builder()
    .description("random cat")
    .executor { src, args ->

        val meowApi = wire<MeowApi>()

        when (val result = meowApi.getRandomPicture()) {
            is Result.Success -> {
                val meowData = result.value

                when (meowData.data.type) {
                    jpg, png, gifv -> {
                        val uploaded = botVk
                            .upload
                            .uploadMessagesPhoto(
                                WebFileProvider(
                                    URI(meowData.data.url),
                                    ExtensionResolverStrategy.FROM_URI
                                )
                            )
                            .first()

                        src.reply {
                            attachPhoto(uploaded)
                        }.await()
                    }
                    else -> {
                        src.reply(meowData.data.url).await()
                    }
                }
            }
            is Result.Failure -> {
                val error = result.error
            }
        }
    }
    .build()

@Command(["capi"])
val capiCommand = CommandSpec
    .builder()
    .arguments(
        GenericArguments.optional(GenericArguments.string("text")),
        GenericArguments.flags()
            .valueFlag(
                GenericArguments.choices(
                "filter",
                listOf("blur", "mono", "sepia", "negative", "paint", "pixel")
                    .map { it to it }
                    .toMap(),
                true
            ), "-filter")
            .valueFlag(
                GenericArguments.color("color"),
                "-color"
            )
            .valueFlag(
                GenericArguments.integer("size"),
                "-size"
            )
            .buildWith(GenericArguments.none())

    )
    .executor { src, args ->

        val cataasApi: CataasApi = wire()

        val text = args.getOne<String>("text")
        val color = args.getOne<Color>("color").orElse(Color.WHITE)
        val size = args.getOne<Int>("size").orElse(25)

        val filter = args.getOne<String>("filter")

        val imageUrl = cataasApi.getSays(
            text,
            color,
            size,
            filter
        )

        src.reply {
            try {
                uploadPhoto(imageUrl, ExtensionResolverStrategy.STATIC("jpg"))
            } catch (e: FileNotFoundException) {
                setText("кот не хочет это говорить")
            }
        }.await()

    }
    .build()

@Command(["cutie"])
val cutieCommand = CommandSpec
    .builder()
    .executor { src, args ->

        val cataasApi: CataasApi = wire()
        val imageUrl = cataasApi.getCute()

        src.reply {
            try {
                uploadPhoto(imageUrl, ExtensionResolverStrategy.STATIC("jpg"))
            } catch (e: FileNotFoundException) {
                setText("кот сломался")
            }
        }.await()
    }
    .build()

@Command(["timer"])
val timerCommand = CommandSpec
    .builder()
    .arguments(
        GenericArguments.duration("duration"),
        GenericArguments.optional(GenericArguments.remainingJoinedStrings("text"))
    )
    .executor { src, args ->

        val duration: Duration = args.requireOne("duration")
        val text = args.getOne<String>("text")
            .map { "($it)" }
            .orElse("")

        src.reply {
            setText("Ок, жду " + DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true))
        }.await()

        delay(duration.toMillis())

        src.reply ("*id${src.message.messageObject.fromId} Таймер $text").await()
    }
    .build()

@Command(["fem"])
val femCommand = CommandSpec
    .builder()
    .arguments(
        GenericArguments.string("text"),
        GenericArguments.flags()
            .flag("-gap")
            .buildWith(GenericArguments.none())
    )
    .executor { src, args ->

        val word = args.requireOne<String>("text")
        val feminizer: Feminizer = wire()

        src.reply(feminizer.transform(word, args.hasAny("gap"))).await()

    }
    .build()
