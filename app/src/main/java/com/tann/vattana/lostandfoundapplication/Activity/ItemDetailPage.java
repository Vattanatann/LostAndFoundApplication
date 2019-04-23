package com.tann.vattana.lostandfoundapplication.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tann.vattana.lostandfoundapplication.R;

public class ItemDetailPage extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView itemImage;
    private TextView itemTitleTextView;
    private TextView foundAddressTextView;
    private TextView dateFoundTextView;
    private TextView founderTextView;
    private TextView founderContactTextView;
    private TextView postDateTextView;
    private TextView descriptionTextView;
    private TextView pickUpContactTextView;
    private TextView pickUpAddressTextView;
    private String phoneNum;
    private Intent callIntent;
    private static final int callRequestCode = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail_page);

        initializedUI();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Item Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle itemData = getIntent().getExtras();
        String Title = itemData.getString("Title");
        String PostDate = itemData.getString("PostDate");
        String Founder = itemData.getString("Founder");
        String FoundAddress = itemData.getString("FoundAddress");
        String DateFound = itemData.getString("DateFound");
        String FounderContact = itemData.getString("FounderContact");
        String PickUpContact = itemData.getString("PickUpContact");
        String PickUpAddress = itemData.getString("PickUpAddress");
        String ItemDesc = itemData.getString("ItemDesc");
        String ImageUrl = itemData.getString("ImageUrl");

        itemTitleTextView.setText(Title);
        postDateTextView.setText(PostDate);
        founderTextView.setText(Founder);
        foundAddressTextView.setText(FoundAddress);
        dateFoundTextView.setText(DateFound);
        founderContactTextView.setText(FounderContact);
        pickUpContactTextView.setText(PickUpContact);
        pickUpAddressTextView.setText(PickUpAddress);
        descriptionTextView.setText(ItemDesc);
        Picasso.with(getApplicationContext())
                .load(ImageUrl)
                .into(itemImage);
    }

    //Call Button OnClick Handler
    public void callBtnOnClicked(View v) {
        phoneNum = founderContactTextView.getText().toString();
        callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CALL_PHONE}, callRequestCode);
        } else {
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == callRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent);
            }
        }
    }

    //SMS Button OnClick Handler
    public void smsBtnOnClicked (View v) {
        phoneNum = founderContactTextView.getText().toString();
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.putExtra("address", phoneNum);
        smsIntent.setType("vnd.android-dir/mms-sms");
        startActivity(smsIntent);
    }

    private void initializedUI () {
        toolbar = (Toolbar) findViewById(R.id.actionBar);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        itemTitleTextView = (TextView) findViewById(R.id.itemTitleTextView);
        foundAddressTextView = (TextView) findViewById(R.id.foundAddressTextView);
        dateFoundTextView = (TextView) findViewById(R.id.dateFoundTextView);
        founderTextView = (TextView) findViewById(R.id.founderTextView);
        founderContactTextView = (TextView) findViewById(R.id.founderContactTextView);
        postDateTextView = (TextView) findViewById(R.id.postDateTextView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        pickUpAddressTextView = (TextView) findViewById(R.id.pickUpAddressTextView);
        pickUpContactTextView = (TextView) findViewById(R.id.pickUpContactTextView);
    }
}
