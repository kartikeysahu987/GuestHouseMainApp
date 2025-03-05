package com.example.guesthousemain.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Data class for the Send OTP request.
 * Example request body: { "email": "user@example.com" }
 */
data class OtpRequest(
    @SerializedName("email")
    val email: String
)

/**
 * Data class for the Send OTP response.
 * Possible success response: { "message": "OTP sent successfully" }
 * Possible error response:   { "error": "Email cannot be empty" }
 */
data class OtpResponse(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("error")
    val error: String? = null
)

/**
 * Data class for the Verify OTP request.
 * Example request body: { "email": "user@example.com", "otp": "123456" }
 */
data class OtpVerifyRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String
)

/**
 * Data class for the Verify OTP response.
 * Success response: { "success": true, "user": "user@example.com", "message": "OTP verified successfully" }
 * Failure response: { "success": false, "message": "OTP entered is wrong" }
 */
data class OtpVerifyResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("user")
    val user: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("error")
    val error: String? = null
)


// Create a logging interceptor for debugging
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

// Build the OkHttp client
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()

// Build the Retrofit instance
private val retrofit = Retrofit.Builder()
    .baseUrl("https://guesthouseportalbackendiitr-production.up.railway.app/") // Update if needed
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// In ApiService.kt
data class RegisterRequest(
    val email: String,
    val name: String,
    val contact: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)


interface AuthService {

    /**
     * Sends an email to /auth/otp to generate and store an OTP.
     * Returns OtpResponse with either "message" or "error".
     */
    @POST("auth/otp")
    fun sendOtp(@Body request: OtpRequest): Call<OtpResponse>

    /**
     * Verifies an email/otp pair at /auth/verifyOtp (or /verifyotp).
     * Returns OtpVerifyResponse with "success", "message", "user", or "error".
     */
    @POST("auth/verifyotp") // or "auth/verifyOtp" depending on your route
    fun verifyOtp(@Body request: OtpVerifyRequest): Call<OtpVerifyResponse>


    @POST("auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

}

object ApiService {
    val authService: AuthService = retrofit.create(AuthService::class.java)
}
