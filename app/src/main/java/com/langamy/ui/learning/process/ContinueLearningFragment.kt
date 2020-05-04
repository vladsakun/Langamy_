package com.langamy.ui.learning.process

import android.app.Activity
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.main.R
import com.google.android.material.button.MaterialButton
import com.langamy.api.LangamyAPI
import com.langamy.base.classes.BaseVariables
import com.langamy.base.classes.BaseVariables.emojiList
import com.langamy.base.kotlin.ScopedFragment
import com.langamy.viewmodel.continue_learning.ContinueLearningViewModel
import com.langamy.viewmodel.continue_learning.ContinueLearningViewModelFactory
import kotlinx.android.synthetic.main.fragment_continue_learning.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 */
class ContinueLearningFragment : ScopedFragment(), KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory by instance<ContinueLearningViewModelFactory>()
    private lateinit var viewModel: ContinueLearningViewModel

    private var masteredWords = 0
    private var amountOfWords = 0
    private var learnMarked = false
    private var studysetId = 0
    var retrofit = BaseVariables.retrofit
    var mLangamyAPI = retrofit.create(LangamyAPI::class.java)
    private var returnToStudySet_MBTN: MaterialButton? = null
    private var continue_MBTN: MaterialButton? = null
    private var cool_IV: ImageView? = null
    var finishStudySet:Boolean = false
    val motivationStrings = listOf(R.string.nice_job, R.string.carry_on, R.string.do_not_stop, R.string.you_are_improving_yourself)

    override fun onResume() {
        super.onResume()
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_continue_learning, container, false)

        continue_MBTN = view.findViewById(R.id.continue_learning_MBTN)
        returnToStudySet_MBTN = view.findViewById(R.id.return_to_studyset_MBTN)
        returnToStudySet_MBTN!!.setOnClickListener { requireActivity().finish() }
        cool_IV = view.findViewById(R.id.emoji_IV)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ContinueLearningViewModel::class.java)

        if (finishStudySet){
            continue_MBTN!!.visibility = View.GONE
            finishStudyset()
            returnToStudySet_MBTN!!.visibility = View.VISIBLE
        }
        cool_IV!!.setImageDrawable(requireContext().getDrawable(emojiList.random()))

        val drawable = cool_IV!!.drawable
        if (drawable is Animatable) {
            (drawable as Animatable).start()
        }

        text.text = getString(motivationStrings.random())
    }

    private fun updateStudySet(studySetId: Int, learnMarked: Boolean) = launch {
        viewModel.updateStudySet(studySetId, learnMarked)
    }

    fun setMasteredWords(masteredWords: Int) {
        this.masteredWords = masteredWords
        if (masteredWords == amountOfWords) {
            finishStudyset()
            continue_MBTN!!.visibility = View.GONE
            returnToStudySet_MBTN!!.visibility = View.VISIBLE
            cool_IV!!.visibility = View.VISIBLE
        }
    }

    fun setAmountOfWords(amountOfWords: Int) {
        this.amountOfWords = amountOfWords
    }

    fun setStudysetId(studysetId: Int, learnMarked: Boolean) {
        this.studysetId = studysetId
        this.learnMarked = learnMarked
    }

    private fun finishStudyset() {

        if (BaseVariables.checkNetworkConnection(context)) {
            val call: Call<Void> = if (learnMarked) {
                mLangamyAPI.finishStudyset(studysetId, "marked")
            } else {
                mLangamyAPI.finishStudyset(studysetId, "words")
            }
            call.enqueue(object : Callback<Void?> {
                override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(context, response.code().toString(), Toast.LENGTH_SHORT).show()
                        return
                    }
                }

                override fun onFailure(call: Call<Void?>, t: Throwable) {
                    Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        }
        updateStudySet(studysetId, learnMarked)
    }
}