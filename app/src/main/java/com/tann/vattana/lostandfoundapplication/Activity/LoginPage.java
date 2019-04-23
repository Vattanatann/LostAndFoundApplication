package com.tann.vattana.lostandfoundapplication.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tann.vattana.lostandfoundapplication.Class.InternetConnectivityHandler;
import com.tann.vattana.lostandfoundapplication.R;
import com.tann.vattana.lostandfoundapplication.Class.UserInfo;

public class LoginPage extends AppCompatActivity {

    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private UserInfo user;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        initializeUI();

    }

    private void loginUser() {
        user.setEmailAddress(emailAddressEditText.getText().toString().trim());
        user.setPassword(passwordEditText.getText().toString().trim());

        //Validating input field
        if (user.getEmailAddress().isEmpty()) {
            emailAddressEditText.setError("Email address is required.");
            emailAddressEditText.requestFocus();
            return;
        }

        if (user.getPassword().isEmpty()) {
            passwordEditText.setError("Password is required.");
            passwordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //User sign in with email and password by Firebase
        mAuth.signInWithEmailAndPassword(user.getEmailAddress(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            finish();

                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            user.setId(firebaseUser.getUid());

                            Intent intent = new Intent(getApplicationContext(), HomePage.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            errorTextView.setText("The email address or password you enter is incorrect.");
                        }
                    }
                });

    }

    //Check if the user already login
    //Then get the id of the login user
    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(getApplicationContext(), HomePage.class));
        }
    }

    //Button Sign In onClick
    public void signInBtnClicked(View v) {
        //Check if device connect to internet
        if (InternetConnectivityHandler.haveNetworkConnection(getApplicationContext())) {
            loginUser();
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

    //Button Create New Account onClick
    public void createNewAccBtnClicked(View v) {
        Intent intent = new Intent(LoginPage.this, SignUpPage.class);
        startActivity(intent);
    }

    //Declaring variables
    private void initializeUI() {
        emailAddressEditText = (EditText) findViewById(R.id.emailAddressEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        user = new UserInfo();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        errorTextView = (TextView) findViewById(R.id.errorTextView);
    }
}
