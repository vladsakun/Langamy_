package com.langamy.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.langamy.activities.MyDictationsActivity;
import com.langamy.base.classes.BaseVariables;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private GoogleSignInClient mGoogleSignInClient;
    private TextView mDetailTextView;
    private MaterialButton mSignOutBtn, mDictationsBtn;
    private CircleImageView accountImage;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mSignOutBtn = view.findViewById(R.id.sign_out_btn);
        mDetailTextView = view.findViewById(R.id.user_email_TV);
        mDictationsBtn = view.findViewById(R.id.my_dictations_BTN);
        accountImage = view.findViewById(R.id.account_image);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);


        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
            String personImage = Objects.requireNonNull(acct.getPhotoUrl()).toString();
            Glide.with(getActivity()).load(personImage).into(accountImage);
            mDetailTextView.setText(personEmail);
            accountImage.setImageURI(personPhoto);
        }


        mAuth = FirebaseAuth.getInstance();

        mSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        mDictationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!BaseVariables.checkNetworkConnection(getContext())){
                    Toast.makeText(getContext(), getString(R.string.you_need_an_internet_connection), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), MyDictationsActivity.class);
                startActivity(intent);
            }
        });

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
        getActivity().finish();
    }

}
