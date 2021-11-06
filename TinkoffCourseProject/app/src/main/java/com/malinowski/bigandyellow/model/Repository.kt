package com.malinowski.bigandyellow.model

import android.annotation.SuppressLint
import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.network.AuthInterceptor
import com.malinowski.bigandyellow.model.network.ZulipChat
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object Repository : IRepository {

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(AuthInterceptor())
        .build()

    private var retrofit = Retrofit.Builder()
        .baseUrl("https://tinkoff-android-fall21.zulipchat.com/api/v1/") // http://192.168.0.21:8081/
        .client(client)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    private var service = retrofit.create(ZulipChat::class.java)
    private val format = Json { ignoreUnknownKeys = true }

    override fun loadStreams(): Single<List<Stream>> {
        return service.getStreams().subscribeOn(Schedulers.io()).map { body ->
            val subscriptionsJSA = format.decodeFromString<JsonObject>(body.string())["streams"]
            format.decodeFromString<List<Stream>>(subscriptionsJSA.toString())
        }
    }

    override fun loadSubscribedStreams(): Single<List<Stream>> {
        return service.getSubscribedStreams().subscribeOn(Schedulers.io()).map { body ->
            val subscriptionsJSA = format.decodeFromString<JsonObject>(body.string())["subscriptions"]
            format.decodeFromString<List<Stream>>(subscriptionsJSA.toString())
        }
    }

    override fun loadTopics(id: Int): Single<List<Topic>> {
        return service.getTopicsInStream(id).subscribeOn(Schedulers.io()).map { body ->
            val topicsJSA = format.decodeFromString<JsonObject>(body.string())["topics"]
            format.decodeFromString<List<Topic>>(topicsJSA.toString())
        }
    }

    override fun loadUsers(): Single<List<User>> {
        return service.getUsers().subscribeOn(Schedulers.io()).map { body ->
            val membersJSA = format.decodeFromString<JsonObject>(body.string())["members"]
            format.decodeFromString<List<User>>(membersJSA.toString())
        }
    }

    fun loadStatus(user: User) =
        service.getPresence(user.id).subscribeOn(Schedulers.io()).map { body ->
            val jso = Json.decodeFromString<JsonObject>(body.string())
                .jsonObject["presence"]?.jsonObject?.get("aggregated")?.jsonObject?.get("status")
            jso?.jsonPrimitive?.content?.let { status ->
                user.status = UserStatus.decodeFromString(status)
                user.status
            }
        }.doOnError { Log.e("LoadUserStatus", "${user.name} ${it.message}") }

    @SuppressLint("CheckResult")
    fun loadOwnUser() {
        val format = Json { ignoreUnknownKeys = true }
        service.getOwnUser().subscribeOn(Schedulers.io()).subscribe(
            { body ->
                User.ME = format.decodeFromString(body.string())
            }, {
                Log.e("LoadOwnUser", it.message.toString())
            }
        )
    }

    class ExpectedError : Throwable("Expected Random Error")

}