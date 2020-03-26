package com.langamy.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bignerdranch.android.main.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.langamy.adapters.StagesAdapter;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.Answer;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.NonSwipeableViewPager;
import com.langamy.base.classes.StudySet;
import com.langamy.base.classes.Word;
import com.langamy.database.StudySetsBaseHelper;
import com.langamy.database.StudySetsScheme.StudySetsTable;
import com.langamy.database.StudySetsScheme.StudySetsTable.Cols;
import com.langamy.fragments.AudioStageFragment;
import com.langamy.fragments.ContinueLearningFragment;
import com.langamy.fragments.DefinitionTermStage;
import com.langamy.fragments.QuizStage;
import com.langamy.fragments.TermDefinitionStage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LearnActivity extends AppCompatActivity {

    ArrayList<Fragment> stages = new ArrayList<Fragment>();
    private StudySet mStudySet;
    private ArrayList<Word> wordsForLearning, words;
    private int studysetId, restWords, firstStageWords, thirdStageWords, masteredWords, amountOfWords;
    private boolean learnMarked;
    private String fromLang, toLang;
    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private SQLiteDatabase mDatabase;
    private GoogleSignInAccount account;

    private AlertDialog correctAlertDialog, wrongAlertDialog;
    private AlertDialog.Builder wrongDialogBuilder;
    private NonSwipeableViewPager learnVP2;
    private StagesAdapter adapter;
    private View wrongDialogView;
    private TextView restWords_TV, firstStageWords_TV, thirdStageWords_TV, masteredWords_TV;
    private RelativeLayout learnContainer_RL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        learnContainer_RL = findViewById(R.id.learn_activity_container);
        learnVP2 = findViewById(R.id.learn_VP2);
        restWords_TV = findViewById(R.id.rest_words_TV);
        firstStageWords_TV = findViewById(R.id.first_stage_words_TV);
        thirdStageWords_TV = findViewById(R.id.third_stage_words_TV);
        masteredWords_TV = findViewById(R.id.mastered_words_TV);

        Intent intent = getIntent();
        mStudySet = (StudySet) intent.getSerializableExtra(BaseVariables.STUDY_SET_MESSAGE);
        words = (ArrayList<Word>) intent.getSerializableExtra(BaseVariables.WORDS_MESSAGE);
        learnMarked = intent.getBooleanExtra(BaseVariables.MARKED_MESSAGE, false);
        fromLang = mStudySet.getLanguage_from();
        toLang = mStudySet.getLanguage_to();
        studysetId = mStudySet.getId();
        amountOfWords = mStudySet.getAmount_of_words();

        for (Word word : words) {

            if (!word.isFirstStage()) {
                restWords++;
                continue;
            }

            if (word.isFirstStage() && !word.isThirdStage()) {
                firstStageWords++;
            } else if (word.isThirdStage() && !word.isForthStage()) {
                thirdStageWords++;
            } else if (word.isForthStage()) {
                masteredWords++;
            }

        }

        ArrayList<Word> wordArrayList = new ArrayList<>(words);

        wordsForLearning = new ArrayList<>();

        wordsForLearning.addAll(getRandomObjects(wordArrayList));

        stages = generateStages(wordsForLearning);

        adapter = new StagesAdapter(getSupportFragmentManager(), stages);

        learnVP2.setAdapter(adapter);
        learnVP2.setOffscreenPageLimit(8);

        restWords_TV.setText(String.valueOf(restWords));
        firstStageWords_TV.setText(String.valueOf(firstStageWords));
        thirdStageWords_TV.setText(String.valueOf(thirdStageWords));
        masteredWords_TV.setText(String.valueOf(masteredWords));

        AlertDialog.Builder correctDialogBuilder = new AlertDialog.Builder(this);
        correctDialogBuilder.setView(View.inflate(this, R.layout.correct_alert_dialog, null));

        correctAlertDialog = correctDialogBuilder.create();

        correctAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                goToNextPage();
            }
        });

        wrongDialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        wrongDialogView = inflater.inflate(R.layout.wrong_alert_dialog, null);
        wrongDialogBuilder.setView(wrongDialogView);

        wrongAlertDialog = wrongDialogBuilder.create();

        account = GoogleSignIn.getLastSignedInAccount(this);

    }

    public void textScaleAnimation(TextView textView) {
        final float startSize = 14; // Size in pixels
        final float endSize = 20;
        long animationDuration = 450; // Animation duration in ms

        ValueAnimator animator = ValueAnimator.ofFloat(startSize, endSize);
        animator.setDuration(animationDuration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                textView.setTextSize(animatedValue);
            }
        });
        animator.setRepeatCount(1);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.start();
    }

    public void reloadViewPager(View view) {

        stages.clear();
        wordsForLearning.clear();

        ArrayList<Word> wordArrayList = new ArrayList<>(words);

        wordsForLearning.addAll(getRandomObjects(wordArrayList));

        stages = new ArrayList<>();
        stages = generateStages(wordsForLearning);

        if (stages.size() == 1) {

            return;
        }

        adapter.setStages(stages);
        learnVP2.setAdapter(adapter);
        learnVP2.invalidate();


        if (stages.get(0).getClass().getSimpleName().equals("AudioStageFragment")) {

            AudioStageFragment currentStage = (AudioStageFragment) stages.get(0);
            currentStage.speakTerm();
        }

        learnVP2.setCurrentItem(0);

    }

    private void showCorrectAlertDialog() {

        correctAlertDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (correctAlertDialog.isShowing()) {

                    goToNextPage();
                    correctAlertDialog.dismiss();

                }
            }
        }, 500);

    }

    private void showWrongAlertDialog(String correctAnswer, int wordId, String stage) {

        TextView correctAnswer_TV = wrongDialogView.findViewById(R.id.correct_answer_TV);
        TextView im_right_TV = wrongDialogView.findViewById(R.id.im_right_TV);
        ImageButton continueLearning_TV = wrongDialogView.findViewById(R.id.continue_learning_TV);

        correctAnswer_TV.setText(correctAnswer);

        wrongAlertDialog.show();

        continueLearning_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wrongAlertDialog.dismiss();
                goToNextPage();
            }
        });

        wrongAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                wrongAlertDialog.dismiss();
                goToNextPage();
            }
        });

        im_right_TV.setOnClickListener(v -> {
            switch (stage) {
                case "first":
                    words.get(wordId).setFirstStage(true);
                    break;
                case "second":
                    words.get(wordId).setSecondStage(true);

                    break;
                case "third":
                    words.get(wordId).setThirdStage(true);

                    break;
                case "forth":
                    words.get(wordId).setForthStage(true);
                    masteredWords++;
                    break;
            }
            wrongAlertDialog.dismiss();
            goToNextPage();

        });

    }

    public void checkAnswerFirstStage(View v) {
        String text = ((TextView) v).getText().toString();

        QuizStage quizStage = (QuizStage) (stages.get(learnVP2.getCurrentItem()));
        Answer userAnswer = quizStage.checkAnswer(text);

        int correctWordIndex = words.indexOf(quizStage.getWord());
        if (userAnswer.getStatus()) {

            Word correctWord = words.get(correctWordIndex);

            correctWord.setFirstStage(true);

            textScaleAnimation(firstStageWords_TV);

            firstStageWords_TV.setText(String.valueOf(Integer.parseInt(firstStageWords_TV.getText().toString()) + 1));
            restWords_TV.setText(String.valueOf(Integer.parseInt(restWords_TV.getText().toString()) - 1));

            showCorrectAlertDialog();

        } else {

            showWrongAlertDialog(userAnswer.getCorrectAnswer(), correctWordIndex, "first");

        }
    }

    public void checkAnswerAudioStage(View view) {

        RelativeLayout parent = (RelativeLayout) view.getParent();
        EditText userAnswer_ET = (EditText) parent.findViewById(R.id.definition_audiostage_ET);
        String text = userAnswer_ET.getText().toString().toLowerCase().trim();

        AudioStageFragment audioStageFragment = (AudioStageFragment) (stages.get(learnVP2.getCurrentItem()));

        Answer userAnswer = audioStageFragment.checkAnswer(text);

        if (view.getId() == R.id.cannot_speak_BTN) {
            userAnswer.setStatus(true);
        }

        int correctWordIndex = words.indexOf(audioStageFragment.getWord());
        if (userAnswer.getStatus()) {

            Word correctWord = words.get(correctWordIndex);

            correctWord.setSecondStage(true);

            showCorrectAlertDialog();

        } else {
            showWrongAlertDialog(userAnswer.getCorrectAnswer(), correctWordIndex, "second");
        }
    }

    public void checkAnswerTermTranslation(View v) {

        RelativeLayout parent = (RelativeLayout) v.getParent();
        EditText child = (EditText) parent.findViewById(R.id.answer_ET);
        String text = child.getText().toString().trim().toLowerCase();

        TermDefinitionStage termDefinitionStage = (TermDefinitionStage) stages.get(learnVP2.getCurrentItem());
        Answer userAnswer = termDefinitionStage.checkAnswer(text);

        int correctWordIndex = words.indexOf(termDefinitionStage.getWord());
        if (userAnswer.getStatus()) {

            Word correctWord = words.get(correctWordIndex);

            correctWord.setThirdStage(true);

            textScaleAnimation(thirdStageWords_TV);

            thirdStageWords_TV.setText(String.valueOf(Integer.parseInt(thirdStageWords_TV.getText().toString()) + 1));
            firstStageWords_TV.setText(String.valueOf(Integer.parseInt(firstStageWords_TV.getText().toString()) - 1));

            showCorrectAlertDialog();

        } else {
            showWrongAlertDialog(userAnswer.getCorrectAnswer(), correctWordIndex, "third");
        }

        parent.setVisibility(View.INVISIBLE);

    }

    public void checkAnswerTranslationTerm(View v) {
        RelativeLayout parent = (RelativeLayout) v.getParent();
        EditText child = (EditText) parent.findViewById(R.id.answer_ET);
        String text = child.getText().toString().trim().toLowerCase();

        DefinitionTermStage definitionTermStage = (DefinitionTermStage) stages.get(learnVP2.getCurrentItem());
        Answer userAnswer = definitionTermStage.checkAnswer(text);

        int correctWordIndex = words.indexOf(definitionTermStage.getWord());
        if (userAnswer.getStatus()) {

            Word correctWord = words.get(correctWordIndex);

            correctWord.setForthStage(true);

            textScaleAnimation(masteredWords_TV);

            masteredWords++;

            masteredWords_TV.setText(String.valueOf(Integer.parseInt(masteredWords_TV.getText().toString()) + 1));
            thirdStageWords_TV.setText(String.valueOf(Integer.parseInt(thirdStageWords_TV.getText().toString()) - 1));

            showCorrectAlertDialog();

        } else {
            showWrongAlertDialog(userAnswer.getTerm(), correctWordIndex, "forth");
        }

    }

    public void goToNextPage() {
        try {
            updateStudyset();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        learnVP2.setCurrentItem(learnVP2.getCurrentItem() + 1);

        Fragment currentFragment = stages.get(learnVP2.getCurrentItem());

        if (currentFragment.getClass().getSimpleName().equals("ContinueLearningFragment")) {

            ContinueLearningFragment continueLearningFragment = (ContinueLearningFragment) currentFragment;
            continueLearningFragment.setStudysetId(studysetId, learnMarked);
            continueLearningFragment.setAmountOfWords(words.size());
            continueLearningFragment.setMasteredWords(masteredWords);

        }
    }

    public ArrayList<Word> getRandomObjects(List<Word> wordArrayList) {

        ArrayList<Word> randomWords = new ArrayList<>();

        Random random = new Random();
        int amountOfRandomObjects = 7;

        if (wordArrayList.size() < 7) {
            amountOfRandomObjects = wordArrayList.size();
        }

        for (int i = 0; i < amountOfRandomObjects; i++) {

            int randomIndex = random.nextInt(wordArrayList.size());
            Word word = wordArrayList.get(randomIndex);
            randomWords.add(word);

            wordArrayList.remove(word);

        }
        return randomWords;
    }

    private ArrayList<Fragment> generateStages(ArrayList<Word> wordsForGenerating) {

        ArrayList<Fragment> generatedStages = new ArrayList<>();

        for (int i = 0; i < wordsForGenerating.size(); i++) {

            if (!wordsForGenerating.get(i).isFirstStage()) {

                ArrayList<Word> wordArrayListCopy = new ArrayList<>(words);

                ArrayList<String> randomAnswers = BaseVariables.generateThreeRandomAnswer(wordsForGenerating.get(i), wordArrayListCopy);

                QuizStage quizStage = new QuizStage(wordsForGenerating.get(i),
                        randomAnswers.get(0), randomAnswers.get(1), randomAnswers.get(2), fromLang, 0);

                generatedStages.add(quizStage);

            } else if (!wordsForGenerating.get(i).isSecondStage()) {

                generatedStages.add(new AudioStageFragment(wordsForGenerating.get(i), fromLang));

            } else if (!wordsForGenerating.get(i).isThirdStage()) {

                generatedStages.add(new TermDefinitionStage(wordsForGenerating.get(i), fromLang));

            } else if (!wordsForGenerating.get(i).isForthStage()) {
                generatedStages.add(new DefinitionTermStage(wordsForGenerating.get(i), toLang));
            }
        }

        generatedStages.add(new ContinueLearningFragment());

        learnVP2.setCurrentItem(learnVP2.getCurrentItem() + 1);

        return generatedStages;

    }

    public void updateStudyset() throws JSONException {

        JSONArray wordsForUpdatingStudySet = new JSONArray();

        for (int i = 0; i < words.size(); i++) {
            JSONObject currentWord = new JSONObject();
            currentWord.put("term", words.get(i).getTerm());
            currentWord.put("translation", words.get(i).getTranslation());
            currentWord.put("firstStage", words.get(i).isFirstStage());
            currentWord.put("secondStage", words.get(i).isSecondStage());
            currentWord.put("thirdStage", words.get(i).isThirdStage());
            currentWord.put("forthStage", words.get(i).isForthStage());
            wordsForUpdatingStudySet.put(currentWord);
        }

        StudySet studySet = mStudySet;

        if (learnMarked) {
            studySet.setMarked_words(wordsForUpdatingStudySet.toString());
        }else{
            mStudySet.setWords(wordsForUpdatingStudySet.toString());
        }

        mDatabase = new StudySetsBaseHelper(this)
                .getWritableDatabase();

        if (BaseVariables.checkNetworkConnection(this)) {

            Call<StudySet> call = mLangamyAPI.patchStudySet(studysetId, studySet);
            call.enqueue(new Callback<StudySet>() {
                @Override
                public void onResponse(Call<StudySet> call, Response<StudySet> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(LearnActivity.this, "Changes have not been saved", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ContentValues values = BaseVariables.getContentValuesForStudyset(studySet, true);

                    String uuidString = String.valueOf(studySet.getId());

                    mDatabase.update(StudySetsTable.NAME, values,
                            Cols.id + "=?", new String[]{uuidString});

                }

                @Override
                public void onFailure(Call<StudySet> call, Throwable t) {
                    Log.d("FAILURE_LEARN_ACTIVITY", t.toString());
                }
            });
        } else {

            ContentValues values = BaseVariables.getContentValuesForStudyset(studySet, false);

            String uuidString = String.valueOf(studySet.getId());

            mDatabase.update(StudySetsTable.NAME, values,
                    Cols.id + "=?", new String[]{uuidString});

        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {

            String className = stages.get(learnVP2.getCurrentItem()).getClass().getSimpleName();
            RelativeLayout parent = (RelativeLayout) stages.get(learnVP2.getCurrentItem()).getView();
            View child = parent.getChildAt(0);

            switch (className) {
                case "AudioStageFragment":
                    checkAnswerAudioStage(child);
                    break;
                case "TermDefinitionStage":
                    checkAnswerTermTranslation(child);
                    break;
                case "DefinitionTermStage":
                    checkAnswerTranslationTerm(child);
                    break;
            }
        }

        return super.onKeyUp(keyCode, event);
    }
}
