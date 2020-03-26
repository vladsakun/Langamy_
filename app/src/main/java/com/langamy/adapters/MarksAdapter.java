package com.langamy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.Mark;

import java.util.ArrayList;
import java.util.List;

public class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.MarksHolder> {

    private List<Mark> mMarks;

    public MarksAdapter(ArrayList<Mark> marks) {

        this.mMarks = marks;

    }

    @NonNull
    @Override
    public MarksHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mark_item, parent, false);
        MarksHolder holder = new MarksHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MarksHolder holder, int position) {
        holder.email_TV.setText(mMarks.get(position).getEmail());
        holder.mark_TV.setText(String.valueOf(mMarks.get(position).getMark()));
    }

    @Override
    public int getItemCount() {
        return mMarks.size();
    }

    public class MarksHolder extends RecyclerView.ViewHolder {

        private TextView email_TV, mark_TV;

        public MarksHolder(@NonNull View itemView) {
            super(itemView);
            email_TV = itemView.findViewById(R.id.mark_email_TV);
            mark_TV = itemView.findViewById(R.id.mark_TV);
        }
    }
}
