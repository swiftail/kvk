package ru.swiftail.kvk.reflection

import mu.KotlinLogging
import org.reflections.Reflections
import ru.swiftail.kvk.command.CommandRegistrar
import ru.swiftail.kvk.command.api.command.CommandCallable
import ru.swiftail.kvk.command.api.command.spec.CommandSpec
import java.lang.reflect.Method
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class CommandScanner(
    private val registrar: CommandRegistrar,
    private val reflections: Reflections
) {

    private fun resolveCommandCallableFromPseudoMethod(method: Method): CommandCallable? {

        val pseudoMethodName = method.name
        val staticFieldName = pseudoMethodName.removeSuffix("\$annotations")

        val originalClass = method.declaringClass

        val staticGetterName = "get" + staticFieldName.capitalize()
        val staticGetter = originalClass.getMethod(staticGetterName)

        return when (val value = staticGetter.invoke(null)) {
            is CommandCallable -> value
            is CommandSpec.Builder -> {
                logger.error { "CommandSpec.Builder was not finished: $staticFieldName" }
                value.build()
            }
            else -> {
                logger.error { "Invalid type annotated with @Command: $staticFieldName ${value::class.java}" }
                null
            }
        }
    }

    private fun getMethodsAnnotatedWithCommand(): List<Method> {
        return reflections
            .getSubTypesOf(Any::class.java)
            .flatMap { it.methods.toList() }
            .filter { it.isAnnotationPresent(Command::class.java) }
    }

    fun scanAndRegister() {

        logger.debug { "Scanning for commands" }

        val methods = getMethodsAnnotatedWithCommand()

        logger.debug { "Found ${methods.size} command descriptors" }

        methods
            .forEach { method ->
                val commandData = method.getAnnotation(Command::class.java)
                val commandCallable = resolveCommandCallableFromPseudoMethod(method) ?: return

                logger.debug { "Registering command: ${commandData.aliases.first()} (${commandData.aliases.drop(1)}) from ${method.declaringClass.canonicalName}" }

                registrar.register(commandData.aliases.toList(), commandCallable)
            }

        logger.info { "Registered ${registrar.getRegisteredCommandsSize()} commands" }

    }

}
