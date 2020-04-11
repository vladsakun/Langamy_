package com.langamy.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bignerdranch.android.main.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.langamy.activities.GoogleSignInActivity;

import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private GoogleSignInClient mGoogleSignInClient;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        MaterialButton signOutBtn = view.findViewById(R.id.sign_out_btn);
        TextView detailTextView = view.findViewById(R.id.user_email_TV);
        CircleImageView accountImage = view.findViewById(R.id.account_image);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(Objects.requireNonNull(getContext()));
        if (acct != null) {
            String personEmail = acct.getEmail();
            Uri personPhoto = acct.getPhotoUrl();
            String personImage = Objects.requireNonNull(acct.getPhotoUrl()).toString();
            Glide.with(getActivity()).load(personImage).into(accountImage);
            detailTextView.setText(personEmail);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), personPhoto);
                accountImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        signOutBtn.setOnClickListener(view1 -> signOut());

        return view;
    }

    private void signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut();

        // Google sign out
        mGoogleSignInClient.signOut();
        startMainActivity();
    }

    private void startMainActivity() {
        Intent intent = new Intent(getContext(), GoogleSignInActivity.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).finish();
    }
}
