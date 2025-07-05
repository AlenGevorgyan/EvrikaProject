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
    private ChatAdapterSimple adapter;
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
        competitionId = getIntent().getStringExtra("competition_id");
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
                        String username = documentSnapshot.getString("username");
                        
                        // Combine real name and surname, or use username as fallback
                        if (realName != null && !realName.isEmpty() && realSurname != null && !realSurname.isEmpty()) {
                            currentUserName = realName + " " + realSurname;
                        } else if (realName != null && !realName.isEmpty()) {
                            currentUserName = realName;
                        } else if (username != null && !username.isEmpty()) {
                            currentUserName = username;
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
        adapter = new ChatAdapterSimple(this, messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        // First check if chat room exists
        db.collection("chat_rooms").document(competitionId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Chat room exists, load messages
                    loadMessagesFromSubcollection();
                } else {
                    Toast.makeText(ChatActivity.this, "Chat room doesn't exist yet", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Error checking chat room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadMessagesFromSubcollection() {
        db.collection("chat_rooms").document(competitionId)
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
                    ((ChatAdapterSimple) adapter).setMessages(messages);
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
        String messageId = db.collection("chat_rooms").document(competitionId)
            .collection("messages").document().getId();
        
        // Create chat message with proper sender name
        String senderName = currentUserName != null ? currentUserName : "Anonymous";
        ChatMessage message = new ChatMessage(messageId, currentUserId, senderName, messageText);
        
        // First ensure chat room exists, then save message
        db.collection("chat_rooms").document(competitionId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Chat room exists, save message
                    saveMessageToSubcollection(message, messageId);
                } else {
                    // Create chat room first, then save message
                    List<String> participantIds = new ArrayList<>();
                    participantIds.add(currentUserId);
                    
                    ChatRoom chatRoom = new ChatRoom(competitionId, competitionId, competitionName, participantIds);
                    db.collection("chat_rooms").document(competitionId).set(chatRoom)
                        .addOnSuccessListener(aVoid -> {
                            saveMessageToSubcollection(message, messageId);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ChatActivity.this, "Failed to create chat room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Failed to check chat room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateChatRoomLastMessage(String lastMessage) {
        // Update or create chat room
        db.collection("chat_rooms").document(competitionId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Update existing chat room
                    documentSnapshot.getReference().update("lastMessage", lastMessage, "lastMessageTime", com.google.firebase.Timestamp.now());
                } else {
                    // Create new chat room
                    List<String> participantIds = new ArrayList<>();
                    participantIds.add(currentUserId);
                    
                    ChatRoom chatRoom = new ChatRoom(competitionId, competitionId, competitionName, participantIds);
                    chatRoom.setLastMessage(lastMessage);
                    chatRoom.setLastMessageTime(com.google.firebase.Timestamp.now());
                    
                    db.collection("chat_rooms").document(competitionId).set(chatRoom);
                }
            });
    }

    private void saveMessageToSubcollection(ChatMessage message, String messageId) {
        db.collection("chat_rooms").document(competitionId)
            .collection("messages").document(messageId).set(message)
            .addOnSuccessListener(aVoid -> {
                etMessage.setText("");
                // Update chat room with last message from subcollection
                updateLastMessageFromSubcollection();
                Toast.makeText(ChatActivity.this, "Message sent successfully!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    // Method to get the last message from the subcollection
    private void updateLastMessageFromSubcollection() {
        db.collection("chat_rooms").document(competitionId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    ChatMessage lastMessage = queryDocumentSnapshots.getDocuments().get(0).toObject(ChatMessage.class);
                    if (lastMessage != null) {
                        db.collection("chat_rooms").document(competitionId)
                            .update("lastMessage", lastMessage.getMessage(), 
                                   "lastMessageTime", lastMessage.getTimestamp());
                    }
                }
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