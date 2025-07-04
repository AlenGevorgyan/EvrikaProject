package com.app.evrikaproject;

import android.app.Application;
import org.maplibre.android.MapLibre;
import org.maplibre.android.WellKnownTileServer;

public class EvrikaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize MapLibre with no API key and MapTiler as the tile server
        MapLibre.getInstance(this, "", WellKnownTileServer.MapTiler);
    }
} 