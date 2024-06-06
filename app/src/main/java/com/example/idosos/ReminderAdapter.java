package com.example.idosos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders;
    private OnReminderRemovedListener listener;

    public interface OnReminderRemovedListener {
        void onReminderRemoved();
    }

    public ReminderAdapter(List<Reminder> reminders, OnReminderRemovedListener listener) {
        this.reminders = reminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.reminderTitleTextView.setText(reminder.getTitle());
        holder.reminderDateTextView.setText(reminder.getDate());
        holder.reminderTimeTextView.setText(reminder.getTime());
        holder.trashIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    reminders.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    notifyItemRangeChanged(currentPosition, reminders.size());
                    listener.onReminderRemoved();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView reminderTitleTextView;
        TextView reminderDateTextView;
        TextView reminderTimeTextView;
        ImageView trashIcon;

        ReminderViewHolder(View itemView) {
            super(itemView);
            reminderTitleTextView = itemView.findViewById(R.id.reminderTitleTextView);
            reminderDateTextView = itemView.findViewById(R.id.reminderDateTextView);
            reminderTimeTextView = itemView.findViewById(R.id.reminderTimeTextView);
            trashIcon = itemView.findViewById(R.id.trashIcon);
        }
    }
}
