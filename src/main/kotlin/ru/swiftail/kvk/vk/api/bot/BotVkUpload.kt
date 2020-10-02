package ru.swiftail.kvk.vk.api.bot

import com.vk.api.sdk.objects.photos.Photo
import com.vk.api.sdk.objects.photos.responses.PhotoUploadResponse
import ru.swiftail.kvk.vk.api.VkContext
import ru.swiftail.kvk.vk.api.file.FileProvider
import ru.swiftail.kvk.vk.lowlevel.runAsync

class BotVkUpload(private val vkContext: VkContext) {

    private suspend fun uploadPhoto(photo: FileProvider): PhotoUploadResponse {

        val photoUploadData = vkContext.llVk
            .photos()
            .getMessagesUploadServer(vkContext.actor)
            .runAsync()
            .await()

        return photo.use<PhotoUploadResponse> { file ->
            val req = vkContext.llVk
                .upload()
                .photo(photoUploadData.uploadUrl.toExternalForm(), file)

            req
                .runAsync()
                .await()
        }
    }

    suspend fun uploadMessagesPhoto(photo: FileProvider): MutableList<Photo> {

        val uploadedPhoto = uploadPhoto(photo)

        return vkContext.llVk
            .photos()
            .saveMessagesPhoto(vkContext.actor, uploadedPhoto.photosList)
            .server(uploadedPhoto.server)
            .hash(uploadedPhoto.hash)
            .runAsync()
            .await()
    }

}
