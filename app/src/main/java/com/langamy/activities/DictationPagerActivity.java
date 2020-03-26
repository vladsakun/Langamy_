package com.langamy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.Answer;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Dictation;
import com.langamy.base.classes.Word;
import com.langamy.fragments.DefinitionTermStage;
import com.langamy.fragments.QuizStage;
import com.langamy.fragments.TermDefinitionStage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class DictationPagerActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private ProgressBar mProgressBar, circularProgressBar;
    Dictation mDictation;

    private ArrayList<Fragment> mFragments;
    private ArrayList<Answer> mAnswers = new ArrayList<>();
    private List<Word> mWordList, markedWordList, otherWordList;

    private int questionAmount;
    private int progress = 1, correctAnswers = 0, dictationId;
    private String typeOfQuestions, fromLang, toLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictation_pager);

        mViewPager = findViewById(R.id.viewpager);
        mProgressBar = findViewById(R.id.dictation_PB);
        circularProgressBar = findViewById(R.id.dictation_viewpager_circular_PB);

        Intent intent = getIntent();

        mDictation = (Dictation) intent.getSerializableExtra(BaseVariables.DICTATION_MESSAGE);
        markedWordList = (List<Word>) intent.getSerializableExtra(BaseVariables.MARKED_WORDS_MESSAGE);
        otherWordList = (List<Word>) intent.getSerializableExtra(BaseVariables.OTHER_WORDS_MESSAGE);
        fromLang = intent.getStringExtra(BaseVariables.FROM_LANG_MESSAGE);
        toLang = intent.getStringExtra(BaseVariables.TO_LANG_MESSAGE);

        dictationId = intent.getIntExtra(BaseVariables.DICTATION_ID_MESSAGE, 0);
        questionAmount = intent.getIntExtra(BaseVariables.QUESTION_AMOUNT_MESSAGE, 0);
        typeOfQuestions = intent.getStringExtra(BaseVariables.DICTATION_TYPE_OF_QUESTIONS_MESSAGE);

        int otherWordsAmount = questionAmount - markedWordList.size();

        if (otherWordsAmount <= 0) {

            mWordList = new ArrayList<>(markedWordList);

        } else {

            Collections.shuffle(otherWordList);
            ArrayList<Word> cuttedOtherWordList = new ArrayList<>();

            for (int i = 0; i < otherWordsAmount; i++) {
                cuttedOtherWordList.add(otherWordList.get(i));
            }

            markedWordList.addAll(cuttedOtherWordList);
            mWordList = new ArrayList<>(markedWordList);

        }

        Collections.shuffle(mWordList);
        mProgressBar.setMax(mWordList.size());

        mFragments = new ArrayList<>();

        switch (typeOfQuestions) {
            case "quiz":

                int questionTime = mDictation.getQuestion_time();

                for (int i = 0; i < mWordList.size(); i++) {

                    List<Word> copyOfWordList = new ArrayList<>(mWordList);
                    ArrayList<String> threeRandomAnswers = BaseVariables.generateThreeRandomAnswer(mWordList.get(i), copyOfWordList);
                    mFragments.add(new QuizStage(mWordList.get(i),
                            threeRandomAnswers.get(0),
                            threeRandomAnswers.get(1),
                            threeRandomAnswers.get(2), fromLang, questionTime));
                }

                break;

            case "term_translation":

                for (int i = 0; i < mWordList.size(); i++) {

                    mFragments.add(new TermDefinitionStage(mWordList.get(i), fromLang));

                }

                break;
            case "translation_term":

                for (int i = 0; i < mWordList.size(); i++) {

                    mFragments.add(new DefinitionTermStage(mWordList.get(i), toLang));

                }

                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        });

        mViewPager.setOffscreenPageLimit(mFragments.size());

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageSelected(int pos) {
                //This is because progress is 0 at the start of the program
                progress++;
            }

        });

    }

    public void checkAnswerFirstStage(View v) {

        String text = ((TextView) v).getText().toString();
        QuizStage quizStage = (QuizStage) mFragments.get(mViewPager.getCurrentItem());
        Answer userAnswer = quizStage.checkAnswer(text);

        mAnswers.add(userAnswer);

        if (userAnswer.getStatus()) {
            correctAnswers++;
        }

        goToNextPage();
    }

    public void checkAnswerTermTranslation(String userAnswer) {
        TermDefinitionStage termDefinitionStage = (TermDefinitionStage) mFragments.get(mViewPager.getCurrentItem());

        Answer answer = termDefinitionStage.checkAnswer(userAnswer);

        mAnswers.add(answer);

        if (answer.getStatus()) {
            correctAnswers++;
        }

        goToNextPage();
    }

    public void checkAnswerTranslationTerm(String userAnswer) {

        DefinitionTermStage definitionTermStage = (DefinitionTermStage) mFragments.get(mViewPager.getCurrentItem());

        Answer answer = definitionTermStage.checkAnswer(userAnswer);

        mAnswers.add(answer);

        if (answer.getStatus()) {
            correctAnswers++;
        }

        goToNextPage();

    }

    public void goToNextPage() {
        mProgressBar.setProgress(progress);

        //If questions were ended
        if (mViewPager.getCurrentItem() == mWordList.size() - 1) {
            mProgressBar.setProgress(progress++);
            final Intent intent = new Intent(DictationPagerActivity.this, DictationResultActivity.class);
            intent.putExtra(BaseVariables.AMOUNT_OF_CORRECT_ANSWERS_MESSAGE, correctAnswers);
            intent.putExtra(BaseVariables.QUESTION_AMOUNT_MESSAGE, questionAmount);
            intent.putExtra(BaseVariables.DICTATION_ID_MESSAGE, dictationId);
            intent.putExtra(BaseVariables.USER_ANSWERS_MESSAGE, mAnswers);
            intent.putExtra(BaseVariables.TYPE_OF_QUESTIONS_MESSAGE, typeOfQuestions);
            startActivity(intent);
            finish();
        }

        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {

            String className = mFragments.get(mViewPager.getCurrentItem()).getClass().getSimpleName();
            RelativeLayout parent = (RelativeLayout) mFragments.get(mViewPager.getCurrentItem()).getView();
            EditText editText = parent.findViewById(R.id.answer_ET);
            String userAnswer = editText.getText().toString().trim().toLowerCase();

            switch (className) {
                case "TermDefinitionStage":
                    checkAnswerTermTranslation(userAnswer);
                    break;
                case "DefinitionTermStage":
                    checkAnswerTranslationTerm(userAnswer);
                    break;
            }
        }

        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }

        return super.onKeyUp(keyCode, event);
    }

    public void addIncorrectAnswer() {
        QuizStage quizStage = (QuizStage) mFragments.get(mViewPager.getCurrentItem());
        Answer userAnswer = quizStage.checkAnswer("");
        mAnswers.add(userAnswer);
    }
}
