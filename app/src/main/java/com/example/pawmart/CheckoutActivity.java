package com.example.pawmart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOC = 2001;

    private GoogleMap map;
    private Marker marker;
    private LatLng selectedLatLng;

    private TextView tvAddress, tvLatLng;
    private MaterialButton btnProceedPayment;

    private FusedLocationProviderClient fused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        tvAddress = findViewById(R.id.tvAddress);
        tvLatLng = findViewById(R.id.tvLatLng);
        btnProceedPayment = findViewById(R.id.btnProceedPayment);

        fused = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnProceedPayment.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "Select delivery location first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, PaymentActivity.class);
            i.putExtra("address", tvAddress.getText().toString());
            i.putExtra("lat", selectedLatLng.latitude);
            i.putExtra("lng", selectedLatLng.longitude);
            startActivity(i);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);
            // show Dhaka default
            setDefaultMarker(new LatLng(23.8103, 90.4125));
            return;
        }

        map.setMyLocationEnabled(true);

        fused.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                setDefaultMarker(new LatLng(loc.getLatitude(), loc.getLongitude()));
            } else {
                setDefaultMarker(new LatLng(23.8103, 90.4125)); // Dhaka fallback
            }
        });

        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDrag(@NonNull Marker m) { }
            @Override public void onMarkerDragStart(@NonNull Marker m) { }
            @Override public void onMarkerDragEnd(@NonNull Marker m) {
                selectedLatLng = m.getPosition();
                tvLatLng.setText("Lat: " + selectedLatLng.latitude + "  Lng: " + selectedLatLng.longitude);
                tvAddress.setText(getAddressFromLatLng(selectedLatLng));
            }
        });
    }

    private void setDefaultMarker(LatLng latLng) {
        selectedLatLng = latLng;

        if (map == null) return;

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));

        if (marker != null) marker.remove();

        marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Delivery Location")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        tvLatLng.setText("Lat: " + latLng.latitude + "  Lng: " + latLng.longitude);
        tvAddress.setText(getAddressFromLatLng(latLng));
    }

    private String getAddressFromLatLng(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (list != null && !list.isEmpty()) {
                Address a = list.get(0);
                return a.getAddressLine(0);
            }
        } catch (Exception ignored) {}
        return "Address not found (drag marker again)";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // recreate map flow quickly
            if (map != null) {
                try {
                    map.setMyLocationEnabled(true);
                } catch (Exception ignored) {}
            }
            setDefaultMarker(new LatLng(23.8103, 90.4125));
        }
    }
}