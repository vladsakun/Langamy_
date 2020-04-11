package com.langamy.fragments

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.main.R
import com.langamy.activities.SpecificStudySetActivity
import com.langamy.adapters.StudySetsAdapter
import com.langamy.base.classes.BaseVariables
import com.langamy.base.classes.StudySet
import com.langamy.base.kotlin.ScopedFragment
import com.langamy.viewmodel.StudySetsViewModel
import com.langamy.viewmodel.StudySetsViewModelFactory
import kotlinx.android.synthetic.main.custom_progress_bar.*
import kotlinx.android.synthetic.main.fragment_study_sets.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class StudySetsKotlinFragment : ScopedFragment(), KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory by instance<StudySetsViewModelFactory>()
    private lateinit var viewModel:StudySetsViewModel

    var searchView: SearchView? = null

    lateinit var mAdapter: StudySetsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_study_sets, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory).get(StudySetsViewModel::class.java)
        val items = arrayListOf<StudySet>()

        mAdapter = StudySetsAdapter(items, context, object : StudySetsAdapter.Callback {
            override fun onDeleteClicked(item: StudySet) {
                TODO("Not yet implemented")
            }

            override fun onItemClicked(item: StudySet) {
                val intent = Intent(context, SpecificStudySetActivity::class.java)
                intent.putExtra(BaseVariables.STUDY_SET_ID_MESSAGE, item.id)
                startActivity(intent)
            }
        })

        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = mAdapter
        updateActionBar()
        bindUI()
    }

    private fun bindUI() = launch{
        val studySetsList = viewModel.studySets.await()

        studySetsList.observe(activity!!, Observer {
            if(it == null) return@Observer

            updateStudySetsList(it)
            progressBar.visibility = View.GONE
        })
    }

    private fun updateActionBar(){
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Study Sets"
    }

    private fun updateStudySetsList(studySetsList: List<StudySet>){
        mAdapter.filterListResult = studySetsList
        mAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_study_sets_fragment, menu)
        val searchManager = context!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
        searchView!!.maxWidth = Int.MAX_VALUE

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mAdapter.filter.filter(newText)
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_search) {
            true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

}
