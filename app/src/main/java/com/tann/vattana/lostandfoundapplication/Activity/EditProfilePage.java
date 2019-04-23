package com.tann.vattana.lostandfoundapplication.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tann.vattana.lostandfoundapplication.Class.InternetConnectivityHandler;
import com.tann.vattana.lostandfoundapplication.R;

public class EditProfilePage extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference historyRef;
    private AuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_page);

        intializedUI();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle profileData = getIntent().getExtras();
        firstName = profileData.getString("Firstname");
        lastName = profileData.getString("Lastname");
        email = profileData.getString("Email");
        password = profileData.getString("Password");

        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);
        emailEditText.setText(email);

        Log.d("Firstname", firstName);
        Log.d("Lastname", lastName);
        Log.d("Email", email);
        Log.d("Password", password);
    }

    private void editProfileInfo() {

        firstName = firstNameEditText.getText().toString();
        lastName = lastNameEditText.getText().toString();
        final String emailChange = emailEditText.getText().toString();
        final String name = firstName + " " + lastName;

        if (firstName.isEmpty()) {
            firstNameEditText.setError("First name is required.");
            firstNameEditText.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            lastNameEditText.setError("Last name is required");
            lastNameEditText.requestFocus();
            return;
        }

        if (emailChange.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailChange).matches()) {
            emailEditText.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            return;
        }

        String id = firebaseUser.getUid();
        Log.d("ID", id);

        //Update data in the firebase database
        myRef.child(id).child("firstName").setValue(firstName);
        myRef.child(id).child("lastName").setValue(lastName);
        myRef.child(id).child("emailAddress").setValue(emailChange);

        //Update email address in Firebase Authentication
        credential = EmailAuthProvider.getCredential(email, password);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("Re-authenticate", "User re-authenticate");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.updateEmail(emailChange).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
            }
        });

        startActivity(new Intent(getApplicationContext(), ProfilePage.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.saveBtn) {
            if (InternetConnectivityHandler.haveNetworkConnection(getApplicationContext())) {
                editProfileInfo();
            } else {
                showNoInternetDialog();
            }
        }
        return super.onOptionsItemSelected(item);
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

    private void intializedUI() {
        toolbar = (Toolbar) findViewById(R.id.actionBar);
        firstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
        emailEditText = (EditText) findViewById(R.id.emailAddressEditText);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        historyRef = database.getReference();
    }
}
