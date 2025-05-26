package com.yaga.kanboardmobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public TicketAdapter(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.titleTextView.setText(ticket.getTitle());
        holder.descriptionTextView.setText(ticket.getDescription());
        String status = ticket.getStatus();
        holder.statusTextView.setText(status);

        switch (status) {
            case "К выполнению":
                holder.statusTextView.setText("📝 К выполнению");
                holder.statusTextView.setTextColor(0xFF1976D2); // синий
                break;
            case "В процессе":
                holder.statusTextView.setText("🔧 В процессе");
                holder.statusTextView.setTextColor(0xFFFF9800); // оранжевый
                break;
            case "Готово":
                holder.statusTextView.setText("✅ Готово");
                holder.statusTextView.setTextColor(0xFF388E3C); // зелёный
                break;
        }
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, statusTextView;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.ticketTitle);
            descriptionTextView = itemView.findViewById(R.id.ticketDescription);
            statusTextView = itemView.findViewById(R.id.ticketStatus);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public void updateList(List<Ticket> newList) {
        ticketList.clear();
        ticketList.addAll(newList);
        notifyDataSetChanged();
    }

    public Ticket getItem(int position) {
        return ticketList.get(position);
    }
}
