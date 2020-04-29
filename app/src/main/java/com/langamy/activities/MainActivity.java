package com.langamy.activities;

import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.os.Build;
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
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.ConnectionModel;
import com.langamy.fragments.CreateStudySetsFragment;
import com.langamy.fragments.ProfileKotlinFragment;
import com.langamy.fragments.StudySetsKotlinFragment;
import com.langamy.viewmodel.MainViewModel;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.listener.OnViewInflateListener;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Fragment> fragments;

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private ViewPager2 mViewPager;
    private TabLayout tabLayout;
    private RelativeLayout mOfflineRelativeLayout;

    private int[] tabIcons = {
            R.drawable.ic_my_studysets,
            R.drawable.ic_create_studyset,
            R.drawable.ic_account
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_activity);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

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

        mViewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab_layout);
        mOfflineRelativeLayout = findViewById(R.id.offline_mode_RL);
        ImageButton infoBtn = findViewById(R.id.offline_mode_IB);

        //Fragment inizialization

        fragments = new ArrayList<>();

        fragments.add(new StudySetsKotlinFragment());
        fragments.add(new CreateStudySetsFragment());
        fragments.add(new ProfileKotlinFragment());

        mViewPager.setAdapter(createAdapter());
        mViewPager.setOffscreenPageLimit(2);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                invalidateFragmentMenus(position);
            }
        });

        new TabLayoutMediator(tabLayout, mViewPager,
                (tab, position) -> {
                    tab.setIcon(tabIcons[position]);
                    if (position == 0) {
                        Objects.requireNonNull(tab.getIcon()).setColorFilter(getResources().getColor(R.color.lightDark), PorterDuff.Mode.SRC_IN);
                    }
                }).attach();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(new BlendModeColorFilter(getColor(R.color.lightDark), BlendMode.SRC_ATOP));
                } else {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(getColor(R.color.lightDark), PorterDuff.Mode.SRC_ATOP);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(new BlendModeColorFilter(getColor(R.color.white), BlendMode.SRC_ATOP));
                } else {
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
                Objects.requireNonNull(tab.getIcon()).setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
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
                    public void onViewInflated(@NotNull View view) {
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

    private ViewPagerAdapter createAdapter() {
        return new ViewPagerAdapter(this);
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {

        ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
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
