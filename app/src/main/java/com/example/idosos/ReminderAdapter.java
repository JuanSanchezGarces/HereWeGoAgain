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
        holder.titleTextView.setText(reminder.getTitle());
        holder.dateTextView.setText(reminder.getDate());
        holder.timeTextView.setText(reminder.getTime());
        holder.daysTextView.setText(getSelectedDays(reminder.getDaysOfWeek()));

        holder.trashIcon.setOnClickListener(v -> {
            reminders.remove(position);
            notifyItemRemoved(position);
            listener.onReminderRemoved();

        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    private String getSelectedDays(boolean[] daysOfWeek) {
        StringBuilder selectedDays = new StringBuilder();
        String[] dayNames = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i]) {
                if (selectedDays.length() > 0) {
                    selectedDays.append(", ");
                }
                selectedDays.append(dayNames[i]);
            }
        }
        return selectedDays.toString();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTextView, timeTextView, daysTextView;
        ImageView trashIcon;

        ReminderViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.reminderTitleTextView);
            dateTextView = itemView.findViewById(R.id.reminderDateTextView);
            timeTextView = itemView.findViewById(R.id.reminderTimeTextView);
            daysTextView = itemView.findViewById(R.id.reminderDaysTextView);
            trashIcon = itemView.findViewById(R.id.trashIcon);
        }
    }
}
