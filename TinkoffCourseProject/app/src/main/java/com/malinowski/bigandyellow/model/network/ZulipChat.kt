package com.malinowski.bigandyellow.model.network

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

@Suppress("SpellCheckingInspection")
interface ZulipChat {
    @GET("users")
    fun getUsers(): Single<ResponseBody>

    @GET("users/{id}/presence")
    fun getPresence(@Path("id") id: Int): Single<ResponseBody>
}