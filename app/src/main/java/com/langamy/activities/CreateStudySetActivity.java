package com.langamy.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bignerdranch.android.main.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.NetworkMonitor;
import com.langamy.base.classes.StudySet;
import com.langamy.base.classes.TranslationResponse;
import com.langamy.base.classes.Word;
import com.langamy.fragments.CreateStudySetsFragment;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.listener.OnViewInflateListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.langamy.base.classes.BaseVariables.showKeyboard;

public class CreateStudySetActivity extends AppCompatActivity {

    private StudySet mStudySet;

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private String languageToTranslate = "ru";
    private String languageFromTranslate = "en";
    private Boolean autoTranslate = false, sendEditRequest = false;
    private int studySetId = 0;

    private EditText mResultEt, mTitleEt;
    private RelativeLayout offlineRL;
    private TextToSpeech tts;
    private Button mScanDocumentBtn, mCommitWordsBtn;
    private FloatingActionButton mAddWordBtn;
    private LinearLayout mWordsLinearLayout, mResultCardView;
    private Switch autoTranslateSwitch;
    private ProgressBar progressBar;
    ScrollView wordScrollView;
    private LayoutInflater wordsInflater;
    private HashMap<String, ArrayList<String>> wordsForSuggestions;
    private BroadcastReceiver broadcastReceiver;
    private ImageButton infoBtn;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String[] cameraPermission;
    String[] storagePermission;

    Uri image_uri;

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter("com.langamy.fragments"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_create_study_set);

        try {
            StudySet studySet = (StudySet) getIntent().getSerializableExtra(BaseVariables.STUDY_SET_MESSAGE);
            mStudySet = studySet;
            studySetId = studySet.getId();

        } catch (NullPointerException e) {
            mStudySet = null;
        }

        //camera permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressBar = findViewById(R.id.progressBar);
        mResultEt = findViewById(R.id.resultEt);
        mTitleEt = findViewById(R.id.title_edittext);
        mScanDocumentBtn = findViewById(R.id.scan_document_btn);
        mAddWordBtn = findViewById(R.id.add_word_btn);
        mCommitWordsBtn = findViewById(R.id.commit_words_btn);
        mResultCardView = findViewById(R.id.result_LL);
        mWordsLinearLayout = findViewById(R.id.main_linearlayout);
        autoTranslateSwitch = findViewById(R.id.auto_translate_switch);
        wordScrollView = findViewById(R.id.word_scrollview);
        offlineRL = findViewById(R.id.offline_mode_RL);
        infoBtn = findViewById(R.id.offline_mode_IB);

        wordsInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        progressBar.setVisibility(View.GONE);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BaseVariables.checkNetworkConnection(CreateStudySetActivity.this)) {
                    disableOfflineMode();
                } else {
                    enableOfflineMode();
                }
            }
        };

        //Inflater for adding words
        final LayoutInflater wordsInflater = LayoutInflater.from(this);

        if (BaseVariables.checkNetworkConnection(this)) {
            disableOfflineMode();
        } else {
            enableOfflineMode();
        }

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playOfflineHelp();
            }
        });

        // адаптер
        final BaseVariables baseVariables = new BaseVariables();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, baseVariables.getLANGUAGES());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner languageFromSpinner = findViewById(R.id.language_form_spinner);
        final Spinner languageToSpinner = findViewById(R.id.language_to_spinner);

        languageFromSpinner.setAdapter(adapter);
        languageToSpinner.setAdapter(adapter);

        // заголовок
        languageFromSpinner.setPrompt("Language from");
        languageToSpinner.setPrompt("Language to");

        //Edit StudySet
        if (this.mStudySet != null) {

            sendEditRequest = true;

            mTitleEt.setText(mStudySet.getName());
            int indexOfLanguageFrom = baseVariables.getLANGUAGES_SHORT().indexOf(mStudySet.getLanguage_from());
            int indexOfLanguageTo = baseVariables.getLANGUAGES_SHORT().indexOf(mStudySet.getLanguage_to());
            languageFromSpinner.setSelection(indexOfLanguageFrom);
            languageToSpinner.setSelection(indexOfLanguageTo);

            ArrayList<Word> mWordList = new ArrayList<>();

            String words = mStudySet.getWords();

            try {
                JSONArray jsonArray = new JSONArray(words);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Word word = new Word(jsonObject.getString("term"),
                            jsonObject.getString("translation"));
                    mWordList.add(word);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < mWordList.size(); i++) {
                View wordView =createListWordItem();

                final EditText term = wordView.findViewById(R.id.term_TV);
                final EditText translation = wordView.findViewById(R.id.translation_TV);

                term.setText(mWordList.get(i).getTerm());
                translation.setText(mWordList.get(i).getTranslation());

                mWordsLinearLayout.addView(wordView);
            }
            mTitleEt.requestFocus();

            //Create StudySet
        } else {

            // выделяем элемент
            languageFromSpinner.setSelection(0);
            languageToSpinner.setSelection(1);

            for (int i = 1; i <= 2; i++) {
                mWordsLinearLayout.addView(createListWordItem(), mWordsLinearLayout.getChildCount());
            }

            mTitleEt.post(new Runnable() {
                @Override
                public void run() {
                    mTitleEt.requestFocus();
                    InputMethodManager imm = (InputMethodManager) mTitleEt.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mTitleEt, InputMethodManager.SHOW_IMPLICIT);
                }
            });
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
                if (!BaseVariables.checkNetworkConnection(CreateStudySetActivity.this)) {
                    Toast.makeText(CreateStudySetActivity.this, getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show();
                    return;
                }
                showImageImportDialog();
                mResultCardView.setVisibility(View.VISIBLE);

            }
        });

        mCommitWordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mResultEt.getText().toString().length() != 0) {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    progressBar.setVisibility(View.VISIBLE);
                    manyTranslate(mResultEt.getText().toString());
                } else {
                    Toast.makeText(CreateStudySetActivity.this, "Result is empty", Toast.LENGTH_SHORT).show();
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

    }

    private void disableOfflineMode() {
        offlineRL.setVisibility(View.GONE);
    }

    private void enableOfflineMode() {
        offlineRL.setVisibility(View.VISIBLE);
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
                    Toast.makeText(CreateStudySetActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] translations = response.body().getTranslation().toString().split(";");
                Log.d("RESPONSE", response.body().getTranslation().toString());

                Log.d("Translations Length", String.valueOf(translations.length));
                Log.d("Term Length", String.valueOf(terms.length));


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

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<TranslationResponse> call, Throwable t) {
                Toast.makeText(CreateStudySetActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playOfflineHelp(){

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


    public View createListWordItem() {

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
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateStudySetActivity.this, R.layout.support_simple_spinner_dropdown_item,
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_study_set, menu);
        return true;
    }

    //handle actionbar item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.submitWords) {

            if (!BaseVariables.checkNetworkConnection(this)) {
                Toast.makeText(CreateStudySetActivity.this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (mTitleEt.getText().length() == 0) {
                Toast.makeText(this, "Title is empty", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Study set must include 4 or more words", Toast.LENGTH_SHORT).show();
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
        return super.onOptionsItemSelected(item);
    }

    private void createStudySet(String name, JSONArray wordList) {
        if (wordList.length() < 3) {
            return;
        }
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        StudySet studySet = new StudySet(acct.getEmail(), name, wordList.toString(), languageToTranslate, languageFromTranslate, wordList.length());

        Call<StudySet> call = mLangamyAPI.createStudySet(studySet);

        call.enqueue(new Callback<StudySet>() {
            @Override
            public void onResponse(Call<StudySet> call, Response<StudySet> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CreateStudySetActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(CreateStudySetActivity.this, SpecificStudySetActivity.class);
                intent.putExtra(BaseVariables.STUDY_SET_ID_MESSAGE, response.body().getId());
                startActivity(intent);
                finish();

            }

            @Override
            public void onFailure(Call<StudySet> call, Throwable t) {
                Toast.makeText(CreateStudySetActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStudySet(final int id, JSONArray wordList) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        StudySet studySet = new StudySet(acct.getEmail(), mTitleEt.getText().toString(), wordList.toString(),
                languageToTranslate, languageFromTranslate, wordList.length());
        Call<StudySet> call = mLangamyAPI.patchStudySet(id, studySet);

        call.enqueue(new Callback<StudySet>() {
            @Override
            public void onResponse(Call<StudySet> call, Response<StudySet> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CreateStudySetActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(CreateStudySetActivity.this, SpecificStudySetActivity.class);
                intent.putExtra(BaseVariables.STUDY_SET_ID_MESSAGE, id);
                startActivity(intent);
                finish();

            }

            @Override
            public void onFailure(Call<StudySet> call, Throwable t) {
                Toast.makeText(CreateStudySetActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
                return;
            }
        });

        NetworkMonitor networkMonitor = new NetworkMonitor();
        networkMonitor.syncDb(this);
    }

    private void showImageImportDialog() {
        //items to display in dialog
        String[] items = {" Camera", " Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(Objects.requireNonNull(this));
        //set title
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0) {
                    //camera option clicked
                    if (!checkCameraPermission()) {
                        //camera permission not allowed, request it
                        requestCameraPersmission();
                        if (checkCameraPermission()) {
                            pickCamera();
                        }
                    } else {
                        //permission allowed, take picture
                        pickCamera();
                    }
                    //for OS marshmallow and above we need to ask runtime permission
                    //for camera and storage
                }
                if (which == 1) {
                    //gallery option clicked
                    if (!checkStoragePermission()) {
                        //Storage permission not allowed, request it
                        requestStoragePersmission();
                        if (checkStoragePermission()) {
                            pickGallery();
                        }
                    } else {
                        //permission allowed, take picture
                        pickGallery();
                    }
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
        image_uri = Objects.requireNonNull(this).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePersmission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(this), storagePermission, STORAGE_REQUEST_CODE);

    }

    private boolean checkStoragePermission() {

        return ContextCompat.checkSelfPermission(Objects.requireNonNull(this),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPersmission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(this), cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(Objects.requireNonNull(this),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
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
                        .start(Objects.requireNonNull(this));//enable image guidlines


            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //got image from camera now crop it
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Objects.requireNonNull(this));//enable image guidlines
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
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
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
                                mResultEt.append(words.toString());
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CreateStudySetActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //if there is ane error show if
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }

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
                    Toast.makeText(CreateStudySetActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }
                translationSupport.setVisibility(View.VISIBLE);
                translationSupport.setText(response.body().getTranslation());
            }

            @Override
            public void onFailure(Call<TranslationResponse> call, Throwable t) {
                Toast.makeText(CreateStudySetActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
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
                        new InputStreamReader(getAssets().open("english_words/" + fileName)));

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

}
