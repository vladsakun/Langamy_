package com.langamy.retrofit

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.langamy.base.classes.StudySet
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface LangamyApiService {

    //StudySet
    @POST("api/create/studyset/")
    fun createStudySet(@Body studySet: StudySet?): Deferred<StudySet>

    @GET("api/studyset/{study_set_id}/")
    fun getSpecificStudySet(@Path("study_set_id") study_set_id: Int): Deferred<StudySet>

    @PATCH("api/studyset/{id}/")
    fun patchStudySet(@Path("id") id: Int, @Body studySet: StudySet?): Deferred<StudySet>

    @DELETE("api/studyset/{id}/")
    fun deleteStudySet(@Path("id") id: Int): Deferred<Void>

    @POST("api/finish/studyset/{studyset_id}/{mode}/")
    fun finishStudyset(@Path("studyset_id") studyset_id: Int, @Path("mode") mode: String?): Deferred<Void>

    @POST("api/clone/studyset/{studyset_id}/{email}/")
    fun cloneStudySet(@Path("studyset_id") studyset_id: Int, @Path("email") email: String?): Deferred<StudySet>

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