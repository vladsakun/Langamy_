package com.langamy.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bignerdranch.android.main.R
import com.langamy.activities.LearnActivity
import com.langamy.base.classes.Answer
import com.langamy.base.classes.Word
import kotlinx.android.synthetic.main.fragment_speech_stage.*

/**
 * A simple [Fragment] subclass.
 */
class SpeechStageFragment(
        val word: Word,
        val language:String
) : Fragment() {

    private val REQUEST_CODE_SPEECH_INPUT: Int = 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_speech_stage, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        translation_TV.text = word.translation

        voiceBtn.setOnClickListener {
            speak()
        }
    }

    private fun speak() {
        //intent to show speech to text dialog
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak something")

        //start activity
        try {
            //show dialog
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "" + e.message, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            REQUEST_CODE_SPEECH_INPUT -> if (resultCode == RESULT_OK && null != data){
                //get text array from voice intent
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                //set to textView
                checkAnswer(result!![0])
            }
        }
    }

    private fun checkAnswer(userAnswer: String){
        val answer = Answer(userAnswer, word.translation, word.term, true)

        if (userAnswer.toLowerCase().trim { it <= ' ' } != word.term.toLowerCase().trim { it <= ' ' }) {
            answer.status = false
        }
        val myActivity = activity!! as LearnActivity
        myActivity.checkAnswerSpeechStage(answer, word)
    }

}
