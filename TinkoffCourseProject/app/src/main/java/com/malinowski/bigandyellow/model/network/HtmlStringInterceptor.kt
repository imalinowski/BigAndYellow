package com.malinowski.bigandyellow.model.network

import android.text.Html
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class HtmlStringInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val contentType = response.body?.contentType()
        val bodyString = if (android.os.Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(response.body?.string(), Html.FROM_HTML_MODE_COMPACT).toString()
        } else {
            Html.fromHtml(response.body?.string()).toString()
        }

        val body = bodyString.toResponseBody(contentType)
        return response.newBuilder().body(body).build()
    }
}