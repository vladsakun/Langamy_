package com.langamy.fragments;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bignerdranch.android.main.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.langamy.activities.MainActivity;
import com.langamy.activities.MyDictationsActivity;
import com.langamy.activities.SpecificDictationActivity;
import com.langamy.activities.SpecificStudySetActivity;
import com.langamy.activities.UserDoneDictationsActivity;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.ConnectionModel;
import com.langamy.base.classes.NetworkMonitor;
import com.langamy.base.classes.StudySet;
import com.langamy.database.StudySetCursorWrapper;
import com.langamy.database.StudySetsBaseHelper;
import com.langamy.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.listener.DismissListener;
import me.toptas.fancyshowcase.listener.OnViewInflateListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;


public class StudySetsFragment extends Fragment {

    public Retrofit retrofit = new BaseVariables().retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private SQLiteDatabase mDatabase;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<StudySet> mStudySetsNamesList;
    private ArrayList<StudySet> mLocaleStudySets = new ArrayList<>();
    private GoogleSignInAccount acct;
    private StudySetAdapter mAdapter;
    private MaterialButton searchDictationBtn, createStudySetBtn;
    private EditText searchDictationET;
    private ProgressBar mProgressBar;
    private RelativeLayout searchDictationRl;
    private AdView adView;
    private ImageButton mDictations, mRandomDictation, mRecentDictations;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Confirm this fragment has menu items.
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getStudySetsNames();

        setHasOptionsMenu(isVisible());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_study_sets, container, false);
        acct = GoogleSignIn.getLastSignedInAccount(getContext());

        adView = (AdView) view.findViewById(R.id.banner_ad);
        recyclerView = view.findViewById(R.id.recycler_view);
        searchDictationBtn = view.findViewById(R.id.search_dictation_btn);
        searchDictationET = view.findViewById(R.id.search_dictation_ET);
        mProgressBar = view.findViewById(R.id.progressBar);
        searchDictationRl = view.findViewById(R.id.searchDictation);
        createStudySetBtn = view.findViewById(R.id.create_study_set_BTN);
        mDictations = view.findViewById(R.id.dictations);
        mRecentDictations = view.findViewById(R.id.recent_dictations);
        mRandomDictation = view.findViewById(R.id.randomDictation);

        mStudySetsNamesList = new ArrayList<>();

        mAdapter = new StudySetAdapter(mStudySetsNamesList);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        searchDictationBtn.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), SpecificDictationActivity.class);
            try {
                int code = Integer.parseInt(searchDictationET.getText().toString().trim());
                intent.putExtra(BaseVariables.DICTATION_CODE_MESSAGE, code);
                startActivity(intent);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Code must be numeric", Toast.LENGTH_SHORT).show();
            }
        });

        createStudySetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                ViewPager2 viewPager2 = mainActivity.getViewPager();
                viewPager2.setCurrentItem(1);
            }
        });

        mRecentDictations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BaseVariables.checkNetworkConnection(getContext())) {
                    Toast.makeText(getContext(), getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), UserDoneDictationsActivity.class);
                    startActivity(intent);
                }
            }
        });
        mDictations.setOnClickListener(v -> {
            if (!BaseVariables.checkNetworkConnection(getContext())) {
                Toast.makeText(getContext(), getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), MyDictationsActivity.class);
                startActivity(intent);
            }
        });
        mRandomDictation.setOnClickListener(v -> {
            randomDictation();
        });

        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getConnectionLiveData().observe(getActivity(), new Observer<ConnectionModel>() {
            @Override
            public void onChanged(ConnectionModel connectionModel) {
                if (connectionModel.getIsConnected()) {
                    AdRequest adRequest = new AdRequest.Builder()
                            .build();

                    adView.loadAd(adRequest);
                }
            }
        });

        return view;
    }

    private void noStudySets() {
        createStudySetBtn.setVisibility(View.VISIBLE);
    }

    public void disableOfflineMode() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        adView.loadAd(adRequest);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_study_sets_fragment, menu);
        inflater.inflate(R.menu.menu_help_item, menu);

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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.help) {
            playHelp();
        }

        return super.onOptionsItemSelected(item);
    }

    private void randomDictation() {

        if (BaseVariables.checkNetworkConnection(getContext())) {
            Intent intent = new Intent(getActivity(), SpecificDictationActivity.class);
            intent.putExtra(BaseVariables.RANDOM_DICTATION_MESSAGE, true);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show();
        }
    }

    private void getStudySetsNames() {

        if (BaseVariables.checkNetworkConnection(getContext())) {

            Call<List<StudySet>> call = mLangamyAPI.getStudySetsNamesOfCurrentUser(acct.getEmail());

            call.enqueue(new Callback<List<StudySet>>() {
                @Override
                public void onResponse(Call<List<StudySet>> call, Response<List<StudySet>> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(getContext(), String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mStudySetsNamesList.clear();
                    mStudySetsNamesList.addAll(response.body());
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE);

                    SharedPreferences sf = getActivity().getPreferences(MODE_PRIVATE);
                    boolean help = sf.getBoolean(BaseVariables.HELP_STUDY_STUDYSETS_FRAGMENT, true);

                    if (help) {
                        playHelp();
                        SharedPreferences.Editor editor = sf.edit();
                        editor.putBoolean(BaseVariables.HELP_STUDY_STUDYSETS_FRAGMENT, false);
                        editor.commit();
                    }

                    if (response.body().size() == 0) {
                        noStudySets();
                    }

                }

                @Override
                public void onFailure(Call<List<StudySet>> call, Throwable t) {
                    Log.d("SETS_FRAGMENT_FAILURE", t.toString());
                }
            });
        } else {
            readDataFromLocaleStorage(acct.getEmail());
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private class StudySetHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private StudySet mStudySet;

        private TextView title, amountOfWords;
        private ImageButton deleteStudySet_BTN;

        public StudySetHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.study_set_item, parent, false));
            itemView.setOnClickListener(this);

            title = itemView.findViewById(R.id.studyset_title);
            amountOfWords = itemView.findViewById(R.id.amount_of_words);
            deleteStudySet_BTN = itemView.findViewById(R.id.delete_studyset_BTN);

        }

        public void bind(final StudySet studySet) {
            mStudySet = studySet;
            title.setText(studySet.getName());
            String amount = getResources().getString(R.string.amount);
            amountOfWords.setText(amount + " " + studySet.getAmount_of_words());

            deleteStudySet_BTN.setVisibility(View.VISIBLE);
            deleteStudySet_BTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    RecyclerView parent = (RecyclerView) deleteStudySet_BTN.getParent().getParent().getParent().getParent();
                    MaterialCardView cardViewParent = (MaterialCardView) deleteStudySet_BTN.getParent().getParent();
                    AlertDialog diaBox = AskOption(studySet.getId(), mAdapter, parent, cardViewParent, studySet);
                    diaBox.show();

                }
            });

        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getContext(), SpecificStudySetActivity.class);
            intent.putExtra(BaseVariables.STUDY_SET_ID_MESSAGE, mStudySet.getId());
            startActivity(intent);
        }
    }

    private class StudySetAdapter extends RecyclerView.Adapter<StudySetHolder> implements Filterable {

        private List<StudySet> mStudySets;
        private List<StudySet> studySetsListFull;

        StudySetAdapter(List<StudySet> studySets) {
            this.mStudySets = studySets;
            this.studySetsListFull = studySets;
        }

        @Override
        public StudySetHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new StudySetHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(StudySetHolder holder, int position) {
            StudySet studySetsNames = studySetsListFull.get(position);
            holder.bind(studySetsNames);
        }

        @Override
        public int getItemCount() {
            return studySetsListFull.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        studySetsListFull = mStudySets;
                    } else {
                        List<StudySet> filteredList = new ArrayList<>();
                        for (StudySet row : mStudySets) {

                            // name match condition. this might differ depending on your requirement
                            // here we are looking for name or phone number match
                            if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }

                        studySetsListFull = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = studySetsListFull;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    studySetsListFull = (ArrayList<StudySet>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

    private void deleteStudySet(int id) {

        Call<Void> call = mLangamyAPI.deleteStudySet(id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        NetworkMonitor networkMonitor = new NetworkMonitor();
        networkMonitor.syncDb(getContext());
    }

    private AlertDialog AskOption(final int id, RecyclerView.Adapter adapter, final RecyclerView parent,
                                  final MaterialCardView cardViewParent, final StudySet studySet) {

        LayoutInflater layoutInflater = getLayoutInflater();
        View alertLayout = layoutInflater.inflate(R.layout.custom_alert_dialog, null);
        Button delete_BTN = alertLayout.findViewById(R.id.delete_item_BTN);
        Button cancel_BTN = alertLayout.findViewById(R.id.cancel_action);
        TextView studySetName_TV = alertLayout.findViewById(R.id.name_of_deleting_item_TV);
        TextView alertMessage_TV = alertLayout.findViewById(R.id.alert_message_TV);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

        alertDialog.setCancelable(true);
        alertDialog.setView(alertLayout);

        final AlertDialog dialog = alertDialog.create();

        if (!BaseVariables.checkNetworkConnection(getContext())) {
            studySetName_TV.setText("No internet connection");
            cancel_BTN.setVisibility(View.GONE);
            delete_BTN.setVisibility(View.GONE);
            alertMessage_TV.setVisibility(View.GONE);
            return dialog;
        }

        studySetName_TV.setText(studySet.getName());

        cancel_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        delete_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteStudySet(id);
                parent.removeView(cardViewParent);
                mStudySetsNamesList.remove(studySet);
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public void readDataFromLocaleStorage(String creator) {
        mDatabase = new StudySetsBaseHelper(getContext()).getReadableDatabase();
        mLocaleStudySets.clear();

        StudySetCursorWrapper cursor = BaseVariables.queryStudySets("creator=?", new String[]{creator}, mDatabase);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                mLocaleStudySets.add(cursor.getStudySet());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        mStudySetsNamesList.clear();
        mStudySetsNamesList.addAll(mLocaleStudySets);
        mAdapter.notifyDataSetChanged();

    }

    private void playHelp() {

        BaseVariables.hideKeyboard(getActivity());

        FancyShowCaseQueue fq = new FancyShowCaseQueue();

        FancyShowCaseView helpBtn = new FancyShowCaseView.Builder(getActivity())
                .customView(R.layout.fancyshowcase_with_image, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setImage(view, getString(R.string.fancy_help_btn),
                                fq, getContext().getDrawable(R.drawable.ic_help_white_30dp));
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build();

        FancyShowCaseView myDictations = new FancyShowCaseView.Builder(getActivity())
                .focusOn(mDictations)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view,
                                getString(R.string.fancy_my_dictations),
                                fq);
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build();

        FancyShowCaseView randomDictations = new FancyShowCaseView.Builder(getActivity())
                .focusOn(mRandomDictation)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view,
                                getString(R.string.fancy_random_dictations),
                                fq);
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build();

        FancyShowCaseView doneDictations = new FancyShowCaseView.Builder(getActivity())
                .focusOn(mRecentDictations)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view,
                                getString(R.string.fancy_done_dictations),
                                fq);
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build();


        MainActivity activity = (MainActivity) getActivity();
        TabLayout navigation = activity.getNavigationView();
        TabLayout.Tab tabAt = navigation.getTabAt(1);
        View tabView = tabAt.view;

        ViewPager2 viewPager = activity.getViewPager();

        FancyShowCaseView createStudySet = new FancyShowCaseView.Builder(getActivity())
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_create_studyset), fq);
                        view.findViewById(R.id.skip).setVisibility(View.GONE);
                    }
                })
                .focusOn(tabView)
                .backgroundColor(Color.parseColor("#E621618C"))
                .dismissListener(new DismissListener() {
                    @Override
                    public void onDismiss(String id) {
                        viewPager.setCurrentItem(1);

                    }

                    @Override
                    public void onSkipped(String id) {
                    }
                })
                .build();

        fq.add(helpBtn)
                .add(myDictations)
                .add(randomDictations)
                .add(doneDictations)
                .add(createStudySet);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fq.show();
            }
        }, 200);

    }
}
