package com.djb.aievo.core.data

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface CloudApiService {
    @GET("/status")
    suspend fun getSystemStatus(): Map<String, Any>

    @POST("/execute-cycle")
    suspend fun triggerCloudCycle(): Map<String, String>
}

object RetrofitClient {
    private const val BASE_URL = "https://your-aievo-cloud-app.render.com" // ستحتاج لتغيير هذا بعد الرفع

    val service: CloudApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(CloudApiService::class.java)
    }
}
