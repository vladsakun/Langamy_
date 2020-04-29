package com.langamy.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.main.R
import com.langamy.api.LangamyAPI
import com.langamy.base.classes.BaseVariables.retrofit
import com.langamy.base.classes.TranslationResponse
import com.langamy.base.classes.Word
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.*

class WordsAdapter(
        val callback: Callback,
        val context: Context,
        wordsArrayLists: ArrayList<Word>,
        val scrollView: ScrollView,
        val resultLinearLayout: LinearLayout,
        val wordsForSuggestions: HashMap<String, ArrayList<String>>,
        var edit:Boolean
) :
        RecyclerView.Adapter<WordsAdapter.WordsHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var mLangamyAPI: LangamyAPI = retrofit.create(LangamyAPI::class.java)
    var languageFromTranslate: String = "en"
    var languageToTranslate: String = "ru"

    init {
        wordsArrayList = wordsArrayLists
    }

    companion object {
        lateinit var wordsArrayList: ArrayList<Word>
    }

    inner class WordsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var termEditText: AutoCompleteTextView = itemView.findViewById(R.id.term_TV) as AutoCompleteTextView
        var translationEditText: EditText = itemView.findViewById(R.id.translation_TV) as EditText
        var translationSupport: TextView = itemView.findViewById(R.id.translation_support) as TextView
        var deleteBtn: ImageButton = itemView.findViewById(R.id.remove_list_word_item_btn) as ImageButton

        init {

            deleteBtn.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    callback.onDeleteClicked(translationSupport, adapterPosition, termEditText)
            }
            termEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    wordsArrayList[adapterPosition].term = termEditText.text.toString()
                    if (s.toString().trim { it <= ' ' }.length == 1) {
                        val firstChar = Character.toLowerCase(s!![0])
                        if (firstChar in 'a'..'z' || firstChar in 'A'..'Z') {
                            val words = wordsForSuggestions["$firstChar.txt"]!!
                            val wordsStringArr = words.toTypedArray()

                            // Создаем адаптер для автозаполнения элемента AutoCompleteTextView
                            val adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item,
                                    wordsStringArr)
                            termEditText.setAdapter(adapter)
                        }
                    }
                }
            })

            termEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollView.post {
                        scrollView.scrollTo(0, itemView.bottom + resultLinearLayout.height)
                    }
                }
            }

            translationEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    wordsArrayList[adapterPosition].translation = translationEditText.text.toString()
                }

            })
            translationEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    translate(termEditText.text.toString().trim { it <= ' ' }, translationSupport)
                    scrollView.post {
                        scrollView.scrollTo(0, itemView.bottom + resultLinearLayout.height)
                    }
                }
            }
            translationSupport.setOnClickListener {
                translationEditText.setText(translationSupport.text.toString())
            }

        }
    }

    private fun translate(stringToTranslate: String, translationSupport: TextView) {
        val postData = JSONObject()
        try {
            postData.put("words", stringToTranslate)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val call = mLangamyAPI.translate(postData, languageFromTranslate, languageToTranslate, "one")
        call.enqueue(object : retrofit2.Callback<TranslationResponse> {
            override fun onResponse(call: Call<TranslationResponse>, response: Response<TranslationResponse>) {
                if (!response.isSuccessful) {
                    Toast.makeText(context, response.code().toString(), Toast.LENGTH_SHORT).show()
                    return
                }
                translationSupport.visibility = View.VISIBLE
                translationSupport.text = response.body()!!.translation
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                Log.d("Translate_Failure", t.toString())
            }
        })
    }

    interface Callback {
        fun onDeleteClicked(translationSupport: TextView, itemId: Int, view: View)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordsHolder {
        val view = inflater.inflate(R.layout.list_words_item, parent, false)
        return WordsHolder(view)
    }

    override fun getItemCount(): Int {
        return wordsArrayList.size
    }

    override fun onBindViewHolder(holder: WordsHolder, position: Int) {
        if (!wordsArrayList.get(position).isShowKeyboard && !edit) {

            wordsArrayList.get(position).isShowKeyboard = true
            holder.termEditText.post(Runnable {
                holder.termEditText.requestFocus()
                val imm = context
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(holder.termEditText, InputMethodManager.SHOW_IMPLICIT)
            })

        }
        holder.termEditText.setText(wordsArrayList[position].term)
        holder.translationEditText.setText(wordsArrayList[position].translation)

        holder.translationSupport.text = ""
        holder.translationSupport.visibility = View.INVISIBLE
    }
}