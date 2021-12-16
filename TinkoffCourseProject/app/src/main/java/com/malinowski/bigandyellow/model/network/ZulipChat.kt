package com.malinowski.bigandyellow.model.network

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

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

    @FormUrlEncoded
    @POST("messages")
    fun sendMessage(
        @Field("type") type: String,
        @Field("to") to: String,
        @Field("content") content: String,
        @Field("topic") topic: String = ""
    ): Single<ResponseBody>

    @FormUrlEncoded
    @POST("messages/{message_id}/reactions")
    fun addEmojiReaction(
        @Path("message_id") messageId: Int,
        @Field("emoji_name") name: String,
    ): Completable

    @DELETE("messages/{message_id}/reactions")
    fun deleteEmojiReacction(
        @Path("message_id") messageId: Int,
        @Query("emoji_name") name: String,
    ): Completable

    @GET("messages")
    fun getMessages(
        @Query("anchor") anchor: String = NEWEST_MES,  // newest / oldest / first_unread
        @Query("num_before") numBefore: Int = 20,
        @Query("num_after") numAfter: Int = 0,
        @Query("narrow") narrow: String,
    ): Single<ResponseBody>

    @DELETE("messages/{msg_id}")
    fun deleteMessage(
        @Path("msg_id") messageId: Int,
    ): Completable

    @PATCH("messages/{msg_id}")
    fun editMessage(
        @Path("msg_id") messageId: Int,
        @Query("content") content: String,
    ): Completable

    @PATCH("messages/{msg_id}")
    fun editMessageTopic(
        @Path("msg_id") messageId: Int,
        @Query("topic") topic: String,
    ): Completable

    companion object {
        const val NEWEST_MES = "newest"
    }
}