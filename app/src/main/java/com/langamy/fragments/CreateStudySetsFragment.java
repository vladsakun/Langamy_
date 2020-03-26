package com.langamy.fragments;


import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.langamy.activities.CreateStudySetActivity;
import com.langamy.activities.MainActivity;
import com.langamy.activities.SpecificStudySetActivity;
import com.langamy.base.classes.BaseVariables;
import com.langamy.api.LangamyAPI;
import com.bignerdranch.android.main.R;
import com.langamy.base.classes.NetworkMonitor;
import com.langamy.base.classes.StudySet;
import com.langamy.base.classes.TranslationResponse;
import com.langamy.base.classes.Word;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.langamy.database.StudySetsBaseHelper;
import com.langamy.database.StudySetsScheme;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.listener.DismissListener;
import me.toptas.fancyshowcase.listener.OnViewInflateListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.langamy.base.classes.BaseVariables.showKeyboard;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateStudySetsFragment extends Fragment implements RewardedVideoAdListener {

    private StudySet mStudySet;

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private String languageToTranslate = "ru";
    private String languageFromTranslate = "en";
    private Boolean autoTranslate = false, sendEditRequest = false;
    private int studySetId = 0;
    private HashMap<String, ArrayList<String>> wordsForSuggestions;

    private EditText mResultEt, mTitleEt;
    private TextToSpeech tts;
    private Button mScanDocumentBtn, mCommitWordsBtn;
    private FloatingActionButton mAddWordBtn;
    private LinearLayout mWordsLinearLayout, mResultCardView;
    private Switch autoTranslateSwitch;
    private ProgressBar progressBar;
    private ScrollView wordScrollView;
    private SharedPreferences sf;
    private LayoutInflater wordsInflater;
    private Spinner languageFromSpinner, languageToSpinner;
    private SQLiteDatabase mDatabase;
    private RewardedVideoAd mAd;
    private BroadcastReceiver broadcastReceiver;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    private Uri image_uri;

    public CreateStudySetsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Confirm this fragment has menu items.
        setHasOptionsMenu(true);
        mDatabase = new StudySetsBaseHelper(getContext()).getWritableDatabase();
    }

    @Override
    public void onStart() {
        super.onStart();
        getContext().registerReceiver(broadcastReceiver, new IntentFilter("com.langamy.fragments"));

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_study_set, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        mResultEt = view.findViewById(R.id.resultEt);
        mTitleEt = view.findViewById(R.id.title_edittext);
        mScanDocumentBtn = view.findViewById(R.id.scan_document_btn);
        mAddWordBtn = view.findViewById(R.id.add_word_btn);
        mCommitWordsBtn = view.findViewById(R.id.commit_words_btn);
        mWordsLinearLayout = view.findViewById(R.id.main_linearlayout);
        autoTranslateSwitch = view.findViewById(R.id.auto_translate_switch);
        wordScrollView = view.findViewById(R.id.word_scrollview);
        mResultCardView = view.findViewById(R.id.result_LL);
        languageFromSpinner = view.findViewById(R.id.language_form_spinner);
        languageToSpinner = view.findViewById(R.id.language_to_spinner);

        MobileAds.initialize(getContext(), BaseVariables.REWARDED_VIDEO_TEST);
        mAd = MobileAds.getRewardedVideoAdInstance(getContext());
        mAd.setRewardedVideoAdListener(this);

        loadRewardedVideoAd();

        //Inflater for adding words
        wordsInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // адаптер
        final BaseVariables baseVariables = new BaseVariables();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, baseVariables.getLANGUAGES());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        languageFromSpinner.setAdapter(adapter);
        languageToSpinner.setAdapter(adapter);

        // заголовок
        languageFromSpinner.setPrompt("Language from");
        languageToSpinner.setPrompt("Language to");

        //Create StudySet

        // выделяем элемент
        languageFromSpinner.setSelection(0);
        languageToSpinner.setSelection(1);

        for (int i = 1; i <= 2; i++) {
            View listWordView = createListWordItem();
            mWordsLinearLayout.addView(listWordView, mWordsLinearLayout.getChildCount());
        }

        languageFromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                languageFromTranslate = baseVariables.getLANGUAGES_SHORT().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        languageToSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                languageToTranslate = baseVariables.getLANGUAGES_SHORT().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mAddWordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View listWordView = createListWordItem();
                EditText term = listWordView.findViewById(R.id.term_TV);
                mWordsLinearLayout.addView(listWordView, mWordsLinearLayout.getChildCount());
                showKeyboard(term);
            }
        });

        mScanDocumentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BaseVariables.checkNetworkConnection(getContext())) {
                    Toast.makeText(getContext(), getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mAd.isLoaded()) {
                    mAd.show();
                }
            }
        });

        mCommitWordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mResultEt.getText().toString().length() != 0) {
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    progressBar.setVisibility(View.VISIBLE);
                    manyTranslate(mResultEt.getText().toString());
                } else {
                    Toast.makeText(getContext(), "Result is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        autoTranslateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                autoTranslate = b;
            }
        });


        wordsForSuggestions = save();

        return view;
    }

    private void playOfflineHelp() {

        FancyShowCaseQueue fq = new FancyShowCaseQueue();

        FancyShowCaseView offlineHelp = new FancyShowCaseView.Builder(getActivity())
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getContext().getString(R.string.abilities_in_offline_mode), fq);
                    }
                })
                .backgroundColor(getContext().getColor(R.color.blueForFancy))
                .build();
        fq.add(offlineHelp);
        fq.show();

    }

    private void manyTranslate(String words) {
        ArrayList<Word> wordArrayList = new ArrayList<>();
        String stringsToBeTranslated = words.replaceAll("\\r?\\n", ";")
                .replaceAll("\\[", "").replaceAll("]", "");

        String[] terms = stringsToBeTranslated.split(";");

        JSONObject postData = new JSONObject();
        try {
            postData.put("words", stringsToBeTranslated);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Call<TranslationResponse> call = mLangamyAPI.translate(postData, languageFromTranslate, languageToTranslate, "many");

        call.enqueue(new Callback<TranslationResponse>() {
            @Override
            public void onResponse(Call<TranslationResponse> call, Response<TranslationResponse> response) {

                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] translations = response.body().getTranslation().toString().split(";");

                for (int i = 0; i < terms.length; i++) {
                    wordArrayList.add(new Word(terms[i], translations[i]));
                }
                for (int i = 0; i < wordArrayList.size(); i++) {

                    View wordListItem = createListWordItem();

                    EditText term = wordListItem.findViewById(R.id.term_TV);
                    EditText translation = wordListItem.findViewById(R.id.translation_TV);

                    term.setText(wordArrayList.get(i).getTerm());
                    translation.setText(wordArrayList.get(i).getTranslation());

                    mWordsLinearLayout.addView(wordListItem, mWordsLinearLayout.getChildCount());

                }

                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<TranslationResponse> call, Throwable t) {
                Toast.makeText(getContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //actionbar menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_create_study_set, menu);
        inflater.inflate(R.menu.menu_help_item, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle actionbar item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.submitWords) {

            if (!BaseVariables.checkNetworkConnection(getContext())) {
                Toast.makeText(getContext(), getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (mTitleEt.getText().length() == 0) {
                Toast.makeText(getContext(), getString(R.string.title_is_empty), Toast.LENGTH_SHORT).show();
                showKeyboard(mTitleEt);
                return false;
            }

            JSONArray wordList = new JSONArray();
            try {
                for (int i = 0; i < mWordsLinearLayout.getChildCount(); i++) {
                    View layout = mWordsLinearLayout.getChildAt(i);
                    JSONObject currentWord = new JSONObject();
                    EditText translation = layout.findViewById(R.id.translation_TV);
                    EditText term = layout.findViewById(R.id.term_TV);
                    if (!(translation.getText().toString().equals("") && term.getText().toString().equals(""))) {
                        currentWord.put("term", term.getText().toString().trim());
                        currentWord.put("translation", translation.getText().toString().trim());
                        currentWord.put("firstStage", false);
                        currentWord.put("secondStage", false);
                        currentWord.put("thirdStage", false);
                        currentWord.put("forthStage", false);
                        wordList.put(currentWord);
                    }
                }
                if (wordList.length() < 4) {
                    Toast.makeText(getContext(), getString(R.string.min_words_in_studyset), Toast.LENGTH_SHORT).show();
                } else {
                    if (sendEditRequest) {
                        updateStudySet(studySetId, wordList);
                    } else {
                        createStudySet(mTitleEt.getText().toString(), wordList);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (id == R.id.help) {
            playHelp();

        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog() {
        //items to display in dialog
        String[] items = {" Camera", " Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        //set title
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0) {
                    //camera option clicked
                    requestCameraPermission();

                }
                if (which == 1) {
                    //gallery option clicked
                    requestStoragePermission();
                }
            }
        });
        dialog.create().show(); //show dialog
    }

    private void pickGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //set intent type to image
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic"); //title of the picture
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image To text");// description
        image_uri = Objects.requireNonNull(getActivity()).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {

        if (checkStoragePermission()) {

            pickGallery();

        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }

    }

    private void requestCameraPermission() {

        if (checkCameraPermission() && checkStoragePermission()) {

            // has the permission.
            pickCamera();

        } else {

            // request the permission
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);

        }

    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //handle image result
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //got image from gallery now crop it
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Objects.requireNonNull(getActivity()));//enable image guidlines


            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //got image from camera now crop it
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Objects.requireNonNull(getActivity()));//enable image guidlines
            }
        }
        //get cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = Objects.requireNonNull(result).getUri(); //get image uri
                //get drawable bitmap for text recognition
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                        .getOnDeviceTextRecognizer();

                textRecognizer.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText result) {
                                String resultText = result.getText();
                                ArrayList<String> allWords = new ArrayList<>();
                                StringBuilder words = new StringBuilder();
                                for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                                    for (FirebaseVisionText.Line line : block.getLines()) {
                                        String editLine = line.getText();
                                        if (editLine.contains("/")) {
                                            int index = editLine.indexOf("/");
                                            String transcription = editLine.substring(index);
                                            editLine = editLine.replace(transcription, "");
                                            allWords.add(editLine);
                                        } else {
                                            allWords.add(editLine);
                                        }
                                    }
                                }
                                for (String word : allWords) {
                                    words.append(word).append("\n");
                                }
                                //set text to edit text
                                mResultEt.setText(words.toString());
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //if there is ane error show if
                Exception error = result.getError();
                Toast.makeText(getActivity(), "" + error, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void createStudySet(String name, JSONArray wordList) {
        if (wordList.length() < 2) {
            return;
        }

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        StudySet studySet = new StudySet(acct.getEmail(), name, wordList.toString(), languageToTranslate, languageFromTranslate, wordList.length());

        Call<StudySet> call = mLangamyAPI.createStudySet(studySet);

        call.enqueue(new Callback<StudySet>() {
            @Override
            public void onResponse(Call<StudySet> call, Response<StudySet> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getContext(), SpecificStudySetActivity.class);
                intent.putExtra(BaseVariables.STUDY_SET_ID_MESSAGE, response.body().getId());
                startActivity(intent);
                mWordsLinearLayout.removeAllViews();
                mTitleEt.setText("");
                for (int i = 1; i <= 2; i++) {
                    View listWordView = createListWordItem();
                    mWordsLinearLayout.addView(listWordView, mWordsLinearLayout.getChildCount());
                }

            }

            @Override
            public void onFailure(Call<StudySet> call, Throwable t) {
                Toast.makeText(getContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        NetworkMonitor networkMonitor = new NetworkMonitor();
        networkMonitor.syncDb(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        setHasOptionsMenu(isVisible());
        sf = getActivity().getPreferences(MODE_PRIVATE);
        boolean help = sf.getBoolean(BaseVariables.HELP_CREATE_STUDYSETS_FRAGMENT, true);

        if (help) {
            playHelp();
            SharedPreferences sf = getActivity().getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = sf.edit();
            editor.putBoolean(BaseVariables.HELP_CREATE_STUDYSETS_FRAGMENT, false);
            editor.commit();
        }
    }

    private void updateStudySet(final int id, JSONArray wordList) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());

        StudySet studySet = new StudySet(acct.getEmail(), mTitleEt.getText().toString(), wordList.toString(),
                languageToTranslate, languageFromTranslate, wordList.length());

        Call<StudySet> call = mLangamyAPI.patchStudySet(id, studySet);

        call.enqueue(new Callback<StudySet>() {
            @Override
            public void onResponse(Call<StudySet> call, Response<StudySet> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getContext(), SpecificStudySetActivity.class);
                intent.putExtra(BaseVariables.STUDY_SET_ID_MESSAGE, id);
                startActivity(intent);
                getActivity().finish();

            }

            @Override
            public void onFailure(Call<StudySet> call, Throwable t) {
                Toast.makeText(getContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Translate(String stringToTranslate, final TextView translationSupport) {

        JSONObject postData = new JSONObject();
        try {
            postData.put("words", stringToTranslate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Call<TranslationResponse> call = mLangamyAPI.translate(postData, languageFromTranslate, languageToTranslate, "one");

        call.enqueue(new Callback<TranslationResponse>() {
            @Override
            public void onResponse(Call<TranslationResponse> call, Response<TranslationResponse> response) {

                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }
                translationSupport.setVisibility(View.VISIBLE);
                translationSupport.setText(response.body().getTranslation());
            }

            @Override
            public void onFailure(Call<TranslationResponse> call, Throwable t) {
                Log.d("Translate_Failure", t.toString());
            }
        });
    }

    private View createListWordItem() {

        View wordView = wordsInflater.inflate(R.layout.list_words_item, null);

        final AutoCompleteTextView term = wordView.findViewById(R.id.term_TV);
        final TextView translationSupport = wordView.findViewById(R.id.translation_support);
        final EditText translation = wordView.findViewById(R.id.translation_TV);
        final ImageButton removeBtn = wordView.findViewById(R.id.remove_list_word_item_btn);

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout parent = (LinearLayout) removeBtn.getParent().getParent().getParent();
                parent.removeView((View) removeBtn.getParent().getParent());
            }
        });

        term.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view1, boolean hasFocus) {
                if (hasFocus) {
                    wordScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            CardView parent = (CardView) translation.getParent().getParent();
                            LinearLayout mainLinearLayout = (LinearLayout) parent.getParent();
                            RelativeLayout mainRelativeLayout = (RelativeLayout) mainLinearLayout.getParent();
                            LinearLayout linearLayout = mainRelativeLayout.findViewById(R.id.result_LL);
                            wordScrollView.scrollTo(0, parent.getBottom() + linearLayout.getHeight());
                        }
                    });
                }
            }
        });
        term.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() == 1) {

                    char firstChar = Character.toLowerCase(s.charAt(0));
                    if ((firstChar >= 'a' && firstChar <= 'z') || (firstChar >= 'A' && firstChar <= 'Z')) {

                        ArrayList<String> words = wordsForSuggestions.get(firstChar + ".txt");
                        String wordsStringArr[] = words.toArray(new String[words.size()]);

                        // Создаем адаптер для автозаполнения элемента AutoCompleteTextView
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item,
                                wordsStringArr);

                        term.setAdapter(adapter);
                    }

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        translation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view1, boolean hasFocus) {
                if (hasFocus) {

                    Translate(term.getText().toString().trim(), translationSupport);

                    wordScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            CardView parent = (CardView) translation.getParent().getParent();
                            LinearLayout mainLinearLayout = (LinearLayout) parent.getParent();
                            RelativeLayout mainRelativeLayout = (RelativeLayout) mainLinearLayout.getParent();
                            LinearLayout linearLayout = mainRelativeLayout.findViewById(R.id.result_LL);
                            wordScrollView.scrollTo(0, parent.getBottom() + linearLayout.getHeight());
                        }
                    });
                }
            }
        });
        translationSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translation.setText(translationSupport.getText().toString());
            }
        });

        return wordView;
    }

    private void playHelp() {

        BaseVariables.hideKeyboard(getActivity());


        FancyShowCaseQueue queue = new FancyShowCaseQueue();

        FancyShowCaseView titleFocus = new FancyShowCaseView.Builder(getActivity())
                .focusOn(mTitleEt)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_title), queue);
                    }
                })
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .backgroundColor(Color.parseColor("#E621618C"))
                .build();

        FancyShowCaseView fromLang = new FancyShowCaseView.Builder(getActivity())
                .focusOn(languageFromSpinner)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_from_lang), queue);
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build();

        FancyShowCaseView toLang = new FancyShowCaseView.Builder(getActivity())
                .focusOn(languageToSpinner)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_to_lang), queue);
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build();

        FancyShowCaseView scan = new FancyShowCaseView.Builder(getActivity())
                .focusOn(mScanDocumentBtn)
                .customView(R.layout.custom_layout_for_fancyshowcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_scan_btn), queue);
                    }
                })
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .backgroundColor(Color.parseColor("#E621618C"))
                .build();

        queue.add(titleFocus);
        queue.add(fromLang);
        queue.add(toLang);
        queue.add(scan);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                queue.show();
            }
        }, 200);
    }

    private HashMap<String, ArrayList<String>> save() {

        HashMap<String, ArrayList<String>> all_words = new HashMap<>();

        String[] fileNames = {
                "a.txt", "b.txt", "c.txt", "d.txt", "e.txt", "f.txt", "g.txt", "h.txt",
                "i.txt", "j.txt", "k.txt", "l.txt", "m.txt", "n.txt",
                "o.txt", "p.txt", "q.txt", "r.txt", "s.txt", "t.txt",
                "u.txt", "v.txt", "w.txt", "x.txt", "y.txt", "z.txt"};

        BufferedReader reader = null;
        for (String fileName : fileNames) {

            try {
                reader = new BufferedReader(
                        new InputStreamReader(getContext().getAssets().open("english_words/" + fileName)));

                // do reading, usually loop until end of file reading
                String mLine;
                ArrayList<String> words = new ArrayList<>();
                while ((mLine = reader.readLine()) != null) {
                    words.add(mLine);
                }
                all_words.put(fileName, words);
            } catch (IOException e) {
                //log the exception
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return all_words;
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Log.d("VIDEO", "An ad has loaded");
        mScanDocumentBtn.setEnabled(true);

    }

    @Override
    public void onRewardedVideoAdOpened() {
        Log.d("VIDEO", "An ad has opened");

    }

    @Override
    public void onRewardedVideoStarted() {
        Log.d("VIDEO", "An ad has started");

    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.d("VIDEO", "An ad has closed");
        loadRewardedVideoAd();

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        showImageImportDialog();
        mResultCardView.setVisibility(View.VISIBLE);
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.d("VIDEO", "An ad has caused focus to leave");

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Log.d("VIDEO", "An ad has failed to load");

    }

    @Override
    public void onRewardedVideoCompleted() {
        Log.d("VIDEO", "An ad has completed");

    }

    private void loadRewardedVideoAd() {
        mAd.loadAd(BaseVariables.REWARDED_VIDEO_TEST, new AdRequest.Builder().build());
    }
}

