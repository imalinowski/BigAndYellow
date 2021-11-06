package com.malinowski.bigandyellow.model.network

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class AuthInterceptor : Interceptor {
    private val auth =
        Credentials.basic("malinowski221106@gmail.com", "rpIqzw4T4kZNj9pA993u3Qcx4Ugpm8p3")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val authenticatedRequest: Request = request.newBuilder()
            .header("Authorization", auth).build()
        return chain.proceed(authenticatedRequest)
    }
}