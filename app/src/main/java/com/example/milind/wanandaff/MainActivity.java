package com.example.milind.wanandaff;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

public class MainActivity extends AppCompatActivity implements DirectionFinderListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LocationActivity";
    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    private Button fare;
    private EditText orgin;
    private EditText dest;
    private ProgressDialog progressDialog;
    private Button map;
    private FusedLocationProviderClient mFusedLocationClient;
    private CheckBox wanandaff;
    private CheckBox pickUp;
    private static final long INTERVAL = 1000*10 ;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;


    private TextView addressField;




    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fare = findViewById(R.id.button2);
        orgin = findViewById(R.id.editText2);
        dest = findViewById(R.id.editText3);
        map = findViewById(R.id.button3);
        addressField = findViewById(R.id.textView3);
        wanandaff = findViewById(R.id.checkBox);
        pickUp = findViewById(R.id.checkBox2);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(this, "Please provide app permission for location access", Toast.LENGTH_LONG).show();
            return;
        }
        if(!(isLocationEnabled(this))){
            Toast.makeText(this,"Please turn on Location",Toast.LENGTH_LONG).show();
            return;
        }


        if (servicesOK()) {
            createLocationRequest();
            // Toast.makeText(this, "Connected to Google Play Services", Toast.LENGTH_SHORT).show();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            // mGoogleApiClient.connect();
            final Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                Toast.makeText(this, "Please turn on Location", Toast.LENGTH_LONG).show();
                return;
            }
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                try {
                                    List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    Address add = address.get(0);
                                    String city = add.getLocality();
                                    ((TextView) findViewById(R.id.textView3)).setText("Your are at: " + city);
                                } catch (Exception e) {


                                }
                            }

                        }
                    });
            getLocation();


        }


        fare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                execute();
            }
        });




    }
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }
    private void updateLocation() throws IOException {
        Log.d(TAG, "Location update initiated .............");
       // Toast.makeText(this,"Please Wait.....",Toast.LENGTH_LONG).show();
        if (null != mCurrentLocation) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> address = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
            Address add = address.get(0);
            String address1 = add.getAddressLine(0);
            orgin.setText(address1);
        } else {
            Log.d(TAG, "location is null ...............");
        }
    }

    public boolean servicesOK() {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {

            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot connect to Google Play Services", Toast.LENGTH_LONG).show();

        }
        return false;
    }


    @Override
    public void onStart() {

        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        if(!(isLocationEnabled(this))){
            Toast.makeText(this,"Please turn on Location",Toast.LENGTH_LONG).show();
            return;
        }

        mGoogleApiClient.connect();
        getLocation();
        // startLocationUpdates();

     if(pickUp.isChecked()) {
    try {
        updateLocation();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    }

    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
      //  stopLocationUpdates();
        // Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }


    public void onResume() {
        super.onResume();
        if(!(isLocationEnabled(this))){
            Toast.makeText(this,"Please turn on Location",Toast.LENGTH_LONG).show();
            return;
        }
        if (mGoogleApiClient.isConnected()) {

            Log.d(TAG, "Location update resumed .....................");
           // startLocationUpdates();
            getLocation();
            if(pickUp.isChecked()) {
                try {
                    updateLocation();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mGoogleApiClient.connect();
           // startLocationUpdates();
            getLocation();
            if(pickUp.isChecked()) {
                try {
                    updateLocation();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    public void execute() {
        String origin = orgin.getText().toString();
        String destination = dest.getText().toString();


        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent Q_1 = new Intent(this, MapsActivity.class);
        Q_1.putExtra("org", orgin.getText().toString());
        Q_1.putExtra("dst", dest.getText().toString());

        startActivity(Q_1);


    }

    public void sendRequest() {
        String origin = orgin.getText().toString();
        String destination = dest.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, origin + " to " + destination, Toast.LENGTH_SHORT).show();


        try {

            new DirectionFinder(this, origin, destination).execute();

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Calculating fare..", true);


    }

    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();

        String city = addressField.getText().toString();

        if (city.indexOf("Bengaluru") >= 0) {


            for (Route route : routes) {

                if (route.distance.value <= 1900) {

                    ((TextView) findViewById(R.id.textView2)).setText("Rs.25");
                    if (wanandaff.isChecked()) {
                        ((TextView) findViewById(R.id.textView2)).setText("Rs.38");
                        return;
                    }
                } else {

                    double dist = route.distance.value;
                    dist = (dist - 1900) / 1000;

                    double rate = ((13) * (dist) + 25);

                    if (wanandaff.isChecked())

                        rate = (int) (rate * (1.5));
                    else
                        rate = (int) rate;


                    String Rate = String.valueOf(rate);


                    //Toast.makeText(this, Rate, Toast.LENGTH_LONG).show();
                    ((TextView) findViewById(R.id.textView2)).setText("Rs " + Rate);

                }


            }
        } else if (city.indexOf("Mumbai") >= 0) {


        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        getLocation();
        startLocationUpdates();

        if(pickUp.isChecked()) {
            try {
                updateLocation();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    protected void startLocationUpdates() {


        PendingResult<Status> pendingResult;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(this, "Please provide app permission for location access", Toast.LENGTH_LONG).show();
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
        Log.d(TAG, "Location update started ..............: ");
    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(this, "Location Unavailable", Toast.LENGTH_LONG).show();
            return;
        }
        mFusedLocationClient.getLastLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Unable to connect to service", Toast.LENGTH_LONG).show();

    }




    @Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(this, "Please provide app permission for location access", Toast.LENGTH_LONG).show();
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(pickUp.isChecked()) {
            try {
                updateLocation();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

