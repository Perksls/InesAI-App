package com.perksls.inesai.data.api

import com.google.gson.*
import com.perksls.inesai.data.model.ContentPart
import com.perksls.inesai.data.model.OpenAIMessage
import com.perksls.inesai.data.model.OpenAIRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody

class MultimodalRequestConverter {

    private val gson = GsonBuilder()
        .registerTypeAdapter(OpenAIMessage::class.java, OpenAIMessageSerializer())
        .registerTypeAdapter(ContentPart::class.java, ContentPartSerializer())
        .create()

    fun convert(request: OpenAIRequest): RequestBody {
        val json = gson.toJson(request)
        return RequestBody.create("application/json".toMediaType(), json)
    }

    private class OpenAIMessageSerializer : JsonSerializer<OpenAIMessage> {
        override fun serialize(
            src: OpenAIMessage?,
            typeOfSrc: java.lang.reflect.Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return when (src) {
                is OpenAIMessage.TextMessage -> {
                    JsonObject().apply {
                        addProperty("role", src.role)
                        addProperty("content", src.content)
                    }
                }
                is OpenAIMessage.VisionMessage -> {
                    JsonObject().apply {
                        addProperty("role", src.role)
                        add("content", context?.serialize(src.content))
                    }
                }
                else -> JsonObject()
            }
        }
    }

    private class ContentPartSerializer : JsonSerializer<ContentPart> {
        override fun serialize(
            src: ContentPart?,
            typeOfSrc: java.lang.reflect.Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return when (src) {
                is ContentPart.Text -> {
                    JsonObject().apply {
                        addProperty("type", "text")
                        addProperty("text", src.text)
                    }
                }
                is ContentPart.ImageUrl -> {
                    JsonObject().apply {
                        addProperty("type", "image_url")
                        add("image_url", JsonObject().apply {
                            addProperty("url", src.image_url.url)
                        })
                    }
                }
                else -> JsonObject()
            }
        }
    }
}
