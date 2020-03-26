package com.langamy.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.langamy.base.classes.BaseVariables;

import java.util.ArrayList;

public class StagesAdapter extends FragmentStatePagerAdapter {

    ArrayList<Fragment> stages;

    public StagesAdapter(@NonNull FragmentManager fm, ArrayList<Fragment> stages) {

        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.stages = stages;

    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = stages.get(position);
        Bundle bundleFeatures = new Bundle();
        bundleFeatures.putInt(BaseVariables.FRAGMENT_POSITION_MESSAGE, position);
        fragment.setArguments(bundleFeatures);
        return fragment;
    }

    @Override
    public int getCount() {
        return stages.size();
    }

    public void setStages(ArrayList<Fragment> stages) {
        this.stages = stages;
    }
}
