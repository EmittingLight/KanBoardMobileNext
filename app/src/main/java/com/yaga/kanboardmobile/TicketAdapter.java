package com.yaga.kanboardmobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;
    private OnItemClickListener listener;
    private TicketDatabaseHelper dbHelper;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public TicketAdapter(List<Ticket> ticketList, TicketDatabaseHelper dbHelper) {
        this.ticketList = ticketList;
        this.dbHelper = dbHelper;
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

        int color;
        int backgroundColor;
        int iconRes;

        switch (status) {
            case "К выполнению":
                color = 0xFF1976D2;
                backgroundColor = 0x101976D2;
                iconRes = R.drawable.ic_todo;
                break;
            case "В процессе":
                color = 0xFFFF9800;
                backgroundColor = 0x10FF9800;
                iconRes = R.drawable.ic_in_progress;
                break;
            case "Готово":
                color = 0xFF388E3C;
                backgroundColor = 0x10388E3C;
                iconRes = R.drawable.ic_done;
                break;
            default:
                color = 0xFF999999;
                backgroundColor = 0x10999999;
                iconRes = R.drawable.ic_todo;
        }

        holder.statusTextView.setTextColor(color);
        holder.statusIcon.setImageResource(iconRes);
        if (holder.statusBadge != null) {
            holder.statusBadge.getBackground().setTint(color);
        }

        holder.itemView.setBackgroundColor(backgroundColor);

        // ✨ Анимация
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(50f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(position * 100L)
                .start();
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, statusTextView;
        ImageView statusIcon;
        View statusBadge;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.ticketTitle);
            descriptionTextView = itemView.findViewById(R.id.ticketDescription);
            statusTextView = itemView.findViewById(R.id.ticketStatus);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            statusBadge = itemView.findViewById(R.id.statusBadge);

            // 💡 Обработчики смены статуса
            statusIcon.setOnClickListener(v -> cycleStatus(getAdapterPosition()));
            statusTextView.setOnClickListener(v -> cycleStatus(getAdapterPosition()));

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

    private void cycleStatus(int position) {
        if (position < 0 || position >= ticketList.size()) return;

        Ticket ticket = ticketList.get(position);
        String current = ticket.getStatus();
        String next;

        switch (current) {
            case "К выполнению":
                next = "В процессе";
                break;
            case "В процессе":
                next = "Готово";
                break;
            case "Готово":
            default:
                next = "К выполнению";
                break;
        }

        ticket.setStatus(next);
        notifyItemChanged(position);
        dbHelper.updateTicket(ticket); // 💾 сохраняем
    }

    public void updateList(List<Ticket> newList) {
        ticketList.clear();
        ticketList.addAll(newList);
        notifyDataSetChanged();
    }

    public Ticket getItem(int position) {
        return ticketList.get(position);
    }

    public void removeItem(int position) {
        ticketList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount()); // 💡 ключевой момент!
    }


    public void restoreItem(Ticket ticket, int position) {
        ticketList.add(position, ticket);
        notifyItemInserted(position);
    }

}
