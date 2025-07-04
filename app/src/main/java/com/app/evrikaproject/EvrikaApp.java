package com.app.evrikaproject;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class EvrikaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        // If you use MapLibre, you can also initialize it here as needed
        // Example:
        // MapLibre.getInstance(this, "", WellKnownTileServer.MapTiler);
    }
} 