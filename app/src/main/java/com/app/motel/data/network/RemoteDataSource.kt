package com.app.motel.data.network

import android.content.Context
import android.util.Log
import com.app.motel.common.AppConstants
import com.app.motel.common.utils.NetworkUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class RemoteDataSource {

    fun <API> buildApi(
        apiClass: Class<API>,
        context: Context,
        baseUrl: String,
        headers: Map<String, String>? = null,
        retryTimeout: Boolean = false
    ): API {

        val client = getHttpClientBuilder(
            context,
            headers,
            retryTimeout,
        )

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(apiClass)
    }

    // build okhttp
    private fun getHttpClientBuilder(
        context: Context,
        headers: Map<String, String>? = null,
        retryTimeout: Boolean = false,
    ) : OkHttpClient{

        return OkHttpClient.Builder().apply {
            this.addInterceptor(getInterceptorLogging())

            this.addInterceptor(getInterceptorHeader(context, headers))

            if(retryTimeout) this.addInterceptor(RetryInterceptor(context))

            this.connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            this.readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            this.writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }.build()
    }

    // header
    private fun getInterceptorHeader(
        context: Context,
        headers: Map<String, String>? = null,
    ): Interceptor = Interceptor {
        val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)

        val originalRequest: Request = it.request()
        val newRequest : Request = originalRequest.newBuilder()
            .apply {
                headers?.forEach { (key, value) -> this.header(key, value) }
                this.header("Authorization", prefs.getString(AppConstants.TOKEN_KEY, "").let { token ->
                    if (token.isNullOrEmpty()) "Basic Y29yZV9jbGllbnQ6c2VjcmV0" else "Bearer $token"
                })
            }
            .build();
        it.proceed(newRequest)
    }

    // logging
    private fun getInterceptorLogging(
    ) : HttpLoggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HTTP ---------->", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    companion object{
        const val TIMEOUT_SECONDS: Long = 10
    }

    // retry when timeout
    class RetryInterceptor(
        private val context: Context,
        private val maxRetries: Int = 3,
        private val retryDelay: Long = 1000L
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            var attempt = 0
            var response: okhttp3.Response? = null
            var lastException: Exception? = null

            while (attempt < maxRetries) {
                try {
                    // Kiểm tra kết nối mạng trước khi thực hiện request
                    if (!NetworkUtil.isNetworkAvailable(context)) {
                        throw IOException("No network available") // Ném lỗi nếu không có mạng
                    }

                    response = chain.proceed(chain.request()) // Thực hiện request
                    return response // Nếu thành công, trả về response
                } catch (e: Exception) {
                    attempt++
                    lastException = e
                    if (attempt >= maxRetries) {
                        throw lastException // Ném lỗi cuối cùng nếu retry hết số lần
                    }
                    // Delay giữa các lần thử
                    Thread.sleep(retryDelay)
                }
            }
            // Nếu không thành công, ném lỗi cuối cùng
            throw lastException ?: IllegalStateException("Unknown error")
        }
    }

}
