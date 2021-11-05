package com.malinowski.bigandyellow.model

import io.reactivex.Single
import okhttp3.Credentials
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header

@Suppress("SpellCheckingInspection")
interface ZulipChat {
    @GET("users")
    fun getUsers(
        @Header("Authorization") token: String
        = Credentials.basic("malinowski221106@gmail.com", "rpIqzw4T4kZNj9pA993u3Qcx4Ugpm8p3")
    ): Single<ResponseBody>
}