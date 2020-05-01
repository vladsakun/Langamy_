package com.langamy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.main.R;
import com.langamy.base.classes.GreetingItem;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapterExample extends
        SliderViewAdapter<SliderAdapterExample.SliderAdapterVH> {

    private Context context;
    private List<GreetingItem> mSliderItems = new ArrayList<>();

    public SliderAdapterExample(Context context, ArrayList<GreetingItem> sliderItems) {
        this.context = context;
        this.mSliderItems = sliderItems;
    }

    public void renewItems(List<GreetingItem> sliderItems) {
        this.mSliderItems = sliderItems;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.mSliderItems.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(GreetingItem sliderItem) {
        this.mSliderItems.add(sliderItem);
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_greeting_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
        viewHolder.greetingImage.setImageDrawable(mSliderItems.get(position).getImage());
        viewHolder.textViewDescription.setText(mSliderItems.get(position).getText());
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView greetingImage;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            greetingImage = itemView.findViewById(R.id.greeting_item_image);
            textViewDescription = itemView.findViewById(R.id.greeting_item_text);
            this.itemView = itemView;
        }
    }

}
