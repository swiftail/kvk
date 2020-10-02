package ru.swiftail.kvk.reflection

import io.micronaut.context.annotation.Factory
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import javax.inject.Named
import javax.inject.Singleton

@Factory
class ReflectionsFactory(
    @Named("KVK_APPLICATION_PACKAGE") private val applicationPackage: String
) {

    @Singleton
    fun getReflections(): Reflections {

        val classLoadersList = arrayOf(
            ClasspathHelper.contextClassLoader(),
            ClasspathHelper.staticClassLoader()
        )

        return Reflections(
            ConfigurationBuilder()
                .setScanners(SubTypesScanner(false /* don't exclude Object.class */), ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(*classLoadersList))
                .forPackages(applicationPackage)

        )
    }

}
