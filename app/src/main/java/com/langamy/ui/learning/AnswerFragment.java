package com.langamy.ui.learning;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.Answer;
import com.langamy.base.classes.BaseVariables;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnswerFragment extends Fragment {

    private String mTerm, mUserAnswer, mCorrectAnswer, typeOfQuestions;
    private boolean mStatus;
    private TextView mTerm_TV, mUserAnswer_TV, mCorrectAnswer_TV;
    private ImageView mIncorrect_IV;

    public AnswerFragment(Answer answer, String typeOfQuestions) {

        this.mTerm = answer.getTerm();
        this.mUserAnswer = answer.getUserAnswer();
        this.mCorrectAnswer = answer.getCorrectAnswer();
        this.mStatus = answer.getStatus();
        this.typeOfQuestions = typeOfQuestions;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_answer, container, false);

        mTerm_TV = view.findViewById(R.id.term_TV);
        mUserAnswer_TV = view.findViewById(R.id.user_answer_TV);
        mCorrectAnswer_TV = view.findViewById(R.id.correct_answer_TV);
        mIncorrect_IV = view.findViewById(R.id.incorrect_answer_hint_IV);

        if (mStatus) {

            mTerm_TV.setTextColor(getResources().getColor(R.color.lightGreen));
            Drawable drawable = getResources().getDrawable(R.drawable.ic_action_submit_words);
            drawable.setTint(getResources().getColor(R.color.lightGreen));
            mIncorrect_IV.setImageDrawable(drawable);

        } else {
            mTerm_TV.setTextColor(getResources().getColor(R.color.red));
            mUserAnswer_TV.setTextColor(getResources().getColor(R.color.red));

        }

        if (typeOfQuestions.equals(BaseVariables.TRANSLATION_TERM)) {

            mTerm_TV.setText(mCorrectAnswer);
            mCorrectAnswer_TV.setText(mTerm);
            mUserAnswer_TV.setText(mUserAnswer);

        } else {

            mTerm_TV.setText(mTerm);
            mUserAnswer_TV.setText(mUserAnswer);
            mCorrectAnswer_TV.setText(mCorrectAnswer);

        }


        return view;
    }

}
