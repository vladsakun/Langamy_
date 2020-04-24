package com.langamy.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.main.R
import com.langamy.base.classes.Word

class WordsAdapter(
    val callback: Callback,
    context: Context,
    wordsArrayLists: ArrayList<Word>
) :
    RecyclerView.Adapter<WordsAdapter.WordsHolder>() {

    private val inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
        wordsArrayList = wordsArrayLists
    }

    companion object {
        lateinit var wordsArrayList: ArrayList<Word>
    }

    inner class WordsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var termEditText: EditText = itemView.findViewById(R.id.term_TV) as EditText
        var translationEditText: EditText = itemView.findViewById(R.id.translation_TV) as EditText

        var deleteBtn: ImageButton = itemView.findViewById(R.id.remove_list_word_item_btn) as ImageButton

        init {
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
                }

            })

            translationEditText.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    TODO("Not yet implemented")
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    TODO("Not yet implemented")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    wordsArrayList[adapterPosition].translation = translationEditText.text.toString()
                }

            })
        }

    }

    interface Callback {
        fun onDeleteClicked(item: Word, itemId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordsHolder {
        val view = inflater.inflate(R.layout.list_words_item, parent, false)
        return WordsHolder(view)
    }

    override fun getItemCount(): Int {
        return wordsArrayList.size
    }

    override fun onBindViewHolder(holder: WordsHolder, position: Int) {
        holder.termEditText.setText(wordsArrayList[position].term)
        holder.translationEditText.setText(wordsArrayList[position].translation)
        holder.deleteBtn.setOnClickListener {
            if (position != RecyclerView.NO_POSITION)
                callback.onDeleteClicked(wordsArrayList[position], position)
        }
    }
}