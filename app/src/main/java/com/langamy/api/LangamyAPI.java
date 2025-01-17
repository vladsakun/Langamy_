package com.langamy.api;

import com.langamy.base.classes.Dictation;
import com.langamy.base.classes.Mark;
import com.langamy.base.classes.StudySet;
import com.langamy.base.classes.TranslationResponse;
import com.langamy.base.classes.User;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LangamyAPI {

    //StudySet
    @POST("api/create/studyset/")
    Call<StudySet> createStudySet(@Body StudySet studySet);

    @GET("api/studyset/{study_set_id}/")
    Call<StudySet> getSpecificStudySet(@Path("study_set_id") int study_set_id);

    @PATCH("api/studyset/{id}/")
    Call<StudySet> patchStudySet(@Path("id") int id, @Body StudySet studySet);

    @DELETE("api/studyset/{id}/")
    Call<Void> deleteStudySet(@Path("id") int id);

    @POST("api/finish/studyset/{studyset_id}/{mode}/")
    Call<Void> finishStudyset(@Path("studyset_id") int studyset_id, @Path("mode") String mode);

    @POST("api/clone/studyset/{studyset_id}/{email}/")
    Call<String> cloneStudySet(@Path("studyset_id") int studyset_id, @Path("email") String email);

    @GET("api/get/studysetsnames/{user_email}/")
    Call<List<StudySet>> getStudySetsNamesOfCurrentUser(@Path("user_email") String user_email);

    //User
    @GET("api/get/user/{user_email}/")
    Call<User> getUser(@Path("user_email") String user_email);

    @POST("api/create/user/")
    Call<User> createUser(@Body User user);

    @PATCH("api/patch/user/mark/{email}/")
    Call<Void> patchUserMark(@Path("email") String email, @Body String mark);

    //Dictation
    @GET("api/get/dictation/{dictation_code}/{mode}/")
    Call<Dictation> getSpecificDictation(@Path("dictation_code") int dictation_code, @Path("mode") String mode);

    @POST("api/create/dictation/")
    Call<Dictation> createDictation(@Body Dictation dictation);

    @DELETE("api/delete/dictation/{id}/")
    Call<Void> deleteDictation(@Path("id") int id);

    @GET("api/get/random/dictation/{user_email}")
    Call<List<Dictation>> getRandomDictation(@Path("user_email") String user_email);

    @GET("api/get/dictationsnames/{user_email}/")
    Call<List<Dictation>> getDictationsOfCurrentUser(@Path("user_email") String user_email);

    @GET("api/get/dictation/marks/{dictation_id}/{mode}/")
    Call<List<Mark>> getDictationMarks(@Path("dictation_id") int dictationId, @Path("mode") String mode);

    @GET("api/get/user/completed/dictations/{email}/")
    Call<ArrayList<Dictation>> getUserCompletedDictations(@Path("email") String email);

    //Translate
    @POST("api/translate/{from_lang}/{to_lang}/{mode}/")
    Call<TranslationResponse> translate(@Body JSONObject stringToTranslate, @Path("from_lang") String fromLang,
                                        @Path("to_lang") String toLang, @Path("mode") String mode);

}
