package com.langamy.fragments;


import android.content.res.Configuration;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.Answer;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Word;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class TermDefinitionStage extends Fragment  {

    private TextView term_TV;
    private EditText definition_ET;
    private Word word;
    private TextToSpeech textToSpeech;
    private String fromLang;

    public TermDefinitionStage(Word word, String fromLang) {
        this.word = word;
        this.fromLang = fromLang;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        ViewGroup viewGroup = (ViewGroup) getView();
        viewGroup.removeAllViewsInLayout();
        View view = onCreateView(inflater, viewGroup, null);
        viewGroup.addView(view);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_term_definition_stage, container, false);

        term_TV = view.findViewById(R.id.term_TV);
        definition_ET = view.findViewById(R.id.answer_ET);
        ImageButton speak = view.findViewById(R.id.volume_up_IB);
        definition_ET.requestFocus();

        term_TV.setText(word.getTerm());

        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.forLanguageTag(fromLang));
                textToSpeech.setSpeechRate(0.8f);
            }
        });

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence c = word.getTerm();
                textToSpeech.speak(c, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        definition_ET.requestFocus();

        BaseVariables.showKeyboard(getActivity());

    }

    public Answer checkAnswer(String userAnswer) {

        Answer answer = new Answer(userAnswer, word.getTranslation(), word.getTerm(), true);

        if (!userAnswer.toLowerCase().trim().equals(word.getTranslation().toLowerCase().trim())) {
            answer.setStatus(false);
        }

        definition_ET.setText("");

        return answer;

    }

    public Word getWord() {
        return word;
    }


}
