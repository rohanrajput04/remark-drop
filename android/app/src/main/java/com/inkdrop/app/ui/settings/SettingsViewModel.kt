package com.inkdrop.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inkdrop.app.data.InkDropApi
import com.inkdrop.app.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

sealed class ConnectionStatus {
    object Idle : ConnectionStatus()
    object Testing : ConnectionStatus()
    object Success : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    val serverUrl: StateFlow<String> = settingsRepository.serverUrl
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsRepository.DEFAULT_SERVER_URL
        )

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Idle)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    fun saveServerUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setServerUrl(url)
            _connectionStatus.value = ConnectionStatus.Idle
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.Testing

            try {
                val url = settingsRepository.getServerUrl()
                if (url.isBlank() || url == SettingsRepository.DEFAULT_SERVER_URL) {
                    _connectionStatus.value = ConnectionStatus.Error("Please enter a server URL first")
                    return@launch
                }

                val api = createApi(url)
                val response = api.healthCheck()

                if (response.isSuccessful && response.body()?.status == "ok") {
                    _connectionStatus.value = ConnectionStatus.Success
                } else {
                    _connectionStatus.value = ConnectionStatus.Error(
                        "Server returned: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "Cannot reach server - check URL"
                    e.message?.contains("timeout") == true ->
                        "Connection timed out"
                    e.message?.contains("Connection refused") == true ->
                        "Connection refused - is server running?"
                    else -> "Connection failed: ${e.message}"
                }
                _connectionStatus.value = ConnectionStatus.Error(message)
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
}
