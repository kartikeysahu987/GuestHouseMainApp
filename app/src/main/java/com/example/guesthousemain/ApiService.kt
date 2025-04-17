package com.example.guesthousemain.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

var url: String = "http://34.0.4.78:4751"

data class Reviewer(
    @SerializedName("role") val role: String,
    @SerializedName("status") val status: String? = null,
    @SerializedName("remarks") val remarks: String? = null
)

data class Applicant(
    @SerializedName("name") val name: String,
    @SerializedName("designation") val designation: String,
    @SerializedName("department") val department: String,
    @SerializedName("code") val code: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("email") val email: String
)

data class CreateReservationRequest(
    @SerializedName("numberOfGuests") val numberOfGuests: Int,
    @SerializedName("numberOfRooms") val numberOfRooms: Int,
    @SerializedName("roomType") val roomType: String,
    @SerializedName("purpose") val purpose: String,
    @SerializedName("guestName") val guestName: String,
    @SerializedName("arrivalDate") val arrivalDate: String,
    @SerializedName("arrivalTime") val arrivalTime: String,
    @SerializedName("departureDate") val departureDate: String,
    @SerializedName("departureTime") val departureTime: String,
    @SerializedName("address") val address: String,
    @SerializedName("category") val category: String,
    @SerializedName("source") val source: String,
    @SerializedName("applicant") val applicant: Applicant?,
    @SerializedName("reviewers") val reviewers: String,
    @SerializedName("subroles") val subroles: String
)

data class CreateReservationResponse(
    @SerializedName("message") val message: String
)

data class OtpRequest(@SerializedName("email") val email: String)
data class OtpResponse(@SerializedName("message") val message: String?, @SerializedName("error") val error: String?)
data class OtpVerifyRequest(@SerializedName("email") val email: String, @SerializedName("otp") val otp: String)
data class OtpVerifyResponse(@SerializedName("success") val success: Boolean?, @SerializedName("user") val user: String?, @SerializedName("message") val message: String?)

data class LoginRequest(@SerializedName("email") val email: String, @SerializedName("otp") val otp: String)
data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("user") val user: UserData?,
    @SerializedName("message") val message: String?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?
)

data class RegisterUserRequest(@SerializedName("email") val email: String, @SerializedName("name") val name: String, @SerializedName("contact") val contact: String)
data class RegisterUserResponse(@SerializedName("success") val success: Boolean, @SerializedName("accessToken") val accessToken: String?, @SerializedName("refreshToken") val refreshToken: String?, @SerializedName("message") val message: String?, @SerializedName("user") val user: UserData?)
data class UserData(@SerializedName("_id") val id: String?, @SerializedName("email") val email: String?, @SerializedName("name") val name: String?, @SerializedName("contact") val contact: String?)

data class Reservation(
    @SerializedName("_id") val id: String?,
    @SerializedName("guestEmail") val guestEmail: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("numberOfGuests") val numberOfGuests: Int? = null,
    @SerializedName("numberOfRooms") val numberOfRooms: Int? = null,
    @SerializedName("roomType") val roomType: String? = null,
    @SerializedName("purpose") val purpose: String? = null,
    @SerializedName("guestName") val guestName: String? = null,
    @SerializedName("arrivalDate") val arrivalDate: String? = null,
    @SerializedName("arrivalTime") val arrivalTime: String? = null,
    @SerializedName("departureDate") val departureDate: String? = null,
    @SerializedName("departureTime") val departureTime: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("applicant") val applicant: Applicant? = null,
    @SerializedName("reviewers") val reviewers: List<Reviewer>? = null
)

data class ReservationDetailResponse(@SerializedName("reservation") val reservation: Reservation)
data class GoogleSignInRequest(@SerializedName("idToken") val idToken: String, @SerializedName("email") val email: String)
data class MailRequest(@SerializedName("to") val to: String, @SerializedName("subject") val subject: String, @SerializedName("body") val body: String)
data class MailResponse(@SerializedName("message") val message: String)

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
    fun getPendingReservations(@Header("accessToken") accessToken: String, @Header("refreshToken") refreshToken: String): Call<List<Reservation>>

    @GET("reservation/approved")
    fun getApprovedReservations(@Header("accessToken") accessToken: String, @Header("refreshToken") refreshToken: String): Call<List<Reservation>>

    @GET("reservation/rejected")
    fun getRejectedReservations(@Header("accessToken") accessToken: String, @Header("refreshToken") refreshToken: String): Call<List<Reservation>>

    @POST("reservation")
    fun createReservation(@Header("accessToken") accessToken: String, @Header("refreshToken") refreshToken: String, @Body reservation: CreateReservationRequest): Call<CreateReservationResponse>

    @Multipart
    @POST("reservation")
    fun createReservationMultipart(
        @Header("accessToken") accessToken: String,
        @Header("refreshToken") refreshToken: String,
        @Part receipt: MultipartBody.Part,
        @Part("guestName") guestName: RequestBody,
        @Part("address") address: RequestBody,
        @Part("numberOfGuests") numberOfGuests: RequestBody,
        @Part("numberOfRooms") numberOfRooms: RequestBody,
        @Part("roomType") roomType: RequestBody,
        @Part("purpose") purpose: RequestBody,
        @Part("arrivalDate") arrivalDate: RequestBody,
        @Part("arrivalTime") arrivalTime: RequestBody,
        @Part("departureDate") departureDate: RequestBody,
        @Part("departureTime") departureTime: RequestBody,
        @Part("category") category: RequestBody,
        @Part("source") source: RequestBody,
        @Part("reviewers") reviewers: RequestBody
    ): Call<CreateReservationResponse>

    @GET("reservation/{id}")
    fun getReservationDetails(@Path("id") id: String, @Header("accessToken") accessToken: String, @Header("refreshToken") refreshToken: String): Call<ReservationDetailResponse>
}

interface MailService {
    @POST("mail")
    fun sendMail(@Body mailRequest: MailRequest): Call<MailResponse>
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
    val mailService: MailService = retrofit.create(MailService::class.java)
}
