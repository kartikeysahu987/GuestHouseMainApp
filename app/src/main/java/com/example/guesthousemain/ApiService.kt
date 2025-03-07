package com.example.guesthousemain.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

var url: String = "https://guesthouseportalbackendiitr-production.up.railway.app/"
// New data class for applicant details
data class Applicant(
    @SerializedName("name")
    val name: String,
    @SerializedName("designation")
    val designation: String,
    @SerializedName("department")
    val department: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("email")
    val email: String
)

// Updated CreateReservationRequest with applicant as an object and new fields reviewers and subroles
data class CreateReservationRequest(
    @SerializedName("numberOfGuests")
    val numberOfGuests: Int,
    @SerializedName("numberOfRooms")
    val numberOfRooms: Int,
    @SerializedName("roomType")
    val roomType: String,
    @SerializedName("purpose")
    val purpose: String,
    @SerializedName("guestName")
    val guestName: String,
    @SerializedName("arrivalDate")
    val arrivalDate: String,  // format: "YYYY-MM-DD"
    @SerializedName("arrivalTime")
    val arrivalTime: String,  // format: "HH:mm"
    @SerializedName("departureDate")
    val departureDate: String,  // format: "YYYY-MM-DD"
    @SerializedName("departureTime")
    val departureTime: String,  // format: "HH:mm"
    @SerializedName("address")
    val address: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("applicant")
    val applicant: Applicant?,
    @SerializedName("reviewers")
    val reviewers: String,
    @SerializedName("subroles")
    val subroles: String
)

// The CreateReservationResponse and the ApiService remain the same.
data class CreateReservationResponse(
    @SerializedName("message")
    val message: String
)

// --- OTP Endpoints ---
data class OtpRequest(
    @SerializedName("email")
    val email: String
)

data class OtpResponse(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("error")
    val error: String? = null
)

data class OtpVerifyRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String
)

data class OtpVerifyResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("user")
    val user: String? = null,
    @SerializedName("message")
    val message: String? = null
)

// --- Login Endpoint ---
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("user")
    val user: UserData?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("accessToken")
    val accessToken: String?,
    @SerializedName("refreshToken")
    val refreshToken: String?
)

// --- Registration Endpoints ---
data class RegisterUserRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("contact")
    val contact: String
)

data class RegisterUserResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("accessToken")
    val accessToken: String?,
    @SerializedName("refreshToken")
    val refreshToken: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("user")
    val user: UserData?
)

data class UserData(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("contact")
    val contact: String?
)

// --- Reservation Endpoints ---
data class Reservation(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("guestEmail")
    val guestEmail: String?,
    @SerializedName("status")
    val status: String?
)
data class GoogleSignInRequest(
    @SerializedName("idToken")
    val idToken: String,
    @SerializedName("email")
    val email: String
)



interface AuthService {
    @POST("auth/otp")
    fun sendOtp(@Body otpRequest: OtpRequest): Call<OtpResponse>

    @POST("auth/verifyotp")
    fun verifyOtp(@Body request: OtpVerifyRequest): Call<OtpVerifyResponse>

    @POST("auth/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("auth/register")
    fun registerUser(@Body request: RegisterUserRequest): Call<RegisterUserResponse>

    @POST("auth/googleLogin")
    fun googleSignIn(@Body request: GoogleSignInRequest): Call<LoginResponse>

    @GET("reservation/pending")
    fun getPendingReservations(
        @Header("accessToken") accessToken: String,
        @Header("refreshToken") refreshToken: String
    ): Call<List<Reservation>>

    @GET("reservation/approved")
    fun getApprovedReservations(
        @Header("accessToken") accessToken: String,
        @Header("refreshToken") refreshToken: String
    ): Call<List<Reservation>>

    @GET("reservation/rejected")
    fun getRejectedReservations(
        @Header("accessToken") accessToken: String,
        @Header("refreshToken") refreshToken: String
    ): Call<List<Reservation>>

    // New endpoint for creating a reservation
    @POST("reservation")
    fun createReservation(
        @Header("accessToken") accessToken: String,
        @Header("refreshToken") refreshToken: String,
        @Body reservation: CreateReservationRequest
    ): Call<CreateReservationResponse>
}

object ApiService {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authService: AuthService = retrofit.create(AuthService::class.java)
}
