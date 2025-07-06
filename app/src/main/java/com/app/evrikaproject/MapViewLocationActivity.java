package com.app.evrikaproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraPosition;

public class MapViewLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view_location);
        
        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Competition Location");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // UI init
        mapView = findViewById(R.id.mapView);
        
        mapView.onCreate(savedInstanceState);
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);
        
        // Validate coordinates
        if (lat == 0 && lng == 0) {
            Toast.makeText(this, "Invalid location coordinates", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        String styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=BNmrQVafEFp815tidnaN";

        mapLibreMap.setStyle(styleUrl, style -> {
            LatLng location = new LatLng(lat, lng);
            
            // Set camera position with better zoom
            mapLibreMap.setCameraPosition(new CameraPosition.Builder()
                .target(location)
                .zoom(15)
                .build());

            // Add marker with better styling
            mapLibreMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Competition Location")
                .snippet("Lat: " + String.format("%.6f", lat) + ", Lng: " + String.format("%.6f", lng)));

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
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 