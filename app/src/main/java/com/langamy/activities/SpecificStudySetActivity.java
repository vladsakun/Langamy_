package com.langamy.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.main.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.StudySet;
import com.langamy.base.classes.Word;
import com.langamy.database.StudySetCursorWrapper;
import com.langamy.database.StudySetsBaseHelper;
import com.langamy.database.StudySetsScheme.StudySetsTable.Cols;
import com.langamy.fragments.MarkedWordsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.listener.OnViewInflateListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SpecificStudySetActivity extends AppCompatActivity {

    private int studySetId;
    private boolean studyMarked = false;

    private ArrayList<Word> mWordList;
    private ArrayList<Word> markedWords = new ArrayList<>();
    private Intent editStudySetActivityIntent, learnIntent, cardIntent;

    private ProgressBar mProgressBar;
    private TextView studySetTitle;
    private FrameLayout containerForRecylcler, parentForContainer;
    private LinearLayout studyCategories_LL;
    private MaterialButton learnBtn, createDictationBtn, cardMode_BTN, studyAll_MBTN, studyMarked_MBTN;
    private AdView mAdView;
    private RecyclerView wordsRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private MarkedWordsFragment fragment;

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private Intent makeDictationIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_studyset);

        //Set window not touchable
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        mWordList = new ArrayList<>();
        editStudySetActivityIntent = new Intent(SpecificStudySetActivity.this, CreateStudySetActivity.class);
        learnIntent = new Intent(SpecificStudySetActivity.this, LearnActivity.class);
        cardIntent = new Intent(SpecificStudySetActivity.this, CardModeActivity.class);
        makeDictationIntent = new Intent(SpecificStudySetActivity.this, MakeDictationActivity.class);

        //Views
        mProgressBar = findViewById(R.id.progressBar);
        studySetTitle = findViewById(R.id.title_of_studyset);
        learnBtn = findViewById(R.id.learn_btn);
        cardMode_BTN = findViewById(R.id.card_mode_btn);
        createDictationBtn = findViewById(R.id.make_dictation_btn);
        wordsRecyclerView = findViewById(R.id.words_recyclerview);
        studyCategories_LL = findViewById(R.id.study_categories_LL);
        studyAll_MBTN = findViewById(R.id.study_all_MBTN);
        studyMarked_MBTN = findViewById(R.id.study_marked_MBTN);
        containerForRecylcler = findViewById(R.id.container_for_recycler);
        parentForContainer = findViewById(R.id.parent_for_container);
        mAdView = findViewById(R.id.adView);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        studySetId = getIntent().getIntExtra(BaseVariables.STUDY_SET_ID_MESSAGE, 0);

        //MarkedWords Fragment initializing
        fragment = new MarkedWordsFragment(mWordList);
        fragmentTransaction.add(R.id.container_for_recycler, fragment);
        fragmentTransaction.commit();

        inizializeRecyclerView(mWordList);

        learnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (studyMarked) {

                    ArrayList<Word> markedWordsForIntent = new ArrayList<>();
                    for (Word word : mWordList) {
                        if (word.isMarked()) {
                            markedWordsForIntent.add(word);
                        }
                    }

                    if (markedWordsForIntent.size() < 4) {
                        Toast.makeText(SpecificStudySetActivity.this, "You have to mark more than 4 words", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    learnIntent.putExtra(BaseVariables.MARKED_MESSAGE, true);
                    learnIntent.putExtra(BaseVariables.WORDS_MESSAGE, markedWords);
                } else {
                    learnIntent.putExtra(BaseVariables.MARKED_MESSAGE, false);
                    learnIntent.putExtra(BaseVariables.WORDS_MESSAGE, mWordList);
                }
                startActivity(learnIntent);
            }
        });

        cardMode_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cardIntent.putExtra(BaseVariables.WORDS_MESSAGE, mWordList);
                startActivity(cardIntent);

            }
        });

        createDictationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeDictationIntent.putExtra(BaseVariables.WORDS_MESSAGE, mWordList);
                makeDictationIntent.putExtra(BaseVariables.TITLE_MESSAGE, studySetTitle.getText().toString());
                startActivity(makeDictationIntent);
                finish();
            }
        });

        studyAll_MBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                studyMarked = false;

                studyAll_MBTN.setBackgroundColor(getColor(R.color.blue));
                studyAll_MBTN.setTextColor(getColor(R.color.white));

                studyMarked_MBTN.setBackgroundColor(getColor(R.color.white));
                studyMarked_MBTN.setTextColor(getColor(R.color.blue));

                studyAll_MBTN.setClickable(false);
                studyMarked_MBTN.setClickable(true);

                parentForContainer.setVisibility(View.GONE);
                wordsRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        studyMarked_MBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                studyMarked = true;

                studyMarked_MBTN.setBackgroundColor(getColor(R.color.blue));
                studyMarked_MBTN.setTextColor(getColor(R.color.white));

                studyAll_MBTN.setBackgroundColor(getColor(R.color.white));
                studyAll_MBTN.setTextColor(getColor(R.color.blue));

                studyMarked_MBTN.setClickable(false);
                studyAll_MBTN.setClickable(true);

                parentForContainer.setVisibility(View.VISIBLE);
                wordsRecyclerView.setVisibility(View.GONE);

                fragment.setWords(mWordList);
            }
        });

    }

    private void cloneStudySet(int studySet_id) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Call<String> call = mLangamyAPI.cloneStudySet(studySet_id, account.getEmail());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SpecificStudySetActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }

                studySetId = Integer.parseInt(response.body());
                getSpecificStudySet(Integer.parseInt(response.body()));

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(SpecificStudySetActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_specific_study_set, menu);
        inflater.inflate(R.menu.menu_help_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit_study_set) {
            if(!BaseVariables.checkNetworkConnection(this)){
                Toast.makeText(this, R.string.you_need_an_internet_connection, Toast.LENGTH_SHORT).show();
                return false;
            }
            startActivity(editStudySetActivityIntent);
            finish();
        }
        if (id == R.id.help) {
            playHelp();
        }
        if (id == R.id.share) {
            if(!BaseVariables.checkNetworkConnection(this)){
                Toast.makeText(this, R.string.you_need_an_internet_connection, Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String sharedDictationText = BaseVariables.getShareStudySetText(String.valueOf(studySetId));
            sendIntent.putExtra(Intent.EXTRA_TEXT, sharedDictationText);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void inizializeRecyclerView(List<Word> words) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        wordsRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new SpecificStudySetAdapterInActivity(words);
        wordsRecyclerView.setAdapter(mAdapter);
        wordsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void playHelp() {

        BaseVariables.hideKeyboard(this);

        FancyShowCaseQueue fq = new FancyShowCaseQueue();

        FancyShowCaseView makeDictation = new FancyShowCaseView.Builder(this)
                .focusOn(createDictationBtn)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_make_dictation_btn), fq);
                    }
                })
                .backgroundColor(getColor(R.color.blueForFancy))
                .build();
//
//        FancyShowCaseView mark = new FancyShowCaseView.Builder(this)
//                .focusOn(wordsRecyclerView.getChildAt(0).findViewById(R.id.starBtn))
//                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
//                    @Override
//                    public void onViewInflated(View view) {
//                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_star), fq);
//                    }
//                })
//                .backgroundColor(getColor(R.color.blueForFancy))
//                .build();

        FancyShowCaseView studyMarked = new FancyShowCaseView.Builder(this)
                .focusOn(studyMarked_MBTN)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_study_marked), fq);
                    }
                })
                .backgroundColor(getColor(R.color.blueForFancy))
                .build();


        FancyShowCaseView edit = new FancyShowCaseView.Builder(this)
                .focusOn(findViewById(R.id.edit_study_set))
                .customView(R.layout.fancyshowcase_with_image, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setImage(view, getString(R.string.fancy_edit),
                                fq, getDrawable(R.drawable.ic_edit_white_24dp));
                    }
                })
                .backgroundColor(getColor(R.color.blueForFancy))
                .build();

        fq.add(makeDictation)
                .add(studyMarked)
                .add(edit);

        fq.show();
    }

    public void getSpecificStudySet(int studySet_id) {

        if (BaseVariables.checkNetworkConnection(this)) {

            Call<StudySet> call = mLangamyAPI.getSpecificStudySet(studySet_id);
            call.enqueue(new Callback<StudySet>() {

                @NonNull
                @Override
                protected Object clone() throws CloneNotSupportedException {
                    return super.clone();
                }

                @Override
                public void onResponse(Call<StudySet> call, Response<StudySet> response) {
                    if (!response.isSuccessful()) {
                        Log.d("SPECIFIC_RESPONSE", String.valueOf(response.code()));
                        return;
                    }

                    initializeStudySetActivity(response.body());

                }

                @Override
                public void onFailure(Call<StudySet> call, Throwable t) {
                    Toast.makeText(SpecificStudySetActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            initializeStudySetActivity(readDataFromLocaleStorage(studySet_id));
            createDictationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SpecificStudySetActivity.this, "To make a dictation" +
                            " you need an internet connection", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void initializeStudySetActivity(StudySet studySet) {

        mWordList.addAll(convertJsonArrayToArray(studySet.getWords()));
        if (studySet.getMarked_words() != null) {
            markedWords.addAll(convertJsonArrayToArray(studySet.getMarked_words()));
        }

        for (int i = 0; i < markedWords.size(); i++) {
            Word word = markedWords.get(i);
            for (Word allWord : mWordList) {
                if (allWord.getTerm().equals(word.getTerm()) && allWord.getTranslation().equals(word.getTranslation())) {
                    allWord.setMarked(true);
                }
            }
        }

        fragment.setWords(mWordList);

        mAdapter.notifyDataSetChanged();

        studyCategories_LL.setVisibility(View.VISIBLE);
        studySetTitle.setText(studySet.getName());

        editStudySetActivityIntent.putExtra(BaseVariables.STUDY_SET_MESSAGE, studySet);

        learnIntent.putExtra(BaseVariables.STUDY_SET_MESSAGE, studySet);

        cardIntent.putExtra(BaseVariables.FROM_LANG_MESSAGE, (Serializable) studySet.getLanguage_from());

        makeDictationIntent.putExtra(BaseVariables.FROM_LANG_MESSAGE, studySet.getLanguage_from());
        makeDictationIntent.putExtra(BaseVariables.TO_LANG_MESSAGE, studySet.getLanguage_to());

        mProgressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        SharedPreferences sf = getPreferences(MODE_PRIVATE);
        boolean help = sf.getBoolean(BaseVariables.HELP_SPECIFIC_STUDYSET, true);
        SharedPreferences.Editor editor = sf.edit();

        if(help){
            playHelp();
            editor.putBoolean(BaseVariables.HELP_SPECIFIC_STUDYSET, false);
            editor.commit();
        }

    }

    private ArrayList<Word> convertJsonArrayToArray(String words) {
        ArrayList<Word> array = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(words);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Word word = new Word(jsonObject.getString("term"),
                        jsonObject.getString("translation"),
                        Boolean.parseBoolean(jsonObject.getString("firstStage")),
                        Boolean.parseBoolean(jsonObject.getString("secondStage")),
                        Boolean.parseBoolean(jsonObject.getString("thirdStage")),
                        Boolean.parseBoolean(jsonObject.getString("forthStage")));
                array.add(word);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWordList.clear();
        try {
            Uri data = getIntent().getData();
            int studySet_id = Integer.parseInt(
                    data.toString().replace("http://vlad12.pythonanywhere.com/studyset/", "")
                            .replace("/", ""));
            cloneStudySet(studySet_id);
        } catch (Exception e) {
            getSpecificStudySet(studySetId);
        }
    }

    //Adapter for word recyclerview
    class SpecificStudySetAdapterInActivity extends RecyclerView.Adapter<SpecificStudySetAdapterInActivity.SpecificStudySetHolder> {

        public List<Word> mWords;

        public class SpecificStudySetHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView term, transaltion;
            private ToggleButton starBtn;

            public SpecificStudySetHolder(View v) {
                super(v);
                term = v.findViewById(R.id.term_TV);
                transaltion = v.findViewById(R.id.translation_TV);
                starBtn = v.findViewById(R.id.starBtn);
            }
        }

        public SpecificStudySetAdapterInActivity(List<Word> words) {
            this.mWords = words;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public SpecificStudySetAdapterInActivity.SpecificStudySetHolder onCreateViewHolder(ViewGroup parent,
                                                                                           int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.studyset_word_item, parent, false);
            SpecificStudySetAdapterInActivity.SpecificStudySetHolder vh = new SpecificStudySetAdapterInActivity.SpecificStudySetHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final SpecificStudySetAdapterInActivity.SpecificStudySetHolder holder, int position) {

            holder.term.setText(mWords.get(position).getTerm());
            holder.transaltion.setText(mWords.get(position).getTranslation());

            holder.starBtn.setOnCheckedChangeListener(null);

            holder.starBtn.setChecked(mWords.get(position).isMarked());

            holder.starBtn.setOnCheckedChangeListener((compoundButton, isChecked) -> {

                if (isChecked) {

                    mWords.get(position).setMarked(true);
                    markedWords.add(mWords.get(position));
                } else {

                    mWords.get(position).setMarked(false);
                    markedWords.remove(mWords.get(position));

                }
            });

        }

        @Override
        public int getItemCount() {
            return mWords.size();
        }
    }

    public void removeMarkedWord(int currentWordIndex) {

        mWordList.get(currentWordIndex).setMarked(false);

        fragment.setWords(mWordList);
        mAdapter.notifyDataSetChanged();

    }

    public StudySet readDataFromLocaleStorage(int studySet_id) {

        SQLiteDatabase mDatabase = new StudySetsBaseHelper(this).getReadableDatabase();

        StudySetCursorWrapper cursor = BaseVariables.queryStudySets(Cols.id + "=?", new String[]{String.valueOf(studySet_id)}, mDatabase);

        StudySet mLocaleStudySet;
        try {
            cursor.moveToFirst();
            mLocaleStudySet = cursor.getStudySet();
        } finally {
            cursor.close();
        }

        return mLocaleStudySet;
    }

}
