package com.tann.vattana.lostandfoundapplication.Activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.tann.vattana.lostandfoundapplication.Class.LostItemInfo;
import com.tann.vattana.lostandfoundapplication.R;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class PostItemPage extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText titleEditText;
    private EditText foundAddressEditText;
    private EditText dateFoundEditText;
    private EditText founderContactEditText;
    private EditText pickUpAddressEditText;
    private EditText pickUpContactEditText;
    private EditText itemDescEditText;
    private ImageView lostItemImageView;
    private TextView imageErrorTextView;
    private DatabaseReference userRef;
    private DatabaseReference lostItemRef;
    private FirebaseDatabase database;
    private FirebaseUser firebaseUser;
    private LostItemInfo item;
    private String founderName;
    private StorageReference storageReference;
    private StorageReference mRef;
    private static final int galleryRequestCode = 101;
    private static final int cameraRequestCode = 102;
    private Uri imageUri;
    private Uri itemImageUri;
    private String imageFilePath;
    private Calendar calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener dateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_item_page);

        initializedUI();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post Item");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Read user name from Users Table
        userRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String firstName = dataSnapshot.child("firstName").getValue(String.class);
                String lastName = dataSnapshot.child("lastName").getValue(String.class);
                founderName = firstName + " " + lastName;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //DatePicker Listener
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateEditText();
            }
        };
    }

    //dateFound EditText onClick Handler
    public void dateFoundOnClicked(View v) {
        new DatePickerDialog(PostItemPage.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    //Set text in dateFound editText after choose date
    private void updateEditText() {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
        dateFoundEditText.setText(simpleDateFormat.format(calendar.getTime()));
    }

    //ImageView onClick handler
    public void imageViewOnClicked(View v) {
        chooseImageOptionDialog();
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
                storePostItemInfo();
            } else {
                showNoInternetDialog();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //Store item information to database
    private void storePostItemInfo() {
        item.setItemTitle(titleEditText.getText().toString());
        item.setFoundAddress(foundAddressEditText.getText().toString());
        item.setFoundDate(dateFoundEditText.getText().toString());
        item.setFounderContact(founderContactEditText.getText().toString());
        item.setPickUpAddress(pickUpAddressEditText.getText().toString());
        item.setPickUpContact(pickUpContactEditText.getText().toString());
        item.setItemDescription(itemDescEditText.getText().toString());
        item.setItemStatus(true);
        final String postDate = DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime());
        item.setFounder(founderName);
        item.setFounderID(firebaseUser.getUid());

        if (item.getItemTitle().isEmpty()) {
            titleEditText.setError("Item title is required.");
            titleEditText.requestFocus();
            return;
        }
        if (item.getFoundAddress().isEmpty()) {
            foundAddressEditText.setError("Found address is required.");
            foundAddressEditText.requestFocus();
            return;
        }
        if (item.getFoundDate().isEmpty()) {
            dateFoundEditText.setError("Found date is required.");
            return;
        }
        if (item.getFounderContact().isEmpty()) {
            founderContactEditText.setError("Founder contact is required.");
            founderContactEditText.requestFocus();
            return;
        }
        if (item.getPickUpAddress().isEmpty()) {
            item.setPickUpAddress("N/A");
        }
        if (item.getPickUpContact().isEmpty()) {
            item.setPickUpContact("N/A");
        }
        if (item.getItemDescription().isEmpty()) {
            item.setItemDescription("N/A");
        }

        //Store image to firebase storage
        //And save info to firebase database
        mRef = storageReference.child("lostItemPic/" + item.getItemTitle() + firebaseUser.getUid() + ".jpg");
        if (itemImageUri == null) {
            imageErrorTextView.setVisibility(View.VISIBLE);
            return;
        } else {
            mRef.putFile(itemImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            item.setItemImageUrl(uri.toString());
                            final DatabaseReference newPost = lostItemRef.push();
                            String itemID = newPost.getKey();
                            item = new LostItemInfo(item.getItemTitle(), item.getFoundAddress(), item.getFoundDate(), postDate, item.getFounderContact(), item.getItemDescription(),
                                    item.getFounder(), item.isItemStatus(), item.getPickUpAddress(), item.getPickUpContact(), item.getFounderID(), item.getItemImageUrl(), itemID);
                            newPost.setValue(item);
                            Log.d("Url", item.getItemImageUrl());
                        }
                    });
                }
            });

            //Delete image from temporary folder
            removeImage(imageUri);
            startActivity(new Intent(getApplicationContext(), HomePage.class));
            finish();
        }

    }

    //Alert dialog for user to choose option
    private void chooseImageOptionDialog() {
        Log.d("Dialog", "Show");
        final CharSequence[] cameraOpt = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PostItemPage.this);
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

    //Show no internet alert dialog
    private void showNoInternetDialog () {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == cameraRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takeImageFromCamera();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //Create temporary image file
    private File createImageFile() throws IOException {
        String imageFileName = "Item" + firebaseUser.getUid();
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
                itemImageUri = FileProvider.getUriForFile(this, "com.vattana.android.provider", imageFile);
                imageUri = itemImageUri;
                takeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, itemImageUri);
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
                    itemImageUri = Uri.fromFile(imageFile);
                    Picasso.with(getApplicationContext())
                            .load(itemImageUri)
                            .into(lostItemImageView);
                }

            } else if (requestCode == galleryRequestCode) {
                Log.d("Gallery", "Gallery Open");
                itemImageUri = data.getData();
                Picasso.with(getApplicationContext())
                        .load(itemImageUri)
                        .into(lostItemImageView);
            }
        } else if (resultCode == RESULT_CANCELED) {
            removeImage(itemImageUri);
            Log.d("Uri", itemImageUri.toString());
        }
    }

    //Remove image from file
    private void removeImage(Uri uri) {
        ContentResolver content = getContentResolver();
        if (uri != null) {
            content.delete(uri, null, null);
        }
    }

    //Initialized variable
    private void initializedUI() {
        toolbar = (Toolbar) findViewById(R.id.actionBar);
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        foundAddressEditText = (EditText) findViewById(R.id.foundAddressEditText);
        dateFoundEditText = (EditText) findViewById(R.id.dateFoundEditText);
        founderContactEditText = (EditText) findViewById(R.id.founderContactEditText);
        pickUpAddressEditText = (EditText) findViewById(R.id.pickUpAddressEditText);
        pickUpContactEditText = (EditText) findViewById(R.id.pickUpContactEditText);
        itemDescEditText = (EditText) findViewById(R.id.descriptionEditText);
        lostItemImageView = (ImageView) findViewById(R.id.postItemImage);
        imageErrorTextView = (TextView) findViewById(R.id.imageErrorTextView);
        item = new LostItemInfo();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
        lostItemRef = database.getReference("Lost Items");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
    }
}
