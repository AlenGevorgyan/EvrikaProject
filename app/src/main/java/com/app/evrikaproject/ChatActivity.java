package com.app.evrikaproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText etMessage;
    private MaterialButton btnSend;
    private List<ChatMessage> messages = new ArrayList<>();
    private String competitionId;
    private String competitionName;
    private String currentUserId;
    private String currentUserName;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get data from intent
        competitionId = getIntent().getStringExtra("compId");
        competitionName = getIntent().getStringExtra("competition_name");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                       FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (competitionId == null || currentUserId == null) {
            Toast.makeText(this, "Error: Missing competition data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        initializeViews();
        loadCurrentUserInfo();
        setupRecyclerView();
        loadMessages();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(competitionName != null ? competitionName : "Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Add click listener to toolbar navigation icon as backup
        toolbar.setNavigationOnClickListener(v -> {
            android.util.Log.d("ChatActivity", "Toolbar navigation clicked");
            finish();
        });

        recyclerView = findViewById(R.id.recycler_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        // Initially disable send button until user info is loaded
        btnSend.setEnabled(false);
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadCurrentUserInfo() {
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Debug: Print all fields in the user document
                        android.util.Log.d("ChatActivity", "User document exists");
                        for (String field : documentSnapshot.getData().keySet()) {
                            android.util.Log.d("ChatActivity", "Field: " + field + " = " + documentSnapshot.get(field));
                        }
                        
                        // Try different possible field names for user name
                        String realName = documentSnapshot.getString("name");
                        String realSurname = documentSnapshot.getString("surname");
                        
                        // Combine real name and surname, or use Anonymous as fallback
                        if (realName != null && !realName.isEmpty() && realSurname != null && !realSurname.isEmpty()) {
                            currentUserName = realName + " " + realSurname;
                        } else if (realName != null && !realName.isEmpty()) {
                            currentUserName = realName;
                        } else {
                            currentUserName = "Anonymous";
                        }
                    } else {
                        currentUserName = "Anonymous";
                        android.util.Log.d("ChatActivity", "User document does not exist");
                    }
                    // Enable send button after user info is loaded
                    btnSend.setEnabled(true);
                    android.util.Log.d("ChatActivity", "Final user name: " + currentUserName);
                })
                .addOnFailureListener(e -> {
                    currentUserName = "Anonymous";
                    btnSend.setEnabled(true);
                    android.util.Log.d("ChatActivity", "Failed to load user name, using Anonymous");
                });
        } else {
            currentUserName = "Anonymous";
            btnSend.setEnabled(true);
        }
    }

    private void setupRecyclerView() {
        // Temporarily use simple adapter for testing
        adapter = new ChatAdapter(this, messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        // Load messages directly from games collection
        db.collection("games").document(competitionId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(ChatActivity.this, "Error loading messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    messages.clear();
                    for (DocumentSnapshot doc : value) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                    adapter.setMessages(messages);
                    scrollToBottom();
                    
                    // Debug info
                    Toast.makeText(ChatActivity.this, "Loaded " + messages.size() + " messages", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        if (currentUserId == null || currentUserName == null) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debug info
        Toast.makeText(this, "Sending message: " + messageText, Toast.LENGTH_SHORT).show();

        // Create message document ID
        String messageId = db.collection("games").document(competitionId)
            .collection("messages").document().getId();
        
        // Create chat message with proper sender name
        String senderName = currentUserName != null ? currentUserName : "Anonymous";
        ChatMessage message = new ChatMessage(messageId, currentUserId, senderName, messageText);
        
        // Save message directly to games collection
        saveMessageToGamesCollection(message, messageId);
    }

    private void saveMessageToGamesCollection(ChatMessage message, String messageId) {
        db.collection("games").document(competitionId)
            .collection("messages").document(messageId).set(message)
            .addOnSuccessListener(aVoid -> {
                etMessage.setText("");
                Toast.makeText(ChatActivity.this, "Message sent successfully!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            recyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            android.util.Log.d("ChatActivity", "Back button pressed");
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        android.util.Log.d("ChatActivity", "onBackPressed called");
        super.onBackPressed();
    }
} 