package com.perksls.inesai.data.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface GenericAIService {

    @Streaming
    @POST
    suspend fun streamChatCompletion(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body request: RequestBody
    ): Response<ResponseBody>

    @POST
    suspend fun chatCompletion(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body request: RequestBody
    ): Response<ResponseBody>
}
