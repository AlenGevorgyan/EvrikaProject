package com.app.evrikaproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ArrayList;

public class CompetitionAdapter extends RecyclerView.Adapter<CompetitionAdapter.CompetitionViewHolder> {
    private List<Competition> competitions;
    private Context context;
    private OnCompetitionClickListener listener;
    private String userId;
    private boolean registeredMode;
    private List<String> registeredGameIds;

    public interface OnCompetitionClickListener {
        void onJoin(Competition competition);
        void onRemove(Competition competition);
        void onViewDetails(Competition competition);
    }

    public CompetitionAdapter(Context context, List<Competition> competitions, OnCompetitionClickListener listener, String userId, boolean registeredMode) {
        this.context = context;
        this.competitions = competitions;
        this.listener = listener;
        this.userId = userId;
        this.registeredMode = registeredMode;
        this.registeredGameIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public CompetitionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_competition, parent, false);
        return new CompetitionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompetitionViewHolder holder, int position) {
        Competition comp = competitions.get(position);
        holder.tvName.setText(comp.name);
        holder.tvSport.setText(comp.sport != null ? comp.sport : "");
        holder.tvDate.setText(comp.date != null ? comp.date + " : " + comp.time : "");
        holder.tvPlayerCount.setText("Players: " + (comp.teamPlayerCount > 0 ? comp.teamPlayerCount : "N/A"));
        holder.tvLocation.setText("Location: Stadium"); // Static location for now
        // Set background image based on sport
        if (comp.sport != null && comp.sport.equalsIgnoreCase("football")) {
            holder.bgSportImage.setImageResource(R.drawable.football);
        } else if (comp.sport != null && comp.sport.equalsIgnoreCase("basketball")) {
            holder.bgSportImage.setImageResource(R.drawable.basketball);
        } else if (comp.sport != null && comp.sport.equalsIgnoreCase("volleyball")) {
            holder.bgSportImage.setImageResource(R.drawable.volley);
        } else {
            holder.bgSportImage.setImageResource(R.drawable.ic_launcher_background);
        }
        boolean isRegistered = registeredGameIds != null && comp.posterId != null && registeredGameIds.contains(comp.posterId);
        boolean isHost = userId != null && userId.equals(comp.createdBy);
        
        // Debug logging
        android.util.Log.d("CompetitionAdapter", "Competition: " + comp.name);
        android.util.Log.d("CompetitionAdapter", "posterId: " + comp.posterId);
        android.util.Log.d("CompetitionAdapter", "createdBy: " + comp.createdBy);
        android.util.Log.d("CompetitionAdapter", "userId: " + userId);
        android.util.Log.d("CompetitionAdapter", "isHost: " + isHost);
        android.util.Log.d("CompetitionAdapter", "isRegistered: " + isRegistered);
        android.util.Log.d("CompetitionAdapter", "registeredMode: " + registeredMode);
        
        // Reset all button visibilities
        holder.btnLeave.setVisibility(View.GONE);
        holder.btnEdit.setVisibility(View.GONE);
        holder.btnDelete.setVisibility(View.GONE);
        holder.btnRequestJoin.setVisibility(View.GONE);
        
        if (registeredMode || isRegistered) {
            holder.btnViewDetails.setText("Chat");
            holder.btnViewDetails.setOnClickListener(v -> {
                // Launch chat activity
                android.content.Intent intent = new android.content.Intent(context, ChatActivity.class);
                intent.putExtra("competition_id", comp.posterId);
                intent.putExtra("competition_name", comp.name);
                context.startActivity(intent);
            });
            
            if (isHost) {
                // Show edit and delete buttons for host
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnDelete.setVisibility(View.VISIBLE);
                
                holder.btnEdit.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(context, EditCompetitionActivity.class);
                    intent.putExtra("competition_id", comp.posterId);
                    context.startActivity(intent);
                });
                
                holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(comp));
            } else {
                // Show leave button for participants
                holder.btnLeave.setVisibility(View.VISIBLE);
                holder.btnLeave.setOnClickListener(v -> listener.onRemove(comp));
            }
        } else {
            holder.btnViewDetails.setText("Join now");
            holder.btnViewDetails.setOnClickListener(v -> listener.onJoin(comp));
        }

        if (comp.type != null && comp.type.equalsIgnoreCase("private") && !isHost) {
            boolean isInTeam = comp.teams != null && comp.teams.contains(userId);
            boolean hasRequested = comp.requests != null && comp.requests.contains(userId);
            if (!isInTeam) {
                holder.btnViewDetails.setVisibility(View.GONE);
                holder.btnRequestJoin.setVisibility(View.VISIBLE);
                if (hasRequested) {
                    holder.btnRequestJoin.setText("Cancel Request");
                    holder.btnRequestJoin.setOnClickListener(v -> {
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("games").document(comp.posterId)
                            .update("requests", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                            .addOnSuccessListener(aVoid -> {
                                if (comp.requests != null) comp.requests.remove(userId);
                                notifyItemChanged(position);
                                android.widget.Toast.makeText(context, "Request canceled", android.widget.Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> android.widget.Toast.makeText(context, "Failed to cancel request: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
                    });
                } else {
                    holder.btnRequestJoin.setText("Request to Join");
                    holder.btnRequestJoin.setOnClickListener(v -> {
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("games").document(comp.posterId)
                            .update("requests", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                            .addOnSuccessListener(aVoid -> {
                                if (comp.requests == null) comp.requests = new java.util.ArrayList<>();
                                comp.requests.add(userId);
                                notifyItemChanged(position);
                                android.widget.Toast.makeText(context, "Request sent", android.widget.Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> android.widget.Toast.makeText(context, "Failed to send request: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
                    });
                }
            }
        }

        holder.ivUserAvatar.setImageResource(R.drawable.ic_person); // Placeholder for user avatar
        holder.tvLocation.setOnClickListener(v -> {
            if (comp.latitude != 0 && comp.longitude != 0) {
                Intent intent = new Intent(context, MapViewLocationActivity.class);
                intent.putExtra("lat", comp.latitude);
                intent.putExtra("lng", comp.longitude);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return competitions != null ? competitions.size() : 0;
    }

    public void setCompetitions(List<Competition> competitions) {
        this.competitions = competitions;
        notifyDataSetChanged();
    }

    public void setRegisteredGameIds(List<String> registeredGameIds) {
        this.registeredGameIds = registeredGameIds;
        notifyDataSetChanged();
    }



    private void showDeleteConfirmation(Competition competition) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Delete Competition")
            .setMessage("Are you sure you want to delete this competition? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Delete the competition
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("games").document(competition.posterId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Also delete the chat room
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("chat_rooms").document(competition.posterId)
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                android.widget.Toast.makeText(context, "Competition deleted successfully", android.widget.Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                android.widget.Toast.makeText(context, "Competition deleted but failed to delete chat room", android.widget.Toast.LENGTH_SHORT).show();
                            });
                    })
                    .addOnFailureListener(e -> {
                        android.widget.Toast.makeText(context, "Failed to delete competition: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    static class CompetitionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSport, tvDate, tvPlayerCount, tvLocation;
        Button btnViewDetails, btnLeave, btnEdit, btnDelete, btnRequestJoin;
        ImageView bgSportImage;
        com.google.android.material.imageview.ShapeableImageView ivUserAvatar;
        CompetitionViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_competition_name);
            tvSport = itemView.findViewById(R.id.tv_sport_type);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvPlayerCount = itemView.findViewById(R.id.tv_player_count);
            tvLocation = itemView.findViewById(R.id.tv_location);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnLeave = itemView.findViewById(R.id.btn_leave);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            bgSportImage = itemView.findViewById(R.id.bg_sport_image);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            btnRequestJoin = itemView.findViewById(R.id.btn_request_join);
        }
    }
}