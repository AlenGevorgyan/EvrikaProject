package com.app.evrikaproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> messages;
    private Context context;
    private String currentUserId;

    public ChatAdapter(Context context, List<ChatMessage> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        boolean isOwnMessage = currentUserId != null && currentUserId.equals(message.getSenderId());
        
        // Debug info
        android.util.Log.d("ChatAdapter", "Binding message: " + message.getMessage() + ", isOwn: " + isOwnMessage);
        
        if (isOwnMessage) {
            // Show sent message layout
            holder.layoutSentMessage.setVisibility(View.VISIBLE);
            holder.layoutReceivedMessage.setVisibility(View.GONE);
            
            holder.tvSentMessage.setText(message.getMessage());
            if (message.getTimestamp() != null) {
                holder.tvSentTime.setText(sdf.format(message.getTimestamp().toDate()));
            }
            
            android.util.Log.d("ChatAdapter", "Showing sent message layout");
        } else {
            // Show received message layout
            holder.layoutSentMessage.setVisibility(View.GONE);
            holder.layoutReceivedMessage.setVisibility(View.VISIBLE);
            
            holder.tvSenderName.setText(message.getSenderName());
            holder.tvReceivedMessage.setText(message.getMessage());
            if (message.getTimestamp() != null) {
                holder.tvReceivedTime.setText(sdf.format(message.getTimestamp().toDate()));
            }
            
            android.util.Log.d("ChatAdapter", "Showing received message layout");
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        android.util.Log.d("ChatAdapter", "setMessages called with " + (messages != null ? messages.size() : 0) + " messages");
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        if (messages != null) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutSentMessage, layoutReceivedMessage;
        TextView tvSentMessage, tvSentTime, tvReceivedMessage, tvReceivedTime, tvSenderName;

        ChatViewHolder(View itemView) {
            super(itemView);
            layoutSentMessage = itemView.findViewById(R.id.layout_sent_message);
            layoutReceivedMessage = itemView.findViewById(R.id.layout_received_message);
            tvSentMessage = itemView.findViewById(R.id.tv_sent_message);
            tvSentTime = itemView.findViewById(R.id.tv_sent_time);
            tvReceivedMessage = itemView.findViewById(R.id.tv_received_message);
            tvReceivedTime = itemView.findViewById(R.id.tv_received_time);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
        }
    }
} 