package com.tallerweb2.android.GPS;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener,
                   LocationListener, View.OnClickListener {
    private static final String TAG = MainActivity.class.getName();

//    private TextView mTvLatitud;
//    private TextView mTvLongitud;
    private TextView mTvDireccion;
    private String localidad;

    private static final int RC_LOCATION_PERMISION= 100;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static int INTERVAL = 10000;
    private static int FAST_INTERVAL = 5000;

    private boolean mRequestingLocationUpdates = false;
    private Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Conectar el UI con la Actividad
        mTvDireccion= (TextView) findViewById(R.id.direccion);

        //Solicitar permisos si es necesario (Android 6.0+)
        requestPermissionIfNeedIt();

        //Inicializar el GoogleAPIClient y armar la Petición de Ubicación
        initGoogleAPIClient();

        //creamos las instancias de botones para realizar los intents
        Button boton1 = (Button)findViewById(R.id.button1);
        boton1.setOnClickListener(this);

        Button boton2 = (Button)findViewById(R.id.button2);
        boton2.setOnClickListener(this);

        Button boton3 = (Button)findViewById(R.id.button3);
        boton3.setOnClickListener(this);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.button1: {

                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://clarin.com/buscador/?q="+localidad));
                startActivity(i);
                break;
            }

            case R.id.button2: {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://buscar.lanacion.com.ar/"+localidad));
                startActivity(i);
                break;
            }

            case  R.id.button3: {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://infobae.com/search/"+localidad));
                startActivity(i);
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected())
                startLocationUpdates();
            else
                mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initGoogleAPIClient(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            //Creamos una peticion de ubicacion con el objeto LocationRequest
            createLocationRequest();
        }
    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FAST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates(){
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                mRequestingLocationUpdates = true;
            }
        }
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mRequestingLocationUpdates){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void requestPermissionIfNeedIt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_LOCATION_PERMISION);
        }
    }

    private void refreshUI(){
        if (mCurrentLocation != null) {
//            mTvLatitud.setText(String.valueOf("Latitud: "+mCurrentLocation.getLatitude()));
//            mTvLongitud.setText(String.valueOf("Longitud: "+mCurrentLocation.getLongitude()));
        }
    }

    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address address = list.get(0);
                    localidad = address.getLocality();
                    mTvDireccion.setText(localidad);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_LOCATION_PERMISION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                requestPermissionIfNeedIt();
            }
        }
    }

    /*
    * Implementación del GoogleApiClient.ConnectionCallbacks
    * */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
        Toast.makeText(this, getString(R.string.app_name), Toast.LENGTH_LONG).show();
    }

    /*
    * Implementación del GoogleApiClient.OnConnectionFailedListener
    * */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    /*
    * Implementación del LocationListener
    * */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        String Text = "Mi ubicacion actual es: " + "\n Lat = "
                + location.getLatitude() + "\n Long = " + location.getLongitude();
        mTvDireccion.setText(Text);
        this.setLocation(location);
        refreshUI();
    }

}
