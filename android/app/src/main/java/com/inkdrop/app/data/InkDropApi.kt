package com.inkdrop.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class SendRequest(
    val url: String
)

data class SendResponse(
    val success: Boolean,
    val title: String,
    val message: String
)

data class ErrorResponse(
    val detail: String
)

data class HealthResponse(
    val status: String,
    val service: String
)

interface InkDropApi {

    @GET("/")
    suspend fun healthCheck(): Response<HealthResponse>

    @POST("/send-to-kindle")
    suspend fun sendToKindle(@Body request: SendRequest): Response<SendResponse>
}
