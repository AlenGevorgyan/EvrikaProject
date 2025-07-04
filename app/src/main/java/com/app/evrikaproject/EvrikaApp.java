package com.app.evrikaproject;

import android.app.Application;
import com.google.firebase.FirebaseApp;

import org.maplibre.android.MapLibre;

public class EvrikaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        MapLibre.getInstance(this); // The empty string is fine for open tiles
    }
} 