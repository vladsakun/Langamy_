package com.langamy.provider

import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn

class UserProviderImpl(context: Context) : UserProvider {

    private val appContext = context.applicationContext

    override fun getUserEmail(): String {
        return GoogleSignIn.getLastSignedInAccount(appContext)?.email.toString()
    }

    override fun getUserPhotoUrl(): Uri? {
        return GoogleSignIn.getLastSignedInAccount(appContext)?.photoUrl
    }


}