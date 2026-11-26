package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object BaserowNetworkFactory {

    private const val PLACEHOLDER_TOKEN = "YOUR_BASEROW_API_TOKEN"

    fun hasValidToken(): Boolean {
        val token = BuildConfig.BASEROW_API_TOKEN.trim()
        return token.isNotEmpty() && token != PLACEHOLDER_TOKEN && token != "null"
    }

    fun createMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun createApiService(): BaserowApiService {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = BuildConfig.BASEROW_API_TOKEN.trim()
            val builder = original.newBuilder()
                .header("Accept", "application/json")
            if (token.isNotEmpty() && token != PLACEHOLDER_TOKEN) {
                builder.header("Authorization", "Token $token")
            }
            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = createMoshi()

        val baseUrl = BuildConfig.BASEROW_BASE_URL.let {
            if (it.endsWith("/")) it else "$it/"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BaserowApiService::class.java)
    }
}
