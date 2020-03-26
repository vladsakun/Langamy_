package com.langamy.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bignerdranch.android.main.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CardBackFragment extends Fragment {

    private TextView frontCard_TV;
    private String frontCardText;

    public CardBackFragment(String frontCardText) {
        this.frontCardText = frontCardText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_back, container, false);

        frontCard_TV = view.findViewById(R.id.back_card_TV);

        frontCard_TV.setText(frontCardText);

        return view;
    }

}
