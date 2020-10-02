package ru.swiftail.intergration.fuel

import com.github.kittinunf.fuel.core.FuelManager
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class FuelFactory  {

    @Singleton
    fun getFuelManager(): FuelManager {
        return FuelManager.instance
    }

}
