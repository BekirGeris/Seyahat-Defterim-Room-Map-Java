package com.begers.seyahatdefterim.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
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

import com.begers.seyahatdefterim.R;
import com.begers.seyahatdefterim.databinding.ActivityMapsBinding;
import com.begers.seyahatdefterim.model.Place;
import com.begers.seyahatdefterim.roomdb.PlaceDao;
import com.begers.seyahatdefterim.roomdb.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ActivityResultLauncher<String> permissionLauncher;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SharedPreferences sharedPreferences;
    boolean flag;

    PlaceDatabase db;
    PlaceDao placeDao;

    Double selectedLatitude;
    Double selectedLongitude;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();

        sharedPreferences = MapsActivity.this.getSharedPreferences("com.begers.seyahatdefterim", MODE_PRIVATE);

        db = Room.databaseBuilder(MapsActivity.this, PlaceDatabase.class, "Places").build();
        placeDao = db.placeDao();

        binding.saveButton.setEnabled(false);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) { //harita hazır ise
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")){
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);


            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) { //konum değiştiğinde bu metot çalışır.

                    flag = sharedPreferences.getBoolean("flag", true);

                    if (flag) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("flag", false).apply();
                    }

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //izin verilmedi istenmeli
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    //izin ayrıntılı bir şekilde isteniyor
                    Snackbar.make(binding.getRoot(), "izin gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //tekrardan izin verilmedi isteniyor
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                }else {
                    //izin verilmemiş isteniyor
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }else {
                //izin verilmiş
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, locationListener);
                //son bilinen konum alınıyor
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }

                mMap.setMyLocationEnabled(true);
            }

        }else {

            mMap.clear();

            place = (Place) intent.getSerializableExtra("place");

            LatLng latLng = new LatLng(place.latitude, place.langitude);

            mMap.addMarker(new MarkerOptions().position(latLng).title(place.name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            binding.placeNameText.setText(place.name);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

        }

        binding.saveButton.setEnabled(false);
    }

    private void registerLauncher(){
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) { //iin verilmiş veya verilmemiş olacaktır.
                if (result){
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        //izin verildi
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, locationListener);

                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null) {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                        }
                    }
                }else {
                    Toast.makeText(MapsActivity.this, "izine ihtiyaçımız var", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) { //haritaya uzun basıldığın
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveButton.setEnabled(true);
    }

    public void save(View view){
        Place place = new Place(binding.placeNameText.getText().toString(), selectedLatitude, selectedLongitude);
        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io()) //io thread de yap
                .observeOn(AndroidSchedulers.mainThread()) //main Thread de gözlemlenecek
                .subscribe(MapsActivity.this::handleResponse)
        );
    }

    public void delete(View view){
        compositeDisposable.add(placeDao.delete(place)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(MapsActivity.this::handleResponse)
        );
    }

    private void handleResponse(){
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}