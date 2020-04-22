package com.langamy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.main.R
import com.langamy.base.classes.StudySet
import java.util.*
import kotlin.collections.ArrayList

class StudySetsAdapter(var itemsList: ArrayList<StudySet>,
                       var context: Context?,
                       val callback: Callback) :
        RecyclerView.Adapter<StudySetsAdapter.StudySetsHolder>(), Filterable {

    var filterListResult = ArrayList<StudySet>()

    init {
        filterListResult = itemsList
    }

    inner class StudySetsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var title: TextView = itemView.findViewById(R.id.studyset_title)
        var amountOfWords: TextView = itemView.findViewById(R.id.amount_of_words)
        var delete: ImageButton = itemView.findViewById(R.id.delete_studyset_BTN)

        fun bind(item: StudySet) {

            title.text = item.name
            amountOfWords.text = context?.getString(R.string.amount_of_words, item.amount_of_words.toString())

            delete.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    callback.onDeleteClicked(filterListResult[adapterPosition], adapterPosition)
            }

            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    callback.onItemClicked(filterListResult[adapterPosition])
            }
        }

    }

    interface Callback {
        fun onDeleteClicked(item: StudySet, itemId:Int)
        fun onItemClicked(item: StudySet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StudySetsHolder(LayoutInflater.from(context).inflate(R.layout.study_set_item, parent, false))

    override fun getItemCount() = filterListResult.size

    override fun onBindViewHolder(holder: StudySetsHolder, position: Int) {
        holder.bind(filterListResult[position])
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charString: CharSequence?): FilterResults {
                val charSearch = charString.toString()
                if (charSearch.isEmpty())
                    filterListResult = itemsList
                else {
                    val resultList = ArrayList<StudySet>()
                    for (row in itemsList) {
                        if (row.name!!.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)))
                        resultList.add(row)
                    }
                    filterListResult = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterListResult
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                filterListResult = filterResults?.values as ArrayList<StudySet>
                notifyDataSetChanged()
            }

        }
    }

}