package com.langamy.fragments;


import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.main.R;
import com.google.android.material.button.MaterialButton;
import com.langamy.activities.LearnActivity;
import com.langamy.activities.SpecificStudySetActivity;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.NonSwipeableViewPager;
import com.langamy.base.classes.StudySet;
import com.langamy.database.StudySetCursorWrapper;
import com.langamy.database.StudySetsBaseHelper;
import com.langamy.database.StudySetsScheme;
import com.langamy.database.StudySetsScheme.StudySetsTable.Cols;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContinueLearningFragment extends Fragment {

    private int masteredWords = 0;
    private int amountOfWords = 0;
    private boolean learnMarked = false;


    private int studysetId = 0;
    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);

    private MaterialButton returnToStudySet_MBTN, continue_MBTN;
    private ImageView cool_IV;


    public ContinueLearningFragment() {
        // Required empty public constructor
    }


    @Override
    public void onResume() {

        super.onResume();

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_continue_learning, container, false);
        continue_MBTN = view.findViewById(R.id.continue_learning_MBTN);
        returnToStudySet_MBTN = view.findViewById(R.id.return_to_studyset_MBTN);

        returnToStudySet_MBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().finish();

            }
        });
        cool_IV = view.findViewById(R.id.cool_emoji_IV);
        return view;
    }

    public void setMasteredWords(int masteredWords) {
        this.masteredWords = masteredWords;
        if (masteredWords == amountOfWords) {
            finishStudyset();
            continue_MBTN.setVisibility(View.GONE);
            returnToStudySet_MBTN.setVisibility(View.VISIBLE);
            cool_IV.setVisibility(View.VISIBLE);
        }
    }

    public void setAmountOfWords(int amountOfWords) {
        this.amountOfWords = amountOfWords;
    }

    public void setStudysetId(int studysetId, boolean learnMarked) {
        this.studysetId = studysetId;
        this.learnMarked = learnMarked;
    }

    public void finishStudyset() {

        if (BaseVariables.checkNetworkConnection(getContext())) {

            Call<Void> call;

            if (this.learnMarked) {

                call = mLangamyAPI.finishStudyset(studysetId, "marked");

            } else {

                call = mLangamyAPI.finishStudyset(studysetId, "words");

            }
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(getContext(), String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), t.toString(), Toast.LENGTH_SHORT).show();
                }
            });

        }

        SQLiteDatabase mDatabase = new StudySetsBaseHelper(getContext()).getWritableDatabase();
        StudySetCursorWrapper cursor = BaseVariables.queryStudySets(Cols.id + "=?", new String[]{String.valueOf(studysetId)}, mDatabase);
        try {
            cursor.moveToFirst();
            StudySet studySet = cursor.getStudySet();
            JSONArray jsonArray;
            if (this.learnMarked) {
                Toast.makeText(getContext(), "MARKED", Toast.LENGTH_SHORT).show();
                jsonArray = new JSONArray(studySet.getMarked_words());

            } else {
                jsonArray = new JSONArray(studySet.getWords());
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.put("firstStage", true);
                jsonObject.put("secondStage", false);
                jsonObject.put("thirdStage", false);
                jsonObject.put("forthStage", false);
            }
            if (this.learnMarked) {
                studySet.setMarked_words(jsonArray.toString());

            } else {
                studySet.setWords(jsonArray.toString());
            }
            mDatabase.update(StudySetsScheme.StudySetsTable.NAME,
                    BaseVariables.getContentValuesForStudyset(studySet, BaseVariables.checkNetworkConnection(getContext())),
                    Cols.id + " =?", new String[]{String.valueOf(studySet.getId())});
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
