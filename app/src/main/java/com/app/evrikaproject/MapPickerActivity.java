package com.app.evrikaproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.annotations.MarkerOptions;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LatLng selectedLatLng;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);
        mapView = findViewById(R.id.mapView);
        btnConfirm = findViewById(R.id.btn_confirm_location);
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
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        this.mapLibreMap = mapLibreMap;
        mapLibreMap.setStyle("https://api.maptiler.com/maps/hybrid/style.json?key=wxQI2tnsdCLI4pSXvQVA", style -> {
            mapLibreMap.addOnMapClickListener(point -> {
                selectedLatLng = point;
                mapLibreMap.clear();
                mapLibreMap.addMarker(new MarkerOptions().position(point).title("Selected Location"));
                return true;
            });
        });
    }

    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { mapView.onPause(); super.onPause(); }
    @Override public void onStop() { mapView.onStop(); super.onStop(); }
    @Override public void onLowMemory() { mapView.onLowMemory(); super.onLowMemory(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
} 