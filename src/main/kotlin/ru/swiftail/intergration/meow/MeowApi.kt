package ru.swiftail.intergration.meow

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.result.Result
import javax.inject.Singleton

@Singleton
class MeowApi (private val fuel: FuelManager) {

    private val deserializer = gsonDeserializerOf(MeowResponse::class.java)

    suspend fun getRandomPicture(): Result<MeowResponse, FuelError> {
        return fuel.get("https://meow.senither.com/v1/random/")
            .awaitObjectResult(deserializer)
    }

}
