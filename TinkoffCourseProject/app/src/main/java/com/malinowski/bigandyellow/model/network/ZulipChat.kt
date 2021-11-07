package com.malinowski.bigandyellow.model.network

import io.reactivex.Single
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Suppress("SpellCheckingInspection")
interface ZulipChat {

    @GET("streams")
    fun getStreams(): Single<ResponseBody>

    @GET("users/me/subscriptions")
    fun getSubscribedStreams(): Single<ResponseBody>

    @GET("users/me/{stream_id}/topics")
    fun getTopicsInStream(@Path("stream_id") streamId: Int): Single<ResponseBody>

    @GET("users/me/{stream_id}/topics")
    fun getTopicsInStreamCall(@Path("stream_id") streamId: Int): Call<ResponseBody>

    @GET("users")
    fun getUsers(): Single<ResponseBody>

    @GET("users/{id}/presence")
    fun getPresence(@Path("id") userId: Int): Single<ResponseBody>

    @GET("users/me")
    fun getOwnUser(): Single<ResponseBody>

    @GET("messages")
    fun getMessages(
        @Query("anchor") anchor: String = "newest",
        @Query("num_before") numBefore: Int = 10,
        @Query("num_after") numAfter: Int = 10,
        @Query("narrow") narrow: String,
    ): Single<ResponseBody>

    @Serializable
    data class NarrowElement(val operator: String, val operand: String)

    @Serializable
    data class NarrowElementInt(val operator: String, val operand: Int)

}