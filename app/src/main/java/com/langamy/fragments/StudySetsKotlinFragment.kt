package com.langamy.fragments

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.main.R
import com.google.android.gms.ads.AdRequest
import com.google.android.material.card.MaterialCardView
import com.langamy.activities.*
import com.langamy.adapters.StudySetsAdapter
import com.langamy.base.classes.BaseVariables
import com.langamy.base.classes.StudySet
import com.langamy.base.kotlin.ScopedFragment
import com.langamy.viewmodel.StudySetsViewModel
import com.langamy.viewmodel.StudySetsViewModelFactory
import kotlinx.android.synthetic.main.custom_progress_bar.*
import kotlinx.android.synthetic.main.fragment_study_sets.*
import kotlinx.coroutines.launch
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.listener.DismissListener
import me.toptas.fancyshowcase.listener.OnViewInflateListener
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class StudySetsKotlinFragment : ScopedFragment(), KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory by instance<StudySetsViewModelFactory>()
    private lateinit var viewModel: StudySetsViewModel

    var searchView: SearchView? = null
    lateinit var items: ArrayList<StudySet>

    lateinit var mAdapter: StudySetsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_study_sets, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory).get(StudySetsViewModel::class.java)
        items = arrayListOf()

        randomDictation.setOnClickListener {
            val intent = Intent(context!!, SpecificDictationActivity::class.java)
            intent.putExtra(BaseVariables.RANDOM_DICTATION_MESSAGE, true)
            startActivity(intent)
        }

        recent_dictations.setOnClickListener {
            startActivity(Intent(context!!, UserDoneDictationsActivity::class.java))
        }

        dictations.setOnClickListener {
            startActivity(Intent(context!!, MyDictationsActivity::class.java))
        }

        search_dictation_btn.setOnClickListener {
            val intent = Intent(context, SpecificDictationActivity::class.java)
            try {
                val code: Int = search_dictation_ET.text.toString().trim { it <= ' ' }.toInt()
                intent.putExtra(BaseVariables.DICTATION_CODE_MESSAGE, code)
                startActivity(intent)
            } catch (e: NumberFormatException) {
                Toast.makeText(context, getString(R.string.code_must_be_numeric), Toast.LENGTH_SHORT).show()
            }
        }

        mAdapter = StudySetsAdapter(items, context, object : StudySetsAdapter.Callback {
            override fun onDeleteClicked(item: StudySet, itemId: Int) {
                showDeleteAlertDialog(item, itemId)
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

    private fun bindUI() = launch {

        viewModel.connectivity.observe(activity!!, Observer {
            if (it.isConnected) {
                initAd()
            }
        })

        val studySetsList = viewModel.studySets.await()

        studySetsList.observe(activity!!, Observer {
            if (it == null) return@Observer

            updateStudySetsList(it)
            progressBar.visibility = View.GONE
        })
    }

    private fun deleteStudySetById(id: Int) = launch {
        viewModel.deleteStudySetById(id)
    }

    private fun initAd() {
        banner_ad.loadAd(AdRequest.Builder().build())
    }

    private fun updateActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Home"
    }

    private fun updateStudySetsList(studySetsList: List<StudySet>) {
        items = studySetsList as ArrayList<StudySet>
        mAdapter.filterListResult = studySetsList
        mAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_help_item, menu)
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
        if (id == R.id.action_search) {
            return true
        } else if (id == R.id.help) {
            playHelp()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun playHelp() {
        BaseVariables.hideKeyboard(activity)
        val fq = FancyShowCaseQueue()
        val helpBtn = FancyShowCaseView.Builder(activity!!)
                .customView(R.layout.fancyshowcase_with_image, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setImage(view, getString(R.string.fancy_help_btn),
                                fq, context!!.getDrawable(R.drawable.ic_help_white_30dp))
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build()
        val myDictations = FancyShowCaseView.Builder(activity!!)
                .focusOn(dictations)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view,
                                getString(R.string.fancy_my_dictations),
                                fq)
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build()
        val randomDictations = FancyShowCaseView.Builder(activity!!)
                .focusOn(randomDictation)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view,
                                getString(R.string.fancy_random_dictations),
                                fq)
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build()
        val doneDictations = FancyShowCaseView.Builder(activity!!)
                .focusOn(recent_dictations)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view,
                                getString(R.string.fancy_done_dictations),
                                fq)
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build()
        val activity = activity as MainActivity?
        val navigation = activity!!.navigationView
        val tabAt = navigation.getTabAt(1)
        val tabView: View = tabAt!!.view
        val viewPager = activity.viewPager
        val createStudySet = FancyShowCaseView.Builder(getActivity()!!)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_create_studyset), fq)
                        view.findViewById<View>(R.id.skip).visibility = View.GONE
                    }
                })
                .focusOn(tabView)
                .backgroundColor(Color.parseColor("#E621618C"))
                .dismissListener(object : DismissListener {
                    override fun onDismiss(id: String?) {
                        viewPager.currentItem = 1
                    }

                    override fun onSkipped(id: String?) {}
                })
                .build()
        fq.add(helpBtn)
                .add(myDictations)
                .add(randomDictations)
                .add(doneDictations)
                .add(createStudySet)
        val handler = Handler()
        handler.postDelayed({ fq.show() }, 200)
    }

    private fun showDeleteAlertDialog(studySet: StudySet, itemId:Int) {
        val layoutInflater = layoutInflater
        val alertLayout = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        val delete_BTN = alertLayout.findViewById<Button>(R.id.delete_item_BTN)
        val cancel_BTN = alertLayout.findViewById<Button>(R.id.cancel_action)
        val studySetName_TV = alertLayout.findViewById<TextView>(R.id.name_of_deleting_item_TV)
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setCancelable(true)
        alertDialog.setView(alertLayout)
        val dialog = alertDialog.create()
        
        studySetName_TV.text = studySet.name
        
        cancel_BTN.setOnClickListener { dialog.dismiss() }
        
        delete_BTN.setOnClickListener {
            deleteStudySetById(studySet.id)
            items.remove(studySet)
            mAdapter.notifyItemRemoved(itemId)
            dialog.dismiss()
        }
        dialog.show()
    }

}
