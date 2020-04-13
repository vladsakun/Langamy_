package com.langamy.activities

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.main.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.langamy.api.LangamyAPI
import com.langamy.base.classes.BaseVariables
import com.langamy.base.classes.StudySet
import com.langamy.base.classes.TranslationResponse
import com.langamy.base.classes.Word
import com.langamy.base.kotlin.ScopedActivity
import com.langamy.database.StudySetsBaseHelper
import com.langamy.viewmodel.EditStudySetViewModel
import com.langamy.viewmodel.EditStudySetViewModelFactory
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.custom_progress_bar.*
import kotlinx.android.synthetic.main.fragment_create_study_set.*
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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class EditStudySetActivity : ScopedActivity(), KodeinAware {

    override val kodein by closestKodein()

    private var mStudySet: StudySet? = null
    var retrofit = BaseVariables.retrofit
    var mLangamyAPI = retrofit.create(LangamyAPI::class.java)
    private var languageToTranslate = "ru"
    private var languageFromTranslate = "en"
    private var autoTranslate = false
    private var sendEditRequest = false
    private var studySetId = 0

    private lateinit var mResultEt: EditText
    private lateinit var mTitleEt: EditText
    private val tts: TextToSpeech? = null
    private lateinit var mScanDocumentBtn: Button
    private lateinit var mCommitWordsBtn: Button
    private lateinit var mAddWordBtn: FloatingActionButton
    private lateinit var mWordsLinearLayout: LinearLayout
    private lateinit var mResultCardView: LinearLayout
    private lateinit var autoTranslateSwitch: Switch
    private lateinit var wordScrollView: ScrollView

    private var wordsInflater: LayoutInflater? = null
    private var wordsForSuggestions: HashMap<String, ArrayList<String>>? = null
    private var mDatabase: SQLiteDatabase? = null
    lateinit var cameraPermission: Array<String>
    lateinit var storagePermission: Array<String>

    var image_uri: Uri? = null

    private val viewModelFactory by instance<EditStudySetViewModelFactory>()
    private lateinit var viewModel: EditStudySetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_study_set)
        try {
            val studySet = intent.getSerializableExtra(BaseVariables.STUDY_SET_MESSAGE) as StudySet
            mStudySet = studySet
            studySetId = studySet.id
        } catch (e: NullPointerException) {
            mStudySet = null
        }
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditStudySetViewModel::class.java)

        //camera permission
        cameraPermission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        mResultEt = resultEt
        mTitleEt = title_edittext
        mScanDocumentBtn = findViewById(R.id.scan_document_btn)
        mAddWordBtn = findViewById(R.id.add_word_btn)
        mCommitWordsBtn = findViewById(R.id.commit_words_btn)
        mResultCardView = findViewById(R.id.result_LL)
        mWordsLinearLayout = findViewById(R.id.main_linearlayout)
        autoTranslateSwitch = findViewById(R.id.auto_translate_switch)
        wordScrollView = findViewById(R.id.word_scrollview)
        mDatabase = StudySetsBaseHelper(this).writableDatabase
        wordsInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        progressBar.visibility = View.GONE

        //Inflater for adding words
        val wordsInflater = LayoutInflater.from(this)

        // адаптер
        val baseVariables = BaseVariables()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, baseVariables.languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val languageFromSpinner = findViewById<Spinner>(R.id.language_form_spinner)
        val languageToSpinner = findViewById<Spinner>(R.id.language_to_spinner)
        languageFromSpinner.adapter = adapter
        languageToSpinner.adapter = adapter

        // заголовок
        languageFromSpinner.prompt = "Language from"
        languageToSpinner.prompt = "Language to"

        //Edit StudySet
        sendEditRequest = true
        mTitleEt.setText(mStudySet!!.name)
        val indexOfLanguageFrom = baseVariables.languageS_SHORT.indexOf(mStudySet!!.language_from)
        val indexOfLanguageTo = baseVariables.languageS_SHORT.indexOf(mStudySet!!.language_to)
        languageFromSpinner.setSelection(indexOfLanguageFrom)
        languageToSpinner.setSelection(indexOfLanguageTo)
        val mWordList = ArrayList<Word>()
        val words = mStudySet!!.words
        try {
            val jsonArray = JSONArray(words)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val word = Word(jsonObject.getString("term"),
                        jsonObject.getString("translation"))
                mWordList.add(word)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        for (i in mWordList.indices) {
            val wordView = createListWordItem()
            val term = wordView.findViewById<EditText>(R.id.term_TV)
            val translation = wordView.findViewById<EditText>(R.id.translation_TV)
            term.setText(mWordList[i].term)
            translation.setText(mWordList[i].translation)
            mWordsLinearLayout.addView(wordView)
        }
        mTitleEt.requestFocus()
        languageFromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View,
                                        position: Int, id: Long) {
                languageFromTranslate = baseVariables.languageS_SHORT[position]
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        languageToSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View,
                                        position: Int, id: Long) {
                languageToTranslate = baseVariables.languageS_SHORT[position]
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        mAddWordBtn.setOnClickListener(View.OnClickListener {
            val listWordView = createListWordItem()
            val term = listWordView.findViewById<EditText>(R.id.term_TV)
            mWordsLinearLayout.addView(listWordView, mWordsLinearLayout.getChildCount())
            BaseVariables.showKeyboard(term)
        })
        mScanDocumentBtn.setOnClickListener(View.OnClickListener {
            if (!BaseVariables.checkNetworkConnection(this@EditStudySetActivity)) {
                Toast.makeText(this@EditStudySetActivity, getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            showImageImportDialog()
            mResultCardView.setVisibility(View.VISIBLE)
        })
        mCommitWordsBtn.setOnClickListener(View.OnClickListener {
            if (mResultEt.getText().toString().length != 0) {
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                progressBar.setVisibility(View.VISIBLE)
                manyTranslate(mResultEt.getText().toString())
            } else {
                Toast.makeText(this@EditStudySetActivity, "Result is empty", Toast.LENGTH_SHORT).show()
            }
        })
        autoTranslateSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b -> autoTranslate = b })
        wordsForSuggestions = save()
    }

    private fun updateLocalStudySet(studySet: StudySet) = launch {
        viewModel.updateStudySet(studySet)
    }

    private fun manyTranslate(words: String) {
        val wordArrayList = ArrayList<Word>()
        val stringsToBeTranslated = words.replace("\\r?\\n".toRegex(), ";")
                .replace("\\[".toRegex(), "").replace("]".toRegex(), "")
        val terms = stringsToBeTranslated.split(";").toTypedArray()
        val postData = JSONObject()
        try {
            postData.put("words", stringsToBeTranslated)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val call = mLangamyAPI.translate(postData, languageFromTranslate, languageToTranslate, "many")
        call.enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(call: Call<TranslationResponse>, response: Response<TranslationResponse>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@EditStudySetActivity, response.code().toString(), Toast.LENGTH_SHORT).show()
                    return
                }
                val translations = response.body()!!.translation.toString().split(";").toTypedArray()
                Log.d("RESPONSE", response.body()!!.translation.toString())
                Log.d("Translations Length", translations.size.toString())
                Log.d("Term Length", terms.size.toString())
                for (i in terms.indices) {
                    wordArrayList.add(Word(terms[i], translations[i]))
                }
                for (i in wordArrayList.indices) {
                    val wordListItem = createListWordItem()
                    val term = wordListItem.findViewById<EditText>(R.id.term_TV)
                    val translation = wordListItem.findViewById<EditText>(R.id.translation_TV)
                    term.setText(wordArrayList[i].term)
                    translation.setText(wordArrayList[i].translation)
                    mWordsLinearLayout!!.addView(wordListItem, mWordsLinearLayout!!.childCount)
                }
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                progressBar!!.visibility = View.GONE
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                Toast.makeText(this@EditStudySetActivity, t.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun createListWordItem(): View {
        val wordView = wordsInflater!!.inflate(R.layout.list_words_item, null)
        val term = wordView.findViewById<AutoCompleteTextView>(R.id.term_TV)
        val translationSupport = wordView.findViewById<TextView>(R.id.translation_support)
        val translation = wordView.findViewById<EditText>(R.id.translation_TV)
        val removeBtn = wordView.findViewById<ImageButton>(R.id.remove_list_word_item_btn)
        removeBtn.setOnClickListener {
            val parent = removeBtn.parent.parent.parent as LinearLayout
            parent.removeView(removeBtn.parent.parent as View)
        }
        term.onFocusChangeListener = OnFocusChangeListener { view1, hasFocus ->
            if (hasFocus) {
                wordScrollView!!.post {
                    val parent = translation.parent.parent as CardView
                    val mainLinearLayout = parent.parent as LinearLayout
                    val mainRelativeLayout = mainLinearLayout.parent as RelativeLayout
                    val linearLayout = mainRelativeLayout.findViewById<LinearLayout>(R.id.result_LL)
                    wordScrollView!!.scrollTo(0, parent.bottom + linearLayout.height)
                }
            }
        }
        term.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim { it <= ' ' }.length == 1) {
                    val firstChar = Character.toLowerCase(s[0])
                    if (firstChar >= 'a' && firstChar <= 'z' || firstChar >= 'A' && firstChar <= 'Z') {
                        val words = wordsForSuggestions!!["$firstChar.txt"]!!
                        val wordsStringArr = words.toTypedArray()

                        // Создаем адаптер для автозаполнения элемента AutoCompleteTextView
                        val adapter = ArrayAdapter(this@EditStudySetActivity, R.layout.support_simple_spinner_dropdown_item,
                                wordsStringArr)
                        term.setAdapter(adapter)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        translation.onFocusChangeListener = OnFocusChangeListener { view1, hasFocus ->
            if (hasFocus) {
                Translate(term.text.toString().trim { it <= ' ' }, translationSupport)
                wordScrollView!!.post {
                    val parent = translation.parent.parent as CardView
                    val mainLinearLayout = parent.parent as LinearLayout
                    val mainRelativeLayout = mainLinearLayout.parent as RelativeLayout
                    val linearLayout = mainRelativeLayout.findViewById<LinearLayout>(R.id.result_LL)
                    wordScrollView!!.scrollTo(0, parent.bottom + linearLayout.height)
                }
            }
        }
        translationSupport.setOnClickListener { translation.setText(translationSupport.text.toString()) }
        return wordView
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_study_set, menu)
        return true
    }

    //handle actionbar item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.submitWords) {
            if (!BaseVariables.checkNetworkConnection(this)) {
                Toast.makeText(this@EditStudySetActivity, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                return false
            }
            if (mTitleEt!!.text.length == 0) {
                Toast.makeText(this, "Title is empty", Toast.LENGTH_SHORT).show()
                BaseVariables.showKeyboard(mTitleEt)
                return false
            }
            val wordList = JSONArray()
            try {
                for (i in 0 until mWordsLinearLayout!!.childCount) {
                    val layout = mWordsLinearLayout!!.getChildAt(i)
                    val currentWord = JSONObject()
                    val translation = layout.findViewById<EditText>(R.id.translation_TV)
                    val term = layout.findViewById<EditText>(R.id.term_TV)
                    if (!(translation.text.toString() == "" && term.text.toString() == "")) {
                        currentWord.put("term", term.text.toString().trim { it <= ' ' })
                        currentWord.put("translation", translation.text.toString().trim { it <= ' ' })
                        currentWord.put("firstStage", false)
                        currentWord.put("secondStage", false)
                        currentWord.put("thirdStage", false)
                        currentWord.put("forthStage", false)
                        wordList.put(currentWord)
                    }
                }
                if (wordList.length() < 4) {
                    Toast.makeText(this, "Study set must include 4 or more words", Toast.LENGTH_SHORT).show()
                } else {
                    updateStudySet(studySetId, wordList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateStudySet(id: Int, wordList: JSONArray) {
        val acct = GoogleSignIn.getLastSignedInAccount(this)

        val studySet = StudySet(acct!!.email, mTitleEt!!.text.toString(), wordList.toString(),
                languageToTranslate, languageFromTranslate, wordList.length())
        studySet.id = id
        val call = mLangamyAPI.patchStudySet(id, studySet)

        call.enqueue(object : Callback<StudySet> {
            override fun onResponse(call: Call<StudySet>, response: Response<StudySet>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@EditStudySetActivity, response.code().toString(), Toast.LENGTH_SHORT).show()
                    return
                }
                updateLocalStudySet(studySet)
                val intent = Intent(this@EditStudySetActivity, SpecificStudySetActivity::class.java)
                intent.putExtra(BaseVariables.STUDY_SET_ID_MESSAGE, id)
                startActivity(intent)
                finish()
            }

            override fun onFailure(call: Call<StudySet>, t: Throwable) {
                Toast.makeText(this@EditStudySetActivity, t.toString(), Toast.LENGTH_SHORT).show()
                return
            }
        })
    }

    private fun showImageImportDialog() {
        //items to display in dialog
        val items = arrayOf(" Camera", " Gallery")
        val dialog = AlertDialog.Builder(Objects.requireNonNull(this))
        //set title
        dialog.setTitle("Select Image")
        dialog.setItems(items) { dialogInterface, which ->
            if (which == 0) {
                //camera option clicked
                if (!checkCameraPermission()) {
                    //camera permission not allowed, request it
                    requestCameraPersmission()
                    if (checkCameraPermission()) {
                        pickCamera()
                    }
                } else {
                    //permission allowed, take picture
                    pickCamera()
                }
                //for OS marshmallow and above we need to ask runtime permission
                //for camera and storage
            }
            if (which == 1) {
                //gallery option clicked
                if (!checkStoragePermission()) {
                    //Storage permission not allowed, request it
                    requestStoragePersmission()
                    if (checkStoragePermission()) {
                        pickGallery()
                    }
                } else {
                    //permission allowed, take picture
                    pickGallery()
                }
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
        image_uri = Objects.requireNonNull(this).contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE)
    }

    private fun requestStoragePersmission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(this), storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(Objects.requireNonNull(this),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPersmission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(this), cameraPermission, CAMERA_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(Objects.requireNonNull(this),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    //handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> if (grantResults.size > 0) {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (cameraAccepted && writeStorageAccepted) {
                    pickCamera()
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQUEST_CODE -> if (grantResults.size > 0) {
                val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (writeStorageAccepted) {
                    pickGallery()
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle image result
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //got image from gallery now crop it
                CropImage.activity(data!!.data)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Objects.requireNonNull(this)) //enable image guidlines
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //got image from camera now crop it
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Objects.requireNonNull(this)) //enable image guidlines
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
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
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
                            mResultEt!!.append(words.toString())
                        }
                        .addOnFailureListener { e -> Toast.makeText(this@EditStudySetActivity, e.toString(), Toast.LENGTH_SHORT).show() }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //if there is ane error show if
                val error = result.error
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun Translate(stringToTranslate: String, translationSupport: TextView) {
        val postData = JSONObject()
        try {
            postData.put("words", stringToTranslate)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val call = mLangamyAPI.translate(postData, languageFromTranslate, languageToTranslate, "one")
        call.enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(call: Call<TranslationResponse>, response: Response<TranslationResponse>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@EditStudySetActivity, response.code().toString(), Toast.LENGTH_SHORT).show()
                    return
                }
                translationSupport.visibility = View.VISIBLE
                translationSupport.text = response.body()!!.translation
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                Toast.makeText(this@EditStudySetActivity, t.toString(), Toast.LENGTH_SHORT).show()
            }
        })
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
                        InputStreamReader(assets.open("english_words/$fileName")))

                // do reading, usually loop until end of file reading
                var mLine: String? = null
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

    companion object {
        private const val CAMERA_REQUEST_CODE = 200
        private const val STORAGE_REQUEST_CODE = 400
        private const val IMAGE_PICK_GALLERY_CODE = 1000
        private const val IMAGE_PICK_CAMERA_CODE = 1001
    }
}