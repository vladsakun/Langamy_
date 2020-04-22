package com.langamy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.main.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.button.MaterialButton
import com.langamy.activities.EditStudySetActivity
import com.langamy.api.LangamyAPI
import com.langamy.base.classes.BaseVariables
import com.langamy.base.classes.StudySet
import com.langamy.base.classes.Word
import com.langamy.base.kotlin.ScopedActivity
import com.langamy.fragments.MarkedWordsFragment
import com.langamy.viewmodel.SpecificStudySetViewModel
import com.langamy.viewmodel.SpecificStudySetsViewModelFactory
import kotlinx.coroutines.launch
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import me.toptas.fancyshowcase.listener.OnViewInflateListener
import org.json.JSONArray
import org.json.JSONException
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.factory
import java.io.Serializable
import java.util.*

class SpecificStudySetActivity : ScopedActivity(), KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory: ((Int) -> SpecificStudySetsViewModelFactory) by factory()
    private lateinit var viewModel: SpecificStudySetViewModel

    private var studySetId = 0
    private var studyMarked = false
    private var mWordList: ArrayList<Word>? = null
    private val markedWords = ArrayList<Word>()
    private var editStudySetActivityIntent: Intent? = null
    private var learnIntent: Intent? = null
    private var cardIntent: Intent? = null
    private lateinit var mProgressBar: ProgressBar
    private lateinit var studySetName: String
    private lateinit var containerForRecylcler: FrameLayout
    private lateinit var parentForContainer: FrameLayout
    private lateinit var studyCategories_LL: LinearLayout
    private lateinit var learnBtn: MaterialButton
    private lateinit var createDictationBtn: MaterialButton
    private lateinit var cardMode_BTN: MaterialButton
    private lateinit var studyAll_MBTN: MaterialButton
    private lateinit var studyMarked_MBTN: MaterialButton
    private lateinit var mAdView: AdView
    private lateinit var wordsRecyclerView: RecyclerView
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var fragment: MarkedWordsFragment? = null
    var retrofit = BaseVariables.retrofit
    var mLangamyAPI = retrofit.create(LangamyAPI::class.java)
    private var makeDictationIntent: Intent? = null

    public override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_studyset)

        //Set window not touchable
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        mWordList = ArrayList()
        editStudySetActivityIntent = Intent(this@SpecificStudySetActivity, EditStudySetActivity::class.java)
        learnIntent = Intent(this@SpecificStudySetActivity, LearnActivity::class.java)
        cardIntent = Intent(this@SpecificStudySetActivity, CardModeActivity::class.java)
        makeDictationIntent = Intent(this@SpecificStudySetActivity, MakeDictationActivity::class.java)

        //Views
        mProgressBar = findViewById(R.id.progressBar)
        learnBtn = findViewById(R.id.learn_btn)
        cardMode_BTN = findViewById(R.id.card_mode_btn)
        createDictationBtn = findViewById(R.id.make_dictation_btn)
        wordsRecyclerView = findViewById(R.id.words_recyclerview)
        studyCategories_LL = findViewById(R.id.study_categories_LL)
        studyAll_MBTN = findViewById(R.id.study_all_MBTN)
        studyMarked_MBTN = findViewById(R.id.study_marked_MBTN)
        containerForRecylcler = findViewById(R.id.container_for_recycler)
        parentForContainer = findViewById(R.id.parent_for_container)
        mAdView = findViewById(R.id.adView)
        MobileAds.initialize(this) { }
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        studySetId = intent.getIntExtra(BaseVariables.STUDY_SET_ID_MESSAGE, 0)

        var cloned: Boolean
        try {
            val data = intent.data
            studySetId = data.toString().replace("http://vlad12.pythonanywhere.com/studyset/", "")
                    .replace("/", "").toInt()
            cloned = true
        } catch (e: Exception) {
            cloned = false
        }

        viewModel = ViewModelProvider(this, viewModelFactory(studySetId)).get(SpecificStudySetViewModel::class.java)

        //MarkedWords Fragment initializing
        fragment = MarkedWordsFragment(mWordList)
        fragmentTransaction.add(R.id.container_for_recycler, fragment!!)
        fragmentTransaction.commit()

        inizializeRecyclerView(mWordList!!)
        learnBtn.setOnClickListener(View.OnClickListener {
            if (studyMarked) {
                val markedWordsForIntent = ArrayList<Word>()
                for (word in mWordList!!) {
                    if (word.isMarked) {
                        markedWordsForIntent.add(word)
                    }
                }
                if (markedWordsForIntent.size < 4) {
                    Toast.makeText(this@SpecificStudySetActivity, "You have to mark more than 4 words", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                learnIntent!!.putExtra(BaseVariables.MARKED_MESSAGE, true)
                learnIntent!!.putExtra(BaseVariables.WORDS_MESSAGE, markedWords)
            } else {
                learnIntent!!.putExtra(BaseVariables.MARKED_MESSAGE, false)
                learnIntent!!.putExtra(BaseVariables.WORDS_MESSAGE, mWordList)
            }
            startActivity(learnIntent)
        })
        cardMode_BTN.setOnClickListener(View.OnClickListener {
            cardIntent!!.putExtra(BaseVariables.WORDS_MESSAGE, mWordList)
            startActivity(cardIntent)
        })
        createDictationBtn.setOnClickListener(View.OnClickListener {
            makeDictationIntent!!.putExtra(BaseVariables.WORDS_MESSAGE, mWordList)
            makeDictationIntent!!.putExtra(BaseVariables.TITLE_MESSAGE, studySetName)
            startActivity(makeDictationIntent)
            finish()
        })
        studyAll_MBTN.setOnClickListener(View.OnClickListener {
            studyMarked = false
            studyAll_MBTN.setBackgroundColor(getColor(R.color.blue))
            studyAll_MBTN.setTextColor(getColor(R.color.white))
            studyMarked_MBTN.setBackgroundColor(getColor(R.color.white))
            studyMarked_MBTN.setTextColor(getColor(R.color.blue))
            studyAll_MBTN.isClickable = false
            studyMarked_MBTN.isClickable = true
            parentForContainer.visibility = View.GONE
            wordsRecyclerView.visibility = View.VISIBLE
        })
        studyMarked_MBTN.setOnClickListener(View.OnClickListener {
            studyMarked = true
            studyMarked_MBTN.setBackgroundColor(getColor(R.color.blue))
            studyMarked_MBTN.setTextColor(getColor(R.color.white))
            studyAll_MBTN.setBackgroundColor(getColor(R.color.white))
            studyAll_MBTN.setTextColor(getColor(R.color.blue))
            studyMarked_MBTN.setClickable(false)
            studyAll_MBTN.setClickable(true)
            parentForContainer.setVisibility(View.VISIBLE)
            wordsRecyclerView.setVisibility(View.GONE)
            fragment!!.words = mWordList
        })

        bindUI(cloned)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_specific_study_set, menu)
        inflater.inflate(R.menu.menu_help_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.edit_study_set) {
            if (!BaseVariables.checkNetworkConnection(this)) {
                Toast.makeText(this, R.string.you_need_an_internet_connection, Toast.LENGTH_SHORT).show()
                return false
            }
            startActivity(editStudySetActivityIntent)
            finish()
        }
        if (id == R.id.help) {
            playHelp()
        }
        if (id == R.id.share) {
            if (!BaseVariables.checkNetworkConnection(this)) {
                Toast.makeText(this, R.string.you_need_an_internet_connection, Toast.LENGTH_SHORT).show()
                return false
            }
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            val sharedDictationText = BaseVariables.getShareStudySetText(studySetId.toString())
            sendIntent.putExtra(Intent.EXTRA_TEXT, sharedDictationText)
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun inizializeRecyclerView(words: List<Word>) {
        wordsRecyclerView.isNestedScrollingEnabled = false
        wordsRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        wordsRecyclerView.layoutManager = layoutManager
        mAdapter = SpecificStudySetAdapterInActivity(words)
        wordsRecyclerView.adapter = mAdapter
    }

    private fun playHelp() {
        BaseVariables.hideKeyboard(this)
        val fq = FancyShowCaseQueue()
        val makeDictation = FancyShowCaseView.Builder(this)
                .focusOn(createDictationBtn)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_make_dictation_btn), fq)
                    }
                })
                .backgroundColor(getColor(R.color.blueForFancy))
                .build()
        val studyMarked = FancyShowCaseView.Builder(this)
                .focusOn(studyMarked_MBTN)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_study_marked), fq)
                    }
                })
                .backgroundColor(getColor(R.color.blueForFancy))
                .build()
        val edit = FancyShowCaseView.Builder(this)
                .focusOn(findViewById(R.id.edit_study_set))
                .customView(R.layout.fancyshowcase_with_image, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setImage(view, getString(R.string.fancy_edit),
                                fq, getDrawable(R.drawable.ic_edit_white_24dp))
                    }
                })
                .backgroundColor(getColor(R.color.blueForFancy))
                .build()
        fq.add(makeDictation)
                .add(studyMarked)
                .add(edit)
        fq.show()
    }

    fun initializeStudySetActivity(studySet: StudySet?) {
        mWordList!!.clear()

        mWordList!!.addAll(convertJsonArrayToArray(studySet!!.words))
        if (studySet.marked_words != null) {
            markedWords.addAll(convertJsonArrayToArray(studySet.marked_words))
        }
        for (i in markedWords.indices) {
            val word = markedWords[i]
            for (allWord in mWordList!!) {
                if (allWord.term == word.term && allWord.translation == word.translation) {
                    allWord.isMarked = true
                }
            }
        }
        fragment!!.words = mWordList
        mAdapter!!.notifyDataSetChanged()
        studyCategories_LL.visibility = View.VISIBLE
        studySetName = studySet.name
        editStudySetActivityIntent!!.putExtra(BaseVariables.STUDY_SET_MESSAGE, studySet)
        learnIntent!!.putExtra(BaseVariables.STUDY_SET_MESSAGE, studySet)
        cardIntent!!.putExtra(BaseVariables.FROM_LANG_MESSAGE, studySet.language_from as Serializable)
        makeDictationIntent!!.putExtra(BaseVariables.FROM_LANG_MESSAGE, studySet.language_from)
        makeDictationIntent!!.putExtra(BaseVariables.TO_LANG_MESSAGE, studySet.language_to)
        mProgressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val sf = getPreferences(Context.MODE_PRIVATE)
        val help = sf.getBoolean(BaseVariables.HELP_SPECIFIC_STUDYSET, true)
        val editor = sf.edit()
        if (help) {
            playHelp()
            editor.putBoolean(BaseVariables.HELP_SPECIFIC_STUDYSET, false)
            editor.apply()
        }

    }

    private fun convertJsonArrayToArray(words: String): ArrayList<Word> {
        val array = ArrayList<Word>()
        try {
            val jsonArray = JSONArray(words)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val word = Word(jsonObject.getString("term"),
                        jsonObject.getString("translation"),
                        java.lang.Boolean.parseBoolean(jsonObject.getString("firstStage")),
                        java.lang.Boolean.parseBoolean(jsonObject.getString("secondStage")),
                        java.lang.Boolean.parseBoolean(jsonObject.getString("thirdStage")),
                        java.lang.Boolean.parseBoolean(jsonObject.getString("forthStage")))
                array.add(word)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return array
    }

    //Adapter for word recyclerview
    internal inner class SpecificStudySetAdapterInActivity(var mWords: List<Word>) : RecyclerView.Adapter<SpecificStudySetAdapterInActivity.SpecificStudySetHolder>() {

        inner class SpecificStudySetHolder(v: View) : RecyclerView.ViewHolder(v) {
            // each data item is just a string in this case
            var term: TextView
            var transaltion: TextView
            val starBtn: ToggleButton

            init {
                term = v.findViewById(R.id.term_TV)
                transaltion = v.findViewById(R.id.translation_TV)
                starBtn = v.findViewById(R.id.starBtn)
            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): SpecificStudySetHolder {
            // create a new view
            val v = LayoutInflater.from(parent.context).inflate(R.layout.studyset_word_item, parent, false)
            return SpecificStudySetHolder(v)
        }

        override fun onBindViewHolder(holder: SpecificStudySetHolder, position: Int) {
            holder.term.text = mWords[position].term
            holder.transaltion.text = mWords[position].translation
            holder.starBtn.setOnCheckedChangeListener(null)
            holder.starBtn.isChecked = mWords[position].isMarked
            holder.starBtn.setOnCheckedChangeListener { compoundButton: CompoundButton?, isChecked: Boolean ->
                if (isChecked) {
                    mWords[position].isMarked = true
                    markedWords.add(mWords[position])
                } else {
                    mWords[position].isMarked = false
                    markedWords.remove(mWords[position])
                }
            }
        }

        override fun getItemCount(): Int {
            return mWords.size
        }

    }

    fun removeMarkedWord(currentWordIndex: Int) {
        mWordList!![currentWordIndex].isMarked = false
        fragment!!.words = mWordList
        mAdapter!!.notifyDataSetChanged()
    }

    fun bindUI(cloned: Boolean) = launch {
        var studySet: LiveData<out StudySet>? = null
        studySet = if (cloned) {
            viewModel.cloneStudySet()
            viewModel.clonedStudySet.await()
        } else {
            viewModel.studySet.await()
        }
        studySet?.observe(this@SpecificStudySetActivity, androidx.lifecycle.Observer {
            if (it == null) return@Observer

            initializeStudySetActivity(it)
        })
    }

//    fun readDataFromLocaleStorage(studySet_id: Int): StudySet {
//
//        val mDatabase = StudySetsBaseHelper(this).readableDatabase
//        val cursor = BaseVariables.queryStudySets(Cols.id + "=?", arrayOf(studySet_id.toString()), mDatabase)
//        val mLocaleStudySet: StudySet
//        mLocaleStudySet = try {
//            cursor.moveToFirst()
//            cursor.studySet
//        } finally {
//            cursor.close()
//        }
//        return mLocaleStudySet
//    }
}