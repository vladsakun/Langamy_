package com.langamy.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Dictation;
import com.langamy.ui.dictation.show.SpecificDictationActivity;

import java.util.List;

public class DoneDictationsAdapter extends RecyclerView.Adapter<DoneDictationsAdapter.DoneDictationsHolder> {

    List<Dictation> mDictationsList;
    private Context context;


    public DoneDictationsAdapter(List<Dictation> doneDictations, Context context) {
        this.mDictationsList = doneDictations;
        this.context = context;
    }

    @NonNull
    @Override
    public DoneDictationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_dictation_item, parent, false);
        DoneDictationsHolder holder = new DoneDictationsHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull DoneDictationsHolder holder, int position) {

        holder.delete.setVisibility(View.GONE);
        holder.name.setText(mDictationsList.get(position).getName() + " (" + mDictationsList.get(position).getCode() + ")");

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, SpecificDictationActivity.class);
            intent.putExtra(BaseVariables.DICTATION_CODE_MESSAGE, Integer.parseInt(mDictationsList.get(position).getCode()));
            context.startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return mDictationsList.size();
    }

    public class DoneDictationsHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private ImageButton delete;

        public DoneDictationsHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.dictation_name_TV);
            delete = itemView.findViewById(R.id.delete_dictation_BTN);

        }

    }

}
