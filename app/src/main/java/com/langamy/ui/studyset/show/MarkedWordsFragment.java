package com.langamy.ui.studyset.show;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.Word;

import java.util.ArrayList;

public class MarkedWordsFragment extends Fragment {

    private ArrayList<Word> words, markedWords;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    public MarkedWordsFragment(ArrayList<Word> words) {

        ArrayList<Word> markedWords = new ArrayList<>();

        for (Word word : words) {
            if (word.isMarked()) {
                markedWords.add(word);
            }
        }

        this.words = words;
        this.markedWords = markedWords;

    }

    public ArrayList<Word> getWords() {
        return words;
    }

    public void setWords(ArrayList<Word> words) {

        ArrayList<Word> markedWords = new ArrayList<>();

        for (Word word : words) {
            if (word.isMarked()) {
                markedWords.add(word);
            }
        }

        this.markedWords.clear();
        this.markedWords.addAll(markedWords);

        if(mAdapter == null){
            mAdapter = new MarkedWordsAdapter(this.markedWords);
        }
        mAdapter.notifyDataSetChanged();

        Log.d("SIZE", String.valueOf(markedWords.size()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_marked_words, container, false);
        recyclerView = view.findViewById(R.id.marked_words_recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    //Adapter for word recyclerview
    class MarkedWordsAdapter extends RecyclerView.Adapter<MarkedWordsAdapter.SpecificStudySetHolder> {

        public ArrayList<Word> mWords;

        public class SpecificStudySetHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView term, transaltion;
            private ToggleButton starBtn;

            SpecificStudySetHolder(View v) {
                super(v);
                term = v.findViewById(R.id.term_TV);
                transaltion = v.findViewById(R.id.translation_TV);
                starBtn = v.findViewById(R.id.starBtn);
            }
        }

        public MarkedWordsAdapter(ArrayList<Word> words) {

            this.mWords = words;

        }

        // Create new views (invoked by the layout manager)
        @Override
        public MarkedWordsAdapter.SpecificStudySetHolder onCreateViewHolder(ViewGroup parent,
                                                                            int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.studyset_word_item, parent, false);
            MarkedWordsAdapter.SpecificStudySetHolder vh = new MarkedWordsAdapter.SpecificStudySetHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final MarkedWordsAdapter.SpecificStudySetHolder holder, int position) {

            holder.term.setText(mWords.get(position).getTerm());
            holder.transaltion.setText(mWords.get(position).getTranslation());
            holder.starBtn.setChecked(true);

            holder.starBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (!isChecked) {
                        Word currentWord = mWords.get(position);
                        int indexOfCurrentWord = words.indexOf(currentWord);
                        SpecificStudySetActivity specificStudySetActivity = (SpecificStudySetActivity) getActivity();
                        specificStudySetActivity.removeMarkedWord(indexOfCurrentWord);
                        mAdapter.notifyDataSetChanged();
                    }

                }
            });

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mWords.size();
        }
    }

}
