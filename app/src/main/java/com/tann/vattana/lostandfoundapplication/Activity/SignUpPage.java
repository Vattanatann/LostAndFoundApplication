package com.tann.vattana.lostandfoundapplication.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tann.vattana.lostandfoundapplication.Class.InternetConnectivityHandler;
import com.tann.vattana.lostandfoundapplication.R;
import com.tann.vattana.lostandfoundapplication.Class.UserInfo;

public class SignUpPage extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private EditText confirmPwdEditText;
    private TextView wrongPasswordErrorTextView;
    private ProgressBar progressBar;
    private UserInfo user;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String defaultImgUrl = "https://firebasestorage.googleapis.com/v0/b/lost-and-found-application.appspot.com/o/profilepics%2Fdefault_profile_image.png?alt=media&token=79247603-e42e-4cee-9792-55be6d3de838";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        initializeUI();

    }

    private void registerUser() {
        user.setFirstName(firstNameEditText.getText().toString());
        user.setLastName(lastNameEditText.getText().toString());
        user.setEmailAddress(emailAddressEditText.getText().toString());
        user.setPassword(passwordEditText.getText().toString());
        user.setProfileImageUrl(defaultImgUrl);
        String confirmPwd = confirmPwdEditText.getText().toString();

        //Validating input field
        if (user.getFirstName().isEmpty()) {
            firstNameEditText.setError("First name is required.");
            firstNameEditText.requestFocus();
            return;
        }

        if (user.getLastName().isEmpty()) {
            lastNameEditText.setError("Last name is required.");
            lastNameEditText.requestFocus();
            return;
        }

        if (user.getEmailAddress().isEmpty()) {
            emailAddressEditText.setError("Email address is required.");
            emailAddressEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(user.getEmailAddress()).matches()) {
            emailAddressEditText.setError("Please enter a valid email address.");
            emailAddressEditText.requestFocus();
            return;
        }

        if (user.getPassword().isEmpty()) {
            passwordEditText.setError("Password is required.");
            passwordEditText.requestFocus();
            return;
        }

        if (user.getPassword().length() < 6) {
            passwordEditText.setError("The minimum length of password is 6 characters.");
            passwordEditText.requestFocus();
            return;
        }

        if (confirmPwd.isEmpty()) {
            confirmPwdEditText.setError("Please confirm the password.");
            confirmPwdEditText.requestFocus();
            return;
        }

        if (!confirmPwd.equals(user.getPassword())) {
            wrongPasswordErrorTextView.setText("Incorrect Password. Please try again.");
            passwordEditText.setText("");
            confirmPwdEditText.setText("");
            passwordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //Authentication Firebase with email and password
        mAuth.createUserWithEmailAndPassword(user.getEmailAddress(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            finish();
                            storeUserInfo();

                            Intent intent = new Intent(getApplicationContext(), HomePage.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                emailAddressEditText.setError("Email is already register.");
                                emailAddressEditText.requestFocus();
                            }
                        }
                    }
                });

    }

    //Store user info to Firebase Realtime Database
    private void storeUserInfo() {
        user = new UserInfo(user.getFirstName(), user.getLastName(), user.getEmailAddress(), user.getPassword(), user.getProfileImageUrl());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user.setId(firebaseUser.getUid());
        myRef.child(user.getId()).setValue(user);
        Log.d("Database: ", "Store success");
    }

    //Button Create Account onClick
    public void createAccBtnClick(View v) {
        if (InternetConnectivityHandler.haveNetworkConnection(getApplicationContext())) {
            registerUser();
        } else {
            showNoInternetDialog();
        }
    }

    //Show no internet alert dialog
    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection")
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setCancelable(false)
                .setMessage("No Internet connection. Make sure that Wi-Fi or mobile data is turned on, then try again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //Declaring variables
    private void initializeUI() {
        firstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
        emailAddressEditText = (EditText) findViewById(R.id.emailAddressEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        confirmPwdEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
        wrongPasswordErrorTextView = (TextView) findViewById(R.id.wrongPasswordErrorEditText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        user = new UserInfo();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
    }
}
