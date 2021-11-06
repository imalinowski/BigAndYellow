package com.malinowski.bigandyellow.model.network

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

@Suppress("SpellCheckingInspection")
interface ZulipChat {

    @GET("streams")
    fun getStreams(): Single<ResponseBody>

    @GET("users/me/subscriptions")
    fun getSubscribedStreams(): Single<ResponseBody>

    @GET("users/me/{stream_id}/topics")
    fun getTopicsInStream(@Path("stream_id") streamId: Int): Single<ResponseBody>

    @GET("users")
    fun getUsers(): Single<ResponseBody>

    @GET("users/{id}/presence")
    fun getPresence(@Path("id") userId: Int): Single<ResponseBody>

    @GET("users/me")
    fun getOwnUser(): Single<ResponseBody>
}