package com.app.evrikaproject;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.annotations.MarkerOptions;

public class MapViewLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view_location);
        
        // Setup toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Add click listener to toolbar navigation icon as backup
        toolbar.setNavigationOnClickListener(v -> {
            android.util.Log.d("MapViewLocationActivity", "Toolbar navigation clicked");
            finish();
        });
        
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        mapLibreMap.setStyle("https://api.maptiler.com/maps/street/style.json?key=wxQI2tnsdCLI4pSXvQVA", style -> {
            LatLng location = new LatLng(lat, lng);
            mapLibreMap.moveCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(location, 20));
            mapLibreMap.addMarker(new MarkerOptions().position(location).title("Competition Location"));
        });
    }

    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { mapView.onPause(); super.onPause(); }
    @Override public void onStop() { mapView.onStop(); super.onStop(); }
    @Override public void onLowMemory() { mapView.onLowMemory(); super.onLowMemory(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
    
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 