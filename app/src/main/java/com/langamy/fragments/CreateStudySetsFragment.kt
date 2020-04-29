package com.langamy.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.main.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.langamy.activities.SpecificStudySetActivity
import com.langamy.adapters.WordsAdapter
import com.langamy.api.LangamyAPI
import com.langamy.base.classes.BaseVariables
import com.langamy.base.classes.StudySet
import com.langamy.base.classes.TranslationResponse
import com.langamy.base.classes.Word
import com.langamy.base.kotlin.ScopedFragment
import com.langamy.viewmodel.edit.EditStudySetViewModel
import com.langamy.viewmodel.edit.EditStudySetViewModelFactory
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.custom_progress_bar.*
import kotlinx.android.synthetic.main.fragment_create_study_set.*
import kotlinx.coroutines.launch
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import me.toptas.fancyshowcase.listener.OnViewInflateListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class CreateStudySetsFragment : ScopedFragment(), RewardedVideoAdListener, KodeinAware {

    override val kodein by closestKodein()

    private val viewModelFactory by instance<EditStudySetViewModelFactory>()
    private lateinit var viewModel: EditStudySetViewModel

    var retrofit: Retrofit = BaseVariables.retrofit
    var mLangamyAPI: LangamyAPI = retrofit.create(LangamyAPI::class.java)
    private var languageToTranslate = "ru"
    private var languageFromTranslate = "en"

    private lateinit var mResultEt: EditText
    private lateinit var mTitleEt: EditText
    private lateinit var mScanDocumentBtn: Button
    private lateinit var mCommitWordsBtn: Button
    private lateinit var mAddWordBtn: FloatingActionButton
    private lateinit var mWordsLinearLayout: LinearLayout
    private lateinit var mResultCardView: LinearLayout
    private lateinit var wordScrollView: ScrollView
    lateinit var wordsModelArrayList: ArrayList<Word>
    lateinit var mAdapter: WordsAdapter
    private var recyclerView: RecyclerView? = null

    private var wordsForSuggestions: HashMap<String, ArrayList<String>>? = null

    private lateinit var mAd: RewardedVideoAd

    private var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(context, BaseVariables.REWARDED_VIDEO_TEST)
        mAd = MobileAds.getRewardedVideoAdInstance(context)
        mAd.rewardedVideoAdListener = this

        // Confirm this fragment has menu items.
        setHasOptionsMenu(true)
        wordsForSuggestions = save()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditStudySetViewModel::class.java)

        wordsModelArrayList = ArrayList()

        mAdapter = WordsAdapter(object : WordsAdapter.Callback {
            override fun onDeleteClicked(translationSupport: TextView, itemId: Int, view: View) {
                view.clearFocus()
                wordsModelArrayList.removeAt(itemId)
                mAdapter.notifyItemRemoved(itemId)
                mAdapter.notifyItemRangeChanged(itemId, wordsModelArrayList.size)
            }

        }, requireContext(), wordsModelArrayList, wordScrollView, result_LL, save(), true)

        recyclerView!!.isNestedScrollingEnabled = false
        recyclerView!!.adapter = mAdapter
        recyclerView!!.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        for (i in 0..1) {
            wordsModelArrayList.add(Word("", ""))
        }

        mAddWordBtn.setOnClickListener {
            mAdapter.edit = false
            wordsModelArrayList.add(Word("", "", false, false, false, false))
            mAdapter.notifyItemInserted(mAdapter.itemCount)
        }
    }

    private fun insertLocalStudySet(studySet: StudySet) = launch {
        viewModel.insertStudySet(studySet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_study_set, container, false)
        mResultEt = view.findViewById(R.id.resultEt)
        mTitleEt = view.findViewById(R.id.title_edittext)
        mScanDocumentBtn = view.findViewById(R.id.scan_document_btn)
        mAddWordBtn = view.findViewById(R.id.add_word_btn)
        mCommitWordsBtn = view.findViewById(R.id.commit_words_btn)
        mWordsLinearLayout = view.findViewById(R.id.main_linearlayout)
        wordScrollView = view.findViewById(R.id.word_scrollview)
        mResultCardView = view.findViewById(R.id.result_LL)
        recyclerView = view.findViewById(R.id.words_recyclerview)

        val languageFromSpinner = view.findViewById<Spinner>(R.id.language_form_spinner)
        val languageToSpinner = view.findViewById<Spinner>(R.id.language_to_spinner)

        loadRewardedVideoAd()

        // адаптер
        val baseVariables = BaseVariables()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, baseVariables.languages)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageFromSpinner.adapter = adapter
        languageToSpinner.adapter = adapter

        // заголовок
        languageFromSpinner.prompt = ("Language from")
        languageToSpinner.prompt = ("Language to")

        // выделяем элемент
        languageFromSpinner.setSelection(0)
        languageToSpinner.setSelection(1)

        languageFromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View,
                                        position: Int, id: Long) {
                languageFromTranslate = baseVariables.languageS_SHORT[position]
                mAdapter.languageFromTranslate = baseVariables.languageS_SHORT[position]
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        languageToSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View,
                                        position: Int, id: Long) {
                languageToTranslate = baseVariables.languageS_SHORT[position]
                mAdapter.languageToTranslate = baseVariables.languageS_SHORT[position]
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }

        mScanDocumentBtn.setOnClickListener(View.OnClickListener {
            if (!BaseVariables.checkNetworkConnection(context) || !mAd.isLoaded) {
                Toast.makeText(context, getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (mAd.isLoaded) {
                mAd.show()
            }
        })

        mCommitWordsBtn.setOnClickListener {
            if (mResultEt.text.toString().isNotEmpty()) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                progressBar.visibility = View.VISIBLE
                manyTranslate(mResultEt.text.toString())
            } else {
                Toast.makeText(context, "Result is empty", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun manyTranslate(words: String) {
        val stringsToBeTranslated = words.replace("\\r?\\n".toRegex(), ";")
                .replace("\\[".toRegex(), "").replace("]".toRegex(), "")
        val terms = stringsToBeTranslated.split(";").toTypedArray()
        val postData = JSONObject()
        try {
            postData.put("words", stringsToBeTranslated)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mAdapter.edit = true

        val call = mLangamyAPI.translate(postData, languageFromTranslate, languageToTranslate, "many")
        call.enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(call: Call<TranslationResponse>, response: Response<TranslationResponse>) {
                if (!response.isSuccessful) {
                    Toast.makeText(context, response.code().toString(), Toast.LENGTH_SHORT).show()
                    return
                }
                val translations = response.body()!!.translation.toString().split(";").toTypedArray()

                for (i in terms.indices) {
                    try {
                        wordsModelArrayList.add(Word(terms[i], translations[i]))
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        wordsModelArrayList.add(Word("", ""))
                    }

                }

                mAdapter.notifyDataSetChanged()
                activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                progressBar!!.visibility = View.GONE
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
            }
        })
        mAdapter.edit = false

    }

    //actionbar menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_create_study_set, menu)
        inflater.inflate(R.menu.menu_help_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    //handle actionbar item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.submitWords) {
            if (!BaseVariables.checkNetworkConnection(context)) {
                Toast.makeText(context, getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show()
                return false
            }
            if (mTitleEt.text.isEmpty()) {
                Toast.makeText(context, getString(R.string.title_is_empty), Toast.LENGTH_SHORT).show()
                BaseVariables.showKeyboard(mTitleEt)
                return false
            }
            val wordList = JSONArray()
            try {
                for (i in WordsAdapter.wordsArrayList) {
                    val currentWord = JSONObject()
                    val translation = i.translation
                    val term = i.term
                    if (!(translation == "" && term == "")) {
                        currentWord.put("term", term.trim { it <= ' ' })
                        currentWord.put("translation", translation.trim { it <= ' ' })
                        currentWord.put("firstStage", i.isFirstStage)
                        currentWord.put("secondStage", i.isSecondStage)
                        currentWord.put("thirdStage", i.isThirdStage)
                        currentWord.put("forthStage", i.isForthStage)
                        wordList.put(currentWord)
                    }
                }
                if (wordList.length() < 4) {
                    Toast.makeText(context, getString(R.string.min_words_in_studyset), Toast.LENGTH_SHORT).show()
                } else {
                    createStudySet(mTitleEt.text.toString(), wordList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (id == R.id.help) {
            playHelp()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toEmptyStudySetData() {
        mTitleEt.text = null

        mResultEt.text = null
        mResultCardView.visibility = View.GONE

        wordsModelArrayList.clear()
        mAdapter.notifyDataSetChanged()
    }

    private fun showImageImportDialog() {
        //items to display in dialog
        val items = arrayOf(" Camera", " Gallery")
        val dialog = AlertDialog.Builder(requireContext())
        //set title
        dialog.setTitle("Select Image")
        dialog.setItems(items) { _, which ->
            if (which == 0) {
                //camera option clicked
                requestCameraPermission()
            }
            if (which == 1) {
                //gallery option clicked
                requestStoragePermission()
            }
        }
        dialog.create().show() //show dialog
    }

    private fun pickGallery() {
        //intent to pick image from gallery
        val intent = Intent(Intent.ACTION_PICK)
        //set intent type to image
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun pickCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "NewPic") //title of the picture
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image To text") // description
        image_uri = Objects.requireNonNull(requireActivity()).contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (checkStoragePermission()) {
            pickGallery()
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        }
    }

    private fun requestCameraPermission() {
        if (checkCameraPermission() && checkStoragePermission()) {

            // has the permission.
            pickCamera()
        } else {

            // request the permission
            requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_REQUEST_CODE)
        }
    }

    //handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (cameraAccepted && writeStorageAccepted) {
                    pickCamera()
                } else {
                    Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (writeStorageAccepted) {
                    pickGallery()
                } else {
                    Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle image result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //got image from gallery now crop it
                CropImage.activity(data!!.data)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Objects.requireNonNull(requireActivity())) //enable image guidlines
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //got image from camera now crop it
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Objects.requireNonNull(requireActivity())) //enable image guidlines
            }
        }
        //get cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = Objects.requireNonNull(result).uri //get image uri
                //get drawable bitmap for text recognition
                var bitmap: Bitmap? = null
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, resultUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val image = FirebaseVisionImage.fromBitmap(bitmap!!)
                val textRecognizer = FirebaseVision.getInstance()
                        .onDeviceTextRecognizer
                textRecognizer.processImage(image)
                        .addOnSuccessListener { result ->
                            val resultText = result.text
                            val allWords = ArrayList<String>()
                            val words = StringBuilder()
                            for (block in result.textBlocks) {
                                for (line in block.lines) {
                                    var editLine = line.text
                                    if (editLine.contains("/")) {
                                        val index = editLine.indexOf("/")
                                        val transcription = editLine.substring(index)
                                        editLine = editLine.replace(transcription, "")
                                        allWords.add(editLine)
                                    } else {
                                        allWords.add(editLine)
                                    }
                                }
                            }
                            for (word in allWords) {
                                words.append(word).append("\n")
                            }
                            //set text to edit text
                            mResultEt.setText(words.toString())
                        }
                        .addOnFailureListener { e -> Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show() }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //if there is ane error show if
                val error = result.error
                Toast.makeText(activity, "" + error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createStudySet(name: String, wordList: JSONArray) {
        if (wordList.length() < 2) {
            return
        }
        val acct = GoogleSignIn.getLastSignedInAccount(context)
        val studySet = StudySet(acct!!.email, name, wordList.toString(), languageToTranslate, languageFromTranslate, wordList.length())
        val call = mLangamyAPI.createStudySet(studySet)

        call.enqueue(object : Callback<StudySet> {
            override fun onResponse(call: Call<StudySet>, response: Response<StudySet>) {
                if (!response.isSuccessful) {
                    Toast.makeText(context, response.code().toString(), Toast.LENGTH_SHORT).show()
                    return
                }
                insertLocalStudySet(response.body()!!)
                val intent = Intent(context, SpecificStudySetActivity::class.java)
                intent.putExtra(BaseVariables.STUDY_SET_ID_MESSAGE, response.body()!!.id)
                startActivity(intent)
            }

            override fun onFailure(call: Call<StudySet>, t: Throwable) {
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
            }
        })
        toEmptyStudySetData()
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(isVisible)
        val sf = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val help = sf.getBoolean(BaseVariables.HELP_CREATE_STUDYSETS_FRAGMENT, true)
        if (help) {
            playHelp()
            val editor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit()
            editor.putBoolean(BaseVariables.HELP_CREATE_STUDYSETS_FRAGMENT, false)
            editor.apply()
        }
    }

    private fun playHelp() {
        BaseVariables.hideKeyboard(activity)
        val queue = FancyShowCaseQueue()
        val titleFocus = FancyShowCaseView.Builder(requireActivity())
                .focusOn(mTitleEt)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_title), queue)
                    }
                })
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .backgroundColor(Color.parseColor("#E621618C"))
                .build()
        val fromLang = FancyShowCaseView.Builder(requireActivity())
                .focusOn(language_form_spinner!!)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_from_lang), queue)
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build()
        val toLang = FancyShowCaseView.Builder(requireActivity())
                .focusOn(language_to_spinner!!)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_to_lang), queue)
                    }
                })
                .backgroundColor(Color.parseColor("#E621618C"))
                .build()
        val scan = FancyShowCaseView.Builder(requireActivity())
                .focusOn(mScanDocumentBtn)
                .customView(R.layout.custom_layout_for_fancyshowcase, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                        BaseVariables.setCustomFancyCaseView(view, getString(R.string.fancy_scan_btn), queue)
                    }
                })
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .backgroundColor(Color.parseColor("#E621618C"))
                .build()
        queue.add(titleFocus)
        queue.add(fromLang)
        queue.add(toLang)
        queue.add(scan)
        val handler = Handler()
        handler.postDelayed({ queue.show() }, 200)
    }

    private fun save(): HashMap<String, ArrayList<String>> {
        val allWords = HashMap<String, ArrayList<String>>()
        val fileNames = arrayOf(
                "a.txt", "b.txt", "c.txt", "d.txt", "e.txt", "f.txt", "g.txt", "h.txt",
                "i.txt", "j.txt", "k.txt", "l.txt", "m.txt", "n.txt",
                "o.txt", "p.txt", "q.txt", "r.txt", "s.txt", "t.txt",
                "u.txt", "v.txt", "w.txt", "x.txt", "y.txt", "z.txt")
        var reader: BufferedReader? = null
        for (fileName in fileNames) {
            try {
                reader = BufferedReader(
                        InputStreamReader(requireContext().assets.open("english_words/$fileName")))

                // do reading, usually loop until end of file reading
                var mLine: String?
                val words = ArrayList<String>()
                while (reader.readLine().also { mLine = it } != null) {
                    words.add(mLine!!)
                }
                allWords[fileName] = words
            } catch (e: IOException) {
                //log the exception
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (ignored: IOException) {
                    }
                }
            }
        }
        return allWords
    }

    override fun onRewardedVideoAdLoaded() {
        Log.d("VIDEO", "An ad has loaded")
        mScanDocumentBtn.isEnabled = true
    }

    override fun onRewardedVideoAdOpened() {
        Log.d("VIDEO", "An ad has opened")
    }

    override fun onRewardedVideoStarted() {
        Log.d("VIDEO", "An ad has started")
    }

    override fun onRewardedVideoAdClosed() {
        Log.d("VIDEO", "An ad has closed")
        loadRewardedVideoAd()
    }

    override fun onRewarded(rewardItem: RewardItem) {
        showImageImportDialog()
        mResultCardView.visibility = View.VISIBLE
        loadRewardedVideoAd()
    }

    override fun onRewardedVideoAdLeftApplication() {
        Log.d("VIDEO", "An ad has caused focus to leave")
    }

    override fun onRewardedVideoAdFailedToLoad(i: Int) {
        Log.d("VIDEO", "An ad has failed to load")
    }

    override fun onRewardedVideoCompleted() {
        Log.d("VIDEO", "An ad has completed")
    }

    private fun loadRewardedVideoAd() {
        mAd.loadAd(BaseVariables.REWARDED_VIDEO_TEST, AdRequest.Builder().build())
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 200
        private const val STORAGE_REQUEST_CODE = 400
        private const val IMAGE_PICK_GALLERY_CODE = 1000
        private const val IMAGE_PICK_CAMERA_CODE = 1001
    }
}