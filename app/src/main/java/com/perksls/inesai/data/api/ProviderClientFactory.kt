package com.perksls.inesai.data.api

import com.perksls.inesai.data.model.AIProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ProviderClientFactory {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // Placeholder obrigatório pelo Retrofit — o URL real vem via @Url em cada chamada
    private val service: GenericAIService by lazy {
        Retrofit.Builder()
            .baseUrl("https://localhost/")
            .client(httpClient)
            .build()
            .create(GenericAIService::class.java)
    }

    fun getService(provider: AIProvider): GenericAIService = service

    fun clearCache() { /* sem cache — URLs são dinâmicos */ }
}
