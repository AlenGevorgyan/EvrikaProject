package com.app.evrikaproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CompetitionAdapter extends RecyclerView.Adapter<CompetitionAdapter.CompetitionViewHolder> {
    private List<Competition> competitions;
    private Context context;
    private OnCompetitionClickListener listener;

    public interface OnCompetitionClickListener {
        void onViewDetails(Competition competition);
    }

    public CompetitionAdapter(Context context, List<Competition> competitions, OnCompetitionClickListener listener) {
        this.context = context;
        this.competitions = competitions;
        this.listener = listener;
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
        holder.tvSport.setText("Sport: " + comp.sport);
        holder.tvType.setText("Type: " + comp.type);
        holder.tvTeams.setText("Teams: " + (comp.teams != null ? comp.teams.size() : 0));
        holder.btnViewDetails.setOnClickListener(v -> listener.onViewDetails(comp));
    }

    @Override
    public int getItemCount() {
        return competitions != null ? competitions.size() : 0;
    }

    public void setCompetitions(List<Competition> competitions) {
        this.competitions = competitions;
        notifyDataSetChanged();
    }

    static class CompetitionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSport, tvType, tvTeams;
        Button btnViewDetails;
        CompetitionViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_competition_name);
            tvSport = itemView.findViewById(R.id.tv_sport_type);
            tvType = itemView.findViewById(R.id.tv_competition_type);
            tvTeams = itemView.findViewById(R.id.tv_teams_count);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}