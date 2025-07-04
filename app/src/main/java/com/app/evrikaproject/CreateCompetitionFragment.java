package com.app.evrikaproject;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.annotations.MarkerOptions;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CreateCompetitionFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int FINE_PERMISSION_CODE = 1;
    private ImageView ivMainImage;
    private Button btnPickImage, btnPickDate, btnCreate;
    private EditText etName, etTeamPlayerCount;
    private Spinner spinnerSport;
    private RadioGroup rgType;
    private TextView tvLocation, tvDate;
//    private MapView mapView;
//    private SearchView mapSearchView;
    private Uri imageUri;
    private String selectedDate = "";
    private double selectedLat = 0, selectedLng = 0;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_create_competition, container, false);
        etName = view.findViewById(R.id.et_competition_name);
        spinnerSport = view.findViewById(R.id.spinner_sport);
        rgType = view.findViewById(R.id.rg_type);
        etTeamPlayerCount = view.findViewById(R.id.et_team_player_count);
        tvLocation = view.findViewById(R.id.tv_location);
        btnPickDate = view.findViewById(R.id.btn_pick_date);
        tvDate = view.findViewById(R.id.tv_date);
        btnCreate = view.findViewById(R.id.btn_create_competition);
//        mapView = view.findViewById(R.id.mapView);
//        mapSearchView = view.findViewById(R.id.mapSearch);
//        mapView.onCreate(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"football", "basketball"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSport.setAdapter(adapter);

//        btnPickImage.setOnClickListener(v -> pickImage());
        btnPickDate.setOnClickListener(v -> pickDate());
        btnCreate.setOnClickListener(v -> createCompetition());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
//        getLastLocation();

//        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                String location = mapSearchView.getQuery().toString();
//                List<Address> addressList = null;
//                if (location != null && !location.isEmpty()) {
//                    Geocoder geocoder = new Geocoder(requireContext());
//                    try {
//                        addressList = geocoder.getFromLocationName(location, 1);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    if (addressList != null && !addressList.isEmpty()) {
//                        Address address = addressList.get(0);
//                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//                        selectedLat = latLng.getLatitude();
//                        selectedLng = latLng.getLongitude();
//                        tvLocation.setText("Lat: " + selectedLat + ", Lng: " + selectedLng);
//                        mapView.getMapAsync(mapLibreMap -> {
//                            mapLibreMap.clear();
//                            mapLibreMap.addMarker(new MarkerOptions().position(latLng).title(location));
//                            mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
//                        });
//                    } else {
//                        Toast.makeText(requireContext(), "Place not found", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                return false;
//            }
//            @Override
//            public boolean onQueryTextChange(String newText) { return false; }
//        });

//        mapView.getMapAsync(mapLibreMap -> {
//            mapLibreMap.setStyle("https://tiles.stadiamaps.com/styles/osm_bright.json", style -> {
//                // Optional: Set initial camera position
//                mapLibreMap.setCameraPosition(new CameraPosition.Builder()
//                        .target(new LatLng(40.1776, 44.5126)) // Yerevan, Armenia
//                        .zoom(10)
//                        .build());
//                mapLibreMap.addOnMapClickListener(point -> {
//                    selectedLat = point.getLatitude();
//                    selectedLng = point.getLongitude();
//                    tvLocation.setText("Lat: " + selectedLat + ", Lng: " + selectedLng);
//                    mapLibreMap.clear();
//                    mapLibreMap.addMarker(new MarkerOptions().position(point).title("Selected Location"));
//                    return true;
//                });
//            });
//        });

        return view;
    }

//    private void getLastLocation() {
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
//            return;
//        }
//        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
//            if (location != null) {
//                currentLocation = location;
//                LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                selectedLat = myLocation.getLatitude();
//                selectedLng = myLocation.getLongitude();
//                tvLocation.setText("Lat: " + selectedLat + ", Lng: " + selectedLng);
//                mapView.getMapAsync(mapLibreMap -> {
//                    mapLibreMap.clear();
//                    mapLibreMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
//                    mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
//                });
//            }
//        });
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == FINE_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLastLocation();
//            } else {
//                Toast.makeText(getContext(), "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void pickDate() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(calendar.getTime());
            tvDate.setText(selectedDate);
            tvDate.setError(null);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == requireActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                ivMainImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createCompetition() {
        String name = etName.getText().toString().trim();
        String sport = spinnerSport.getSelectedItem().toString();
        int checkedId = rgType.getCheckedRadioButtonId();
        String type = checkedId == R.id.rb_public ? "public" : checkedId == R.id.rb_private ? "private" : "";
        String teamPlayerCountStr = etTeamPlayerCount.getText().toString().trim();
        int teamPlayerCount = teamPlayerCountStr.isEmpty() ? 0 : Integer.parseInt(teamPlayerCountStr);

        boolean hasError = false;
        if (name.isEmpty()) {
            etName.setError("Enter a name");
            hasError = true;
        }
        if (teamPlayerCount <= 0) {
            etTeamPlayerCount.setError("Enter a valid team player count");
            hasError = true;
        }
        if (selectedDate.isEmpty()) {
            tvDate.setError("Pick a date");
            hasError = true;
        }
        if (selectedLat == 0 && selectedLng == 0) {
            // Set static location (e.g., Yerevan, Armenia)
            selectedLat = 40.1776;
            selectedLng = 44.5126;
            tvLocation.setText("Lat: 40.1776, Lng: 44.5126");
        }
        if (type.isEmpty()) {
            Toast.makeText(getContext(), "Select competition type", Toast.LENGTH_SHORT).show();
            hasError = true;
        }
        if (hasError) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";
        String imageUrl = imageUri != null ? imageUri.toString() : "";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String compId = db.collection("games").document().getId();
        Competition comp = new Competition(
                compId,
                name,
                sport,
                type,
                userId,
                new ArrayList<>(),
                new ArrayList<>(),
                selectedDate,
                teamPlayerCount
        );
        db.collection("competitions").document(compId)
                .set(comp)
                .addOnSuccessListener(aVoid -> {
                    db.collection("competitions").document(compId)
                        .update("imageUrl", imageUrl,
                                "teamPlayerCount", teamPlayerCount,
                                "latitude", selectedLat,
                                "longitude", selectedLng,
                                "date", selectedDate)
                        .addOnSuccessListener(aVoid2 -> {
                            Map<String, Object> competitionData = new HashMap<>();
                            competitionData.put("id", compId);
                            competitionData.put("name", name);
                            competitionData.put("sport", sport);
                            competitionData.put("type", type);
                            competitionData.put("createdBy", userId);
                            competitionData.put("teams", new ArrayList<>());
                            competitionData.put("invitedTeams", new ArrayList<>());
                            competitionData.put("imageUrl", imageUrl);
                            competitionData.put("teamPlayerCount", teamPlayerCount);
                            competitionData.put("latitude", selectedLat);
                            competitionData.put("longitude", selectedLng);
                            competitionData.put("date", selectedDate);
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("competitions");
                            dbRef.child(compId).setValue(competitionData)
                                .addOnSuccessListener(aVoid3 -> {
                                    Toast.makeText(getContext(), "Competition created!", Toast.LENGTH_SHORT).show();
                                    if (getActivity() != null) {
                                        getActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, new HomeFragment())
                                            .commit();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save to Realtime DB: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        });
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // MapView lifecycle
//    @Override public void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
//    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
//    @Override public void onPause() { if (mapView != null) mapView.onPause(); super.onPause(); }
//    @Override public void onStop() { if (mapView != null) mapView.onStop(); super.onStop(); }
//    @Override public void onLowMemory() { if (mapView != null) mapView.onLowMemory(); super.onLowMemory(); }
//    @Override public void onDestroyView() { if (mapView != null) mapView.onDestroy(); super.onDestroyView(); }
}