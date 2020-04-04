package com.langamy.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.main.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.langamy.adapters.DoneDictationsAdapter;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Dictation;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UserDoneDictationsActivity extends AppCompatActivity {

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ProgressBar progressBar;
    private TextView recentDictation, noRecentDictations;

    private ArrayList<Dictation> mDictations = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_done_dictations);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.done_dictations_recyclerview);
        progressBar = findViewById(R.id.progressBar);
        recentDictation = findViewById(R.id.recent_dictations_TV);
        noRecentDictations = findViewById(R.id.no_recent_dictations_TV);

        adapter = new DoneDictationsAdapter(mDictations, this);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void noRecentDictations() {
        recentDictation.setVisibility(View.GONE);
        noRecentDictations.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDoneDictations();
    }

    private void getDoneDictations() {

        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        Call<ArrayList<Dictation>> call = mLangamyAPI.getUserCompletedDictations(acc.getEmail());
        call.enqueue(new Callback<ArrayList<Dictation>>() {
            @Override
            public void onResponse(Call<ArrayList<Dictation>> call, Response<ArrayList<Dictation>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(UserDoneDictationsActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }
                mDictations.clear();
                mDictations.addAll(response.body());
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if (response.body().size() == 0) {
                    noRecentDictations();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Dictation>> call, Throwable t) {
                Toast.makeText(UserDoneDictationsActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
