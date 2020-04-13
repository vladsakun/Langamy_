package com.langamy.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.main.R
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.langamy.activities.MainActivity
import com.langamy.base.kotlin.ScopedFragment
import com.langamy.viewmodel.ProfileViewModel
import com.langamy.viewmodel.ProfileViewModelFactory
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class ProfileKotlinFragment : ScopedFragment(), KodeinAware {

    override val kodein by closestKodein()

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val viewModelFactory by instance<ProfileViewModelFactory>()
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(activity!!, gso)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)

        val personPhoto = viewModel.getUserPhotoUrl()
        user_email_TV.text = viewModel.getUserEmail()

        Glide.with(this).load(personPhoto).into(account_image);

        sign_out_btn.setOnClickListener { signOut() }
    }

    private fun startMainActivity() {
        startActivity(Intent(context, MainActivity::class.java))
        activity?.finish()
    }

    private fun deleteAllStudySets() = launch {
        viewModel.deleteAllStudySets()
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()

        // Google sign out
        mGoogleSignInClient.signOut()
        deleteAllStudySets()
        startMainActivity()
    }


}
