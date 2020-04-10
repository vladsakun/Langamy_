package com.langamy.retrofit

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.langamy.base.classes.StudySet
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface LangamyApiService {
    @GET("api/get/studysetsnames/{user_email}/")
    fun getStudySets(@Path("user_email") userEmail:String):Deferred<List<StudySet>>

    companion object {
        val HOST_URL = "http://vlad12.pythonanywhere.com/"
        val LOCAL_URL = "http://192.168.1.108:8080/"

        operator fun invoke(
                connectivityInterceptor: ConnectivityInterceptor
        ): LangamyApiService {

            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(connectivityInterceptor)
                    .build()

            return Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(HOST_URL)
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(LangamyApiService::class.java)
        }

    }
}