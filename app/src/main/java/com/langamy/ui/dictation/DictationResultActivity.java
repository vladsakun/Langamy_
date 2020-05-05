package com.langamy.ui.dictation;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bignerdranch.android.main.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.langamy.activities.MainActivity;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.Answer;
import com.langamy.base.classes.BaseVariables;
import com.langamy.ui.dictation.show.SpecificDictationActivity;
import com.langamy.ui.learning.AnswerFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DictationResultActivity extends AppCompatActivity {

    private int amountOfQuestions = 0, amountOfCorrectAnswers = 0, dictationId;
    private ArrayList<Answer> mAnswers;
    private ArrayList<Fragment> mAnswerFragments;
    private String typeOfQuestions;

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);

    private TextView dictationResult_TV;
    private ImageButton retakeDictation_BTN;
    private ViewPager mAnswers_VP;
    private ImageView emoji_IV;

    @Override
    protected void onStart() {
        super.onStart();
        BaseVariables.hideKeyboard(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictation_result);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        mAnswerFragments = new ArrayList<>();

        dictationResult_TV = findViewById(R.id.dictation_result_TV);
        retakeDictation_BTN = findViewById(R.id.retake_dictation_BTN);
        mAnswers_VP = findViewById(R.id.answers_VP);
        emoji_IV = findViewById(R.id.result_emoji);

        amountOfQuestions = getIntent().getIntExtra(BaseVariables.QUESTION_AMOUNT_MESSAGE, 0);
        amountOfCorrectAnswers = getIntent().getIntExtra(BaseVariables.AMOUNT_OF_CORRECT_ANSWERS_MESSAGE, 0);
        dictationId = getIntent().getIntExtra(BaseVariables.DICTATION_ID_MESSAGE, 0);
        mAnswers = (ArrayList<Answer>) getIntent().getSerializableExtra(BaseVariables.USER_ANSWERS_MESSAGE);
        typeOfQuestions = getIntent().getStringExtra(BaseVariables.TYPE_OF_QUESTIONS_MESSAGE);

        float percentOfCorrect = ((float)amountOfCorrectAnswers / (float)amountOfQuestions) * 100;

        if (percentOfCorrect < 19) {
            emoji_IV.setImageDrawable(getDrawable(R.drawable.sad_emoji));
        } else if (percentOfCorrect >= 20 && percentOfCorrect < 40) {
            emoji_IV.setImageDrawable(getDrawable(R.drawable.anim_surprised));
        } else if (percentOfCorrect >= 40 && percentOfCorrect < 60) {
            emoji_IV.setImageDrawable(getDrawable(R.drawable.anim_happy));
        } else if (percentOfCorrect >= 60) {
            emoji_IV.setImageDrawable(getDrawable(R.drawable.anim_brows));
        }

        Drawable drawable = emoji_IV.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }

        String resultText = getString(R.string.dictation_result, String.valueOf(amountOfCorrectAnswers), String.valueOf(amountOfQuestions));

        dictationResult_TV.setText(resultText);

        retakeDictation_BTN.setOnClickListener(view -> {
            Intent intent = new Intent(DictationResultActivity.this, SpecificDictationActivity.class);
            intent.putExtra(BaseVariables.DICTATION_ID_MESSAGE, dictationId);
            startActivity(intent);
            finish();
        });

        assert acct != null;
        updateUserMark(acct.getEmail(), amountOfCorrectAnswers, dictationId);

        for (Answer answer : mAnswers) {
            mAnswerFragments.add(new AnswerFragment(answer, typeOfQuestions));
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        mAnswers_VP.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mAnswerFragments.get(position);
            }

            @Override
            public int getCount() {
                return mAnswerFragments.size();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_home_item, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {

            Intent intent = new Intent(DictationResultActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUserMark(String email, int mark, int dictationId) {
        JSONObject JSONmark = new JSONObject();
        try {
            JSONmark.put("dictation_id", dictationId);
            JSONmark.put("mark", mark);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Call<Void> call = mLangamyAPI.patchUserMark(email, JSONmark.toString());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(DictationResultActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DictationResultActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
