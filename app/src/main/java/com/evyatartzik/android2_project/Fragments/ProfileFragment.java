package com.evyatartzik.android2_project.Fragments;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.evyatartzik.android2_project.Adapters.GlobalApplication;
import com.evyatartzik.android2_project.Models.User;
import com.evyatartzik.android2_project.Models.UserPreferences;
import com.evyatartzik.android2_project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class ProfileFragment extends Fragment {

    /*Layout Element*/
    TextView textViewUserName;
    TextView textViewUserLocation;
    ImageView imageViewProfilePicture;

    /*Firebase*/
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference mStorageRef;
    private FirebaseDatabase database;
    DatabaseReference databaseUsers;
    private DatabaseReference ref;
    ArrayList<User> userArrayList;
    private View rootView;

    public ProfileFragment() {
    }

    private void updateUI() {
        textViewUserName.setText(currentUser.getEmail());
        textViewUserLocation.setText("Test");//Get current location


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = GlobalApplication.getAppContext();
        rootView =  inflater.inflate(R.layout.profile_fragment, container, false);

        initLayoutById();
        userArrayList = new ArrayList<>();
        initFirebase(); //Update ui elements
        initUserDatabaseToList();
        User dbUser = findInArrayList(currentUser.getEmail());//need to test
        if(dbUser!=null)
        {
            Toast.makeText(context, dbUser.getEmail(), Toast.LENGTH_LONG).show();
        }

        return rootView;


    }

    private User findInArrayList(String email) {
        for (User tempUser:userArrayList)
        {
          if(tempUser.getEmail().equals(email))
              return tempUser;
        }
        return null;
    }

    private void initFirebase() {
        database = FirebaseDatabase.getInstance();

        ref = database.getReference("database");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseUsers =ref.child("users");

        updateUI();

    }

    private void initLayoutById() {

        textViewUserName = rootView.findViewById(R.id.user_name);
        textViewUserLocation = rootView.findViewById(R.id.user_address);
        imageViewProfilePicture = rootView.findViewById(R.id.backdrop);
    }

    public void initUserDatabaseToList()
    {
        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User userPreference =postSnapshot.getValue(User.class);
                    userArrayList.add(userPreference);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

}