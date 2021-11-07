package com.malinowski.bigandyellow.model.network

import io.reactivex.Single
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

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

    @FormUrlEncoded
    @POST("messages")
    fun sendMessage(
        @Field("type") type: String,
        @Field("to") to: String,
        @Field("content") content: String,
        @Field("topic") topic: String = ""
    ): Single<ResponseBody>

    @GET("messages")
    fun getMessages(
        @Query("anchor") anchor: String = "newest",
        @Query("num_before") numBefore: Int = 1000,
        @Query("num_after") numAfter: Int = 1000,
        @Query("narrow") narrow: String,
    ): Single<ResponseBody>

    @Serializable
    data class NarrowElement(val operator: String, val operand: String)

    @Serializable
    data class NarrowElementInt(val operator: String, val operand: Int)


}