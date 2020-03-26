package com.langamy.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.bignerdranch.android.main.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GreetingItemFragment extends Fragment {


    public GreetingItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_greeting_item, container, false);
    }

}
