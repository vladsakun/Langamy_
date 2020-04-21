package com.langamy.fragments;


import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bignerdranch.android.main.R;
import com.langamy.activities.DictationPagerActivity;
import com.langamy.base.classes.Answer;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuizStage extends Fragment {

    private ArrayList<String> answers;
    private Word word;
    private String speakLang;
    private int startTime;

    private TextToSpeech textToSpeech;
    private Chronometer mChronometer;
    private TextView term_tv, firstAnswer_tv, secondAnswer_tv, thirdAnswer_tv, forthAnswer_tv;
    private ImageButton speak;

    public QuizStage(Word word, String firstIncorrectAnswer, String secondIncorrectAnswer, String thirdIncorrectAnswer,
                     String speakLang, int time) {

        this.word = word;
        this.speakLang = speakLang;
        this.startTime = time;
        this.answers = new ArrayList<>();
        this.answers.add(word.getTranslation());
        this.answers.add(firstIncorrectAnswer);
        this.answers.add(secondIncorrectAnswer);
        this.answers.add(thirdIncorrectAnswer);

        Collections.shuffle(this.answers);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quiz_stage, container, false);

        setRetainInstance(true);

        term_tv = view.findViewById(R.id.term_TV);
        firstAnswer_tv = view.findViewById(R.id.answer1);
        secondAnswer_tv = view.findViewById(R.id.answer2);
        thirdAnswer_tv = view.findViewById(R.id.answer3);
        forthAnswer_tv = view.findViewById(R.id.answer4);
        speak = view.findViewById(R.id.volume_up_IB);
        mChronometer = view.findViewById(R.id.quiz_chronometer);

        term_tv.setText(word.getTerm());

        firstAnswer_tv.setText(answers.get(0));
        secondAnswer_tv.setText(answers.get(1));
        thirdAnswer_tv.setText(answers.get(2));
        forthAnswer_tv.setText(answers.get(3));

        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.forLanguageTag(speakLang));
                textToSpeech.setSpeechRate(0.8f);
            }
        });

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence s = word.getTerm();
                textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        textToSpeech.shutdown();
    }

    public Answer checkAnswer(String text) {

        Answer answer = new Answer(text, word.getTranslation(), word.getTerm(), true);

        if (!text.equals(word.getTranslation())) {
            answer.setStatus(false);
        }

        return answer;
    }

    public Word getWord() {
        return word;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        BaseVariables.hideKeyboard(getActivity());
        if (startTime != 0) {

            mChronometer.setBase(SystemClock.elapsedRealtime() + 1000 * startTime);
            mChronometer.setCountDown(true);
            mChronometer.setVisibility(View.VISIBLE);

            mChronometer.start();
            new CountDownTimer(1000 * startTime, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    DictationPagerActivity activity = (DictationPagerActivity) getActivity();
                    activity.addIncorrectAnswer();
                    activity.goToNextPage();
                }

            }.start();
        }
    }
}
