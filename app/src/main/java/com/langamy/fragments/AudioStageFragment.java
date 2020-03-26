package com.langamy.fragments;


import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.Answer;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Word;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioStageFragment extends Fragment {

    private Word word;
    private String fromLang;
    private ImageButton speakTerm_IB, speakTermSlow_IB;
    private TextToSpeech textToSpeechFast, textToSpeechSlow;
    private CharSequence term_cs;
    private EditText definition_ET;

    public AudioStageFragment(Word word, String fromLang) {

        this.word = word;
        this.fromLang = fromLang;

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        term_cs = word.getTerm();

        Bundle arguments = getArguments();
        int position = arguments.getInt(BaseVariables.FRAGMENT_POSITION_MESSAGE);

        textToSpeechFast = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                textToSpeechFast.setLanguage(Locale.forLanguageTag(fromLang));
                textToSpeechFast.setSpeechRate(0.8f);

                if(position == 0){
                    textToSpeechFast.speak(term_cs, TextToSpeech.QUEUE_FLUSH, null, null);
                }

            }
        });

        textToSpeechSlow = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                textToSpeechSlow.setLanguage(Locale.forLanguageTag(fromLang));
                textToSpeechSlow.setSpeechRate(0.4f);

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_audio_stage, container, false);

        speakTerm_IB = view.findViewById(R.id.speak_term_IB);
        speakTermSlow_IB = view.findViewById(R.id.speak_term_slow_IB);
        definition_ET = view.findViewById(R.id.definition_audiostage_ET);

        definition_ET.requestFocus();

        speakTerm_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(textToSpeechSlow != null){
                    textToSpeechSlow.stop();
                }
                textToSpeechFast.speak(term_cs, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        speakTermSlow_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(textToSpeechFast != null){
                    textToSpeechFast.stop();
                }

                textToSpeechSlow.speak(term_cs, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        Bundle arguments = getArguments();
        int position = arguments.getInt(BaseVariables.FRAGMENT_POSITION_MESSAGE);
        if(position == 0){
            speakTerm();
        }

        return view;
    }

    public Answer checkAnswer(String answer) {

        Answer userAnswer = new Answer(answer, word.getTranslation(), word.getTerm(),true);

        if (!answer.toLowerCase().trim().equals(word.getTranslation().toLowerCase().trim())) {

            userAnswer.setStatus(false);

        }

        definition_ET.setText("");

        return userAnswer;

    }

    @Override
    public void onResume() {

        super.onResume();

        speakTerm();
        definition_ET.requestFocus();

        BaseVariables.showKeyboard(getActivity());

    }


    public void speakTerm(){
        textToSpeechFast.speak(word.getTerm(), TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public Word getWord() {
        return word;
    }
}
