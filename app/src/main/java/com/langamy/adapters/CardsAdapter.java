package com.langamy.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.CardItem;
import com.langamy.ui.studyset.show.CardModeActivity;

import java.util.List;

public class CardsAdapter extends BaseAdapter {

    private Activity activity;
    private List<CardItem> data;

    public CardsAdapter(Activity activity, List<CardItem> data) {
        this.data = data;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public CardItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // If holder not exist then locate all view from UI file.
        if (convertView == null) {
            // inflate UI from XML file
            convertView = inflater.inflate(R.layout.item_card, parent, false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        } else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        //setting data to views
        holder.term_TV.setText(getItem(position).getTerm());
        holder.translation_TV.setText(getItem(position).getTranslation());

        holder.volumeUp_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardModeActivity.speakTerm(getItem(position).getTerm());
            }
        });

        return convertView;
    }

    private class ViewHolder {

        private TextView term_TV;
        private TextView translation_TV;
        private ImageButton volumeUp_IB;

        public ViewHolder(View view) {

            term_TV = (TextView) view.findViewById(R.id.term_TV);
            translation_TV = (TextView) view.findViewById(R.id.translation_TV);
            volumeUp_IB = view.findViewById(R.id.volume_up_IB);

        }
    }
}