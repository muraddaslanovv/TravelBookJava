package com.muradaslanov.travelbookjava.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.muradaslanov.travelbookjava.R;
import com.muradaslanov.travelbookjava.adapter.PlaceAdaptor;
import com.muradaslanov.travelbookjava.databinding.ActivityMapsBinding;
import com.muradaslanov.travelbookjava.model.Place;
import com.muradaslanov.travelbookjava.room.Database;
import com.muradaslanov.travelbookjava.room.PlaceDao;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String>  permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    Database database;
    PlaceDao placeDao;
    Double chosenLat;
    Double chosenLong;
    Place selectedPlace;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = MapsActivity.this.getSharedPreferences("com.muradaslanov.travelbookjava",MODE_PRIVATE);
        info = false;

        database = Room.databaseBuilder(getApplicationContext(),
                com.muradaslanov.travelbookjava.room.Database.class, "Places")
//                .allowMainThreadQueries()
                .build();

        placeDao = database.placeDao();

        chosenLat = 0.0;
        chosenLong = 0.0;

        binding.saveButton.setEnabled(false);

        registerLauncher();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(MapsActivity.this);

        Intent intent = getIntent();
        String intentStringInfo = intent.getStringExtra("info");

        if(intentStringInfo.equals("new")){

            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);


// casting
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    info = sharedPreferences.getBoolean("info",false);

                    if(!info){
                        LatLng userLoc = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc,15));
                        sharedPreferences.edit().putBoolean("info",true).apply();
                    }


                }

            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            request permission
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.getRoot(),"Permission Required for Maps",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Request permission
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();

                }  else{
                    //            Request Permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastlocation != null){
                    LatLng lastUserLocation = new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }

                mMap.setMyLocationEnabled(true);
            }

//        It won't work this way, first gotta check for the permission
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

//        Latitude Longitude


        }else{

            mMap.clear();

            selectedPlace = (Place) intent.getSerializableExtra("place");

            LatLng latLng = new LatLng(selectedPlace.latitude,selectedPlace.longitude);

            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));


            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

            binding.placeText.setText(selectedPlace.name);
            binding.placeText.setEnabled(false);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

        }


    }

    private void registerLauncher(){
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
//                    granted
                    if(ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                        Location lastlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(lastlocation != null){
                            LatLng lastUserLocation = new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                    }
                    }
                }else{
//                    denied
                    Toast.makeText(MapsActivity.this, "Permission Required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        chosenLong = latLng.longitude;
        chosenLat = latLng.latitude;

        binding.saveButton.setEnabled(true);

    }

    public void save(View view){
        Place place = new Place(binding.placeText.getText().toString(),chosenLat,chosenLong);

//        threading -> Main(UI), Default (CPU intensive), IO (Network and Database)

//        placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe();

//        Disposable

        if(binding.placeText.getText().toString().replaceAll("\\s+", " ").trim().matches("")){
            Toast toast = Toast.makeText(MapsActivity.this, "Enter a name for the place", Toast.LENGTH_LONG);
            toast.show();
        }else {
            compositeDisposable.add(placeDao.insert(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse)
            );
        }

    }

    private void handleResponse(){
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent );
    }

    public void delete(View view){

        if(selectedPlace != null){
            compositeDisposable.add(placeDao.delete(selectedPlace)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse)
            );
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}