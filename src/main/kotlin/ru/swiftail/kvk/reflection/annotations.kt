package ru.swiftail.kvk.reflection

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command (val aliases: Array<String>)

