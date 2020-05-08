package com.langamy.ui.dictation.create;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.main.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Dictation;
import com.langamy.base.classes.StudySet;
import com.langamy.base.classes.Word;
import com.langamy.ui.dictation.show.SpecificDictationActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class CreateDictationActivity extends AppCompatActivity{

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private ArrayList<Word> mWords, mustHaveWords, otherWords;
    private String[] dictationModes = {"quiz", "term_translation", "translation_term"};
    private String dictationMode = dictationModes[0], title;
    private int questionCount, amountOfWords, questionTime = 10;
    private List<StudySet> studySets = new ArrayList<>();
    private List<StudySet> checkedStudySets = new ArrayList<>();
    private ArrayList<String> studySetsNames = new ArrayList<>();
    private boolean[] checkedStudySetsNames;
    private boolean timerStatus = false;
    private GoogleSignInAccount account;
    private AlertDialog.Builder addStudySetAlertBuilder;

    private MaterialButton startDictationBtn;
    private RecyclerView wordsRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView titleTextView;
    private TextView questionCountTV, questionTime_TV;
    private ImageButton cancelTimer, activeTimer;
    private RelativeLayout typesOfQuestions;
    private LinearLayout quiz_LL;
    private LinearLayout term_LL;
    private LinearLayout translation_LL;
    private LinearLayout dictationSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dictation);

        quiz_LL = findViewById(R.id.quiz_test_LL);
        term_LL = findViewById(R.id.term_LL);
        translation_LL = findViewById(R.id.translation_LL);
        wordsRecyclerView = findViewById(R.id.words_recyclerview);
        titleTextView = findViewById(R.id.dictation_name);
        dictationSettings = findViewById(R.id.dictation_setting_LL);
        questionCountTV = findViewById(R.id.question_amount_tv);
        questionTime_TV = findViewById(R.id.question_time_tv);
        startDictationBtn = findViewById(R.id.make_dictation_btn);
        typesOfQuestions = findViewById(R.id.dictation_buttons_RL);
        LinearLayout addTimer = findViewById(R.id.add_time_LL);
        activeTimer = findViewById(R.id.active_timer_IB);
        cancelTimer = findViewById(R.id.cancel_timer_IB);

        account = GoogleSignIn.getLastSignedInAccount(this);

        Drawable blue = getDrawable(R.drawable.corner_radius_linearlayout);
        Drawable green = getDrawable(R.drawable.corner_radius_orange_linearlayout);

        quiz_LL.setBackground(green);

        quiz_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dictationMode = dictationModes[0];

                v.setBackground(green);
                translation_LL.setBackground(blue);
                term_LL.setBackground(blue);

            }
        });

        term_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dictationMode = dictationModes[1];

                term_LL.setBackground(green);
                translation_LL.setBackground(blue);
                quiz_LL.setBackground(blue);

            }
        });

        translation_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dictationMode = dictationModes[2];

                translation_LL.setBackground(green);
                quiz_LL.setBackground(blue);
                term_LL.setBackground(blue);

            }
        });

        dictationSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show("questionCount");
            }
        });

        addTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show("questionTime");
            }
        });

        title = getIntent().getStringExtra(BaseVariables.TITLE_MESSAGE);
        titleTextView.setText(title);

        mustHaveWords = new ArrayList<>();
        mWords = (ArrayList<Word>) getIntent().getSerializableExtra(BaseVariables.WORDS_MESSAGE);
        amountOfWords = mWords.size();
        otherWords = new ArrayList<>(mWords);

        inizializeRecyclerView(mWords);

        if (mWords.size() / 2 <= 4) {
            questionCount = 4;
            questionCountTV.setText(String.valueOf(4));
        } else {
            questionCount = mWords.size() / 2;
            questionCountTV.setText(String.valueOf(mWords.size() / 2));
        }

        startDictationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDictation(titleTextView.getText().toString(), convertWordArrayListToJsonArray(otherWords), convertWordArrayListToJsonArray(mustHaveWords),
                        questionCount, dictationMode);
            }
        });

        addStudySetAlertBuilder = new AlertDialog.Builder(this);
        addStudySetAlertBuilder.setTitle("Select study sets");
        addStudySetAlertBuilder.setCancelable(true);
        addStudySetAlertBuilder.setPositiveButton("Add",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Word> mWordsCopy = new ArrayList<>(mWords);
                        for (StudySet studySet : checkedStudySets) {
                            mWords.addAll(BaseVariables.convertJSONArrayToArrayOfWords(studySet.getWords()));
                            otherWords.addAll(BaseVariables.convertJSONArrayToArrayOfWords(studySet.getWords()));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });

        cancelTimer.setOnClickListener(v -> {
            timerStatus = false;
            activeTimer.setAlpha(0.5f);
            cancelTimer.setAlpha(1f);
        });

        getStudySetsOfCurrentUser();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_help_item, menu);
        inflater.inflate(R.menu.menu_plus_item, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.help) {
            playHelp();
        }
        if (id == R.id.plus) {
            AlertDialog alertDialog = addStudySetAlertBuilder.create();
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void playHelp() {

        BaseVariables.hideKeyboard(this);

        FancyShowCaseQueue fq = new FancyShowCaseQueue();

        FancyShowCaseView amountOfQuestions = new FancyShowCaseView.Builder(this)
                .focusOn(findViewById(R.id.question_count_LL))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_question_count), fq);
                    }
                })
                .backgroundColor(getColor(R.color.blueForFancy))
                .build();

        FancyShowCaseView timer = new FancyShowCaseView.Builder(this)
                .focusOn(findViewById(R.id.add_time_LL))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_timer), fq);
                    }
                })
                .backgroundColor(getColor(R.color.blueForFancy))
                .build();

        FancyShowCaseView typeOfQuestions = new FancyShowCaseView.Builder(this)
                .focusOn(typesOfQuestions)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .backgroundColor(getColor(R.color.blueForFancy))
                .customView(R.layout.types_of_questions_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_types_of_questions), fq);
                    }
                })
                .build();

        FancyShowCaseView quiz = new FancyShowCaseView.Builder(this)
                .focusOn(quiz_LL)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .backgroundColor(getColor(R.color.blueForFancy))
                .customView(R.layout.question_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setImage(view, getString(R.string.fancy_quiz), fq, getDrawable(R.drawable.quiz_screen));
                    }
                })
                .build();

        FancyShowCaseView term = new FancyShowCaseView.Builder(this)
                .focusOn(term_LL)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .backgroundColor(getColor(R.color.blueForFancy))
                .customView(R.layout.question_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setImage(view, getString(R.string.fancy_term), fq, getDrawable(R.drawable.translation_screen));
                    }
                })
                .build();

        FancyShowCaseView translation = new FancyShowCaseView.Builder(this)
                .focusOn(translation_LL)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .backgroundColor(getColor(R.color.blueForFancy))
                .customView(R.layout.question_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setImage(view, getString(R.string.fancy_translation), fq, getDrawable(R.drawable.term_screen));
                    }
                })
                .build();

        FancyShowCaseView addStudySet = new FancyShowCaseView.Builder(this)
                .focusOn(findViewById(R.id.plus))
                .backgroundColor(getColor(R.color.blueForFancy))
                .customView(R.layout.fancyshowcase_with_image, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setImage(view, getString(R.string.add_study_set_to_dictation), fq, getDrawable(R.drawable.ic_plus_white_24dp));
                    }
                })
                .build();

        fq.add(amountOfQuestions)
                .add(timer)
                .add(typeOfQuestions)
                .add(quiz)
                .add(term)
                .add(translation)
                .add(addStudySet);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fq.show();
            }
        }, 200);

    }

    private void getStudySetsOfCurrentUser() {
        Call<List<StudySet>> call = mLangamyAPI.getStudySetsNamesOfCurrentUser(account.getEmail());

        call.enqueue(new Callback<List<StudySet>>() {
            @Override
            public void onResponse(Call<List<StudySet>> call, Response<List<StudySet>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CreateDictationActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }

                studySets.addAll(response.body());

                for (StudySet studySet : response.body()) {
                    studySetsNames.add(studySet.getName() + " (" + studySet.getAmount_of_words() + ")");
                }

                checkedStudySetsNames = new boolean[studySetsNames.size()];

                addStudySetAlertBuilder.setMultiChoiceItems(studySetsNames.toArray(new CharSequence[studySetsNames.size()]), checkedStudySetsNames,
                        (dialog, which, isChecked) -> {
                    if (isChecked) {
                        checkedStudySets.add(studySets.get(which));
                    } else {
                        checkedStudySets.remove(studySets.get(which));
                    }
                    checkedStudySetsNames[which] = isChecked;
                });

                SharedPreferences sf = getPreferences(MODE_PRIVATE);
                boolean help = sf.getBoolean(BaseVariables.HELP_MAKE_DICTATION, true);
                SharedPreferences.Editor editor = sf.edit();

                if(help){
                    playHelp();
                    editor.putBoolean(BaseVariables.HELP_MAKE_DICTATION, false);
                    editor.apply();
                }

            }

            @Override
            public void onFailure(Call<List<StudySet>> call, Throwable t) {
                Toast.makeText(CreateDictationActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void show(String numberPickerName) {

        final Dialog d = new Dialog(CreateDictationActivity.this);
        d.setContentView(R.layout.numberpicker_dialog);

        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        if(numberPickerName.equals("questionCount")){

            np.setMinValue(4);
            np.setMaxValue(mWords.size());
            np.setValue(Integer.parseInt(questionCountTV.getText().toString()));

        }else{
            np.setMinValue(10);
            np.setMaxValue(120);
            np.setValue(questionTime);
        }

        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(numberPickerName.equals("questionCount")) {
                    questionCount = newVal;
                    questionCountTV.setText(String.valueOf(questionCount));
                }else{
                    timerStatus = true;
                    questionTime = newVal;
                    questionTime_TV.setText(String.valueOf(questionTime));
                    cancelTimer.setAlpha(0.5f);
                    activeTimer.setAlpha(1f);
                }
            }
        });

        MaterialButton submitNumberPickerBtn = d.findViewById(R.id.submit_numberpicker);
        submitNumberPickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numberPickerName.equals("questionTime")){
                    timerStatus = true;
                    questionTime_TV.setText(String.valueOf(questionTime));
                }
                d.dismiss();
            }
        });

        d.show();
    }

    private void inizializeRecyclerView(List<Word> words) {
        layoutManager = new LinearLayoutManager(this);
        wordsRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new SpecificDictationAdapter(words);
        wordsRecyclerView.setAdapter(mAdapter);
        wordsRecyclerView.setNestedScrollingEnabled(false);
    }

    public JSONArray convertWordArrayListToJsonArray(ArrayList<Word> words) {
        JSONArray wordList = new JSONArray();
        for (int i = 0; i < words.size(); i++) {
            JSONObject wordItem = new JSONObject();
            try {
                wordItem.put("term", words.get(i).getTerm());
                wordItem.put("translation", words.get(i).getTranslation());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            wordList.put(wordItem);
        }
        return wordList;
    }

    private void createDictation(String name, JSONArray wordList,
                                 JSONArray markedWords, int amountOfWordsForDictation,
                                 String typeOfQuestions) {

        if (wordList.length() < 2) {
            return;
        }
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        Dictation dictation = new Dictation(acct.getEmail(), name, wordList.toString(), markedWords.toString(),
                amountOfWords, amountOfWordsForDictation, typeOfQuestions);

        if(timerStatus){
            if(dictationMode.equals(dictationModes[0])){
                dictation.setQuestion_time(questionTime);
            }else {
                Toast.makeText(this, getString(R.string.add_timer_dictation_mode), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        dictation.setLanguage_from(getIntent().getStringExtra(BaseVariables.FROM_LANG_MESSAGE));
        dictation.setLanguage_to(getIntent().getStringExtra(BaseVariables.FROM_LANG_MESSAGE));

        Call<Dictation> call = mLangamyAPI.createDictation(dictation);

        call.enqueue(new Callback<Dictation>() {
            @Override
            public void onResponse(Call<Dictation> call, Response<Dictation> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CreateDictationActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(CreateDictationActivity.this, SpecificDictationActivity.class);
                intent.putExtra(BaseVariables.DICTATION_ID_MESSAGE, response.body().getId());
                startActivity(intent);
                finish();

            }

            @Override
            public void onFailure(Call<Dictation> call, Throwable t) {
                Toast.makeText(CreateDictationActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class SpecificDictationAdapter extends RecyclerView.Adapter<SpecificDictationAdapter.SpecificDictationHolder> {

        private List<Word> mWords;

        class SpecificDictationHolder extends RecyclerView.ViewHolder {

            TextView term, transaltion;
//            private ToggleButton starBtn;

            SpecificDictationHolder(View v) {
                super(v);
                term = v.findViewById(R.id.term_TV);
                transaltion = v.findViewById(R.id.translation_TV);
//                starBtn = v.findViewById(R.id.starBtn);
            }
        }

        SpecificDictationAdapter(List<Word> words) {
            this.mWords = words;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public SpecificDictationAdapter.SpecificDictationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_item_without_star, parent, false);
            return new SpecificDictationHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final SpecificDictationHolder holder, final int position) {
            holder.term.setText(mWords.get(position).getTerm());
            holder.transaltion.setText(mWords.get(position).getTranslation());
//            holder.starBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                    if (isChecked) {
//                        mustHaveWords.add(mWords.get(position));
//                        otherWords.remove(mWords.get(position));
//                    } else {
//                        otherWords.add(mWords.get(position));
//                        mustHaveWords.remove(mWords.get(position));
//                    }
//                }
//            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mWords.size();
        }
    }

}
