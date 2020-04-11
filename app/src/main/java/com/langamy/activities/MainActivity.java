package com.langamy.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bignerdranch.android.main.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.ConnectionModel;
import com.langamy.base.classes.NetworkMonitor;
import com.langamy.database.StudySetsBaseHelper;
import com.langamy.fragments.CreateStudySetsFragment;
import com.langamy.fragments.ProfileFragment;
import com.langamy.fragments.ProfileKotlinFragment;
import com.langamy.fragments.StudySetsKotlinFragment;
import com.langamy.retrofit.LangamyAPI;
import com.langamy.viewmodel.MainViewModel;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.ArrayList;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.listener.OnViewInflateListener;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    //    Fragments
    ProfileFragment mProfileFragment;
    StudySetsKotlinFragment mStudySetsFragment;
    CreateStudySetsFragment createStudySetsFragment;

    private ArrayList<Fragment> fragments;
    private SQLiteDatabase mDatabase;

    private BroadcastReceiver broadcastReceiver;
    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private GoogleSignInAccount acct;
    private ViewPager2 mViewPager;
    private TabLayout tabLayout;
    private RelativeLayout mOfflineRelativeLayout;
    private ImageButton infoBtn;

    private int[] tabIcons = {
            R.drawable.ic_my_studysets,
            R.drawable.ic_create_studyset,
            R.drawable.ic_account
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sf = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sf.edit();
        try {
            sf.getBoolean(BaseVariables.HELP_CREATE_STUDYSETS_FRAGMENT, true);
        } catch (Exception e) {
            ed.putBoolean(BaseVariables.HELP_CREATE_STUDYSETS_FRAGMENT, true);
            ed.commit();
        }

        acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

        if (acct == null) {
            Intent intent = new Intent(this, GoogleSignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getConnectionLiveData().observe(this, new Observer<ConnectionModel>() {
            @Override
            public void onChanged(ConnectionModel connectionModel) {
                if(connectionModel.getIsConnected()){
                    disableOfflineMode();
                }else{
                    enableOfflineMode();
                }
            }
        });

        setContentView(R.layout.start_activity);

        mViewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab_layout);
        mOfflineRelativeLayout = findViewById(R.id.offline_mode_RL);
        infoBtn = findViewById(R.id.offline_mode_IB);

        //Fragment inizialization
        mProfileFragment = new ProfileFragment();
        mStudySetsFragment = new StudySetsKotlinFragment();
        createStudySetsFragment = new CreateStudySetsFragment();

        fragments = new ArrayList<>();

        fragments.add(mStudySetsFragment);
        fragments.add(createStudySetsFragment);
        fragments.add(new ProfileKotlinFragment());

        mViewPager.setAdapter(createCardAdapter());
        mViewPager.setOffscreenPageLimit(2);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                invalidateFragmentMenus(position);
            }
        });

        mDatabase = new StudySetsBaseHelper(this)
                .getWritableDatabase();

        new TabLayoutMediator(tabLayout, mViewPager,
                (tab, position) -> {
                    tab.setIcon(tabIcons[position]);
                    if (position == 0) {
                        tab.getIcon().setColorFilter(getResources().getColor(R.color.lightDark), PorterDuff.Mode.SRC_IN);
                    }
                }).attach();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.lightDark), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        KeyboardVisibilityEvent.setEventListener(
                this,
                isOpen -> {
                    if (isOpen) {
                        tabLayout.setVisibility(View.GONE);
                    } else {
                        tabLayout.setVisibility(View.VISIBLE);
                    }
                });

        broadcastReceiver = new NetworkMonitor();
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playOfflineHelp();
            }
        });
    }

    private void playOfflineHelp() {

        FancyShowCaseQueue fq = new FancyShowCaseQueue();

        FancyShowCaseView offlineHelp = new FancyShowCaseView.Builder(this)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.abilities_in_offline_mode), fq);
                    }
                })
                .backgroundColor(getColor(R.color.blueForFancy))
                .build();
        fq.add(offlineHelp);
        fq.show();
    }

    private void enableOfflineMode() {
        mOfflineRelativeLayout.setVisibility(View.VISIBLE);
    }

    private void disableOfflineMode() {
        mOfflineRelativeLayout.setVisibility(View.GONE);
    }


    public TabLayout getNavigationView() {
        return tabLayout;
    }

    public ViewPager2 getViewPager() {
        return mViewPager;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, imageData);
        }

    }

    private ViewPagerAdapter createCardAdapter() {
        return new ViewPagerAdapter(this);
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }


    }

    private void invalidateFragmentMenus(int position) {

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        for (int i = 0; i < fragments.size(); i++) {
            viewPagerAdapter.createFragment(i).setHasOptionsMenu(i == position);
        }
        invalidateOptionsMenu();

    }

}
