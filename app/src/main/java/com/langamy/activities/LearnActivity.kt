package com.langamy.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.main.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.langamy.adapters.StagesAdapter
import com.langamy.api.LangamyAPI
import com.langamy.base.classes.BaseVariables
import com.langamy.base.classes.NonSwipeableViewPager
import com.langamy.base.classes.StudySet
import com.langamy.base.classes.Word
import com.langamy.base.kotlin.ScopedActivity
import com.langamy.fragments.*
import com.langamy.viewmodel.LearnActivityViewModel
import com.langamy.viewmodel.LearnActivityViewModelFactory
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LearnActivity : ScopedActivity(), KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory by instance<LearnActivityViewModelFactory>()
    private lateinit var viewModel: LearnActivityViewModel

    var stages = ArrayList<Fragment>()
    private var mStudySet: StudySet? = null
    private lateinit var wordsForLearning: ArrayList<Word>
    private lateinit var words: ArrayList<Word>
    private var studysetId = 0
    private var restWords = 0
    private var firstStageWords = 0
    private var thirdStageWords = 0
    private var masteredWords = 0
    private var amountOfWords = 0
    private var learnMarked = false
    private var fromLang: String? = null
    private var toLang: String? = null
    var retrofit = BaseVariables.retrofit
    var mLangamyAPI = retrofit.create(LangamyAPI::class.java)
    private var mDatabase: SQLiteDatabase? = null
    private var account: GoogleSignInAccount? = null
    private var correctAlertDialog: AlertDialog? = null
    private var wrongAlertDialog: AlertDialog? = null
    private var wrongDialogBuilder: AlertDialog.Builder? = null
    private lateinit var learnVP2: NonSwipeableViewPager
    private lateinit var adapter: StagesAdapter
    private var wrongDialogView: View? = null
    private lateinit var restWords_TV: TextView
    private lateinit var firstStageWords_TV: TextView
    private lateinit var thirdStageWords_TV: TextView
    private lateinit var masteredWords_TV: TextView
    private lateinit var learnContainer_RL: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn)

        viewModel = ViewModelProvider(this, viewModelFactory).get(LearnActivityViewModel::class.java)

        learnContainer_RL = findViewById(R.id.learn_activity_container)
        learnVP2 = findViewById(R.id.learn_VP2)
        restWords_TV = findViewById(R.id.rest_words_TV)
        firstStageWords_TV = findViewById(R.id.first_stage_words_TV)
        thirdStageWords_TV = findViewById(R.id.third_stage_words_TV)
        masteredWords_TV = findViewById(R.id.mastered_words_TV)

        val intent = intent

        mStudySet = intent.getSerializableExtra(BaseVariables.STUDY_SET_MESSAGE) as StudySet
        words = intent.getSerializableExtra(BaseVariables.WORDS_MESSAGE) as ArrayList<Word>
        learnMarked = intent.getBooleanExtra(BaseVariables.MARKED_MESSAGE, false)
        fromLang = mStudySet!!.language_from
        toLang = mStudySet!!.language_to
        studysetId = mStudySet!!.id
        amountOfWords = mStudySet!!.amount_of_words
        for (word in words!!) {
            if (!word.isFirstStage) {
                restWords++
                continue
            }
            if (word.isFirstStage && !word.isThirdStage) {
                firstStageWords++
            } else if (word.isThirdStage && !word.isForthStage) {
                thirdStageWords++
            } else if (word.isForthStage) {
                masteredWords++
            }
        }
        val wordArrayList = ArrayList(words)
        wordsForLearning = ArrayList()
        wordsForLearning!!.addAll(getRandomObjects(wordArrayList))
        stages = generateStages(wordsForLearning)
        adapter = StagesAdapter(supportFragmentManager, stages)
        learnVP2.setAdapter(adapter)
        learnVP2.setOffscreenPageLimit(8)
        restWords_TV.setText(restWords.toString())
        firstStageWords_TV.setText(firstStageWords.toString())
        thirdStageWords_TV.setText(thirdStageWords.toString())
        masteredWords_TV.setText(masteredWords.toString())
        val correctDialogBuilder = AlertDialog.Builder(this)
        correctDialogBuilder.setView(View.inflate(this, R.layout.correct_alert_dialog, null))
        correctAlertDialog = correctDialogBuilder.create()
        correctAlertDialog!!.setOnCancelListener { goToNextPage() }
        wrongDialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        wrongDialogView = inflater.inflate(R.layout.wrong_alert_dialog, null)
        wrongDialogBuilder!!.setView(wrongDialogView)
        wrongAlertDialog = wrongDialogBuilder!!.create()
        account = GoogleSignIn.getLastSignedInAccount(this)
    }

    fun textScaleAnimation(textView: TextView?) {
        val startSize = 14f // Size in pixels
        val endSize = 20f
        val animationDuration: Long = 450 // Animation duration in ms
        val animator = ValueAnimator.ofFloat(startSize, endSize)
        animator.duration = animationDuration
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            textView!!.textSize = animatedValue
        }
        animator.repeatCount = 1
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.start()
    }

    fun reloadViewPager(view: View?) {
        stages.clear()
        wordsForLearning!!.clear()
        val wordArrayList = ArrayList(words)
        wordsForLearning!!.addAll(getRandomObjects(wordArrayList))
        stages = ArrayList()
        stages = generateStages(wordsForLearning)
        if (stages.size == 1) {
            return
        }
        adapter!!.setStages(stages)
        learnVP2!!.adapter = adapter
        learnVP2!!.invalidate()
        if (stages[0].javaClass.simpleName == "AudioStageFragment") {
            val currentStage = stages[0] as AudioStageFragment
            currentStage.speakTerm()
        }
        learnVP2!!.currentItem = 0
    }

    private fun showCorrectAlertDialog() {
        correctAlertDialog!!.show()
        val handler = Handler()
        handler.postDelayed({
            if (correctAlertDialog!!.isShowing) {
                goToNextPage()
                correctAlertDialog!!.dismiss()
            }
        }, 500)
    }

    private fun showWrongAlertDialog(correctAnswer: String, wordId: Int, stage: String) {
        val correctAnswer_TV = wrongDialogView!!.findViewById<TextView>(R.id.correct_answer_TV)
        val im_right_TV = wrongDialogView!!.findViewById<TextView>(R.id.im_right_TV)
        val continueLearning_TV = wrongDialogView!!.findViewById<ImageButton>(R.id.continue_learning_TV)
        correctAnswer_TV.text = correctAnswer
        wrongAlertDialog!!.show()
        continueLearning_TV.setOnClickListener {
            wrongAlertDialog!!.dismiss()
            goToNextPage()
        }
        wrongAlertDialog!!.setOnCancelListener {
            wrongAlertDialog!!.dismiss()
            goToNextPage()
        }
        im_right_TV.setOnClickListener { v: View? ->
            when (stage) {
                "first" -> words!![wordId].isFirstStage = true
                "second" -> words!![wordId].isSecondStage = true
                "third" -> words!![wordId].isThirdStage = true
                "forth" -> {
                    words!![wordId].isForthStage = true
                    masteredWords++
                }
            }
            wrongAlertDialog!!.dismiss()
            goToNextPage()
        }
    }

    fun checkAnswerFirstStage(v: View) {
        val text = (v as TextView).text.toString()
        val quizStage = stages[learnVP2!!.currentItem] as QuizStage
        val userAnswer = quizStage.checkAnswer(text)
        val correctWordIndex = words!!.indexOf(quizStage.word)
        if (userAnswer.status) {
            val correctWord = words!![correctWordIndex]
            correctWord.isFirstStage = true
            textScaleAnimation(firstStageWords_TV)
            firstStageWords_TV!!.text = (firstStageWords_TV!!.text.toString().toInt() + 1).toString()
            restWords_TV!!.text = (restWords_TV!!.text.toString().toInt() - 1).toString()
            showCorrectAlertDialog()
        } else {
            showWrongAlertDialog(userAnswer.correctAnswer, correctWordIndex, "first")
        }
    }

    fun checkAnswerAudioStage(view: View) {
        val parent = view.parent as RelativeLayout
        val userAnswer_ET = parent.findViewById<View>(R.id.definition_audiostage_ET) as EditText
        val text = userAnswer_ET.text.toString().toLowerCase().trim { it <= ' ' }
        val audioStageFragment = stages[learnVP2!!.currentItem] as AudioStageFragment
        val userAnswer = audioStageFragment.checkAnswer(text)
        if (view.id == R.id.cannot_speak_BTN) {
            userAnswer.status = true
        }
        val correctWordIndex = words!!.indexOf(audioStageFragment.word)
        if (userAnswer.status) {
            val correctWord = words!![correctWordIndex]
            correctWord.isSecondStage = true
            showCorrectAlertDialog()
        } else {
            showWrongAlertDialog(userAnswer.correctAnswer, correctWordIndex, "second")
        }
    }

    fun checkAnswerTermTranslation(v: View) {
        val parent = v.parent as RelativeLayout
        val child = parent.findViewById<View>(R.id.answer_ET) as EditText
        val text = child.text.toString().trim { it <= ' ' }.toLowerCase()
        val termDefinitionStage = stages[learnVP2!!.currentItem] as TermDefinitionStage
        val userAnswer = termDefinitionStage.checkAnswer(text)
        val correctWordIndex = words!!.indexOf(termDefinitionStage.word)
        if (userAnswer.status) {
            val correctWord = words!![correctWordIndex]
            correctWord.isThirdStage = true
            textScaleAnimation(thirdStageWords_TV)
            thirdStageWords_TV!!.text = (thirdStageWords_TV!!.text.toString().toInt() + 1).toString()
            firstStageWords_TV!!.text = (firstStageWords_TV!!.text.toString().toInt() - 1).toString()
            showCorrectAlertDialog()
        } else {
            showWrongAlertDialog(userAnswer.correctAnswer, correctWordIndex, "third")
        }
        parent.visibility = View.INVISIBLE
    }

    fun checkAnswerTranslationTerm(v: View) {
        val parent = v.parent as RelativeLayout
        val child = parent.findViewById<View>(R.id.answer_ET) as EditText
        val text = child.text.toString().trim { it <= ' ' }.toLowerCase()
        val definitionTermStage = stages[learnVP2!!.currentItem] as DefinitionTermStage
        val userAnswer = definitionTermStage.checkAnswer(text)
        val correctWordIndex = words!!.indexOf(definitionTermStage.word)
        if (userAnswer.status) {
            val correctWord = words!![correctWordIndex]
            correctWord.isForthStage = true
            textScaleAnimation(masteredWords_TV)
            masteredWords++
            masteredWords_TV!!.text = (masteredWords_TV!!.text.toString().toInt() + 1).toString()
            thirdStageWords_TV!!.text = (thirdStageWords_TV!!.text.toString().toInt() - 1).toString()
            showCorrectAlertDialog()
        } else {
            showWrongAlertDialog(userAnswer.term, correctWordIndex, "forth")
        }
    }

    fun goToNextPage() {
        learnVP2.currentItem = learnVP2.currentItem + 1
        val currentFragment = stages[learnVP2.currentItem]
        if (currentFragment.javaClass.simpleName == "ContinueLearningFragment") {
            try {
                updateStudyset()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val continueLearningFragment = currentFragment as ContinueLearningFragment
            continueLearningFragment.setStudysetId(studysetId, learnMarked)
            continueLearningFragment.setAmountOfWords(words.size)
            continueLearningFragment.setMasteredWords(masteredWords)
        }
    }

    fun getRandomObjects(wordArrayList: MutableList<Word>): ArrayList<Word> {
        val randomWords = ArrayList<Word>()
        val random = Random()
        var amountOfRandomObjects = 7
        if (wordArrayList.size < 7) {
            amountOfRandomObjects = wordArrayList.size
        }
        for (i in 0 until amountOfRandomObjects) {
            val randomIndex = random.nextInt(wordArrayList.size)
            val word = wordArrayList[randomIndex]
            randomWords.add(word)
            wordArrayList.remove(word)
        }
        return randomWords
    }

    private fun generateStages(wordsForGenerating: ArrayList<Word>?): ArrayList<Fragment> {
        val generatedStages = ArrayList<Fragment>()
        for (i in wordsForGenerating!!.indices) {
            if (!wordsForGenerating[i].isFirstStage) {
                val wordArrayListCopy = ArrayList(words)
                val randomAnswers = BaseVariables.generateThreeRandomAnswer(wordsForGenerating[i], wordArrayListCopy)
                val quizStage = QuizStage(wordsForGenerating[i],
                        randomAnswers[0], randomAnswers[1], randomAnswers[2], fromLang, 0)
                generatedStages.add(quizStage)
            } else if (!wordsForGenerating[i].isSecondStage) {
                generatedStages.add(AudioStageFragment(wordsForGenerating[i], fromLang))
            } else if (!wordsForGenerating[i].isThirdStage) {
                generatedStages.add(TermDefinitionStage(wordsForGenerating[i], fromLang))
            } else if (!wordsForGenerating[i].isForthStage) {
                generatedStages.add(DefinitionTermStage(wordsForGenerating[i], toLang))
            }
        }
        generatedStages.add(ContinueLearningFragment())
        learnVP2.currentItem = learnVP2.currentItem + 1
        return generatedStages
    }

    @Throws(JSONException::class)
    fun updateStudyset() {
        val wordsForUpdatingStudySet = JSONArray()

        for (i in words.indices) {
            val currentWord = JSONObject()
            currentWord.put("term", words[i].term)
            currentWord.put("translation", words[i].translation)
            currentWord.put("firstStage", words[i].isFirstStage)
            currentWord.put("secondStage", words[i].isSecondStage)
            currentWord.put("thirdStage", words[i].isThirdStage)
            currentWord.put("forthStage", words[i].isForthStage)
            wordsForUpdatingStudySet.put(currentWord)
        }

        val studySet = mStudySet

        if (learnMarked) {
            studySet!!.marked_words = wordsForUpdatingStudySet.toString()
        } else {
            studySet!!.words = wordsForUpdatingStudySet.toString()
        }


        if (BaseVariables.checkNetworkConnection(this)) {

            val call = mLangamyAPI.patchStudySet(studysetId, studySet)

            call.enqueue(object : Callback<StudySet?> {
                override fun onResponse(call: Call<StudySet?>, response: Response<StudySet?>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@LearnActivity, "Changes have not been saved", Toast.LENGTH_SHORT).show()
                        return
                    }
                }

                override fun onFailure(call: Call<StudySet?>, t: Throwable) {
                    Log.d("FAILURE_LEARN_ACTIVITY", t.toString())
                }
            })
            updateLocalStudySet(studySet, true)
        } else {
            updateLocalStudySet(studySet, false)
        }
    }

    fun updateLocalStudySet(studySet: StudySet, syncStatus: Boolean) = launch {
        studySet.isSync_status = syncStatus
        viewModel.updateLocalStudySet(studySet)
        Log.d("UPDATE", studySet.words.toString())

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            val className = stages[learnVP2!!.currentItem].javaClass.simpleName
            val parent = stages[learnVP2!!.currentItem].view as RelativeLayout?
            val child = parent!!.getChildAt(0)
            when (className) {
                "AudioStageFragment" -> checkAnswerAudioStage(child)
                "TermDefinitionStage" -> checkAnswerTermTranslation(child)
                "DefinitionTermStage" -> checkAnswerTranslationTerm(child)
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}