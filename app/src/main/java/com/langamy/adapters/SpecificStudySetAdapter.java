package com.langamy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.Word;

import java.util.ArrayList;
import java.util.List;

public class SpecificStudySetAdapter extends RecyclerView.Adapter<SpecificStudySetAdapter.SpecificStudySetHolder> {

    public List<Word> mWords;

    public static class SpecificStudySetHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView term, transaltion;
        private ToggleButton starBtn;

        public SpecificStudySetHolder(View v) {
            super(v);
            term = v.findViewById(R.id.term_TV);
            transaltion = v.findViewById(R.id.translation_TV);
            starBtn = v.findViewById(R.id.starBtn);
        }
    }

    public SpecificStudySetAdapter(List<Word> words) {
        this.mWords = words;
    }

    public List<Word> getmWords() {
        return mWords;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SpecificStudySetAdapter.SpecificStudySetHolder onCreateViewHolder(ViewGroup parent,
                                                                             int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.studyset_word_item, parent, false);
        SpecificStudySetHolder vh = new SpecificStudySetHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final SpecificStudySetHolder holder, int position) {
        holder.term.setText(mWords.get(position).getTerm());
        holder.transaltion.setText(mWords.get(position).getTranslation());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mWords.size();
    }
}
