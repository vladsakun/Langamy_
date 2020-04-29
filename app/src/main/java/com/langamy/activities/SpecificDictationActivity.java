package com.langamy.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.main.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.material.button.MaterialButton;
import com.langamy.adapters.MarksAdapter;
import com.langamy.adapters.SpecificStudySetAdapter;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Dictation;
import com.langamy.base.classes.Mark;
import com.langamy.base.classes.Word;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.langamy.base.kotlin.BaseFunctionsKt.includeConnectivityErrorLayout;

public class SpecificDictationActivity extends AppCompatActivity {

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);

    private List<Word> mWords, globalMarkedWordsList, globalOtherWordsList;
    private int code, dictationId, dictationDataForAPI;
    private Intent intent;
    private String mode;

    private RecyclerView wordsRecyclerView, marks_RV;
    private RecyclerView.Adapter mAdapter, marksAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView dictationName, questionAmount, creator, typeOfQuestions, dictationCode;
    private MaterialButton startDictationBtn, showMarks_BTN;
    private ImageButton updateMarks_BTN;
    private ProgressBar progressBar;
    private ArrayList<Mark> marks;
    private RelativeLayout specificDictation_RL, error_RL;
    private RelativeLayout specificDictation_VIEW;
    private RelativeLayout content;
    private boolean error = false;

    @Override
    protected void onPause() {
        super.onPause();
        marks_RV.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_dictation);

        mWords = new ArrayList<>();
        marks = new ArrayList<>();
        globalMarkedWordsList = new ArrayList<>();
        globalOtherWordsList = new ArrayList<>();
        intent = new Intent(SpecificDictationActivity.this, DictationPagerActivity.class);

        wordsRecyclerView = findViewById(R.id.words_recyclerview);
        dictationName = findViewById(R.id.dictation_name);
        questionAmount = findViewById(R.id.question_amount_tv);
        creator = findViewById(R.id.creator_tv);
        typeOfQuestions = findViewById(R.id.type_of_question_tv);
        startDictationBtn = findViewById(R.id.start_dictation_btn);
        progressBar = findViewById(R.id.progressBar);
        marks_RV = findViewById(R.id.dictation_marks_RV);
        showMarks_BTN = findViewById(R.id.show_marks_BTN);
        updateMarks_BTN = findViewById(R.id.update_marks_BTN);
        specificDictation_RL = findViewById(R.id.specific_dictation_RL);
        specificDictation_VIEW = findViewById(R.id.specific_dictation_VIEW);
        error_RL = findViewById(R.id.error_RL);
        dictationCode = findViewById(R.id.dictation_code);
        content =  findViewById(R.id.content);

        specificDictation_VIEW.setVisibility(View.GONE);

        startDictationBtn.setEnabled(false);

        boolean randomDictation = getIntent().getBooleanExtra(BaseVariables.RANDOM_DICTATION_MESSAGE, false);

        code = getIntent().getIntExtra(BaseVariables.DICTATION_CODE_MESSAGE, 0);
        dictationId = getIntent().getIntExtra(BaseVariables.DICTATION_ID_MESSAGE, 0);

        try {

            Intent intent = getIntent();
            Uri data = intent.getData();

            String stringData = data.toString();
            stringData = stringData.replace(BaseVariables.HOST_URL + "/get/dictation/", "").replace("/", "");
            code = Integer.parseInt(stringData);

        } catch (NullPointerException ignored) {

        }

        if (dictationId != 0) {

            mode = "id";
            dictationDataForAPI = dictationId;

        } else if (code != 0) {

            mode = "code";
            dictationDataForAPI = code;

        }

        getSpecificDictation(dictationDataForAPI, mode, randomDictation);
        getDictationMarks(dictationDataForAPI, mode);

        inizializeRecyclerView(mWords);
        inizializeMarksRecyclerView(marks);

        startDictationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra(BaseVariables.WORDS_MESSAGE, (Serializable) mWords);
                intent.putExtra(BaseVariables.MARKED_WORDS_MESSAGE, (Serializable) globalMarkedWordsList);
                intent.putExtra(BaseVariables.OTHER_WORDS_MESSAGE, (Serializable) globalOtherWordsList);
                intent.putExtra(BaseVariables.QUESTION_AMOUNT_MESSAGE, Integer.parseInt(questionAmount.getText().toString()));
                intent.putExtra(BaseVariables.TYPE_OF_QUESTIONS_MESSAGE, typeOfQuestions.getText());
                startActivity(intent);
            }
        });

        showMarks_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDictationMarks(dictationDataForAPI, mode);
                if (marks_RV.getVisibility() == View.VISIBLE) {
                    marks_RV.setVisibility(View.GONE);
                } else {
                    marks_RV.setVisibility(View.VISIBLE);
                }

            }
        });

        updateMarks_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDictationMarks(dictationDataForAPI, mode);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_specific_dictation, menu);
        return true;
    }

    private void updateActionBar(String dictationName) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.dictation));
        getSupportActionBar().setSubtitle(dictationName);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share) {
            if (!error) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String sharedDictationText = BaseVariables.getShareDictationText(dictationCode.getText().toString());
                sendIntent.putExtra(Intent.EXTRA_TEXT, sharedDictationText);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            } else {
                item.setVisible(false);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public List<Word> convertJsonToWordObject(JSONArray words) {
        List<Word> wordsArray = new ArrayList<>();
        for (int i = 0; i < words.length(); i++) {
            try {
                JSONObject word = words.getJSONObject(i);
                String term = word.get("term").toString();
                String translation = word.get("translation").toString();
                Word wordObject = new Word(term, translation);
                wordsArray.add(wordObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return wordsArray;
    }

    private void inizializeRecyclerView(List<Word> words) {
        layoutManager = new LinearLayoutManager(this);
        wordsRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new SpecificStudySetAdapter(words);
        wordsRecyclerView.setAdapter(mAdapter);
        wordsRecyclerView.setNestedScrollingEnabled(false);

    }

    private void inizializeMarksRecyclerView(ArrayList<Mark> marks) {
        layoutManager = new LinearLayoutManager(this);
        marks_RV.setLayoutManager(layoutManager);

        marksAdapter = new MarksAdapter(marks);
        marks_RV.setAdapter(marksAdapter);
        marks_RV.setNestedScrollingEnabled(false);
    }

    private void getDictationMarks(int dictationId, String mode) {
        Call<List<Mark>> call = mLangamyAPI.getDictationMarks(dictationId, mode);
        call.enqueue(new Callback<List<Mark>>() {
            @Override
            public void onResponse(Call<List<Mark>> call, Response<List<Mark>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                if (marks.size() != 0) {
                    marks.clear();
                }
                marks.addAll(response.body());
                marksAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Mark>> call, Throwable t) {

            }
        });
    }


    public void getSpecificDictation(int code, String mode, boolean randomDictation) {

        if (randomDictation) {

            Call<List<Dictation>> call = mLangamyAPI.getRandomDictation(GoogleSignIn.getLastSignedInAccount(this).getEmail());
            call.enqueue(new Callback<List<Dictation>>() {
                @Override
                public void onResponse(Call<List<Dictation>> call, Response<List<Dictation>> response) {
                    if (response.code() == 500) {
                        noDictation();
                        return;
                    } else if (!response.isSuccessful()) {
                        includeConnectivityErrorLayout(String.valueOf(response.code()), content, getLayoutInflater(), SpecificDictationActivity.this);
                        progressBar.setVisibility(View.GONE);

                        return;
                    }
                    assert response.body() != null;
                    prepareDictation(response.body().get(0));

                }

                @Override
                public void onFailure(Call<List<Dictation>> call, Throwable t) {
                    includeConnectivityErrorLayout("failure", content, getLayoutInflater(), SpecificDictationActivity.this);
                    progressBar.setVisibility(View.GONE);

                }
            });
        } else {
            Call<Dictation> call = mLangamyAPI.getSpecificDictation(code, mode);
            call.enqueue(new Callback<Dictation>() {
                @Override
                public void onResponse(Call<Dictation> call, Response<Dictation> response) {
                    if (response.code() == 500) {
                        noDictation();
                        return;
                    } else if (!response.isSuccessful()) {
                        includeConnectivityErrorLayout(String.valueOf(response.code()), content, getLayoutInflater(), SpecificDictationActivity.this);
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    assert response.body() != null;
                    prepareDictation(response.body());
                }

                @Override
                public void onFailure(Call<Dictation> call, Throwable t) {
                    includeConnectivityErrorLayout("failure", content, getLayoutInflater(), SpecificDictationActivity.this);
                    progressBar.setVisibility(View.GONE);

                }
            });
        }
    }

    private void noDictation() {

        error = true;

        specificDictation_VIEW.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        error_RL.setVisibility(View.VISIBLE);

    }

    private void prepareDictation(Dictation dictation) {

        String otherWords = dictation.getWords();
        String markedWords = dictation.getMarked_words();
        List<Word> allWords = new ArrayList<>();
        try {
            JSONArray otherWordsJSONArray = new JSONArray(otherWords);
            JSONArray markedWordsJSONArray = new JSONArray(markedWords);
            List<Word> otherWordsList = convertJsonToWordObject(otherWordsJSONArray);
            List<Word> markedWordsList = convertJsonToWordObject(markedWordsJSONArray);
            globalMarkedWordsList.addAll(markedWordsList);
            globalOtherWordsList.addAll(otherWordsList);
            allWords.addAll(otherWordsList);
            allWords.addAll(markedWordsList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWords.addAll(allWords);
        mAdapter.notifyDataSetChanged();

        dictationName.setText(dictation.getName());
        typeOfQuestions.setText(dictation.getType_of_questions());
        questionAmount.setText(String.valueOf(dictation.getAmount_of_words_for_dictation()));
        creator.setText(dictation.getCreator());
        dictationCode.setText(String.valueOf(dictation.getCode()));

        intent.putExtra(BaseVariables.DICTATION_MESSAGE, dictation);
        intent.putExtra(BaseVariables.DICTATION_ID_MESSAGE, dictation.getId());
        intent.putExtra(BaseVariables.DICTATION_TYPE_OF_QUESTIONS_MESSAGE, dictation.getType_of_questions());
        intent.putExtra(BaseVariables.FROM_LANG_MESSAGE, dictation.getLanguage_from());
        intent.putExtra(BaseVariables.TO_LANG_MESSAGE, dictation.getLanguage_to());

        progressBar.setVisibility(View.GONE);

        startDictationBtn.setEnabled(true);

        updateActionBar(dictation.getName());

        specificDictation_VIEW.setVisibility(View.VISIBLE);


    }

}
