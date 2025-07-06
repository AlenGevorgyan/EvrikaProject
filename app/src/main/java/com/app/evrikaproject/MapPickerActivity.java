package com.app.evrikaproject;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LatLng selectedLatLng;
    private Button btnConfirm;
    private ProgressBar progressBar;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pick Location");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // UI init
        mapView = findViewById(R.id.mapView);
        btnConfirm = findViewById(R.id.btn_confirm_location);
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnConfirm.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Intent result = new Intent();
                result.putExtra("lat", selectedLatLng.getLatitude());
                result.putExtra("lng", selectedLatLng.getLongitude());
                setResult(RESULT_OK, result);
                finish();
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        this.mapLibreMap = mapLibreMap;

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Loading map...");
        tvStatus.setVisibility(View.VISIBLE);

        String styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=BNmrQVafEFp815tidnaN";

        mapLibreMap.setStyle(styleUrl, style -> {
            // Hide loading
            progressBar.setVisibility(View.GONE);
            tvStatus.setText("Tap on the map to select a location");

            // Default camera position (Yerevan)
            mapLibreMap.setCameraPosition(new CameraPosition.Builder()
                    .target(new LatLng(40.1776, 44.5126))
                    .zoom(12)
                    .build());

            // Handle map clicks
            mapLibreMap.addOnMapClickListener(point -> {
                selectedLatLng = point;
                mapLibreMap.clear();
                mapLibreMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title("Selected Location")
                        .snippet("Lat: " + String.format("%.6f", point.getLatitude()) +
                                ", Lng: " + String.format("%.6f", point.getLongitude())));
                tvStatus.setText("Location selected! Tap 'Confirm Location' to save.");
                return true;
            });

            // Try enabling user location
            try {
                mapLibreMap.getLocationComponent().activateLocationComponent(
                        LocationComponentActivationOptions.builder(this, style).build());
                mapLibreMap.getLocationComponent().setLocationComponentEnabled(true);
            } catch (Exception e) {
                Log.d("MapPickerActivity", "Location permission not granted: " + e.getMessage());
            }

        });
    }

    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { mapView.onPause(); super.onPause(); }
    @Override public void onStop() { mapView.onStop(); super.onStop(); }
    @Override public void onLowMemory() { mapView.onLowMemory(); super.onLowMemory(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
