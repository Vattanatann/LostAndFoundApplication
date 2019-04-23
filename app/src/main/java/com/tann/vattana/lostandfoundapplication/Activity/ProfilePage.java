package com.tann.vattana.lostandfoundapplication.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.tann.vattana.lostandfoundapplication.Class.InternetConnectivityHandler;
import com.tann.vattana.lostandfoundapplication.Class.UserInfo;
import com.tann.vattana.lostandfoundapplication.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;


public class ProfilePage extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView profileImg;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView emailTextView;
    private TextView passwordTextView;
    private UserInfo user;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private String userPassword;
    private static final int galleryRequestCode = 101;
    private static final int cameraRequestCode = 102;
    private Uri imageUri;
    private Uri profileImageUri;
    private String profileImageUrl;
    private StorageReference storageReference;
    private StorageReference mRef;
    private String imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        initializedUI();
        setSupportActionBar(toolbar);
        nav_slideMenu();

        if (InternetConnectivityHandler.haveNetworkConnection(getApplicationContext())) {
            readFromDatabase();

            //Set name, email, profile image of user to header of Slide Menu
            View navHeader = navigationView.getHeaderView(0);
            final ImageView headerImg = navHeader.findViewById(R.id.headerImgView);
            final TextView headerName = navHeader.findViewById(R.id.headerNameTextView);
            final TextView headerEmail = navHeader.findViewById(R.id.headerEmailTextView);
            userRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserInfo user = dataSnapshot.getValue(UserInfo.class);
                    headerName.setText(user.getFirstName() + " " + user.getLastName());
                    headerEmail.setText(user.getEmailAddress());
                    Picasso.with(getApplicationContext())
                            .load(user.getProfileImageUrl())
                            .transform(new CropCircleTransformation())
                            .into(headerImg);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

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

    //onClick handle for Change Profile Pic
    public void changeProfileImgOnClicked(View v) {
        chooseImageOptionDialog();
    }

    //Dialog for user to choose option how to get picture
    private void chooseImageOptionDialog() {
        Log.d("Dialog", "Show");
        final CharSequence[] cameraOpt = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfilePage.this);
        builder.setTitle("Choose an option:");
        builder.setItems(cameraOpt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int opt) {
                if (cameraOpt[opt].equals("Take Photo")) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, cameraRequestCode);
                    } else {
                        takeImageFromCamera();
                    }
                } else if (cameraOpt[opt].equals("Choose from Gallery")) {
                    chooseImageFromGallery();
                } else if (cameraOpt[opt].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == cameraRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takeImageFromCamera();
            }
        }
    }

    //Create temporary image file
    private File createImageFile() throws IOException {
        String imageFileName = user.getId();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    //Function for letting user use their camera to take the image
    private void takeImageFromCamera() {
        Log.d("Camera", "Camera Open");
        Intent takeImageIntent = new Intent();
        takeImageIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takeImageIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageFile != null) {
                profileImageUri = FileProvider.getUriForFile(this, "com.vattana.android.provider", imageFile);
                imageUri = profileImageUri;
                takeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, profileImageUri);
                startActivityForResult(takeImageIntent, cameraRequestCode);
            }
        }
    }

    //Function for letting user to choose image from gallery
    private void chooseImageFromGallery() {
        Log.d("Gallery", "Gallery Open");
        Intent profileImageCaptureIntent = new Intent();
        profileImageCaptureIntent.setType("image/*");
        profileImageCaptureIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(profileImageCaptureIntent, galleryRequestCode);
    }

    //Override method for when user choose image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == cameraRequestCode) {
                Log.d("Camera", "Camera Open");
                if (imageFilePath != null) {
                    File imageFile = new File(imageFilePath);
                    profileImageUri = Uri.fromFile(imageFile);
                    Picasso.with(getApplicationContext())
                            .load(profileImageUri)
                            .transform(new CropCircleTransformation())
                            .into(profileImg);
                    uploadImageToFirebaseStorage();
                    removeImage(imageUri);
                }

            } else if (requestCode == galleryRequestCode) {
                Log.d("Gallery", "Gallery Open");
                profileImageUri = data.getData();
                Picasso.with(getApplicationContext())
                        .load(profileImageUri)
                        .transform(new CropCircleTransformation())
                        .into(profileImg);

                uploadImageToFirebaseStorage();
            }
        } else if (resultCode == RESULT_CANCELED) {
            removeImage(profileImageUri);
            Log.d("Uri", profileImageUri.toString());
        }
    }

    //Remove image from file
    private void removeImage(Uri uri) {
        ContentResolver content = getContentResolver();
        if (uri != null) {
            content.delete(uri, null, null);
        }
    }

    //Function for uploading image to FirebaseStorage
    //And save the image url to Firebase Database
    private void uploadImageToFirebaseStorage() {
        mRef = storageReference.child("profilepics/" + firebaseUser.getUid() + ".jpg");
        mRef.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profileImageUrl = uri.toString();
                        myRef.child(firebaseUser.getUid()).child("profileImageUrl").setValue(profileImageUrl);
                        Log.d("URL", profileImageUrl);
                    }
                });
            }
        });
    }

    //onClick handler for Edit Password
    public void editPasswordOnClicked(View v) {
        String email = emailTextView.getText().toString();

        Bundle passwordData = new Bundle();
        passwordData.putString("Password", userPassword);
        passwordData.putString("Email", email);

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), EditPasswordPage.class);
        intent.putExtras(passwordData);
        startActivity(intent);
    }

    //Function to read user data from database
    private void readFromDatabase() {
        user.setId(firebaseUser.getUid());
        Log.d("ID", user.getId());

        myRef.child(user.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String passwordDot = "";
                UserInfo user = dataSnapshot.getValue(UserInfo.class);
                firstNameTextView.setText(user.getFirstName());
                lastNameTextView.setText(user.getLastName());
                emailTextView.setText(user.getEmailAddress());
                userPassword = user.getPassword();
                for (int i = 0; i < user.getPassword().length(); i++) {
                    passwordDot += "*";
                }
                passwordTextView.setText(passwordDot);
                if (!user.getProfileImageUrl().equals(null)) {
                    Picasso.with(getApplicationContext())
                            .load(user.getProfileImageUrl())
                            .transform(new CropCircleTransformation())
                            .into(profileImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Manage the edit button on the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.editBtn) {
            String firstName = firstNameTextView.getText().toString();
            String lastName = lastNameTextView.getText().toString();
            String email = emailTextView.getText().toString();
            String password = userPassword;

            Bundle profileData = new Bundle();
            profileData.putString("Firstname", firstName);
            profileData.putString("Lastname", lastName);
            profileData.putString("Email", email);
            profileData.putString("Password", password);

            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), EditProfilePage.class);
            intent.putExtras(profileData);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //Initialized the variable
    private void initializedUI() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.slideMenu);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        toolbar = (Toolbar) findViewById(R.id.actionBarProfile);
        profileImg = (ImageView) findViewById(R.id.profileImage);
        firstNameTextView = (TextView) findViewById(R.id.firstNameTextView);
        lastNameTextView = (TextView) findViewById(R.id.lastNameTextView);
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        passwordTextView = (TextView) findViewById(R.id.passwordTextView);
        user = new UserInfo();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        userRef = database.getReference("Users");
    }

    //Create actionbar and add slide menu with slidemenu button
    private void nav_slideMenu() {

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.homePage:
                        Log.d("Home", "Select");
                        startActivity(new Intent(getApplicationContext(), HomePage.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.historyPage:
                        Log.d("History", "Select");
                        startActivity(new Intent(getApplicationContext(), HistoryPage.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.profilePage:
                        Log.d("Profile", "Select");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.logOut:
                        mAuth.signOut();
                        Intent intent = new Intent(getApplicationContext(), LoginPage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setTitle("Profile");
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
    }
}
