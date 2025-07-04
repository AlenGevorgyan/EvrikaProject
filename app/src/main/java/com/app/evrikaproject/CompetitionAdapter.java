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
        holder.tvDate.setText(comp.date != null ? comp.date : "");
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
        if (registeredMode || isRegistered) {
            holder.btnViewDetails.setText("Chat");
            holder.btnViewDetails.setOnClickListener(v -> listener.onViewDetails(comp));
            holder.btnViewDetails.setOnLongClickListener(v -> {
                listener.onRemove(comp);
                return true;
            });
        } else {
            holder.btnViewDetails.setText("Join now");
            holder.btnViewDetails.setOnClickListener(v -> listener.onJoin(comp));
            holder.btnViewDetails.setOnLongClickListener(null);
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

    static class CompetitionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSport, tvDate, tvPlayerCount, tvLocation;
        Button btnViewDetails;
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
            bgSportImage = itemView.findViewById(R.id.bg_sport_image);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
        }
    }
}