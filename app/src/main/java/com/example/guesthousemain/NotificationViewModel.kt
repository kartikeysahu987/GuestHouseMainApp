package com.example.guesthousemain
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.example.guesthousemain.network.Notification
import com.example.guesthousemain.network.MarkAsReadRequest
import com.example.guesthousemain.network.MarkAsReadResponse

import com.example.guesthousemain.network.ApiService


class NotificationViewModel : ViewModel() {

    var accessToken: String = ""
        private set

    var refreshToken: String = ""
        private set

    var isDarkTheme: Boolean = false
        private set

    fun initialize(access: String, refresh: String, dark: Boolean) {
        accessToken = access
        refreshToken = refresh
        isDarkTheme = dark
    }

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchNotifications() {
        _isLoading.value = true
        _error.value = null

        ApiService.notificationService.getNotifications(accessToken, refreshToken)
            .enqueue(object : Callback<List<Notification>> {
                override fun onResponse(
                    call: Call<List<Notification>>,
                    response: Response<List<Notification>>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        _notifications.value = response.body() ?: emptyList()
                    } else {
//                        _error.value = "Failed to fetch notifications: ${response.message()}"
                        val errorBody = response.errorBody()?.string()
                        _error.value = "Failed to fetch notifications: ${response.code()} - ${errorBody ?: response.message()}"

                    }
                }

                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                    _isLoading.value = false
                    _error.value = "Network error: ${t.message}"
                }
            })
    }

    fun fetchUnreadCount() {
        ApiService.notificationService.getUnreadCount(accessToken, refreshToken)
            .enqueue(object : Callback<Map<String, Int>> {
                override fun onResponse(
                    call: Call<Map<String, Int>>,
                    response: Response<Map<String, Int>>
                ) {
                    if (response.isSuccessful) {
                        _unreadCount.value = response.body()?.get("count") ?: 0
                    }
                }

                override fun onFailure(call: Call<Map<String, Int>>, t: Throwable) {
                    // Handle error but don't show it to user for this background operation
                }
            })
    }

    fun markAsRead(notificationId: String) {
        ApiService.notificationService.markAsRead(
            accessToken,
            refreshToken,
            MarkAsReadRequest(notificationId)
        ).enqueue(object : Callback<MarkAsReadResponse> {
            override fun onResponse(
                call: Call<MarkAsReadResponse>,
                response: Response<MarkAsReadResponse>
            ) {
                if (response.isSuccessful) {
                    // Update the local notification list to mark this as read
                    _notifications.value = _notifications.value.filterNot {
                        it.id == notificationId
                    }
                }
            }

            override fun onFailure(call: Call<MarkAsReadResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }
}