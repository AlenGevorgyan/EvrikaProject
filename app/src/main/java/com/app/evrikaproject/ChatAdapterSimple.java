package com.app.evrikaproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapterSimple extends RecyclerView.Adapter<ChatAdapterSimple.ChatViewHolder> {
    private List<ChatMessage> messages;
    private Context context;

    public ChatAdapterSimple(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_simple, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        // Use the sender name from the message
        String displayName = message.getSenderName();
        if (displayName == null || displayName.isEmpty() || displayName.equals("Anonymous")) {
            displayName = "Anonymous";
        }
        holder.tvSenderName.setText("Sender: " + displayName);
        
        holder.tvMessage.setText(message.getMessage());
        if (message.getTimestamp() != null) {
            holder.tvTime.setText(sdf.format(message.getTimestamp().toDate()));
        }
        
        android.util.Log.d("ChatAdapterSimple", "Binding message from: " + displayName + " - " + message.getMessage());
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        android.util.Log.d("ChatAdapterSimple", "setMessages called with " + (messages != null ? messages.size() : 0) + " messages");
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvMessage, tvTime;

        ChatViewHolder(View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
} 