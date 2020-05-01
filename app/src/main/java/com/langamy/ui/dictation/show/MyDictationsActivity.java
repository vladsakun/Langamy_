package com.langamy.ui.dictation.show;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.main.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.card.MaterialCardView;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.Dictation;
import com.langamy.base.kotlin.BaseFunctionsKt;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.langamy.base.kotlin.BaseFunctionsKt.includeConnectivityErrorLayout;

public class MyDictationsActivity extends AppCompatActivity {

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);

    private RecyclerView mRecyclerView;
    private DictationsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
    private TextView noDictations;
    private RelativeLayout content;

    private List<Dictation> mDictationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dictations);

        BaseFunctionsKt.updateSupportActionBar(Objects.requireNonNull(getSupportActionBar()), getString(R.string.my_dictations));

        mDictationList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.dictations_RV);
        mProgressBar = findViewById(R.id.progressBar);
        noDictations= findViewById(R.id.no_dictations_TV);
        content = findViewById(R.id.content);

        initializeRecyclerView(mDictationList);

        getDictationsOfCurrentUser();

    }

    private void noDictations(){
        noDictations.setVisibility(View.VISIBLE);
    }

    private void initializeRecyclerView(List<Dictation> dictations) {
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DictationsAdapter(dictations);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dictation, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    private void getDictationsOfCurrentUser() {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        Call<List<Dictation>> call = null;
        if (acct != null) {
            call = mLangamyAPI.getDictationsOfCurrentUser(acct.getEmail());
        }
        if (call != null) {
            call.enqueue(new Callback<List<Dictation>>() {
                @Override
                public void onResponse(@NotNull Call<List<Dictation>> call, @NotNull Response<List<Dictation>> response) {
                    if (!response.isSuccessful()) {
                        includeConnectivityErrorLayout(String.valueOf(response.code()), content, getLayoutInflater(), MyDictationsActivity.this);
                        mProgressBar.setVisibility(View.GONE);
                        return;
                    }
                    mDictationList.clear();
                    if (response.body() != null) {
                        mDictationList.addAll(response.body());
                    }
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE);
                    assert response.body() != null;
                    if(response.body().size() == 0){
                        noDictations();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<List<Dictation>> call, @NotNull Throwable t) {
                    includeConnectivityErrorLayout("failure", content, getLayoutInflater(), MyDictationsActivity.this);
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void deleteDictation(int id) {

        Call<Void> call = mLangamyAPI.deleteDictation(id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MyDictationsActivity.this, response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(MyDictationsActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }

        });

    }

    public class DictationsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Dictation mDictation;

        private TextView dictationName, dictationCode;
        private ImageButton deleteDictation_BTN;

        public DictationsHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.my_dictation_item, parent, false));
            itemView.setOnClickListener(this);

            dictationName = itemView.findViewById(R.id.dictation_name_TV);
            deleteDictation_BTN = itemView.findViewById(R.id.delete_dictation_BTN);
        }

        public void bind(final Dictation dictation) {
            mDictation = dictation;
            dictationName.setText(mDictation.getName());

            deleteDictation_BTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecyclerView parent = (RecyclerView) deleteDictation_BTN.getParent().getParent().getParent().getParent();
                    MaterialCardView cardViewParent = (MaterialCardView) deleteDictation_BTN.getParent().getParent();
                    AlertDialog alertDialog = AskOption(dictation.getId(), parent, cardViewParent, dictation);
                    alertDialog.show();
                }
            });

        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MyDictationsActivity.this, SpecificDictationActivity.class);
            intent.putExtra(BaseVariables.DICTATION_ID_MESSAGE, mDictation.getId());
            startActivity(intent);
        }

    }

    public class DictationsAdapter extends RecyclerView.Adapter<DictationsHolder> implements Filterable {

        private List<Dictation> mDictations;
        private List<Dictation> dictationsListFiltered;

        DictationsAdapter(List<Dictation> dictations) {
            this.mDictations = dictations;
            this.dictationsListFiltered = dictations;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        dictationsListFiltered = mDictations;
                    } else {
                        List<Dictation> filteredList = new ArrayList<>();
                        for (Dictation row : mDictations) {

                            // name match condition. this might differ depending on your requirement
                            // here we are looking for name or phone number match
                            if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }

                        dictationsListFiltered = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = dictationsListFiltered;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    dictationsListFiltered = (ArrayList<Dictation>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }

        @NonNull
        @Override
        public DictationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(MyDictationsActivity.this);
            return new DictationsHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull DictationsHolder holder, int position) {
            Dictation dictation = dictationsListFiltered.get(position);
            holder.bind(dictation);
        }

        @Override
        public int getItemCount() {
            return dictationsListFiltered.size();
        }

    }

    public AlertDialog AskOption(final int id, final RecyclerView parent,
                                 final MaterialCardView cardViewParent, final Dictation dictation) {

        LayoutInflater layoutInflater = getLayoutInflater();
        View alertLayout = layoutInflater.inflate(R.layout.custom_alert_dialog, null);
        Button delete_BTN = alertLayout.findViewById(R.id.delete_item_BTN);
        Button cancel_BTN = alertLayout.findViewById(R.id.cancel_action);
        TextView studySetName_TV = alertLayout.findViewById(R.id.name_of_deleting_item_TV);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyDictationsActivity.this);

        alertDialog.setCancelable(true);
        alertDialog.setView(alertLayout);

        final AlertDialog dialog = alertDialog.create();

        studySetName_TV.setText(dictation.getName());

        cancel_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        delete_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDictation(id);
                parent.removeView(cardViewParent);
                mDictationList.remove(dictation);
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        return dialog;

    }
}
