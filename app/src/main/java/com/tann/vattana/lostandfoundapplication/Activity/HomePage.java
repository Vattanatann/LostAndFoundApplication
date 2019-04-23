package com.tann.vattana.lostandfoundapplication.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;
import com.tann.vattana.lostandfoundapplication.Class.HomePageCustomListView;
import com.tann.vattana.lostandfoundapplication.Class.InternetConnectivityHandler;
import com.tann.vattana.lostandfoundapplication.Class.LostItemInfo;
import com.tann.vattana.lostandfoundapplication.Class.UserInfo;
import com.tann.vattana.lostandfoundapplication.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class HomePage extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ListView listView;
    private ArrayList<LostItemInfo> itemArrayList;
    private FirebaseDatabase database;
    private DatabaseReference lostItemRef;
    private DatabaseReference userRef;
    private HomePageCustomListView customListView;
    private LostItemInfo lostItem;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private SwipeRefreshLayout pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        initializedUI();
        nav_slideMenu();

        if (InternetConnectivityHandler.haveNetworkConnection(getApplicationContext())) {
            //Custom Listview for browsing lost item
            //And handle ListView click event
            readDataFromFirebaseDatabase();
            customListView = new HomePageCustomListView(HomePage.this, itemArrayList);
            listView.setAdapter(customListView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    lostItem = itemArrayList.get(position);
                    Bundle itemData = new Bundle();
                    itemData.putString("Title", lostItem.getItemTitle());
                    itemData.putString("PostDate", lostItem.getPostDate());
                    itemData.putString("Founder", lostItem.getFounder());
                    itemData.putString("FoundAddress", lostItem.getFoundAddress());
                    itemData.putString("DateFound", lostItem.getFoundDate());
                    itemData.putString("FounderContact", lostItem.getFounderContact());
                    itemData.putString("PickUpContact", lostItem.getPickUpContact());
                    itemData.putString("PickUpAddress", lostItem.getPickUpAddress());
                    itemData.putString("ItemDesc", lostItem.getItemDescription());
                    itemData.putString("ImageUrl", lostItem.getItemImageUrl());

                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), ItemDetailPage.class);
                    intent.putExtras(itemData);
                    startActivity(intent);
                }
            });

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

            //Pull to refresh listView
            pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    readDataFromFirebaseDatabase();
                    pullToRefresh.setRefreshing(false);
                }
            });

        } else {
            showNoInternetDialog();
        }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Read data from Firebase database based on itemStatus for populating ListView
    private void readDataFromFirebaseDatabase () {
        lostItemRef.child("Lost Items").orderByChild("itemStatus").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemArrayList.clear();
                Log.d("List", "List Clear");
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d("Firebase","Reading");
                    String title = ds.child("itemTitle").getValue(String.class);
                    String postdate = ds.child("postDate").getValue(String.class);
                    String founder = ds.child("founder").getValue(String.class);
                    String foundAddress = ds.child("foundAddress").getValue(String.class);
                    String dateFound = ds.child("foundDate").getValue(String.class);
                    String founderContact = ds.child("founderContact").getValue(String.class);
                    String pickUpContact = ds.child("pickUpContact").getValue(String.class);
                    String pickUpAddress = ds.child("pickUpAddress").getValue(String.class);
                    String itemDesc = ds.child("itemDescription").getValue(String.class);
                    boolean itemStatus = ds.child("itemStatus").getValue(boolean.class);
                    String founderID = ds.child("founderID").getValue(String.class);
                    String imageUrl = ds.child("itemImageUrl").getValue(String.class);
                    String itemID = ds.child("itemID").getValue(String.class);

                    lostItem = new LostItemInfo(title, foundAddress, dateFound, postdate, founderContact, itemDesc, founder, itemStatus,
                            pickUpAddress, pickUpContact, founderID, imageUrl, itemID);
                    itemArrayList.add(lostItem);
                }
                Log.d("List", "New List");
                customListView.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d("Read", "Data receive");
    }

    //Floating button onClick
    public void addNewItemBtnClicked (View v) {
        startActivity(new Intent(getApplicationContext(), PostItemPage.class));
    }

    private void initializedUI () {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.slideMenu);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        listView = (ListView) findViewById(android.R.id.list);
        itemArrayList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        lostItemRef = database.getReference();
        lostItem = new LostItemInfo();
        mAuth = FirebaseAuth.getInstance();
        userRef = database.getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.pullRefresh);
    }

    //Create actionbar and add slide slidemenu with slidemenu button
    private void nav_slideMenu() {
        toolbar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(toolbar);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.homePage:
                        Log.d("Home", "Select");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.historyPage:
                        Log.d("History", "Select");
                        startActivity(new Intent(getApplicationContext(), HistoryPage.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.profilePage:
                        Log.d("Profile", "Select");
                        startActivity(new Intent(getApplicationContext(), ProfilePage.class));
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
        getSupportActionBar().setTitle("Home");
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
    }
}
