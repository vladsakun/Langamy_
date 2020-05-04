package com.langamy.base.classes;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.main.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class BaseVariables {

    final public static String STUDY_SET_ID_MESSAGE = "study_set_id";
    final public static String STUDY_SET_MESSAGE = "study_set";
    final public static String WORDS_MESSAGE = "words";
    final public static String MARKED_WORDS_MESSAGE = "marked_words";
    final public static String OTHER_WORDS_MESSAGE = "other_words";
    final public static String QUESTION_AMOUNT_MESSAGE = "question_amount";
    final public static String TYPE_OF_QUESTIONS_MESSAGE = "type_of_questions";
    final public static String TITLE_MESSAGE = "title";
    final public static String AMOUNT_OF_CORRECT_ANSWERS_MESSAGE = "amount_of_correct_answers";
    final public static String DICTATION_ID_MESSAGE = "dictation_id";
    final public static String RANDOM_DICTATION_MESSAGE = "random_dictation";
    final public static String DICTATION_CODE_MESSAGE = "dictation_code";
    public static final String DICTATION_TYPE_OF_QUESTIONS_MESSAGE = "type_of_questions";
    public static final String USER_ANSWERS_MESSAGE = "user_answers";
    public static final String FROM_LANG_MESSAGE = "from_lang";
    public static final String TO_LANG_MESSAGE = "to_lang";
    public static final String FRAGMENT_POSITION_MESSAGE = "fragment_position";
    public static final String MARKED_MESSAGE = "marked";
    public static final String AMOUNT_OF_WORDS_MESSAGE = "amount_of_words";
    public static final String DICTATION_MESSAGE = "dictation";

    public static final String TRANSLATION_TERM = "translation_term";

    public static final String HELP_CREATE_STUDYSETS_FRAGMENT = "help_create_studysets_fragment";
    public static final String HELP_STUDY_STUDYSETS_FRAGMENT = "help_studysets_fragment";
    public static final String HELP_SPECIFIC_STUDYSET = "help_specific_studyset";
    public static final String HELP_MAKE_DICTATION = "help_make_dictation";

    public static final String AD_ID = "ca-app-pub-1867610337047797~1889776259";
    public static final String REWARDED_VIDEO_TEST = "ca-app-pub-3940256099942544/5224354917";
    public static final String REWARDED_VIDEO = "ca-app-pub-1867610337047797/8527051382";

    public static final String HOST_URL = "http://vlad12.pythonanywhere.com/";
    public static final String LOCAL_URL = "http://192.168.1.108:8080/";

    public static final String GET_DICTATION_HOST_URL = HOST_URL + "get/dictation/";
    public static final String STUDY_SET_HOST_URL = HOST_URL + "studyset/";

    private String[] languages = {
            "English", "Russian", "French", "Spanish", "German", "Ukrainian",
            "Italian", "Chinese", "Hungarian", "Norwegian",
            "Arabic", "Malay", "Portuguese", "Czech", "Dutch",
            "Swedish", "Japanese", "Polish", "Korean", "Turkish",
            "Azerbaijan", "Albanian", "Armenian", "Macedonian", "Belarusian",
            "Welsh", "Greek", "Georgian", "Slovakian", "Slovenian",
            "Icelandic", "Kazakh", "Uzbek", "Croatian", "Latin"};
    private String[] languagesShort = {
            "en", "ru", "fr", "sp", "de", "uk",
            "it", "zh", "hu", "no", "ar", "ms", "pt", "cs",
            "nl", "sv", "ja", "pl", "ko", "tr",
            "az", "sq", "hy", "mk", "be", "cy", "el", "ka", "sk", "sl",
            "is", "kk", "uz", "hr", "la"};

    final public List<String> LANGUAGES = Arrays.asList(languages);
    final public List<String> LANGUAGES_SHORT = Arrays.asList(languagesShort);

    public static final List<Integer> emojiList = Arrays.asList(R.drawable.anim_brows, R.drawable.anim_rich, R.drawable.anim_smart, R.drawable.anim_happy);

    public static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(HOST_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public List<String> getLANGUAGES() {
        return LANGUAGES;
    }

    public List<String> getLANGUAGES_SHORT() {
        return LANGUAGES_SHORT;
    }

    public static String getShareDictationText(String code) {
        return "Code : " + code + " \n " + GET_DICTATION_HOST_URL + code + "/";
    }

    public static String getShareStudySetText(String id) {
        return STUDY_SET_HOST_URL + id + "/";
    }

    public static void showKeyboard(final EditText editText) {
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) editText.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    public static ArrayList<String> generateThreeRandomAnswer(Word word, List<Word> words) {

        int amountOfAnswers = 3;
        words.remove(word);
        ArrayList<String> answers = new ArrayList<>();
        Random random = new Random();
        for (int j = 0; j < amountOfAnswers; j++) {
            int randomIndex = random.nextInt(words.size());
            answers.add(words.get(randomIndex).getTranslation());
            words.remove(randomIndex);
        }

        return answers;
    }

    public static void hideKeyboard(Activity activity) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Activity activity) {

        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

    }

    public static void setCustomFancyCaseView(View view, String text, FancyShowCaseQueue queue) {

        TextView textView = view.findViewById(R.id.fancyshowcase_text);
        TextView skip = view.findViewById(R.id.skip);
        textView.setText(text);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queue.cancel(true);
            }
        });

    }

    public static void setImage(View view, String text, FancyShowCaseQueue queue, Drawable drawable) {

        setCustomFancyCaseView(view, text, queue);
        ImageView image = view.findViewById(R.id.showcase_image);

        image.setImageDrawable(drawable);

    }

    public static boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

        Network[] networks = connectivityManager.getAllNetworks();
        boolean hasInternet = false;
        if (networks.length > 0) {
            for (Network network : networks) {
                NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
                if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                    hasInternet = true;
            }
        }
        return hasInternet;
    }

    public static ArrayList<Word> convertJSONArrayToArrayOfWords(String jsonString) {
        ArrayList<Word> wordArrayList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                wordArrayList.add(new Word(jsonObject.getString("term"), jsonObject.getString("translation")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wordArrayList;
    }

}
