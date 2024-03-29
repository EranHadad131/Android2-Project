package com.evyatartzik.android2_project.Fragments;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evyatartzik.android2_project.Adapters.ActivityRvAdapter;
import com.evyatartzik.android2_project.Models.Activity;
import com.evyatartzik.android2_project.Models.UserPreferences;
import com.evyatartzik.android2_project.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener, ActivityRvAdapter.ObjectListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    static Location current_location;
    private ImageView loctionButton;
    private View rootView;
    private ArrayList<UserPreferences> PreferencesList;
    private ArrayList<Activity> ActivitysList;
    private ArrayList<Activity> SearchActivityList;
    private ActivityRvAdapter activityRvAdapter;
    private RecyclerView SearchRv;




    boolean isAdvancedSearchOpen = false;
    private EditText freeTextTv;
    ChipGroup chipGroup;
    LinearLayout advancedLayout;
    ImageView searchBg;



    public SearchFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.search_fragment, container, false);
        SearchActivityList = new ArrayList<>();
        activityRvAdapter = new ActivityRvAdapter(getActivity(), SearchActivityList);
        SearchRv = rootView.findViewById(R.id.search_results_rv);
        SearchRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        SearchRv.setAdapter(activityRvAdapter);
        activityRvAdapter.setListener(this);


        loctionButton = rootView.findViewById(R.id.location_btn);

        advancedLayout = rootView.findViewById(R.id.advanced_layout);
        searchBg = rootView.findViewById(R.id.search_bg);
        final TextView rangeTv = rootView.findViewById(R.id.range_tv);

        freeTextTv = rootView.findViewById(R.id.free_text_tv);

        chipGroup = rootView.findViewById(R.id.chips_container);

        freeTextTv.setOnEditorActionListener(this);





        getAllActivitysTypeList_And_Add_choices();

        //getAllActivitys();

        ImageView advancedSearchBtn = rootView.findViewById(R.id.advanced_search_btn);
        advancedSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdvancedSearchOpen = !isAdvancedSearchOpen;
                advancedLayout.setVisibility(isAdvancedSearchOpen ? View.VISIBLE : View.GONE);
            }
        });

        loctionButton.setOnClickListener(this);


        return rootView;

    }



    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10.0f);
    }


    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                current_location = locationResult.getLastLocation();

            }
        };

    }


    public void checkLoctionAndUpdateText(){

    // check permissions
    try {
        Dexter.withActivity(getActivity())
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            buildLocationRequest();
                            buildLocationCallBack();

                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);

                            }
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    freeTextTv.setText(getLocationNameByLocation(location));

                                }
                            });

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Toast.makeText(getActivity(), "Request Denied", Toast.LENGTH_SHORT).show();
                    }
                }).check();

    }

    catch(Exception ex){

        Log.d("check_prm","lala");
    }
}


    String getLocationNameByLocation(Location location) {
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String country = addressList.get(0).getCountryName();
            String city = addressList.get(0).getLocality();
            return city + ", " + country;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    private void Search(){

        isAdvancedSearchOpen = false;
        advancedLayout.setVisibility(View.GONE);

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(freeTextTv.getWindowToken(), 0);

        ArrayList<String> activitys = new ArrayList<>();

        for (int i = 0; i < chipGroup.getChildCount(); ++i) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                activitys.add((String) chip.getText());
            }

        }


        // Add here search logic


    }


    public void getAllActivitysTypeList_And_Add_choices(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("database");
        DatabaseReference preferencesRef = ref.child("preferences");

        PreferencesList = new ArrayList<>();



            preferencesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        UserPreferences preferencesRef = postSnapshot.getValue(UserPreferences.class);
                        PreferencesList.add(preferencesRef);
                    }
                    for(UserPreferences user_Preference : PreferencesList){
                        Chip chip = new Chip(getActivity(), null , R.style.Widget_MaterialComponents_Chip_Filter);
                        chip.setText(user_Preference.getName());
                        chip.setClickable(true);
                        chip.setCheckable(true);
                        chip.setChipBackgroundColorResource(R.color.chip);
                        chipGroup.addView(chip);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
    }


    public void getAllActivitys(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("database");
        DatabaseReference activitysRef = ref.child("activity");

        ActivitysList = new ArrayList<>();



        activitysRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Activity activitysRef = postSnapshot.getValue(Activity.class);
                    ActivitysList.add(activitysRef);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.location_btn:
                checkLoctionAndUpdateText();

        }

    }


    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_SEARCH) {
            Search();
            return true;
        }
        return false;
    }

    /* open display activity fragment */
    @Override
    public void onObjectClicked(int pos, View view) {

    }
}