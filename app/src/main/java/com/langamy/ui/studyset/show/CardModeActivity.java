package com.langamy.ui.studyset.show;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bignerdranch.android.main.R;
import com.langamy.adapters.CardsAdapter;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.CardItem;
import com.langamy.base.classes.Word;

import java.util.ArrayList;
import java.util.Locale;

import link.fls.swipestack.SwipeStack;

public class CardModeActivity extends AppCompatActivity {

    private CardsAdapter cardsAdapter;
    private ArrayList<Word> wordArrayList = new ArrayList<>();
    private ArrayList<CardItem> cardItems;
    private String fromLang;

    private SwipeStack cardStack;
    private TextView cardAmount;
    private ImageButton refresh, volumeUp, undoCard;
    private static TextToSpeech textToSpeech;

    private int currentPosition;
    boolean speakTerm = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_mode);

        cardStack = (SwipeStack) findViewById(R.id.swipeStack);
        refresh = findViewById(R.id.refresh_IB);
        volumeUp = findViewById(R.id.volume_up_IB);
        undoCard = findViewById(R.id.undo_card_IB);
        cardAmount = findViewById(R.id.card_amount_TV);

        wordArrayList = (ArrayList<Word>) getIntent().getSerializableExtra(BaseVariables.WORDS_MESSAGE);
        fromLang = getIntent().getStringExtra(BaseVariables.FROM_LANG_MESSAGE);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.forLanguageTag(fromLang));
                textToSpeech.setSpeechRate(0.8f);
            }
        });

        setCardStackAdapter();
        currentPosition = 1;
        cardAmount.setText(currentPosition + "/" + cardItems.size());

        //Handling swipe event of Cards stack
        cardStack.setListener(new SwipeStack.SwipeStackListener() {
            @Override
            public void onViewSwipedToLeft(int position) {
                if (!(position + 1 == cardItems.size())) {
                    currentPosition++;
                }

                cardAmount.setText(currentPosition + "/" + cardItems.size());
                if (!(position == cardItems.size() - 1) && speakTerm) {
                    speakTerm(cardItems.get(position + 1).getTerm());
                }
            }

            @Override
            public void onViewSwipedToRight(int position) {
                if (!(position + 1 == cardItems.size())) {
                    currentPosition++;
                }

                cardAmount.setText(currentPosition + "/" + cardItems.size());
                if (!(position == cardItems.size() - 1) && speakTerm) {
                    speakTerm(cardItems.get(position + 1).getTerm());
                }
            }

            @Override
            public void onStackEmpty() {
                currentPosition = 1;
                refresh.setVisibility(View.VISIBLE);
            }

        });


        volumeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakTerm = !speakTerm;
                if (speakTerm) {
                    volumeUp.setImageDrawable(getDrawable(R.drawable.ic_volume_up_blue_34dp));
                } else {
                    volumeUp.setImageDrawable(getDrawable(R.drawable.ic_volume_off_blue_34dp));
                }
            }
        });

        refresh.setOnClickListener(v -> {
            cardStack.resetStack();
            refresh.setVisibility(View.GONE);
            cardAmount.setText(currentPosition + "/" + cardItems.size());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textToSpeech.shutdown();
    }

    private void setCardStackAdapter() {
        cardItems = new ArrayList<>();

        for (Word word : wordArrayList) {
            cardItems.add(new CardItem(word.getTerm(), word.getTranslation()));
        }

        cardsAdapter = new CardsAdapter(this, cardItems);
        cardStack.setAdapter(cardsAdapter);
    }

    public static void speakTerm(String term) {

        CharSequence charSequence = term;
        textToSpeech.speak(charSequence, TextToSpeech.QUEUE_FLUSH, null, null);
    }


}
