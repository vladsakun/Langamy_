package com.langamy.ui.learning.stages;


import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.Answer;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Word;

import java.util.Locale;

public class DefinitionTermStage extends Fragment {

    private Word word;
    private EditText term_ET;
    private TextToSpeech textToSpeech;
    private String fromLang;
    private ImageButton speak;

    public DefinitionTermStage(Word word, String fromLang) {
        this.fromLang = fromLang;
        this.word = word;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        textToSpeech.shutdown();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_definition_term_stage, container, false);

        TextView mDefinition_TV = view.findViewById(R.id.definition_TV);
        term_ET = view.findViewById(R.id.answer_ET);
        speak = view.findViewById(R.id.volume_up_IB);

        term_ET.requestFocus();
        mDefinition_TV.setText(word.getTranslation());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textToSpeech = new TextToSpeech(getActivity(), status -> {
            textToSpeech.setLanguage(Locale.forLanguageTag(fromLang));
            textToSpeech.setSpeechRate(0.8f);
        });

        speak.setOnClickListener(v -> {
            CharSequence c = word.getTranslation();
            textToSpeech.speak(c, TextToSpeech.QUEUE_FLUSH, null, null);
        });
    }

    public Answer checkAnswer(String userAnswer) {
        Answer answer = new Answer(userAnswer, word.getTranslation(), word.getTerm(), true);

        if (!userAnswer.toLowerCase().trim().equals(word.getTerm().toLowerCase().trim())) {

            answer.setStatus(false);
        }

        term_ET.setText("");
        return answer;
    }

    public Word getWord() {
        return word;
    }

    @Override
    public void onResume() {

        super.onResume();
        term_ET.requestFocus();

        BaseVariables.showKeyboard(getActivity());
    }

}
