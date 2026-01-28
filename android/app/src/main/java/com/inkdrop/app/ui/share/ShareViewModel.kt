package com.inkdrop.app.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.inkdrop.app.data.ErrorResponse
import com.inkdrop.app.data.InkDropApi
import com.inkdrop.app.data.SendRequest
import com.inkdrop.app.data.SettingsRepository
import com.inkdrop.app.util.NotificationHelper
import com.inkdrop.app.util.UrlValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

sealed class ShareResult {
    object Idle : ShareResult()
    object Processing : ShareResult()
    data class Success(val title: String) : ShareResult()
    data class Error(val message: String) : ShareResult()
}

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _result = MutableStateFlow<ShareResult>(ShareResult.Idle)
    val result: StateFlow<ShareResult> = _result

    private val gson = Gson()

    fun processSharedUrl(sharedText: String) {
        viewModelScope.launch {
            _result.value = ShareResult.Processing

            // Check if server is configured
            if (!settingsRepository.isServerConfigured()) {
                _result.value = ShareResult.Error("Open Ink Drop to configure server URL")
                notificationHelper.showError("Open app to configure server URL")
                return@launch
            }

            // Extract and validate URL
            val url = UrlValidator.extractTwitterUrl(sharedText)
            if (url == null) {
                _result.value = ShareResult.Error("Not a valid Twitter/X article URL")
                notificationHelper.showError("Not a valid Twitter/X URL")
                return@launch
            }

            // Show progress notification
            notificationHelper.showProgress(url)

            try {
                // Create API client with configured server URL
                val serverUrl = settingsRepository.getServerUrl()
                val api = createApi(serverUrl)

                // Send to Kindle
                val response = api.sendToKindle(SendRequest(url))

                notificationHelper.dismissProgress()

                when {
                    response.isSuccessful -> {
                        val body = response.body()!!
                        _result.value = ShareResult.Success(body.title)
                        notificationHelper.showSuccess(body.title)
                    }
                    response.code() == 409 -> {
                        _result.value = ShareResult.Error("Article already sent")
                        notificationHelper.showError("Article already sent to Kindle")
                    }
                    response.code() == 401 -> {
                        _result.value = ShareResult.Error("Server authentication expired")
                        notificationHelper.showError("Server authentication expired")
                    }
                    response.code() == 400 -> {
                        val errorBody = response.errorBody()?.string()
                        val detail = parseErrorDetail(errorBody)
                        _result.value = ShareResult.Error(detail)
                        notificationHelper.showError(detail)
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        val detail = parseErrorDetail(errorBody)
                        _result.value = ShareResult.Error(detail)
                        notificationHelper.showError(detail)
                    }
                }
            } catch (e: Exception) {
                notificationHelper.dismissProgress()
                val message = when {
                    e.message?.contains("Unable to resolve host") == true -> "Connection failed - check server URL"
                    e.message?.contains("timeout") == true -> "Request timed out"
                    e.message?.contains("Connection refused") == true -> "Server unavailable"
                    else -> "Connection failed: ${e.message}"
                }
                _result.value = ShareResult.Error(message)
                notificationHelper.showError(message)
            }
        }
    }

    private fun createApi(baseUrl: String): InkDropApi {
        // Retrofit requires baseUrl to end with /
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InkDropApi::class.java)
    }

    private fun parseErrorDetail(errorBody: String?): String {
        if (errorBody == null) return "Unknown error"
        return try {
            val error = gson.fromJson(errorBody, ErrorResponse::class.java)
            error.detail
        } catch (e: Exception) {
            "Server error"
        }
    }
}
