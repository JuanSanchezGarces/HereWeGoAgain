package com.example.idosos;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.View;
import android.app.PendingIntent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class AddReminderActivity extends AppCompatActivity implements ReminderAdapter.OnReminderRemovedListener {

    private EditText reminderTitleEditText;
    private TextView selectedDateTextView;
    private TextView selectedTimeTextView;
    private RecyclerView remindersRecyclerView;
    private ReminderAdapter reminderAdapter;
    private List<Reminder> reminders;
    private Calendar calendar;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_layout);

        sharedPreferences = getSharedPreferences("Reminders", Context.MODE_PRIVATE);
        gson = new Gson();

        reminderTitleEditText = findViewById(R.id.reminderTitleEditText);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        selectedTimeTextView = findViewById(R.id.selectedTimeTextView);
        remindersRecyclerView = findViewById(R.id.remindersRecyclerView);
        Button addReminderButton = findViewById(R.id.addReminderButton);

        calendar = Calendar.getInstance();

        reminders = loadReminders();
        reminderAdapter = new ReminderAdapter(reminders, this);
        remindersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        remindersRecyclerView.setAdapter(reminderAdapter);

        addReminderButton.setOnClickListener(v -> addReminder());

        ImageView calendarIcon = findViewById(R.id.calendarIcon);
        calendarIcon.setOnClickListener(v -> openDatePicker());

        ImageView timeIcon = findViewById(R.id.timeIcon);
        timeIcon.setOnClickListener(v -> openTimePicker());

        initializeDayOfWeekSelection();
    }

    private void openDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year1);
                    calendar.set(Calendar.MONTH, month1);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedDateTextView.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void openTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute1);
            selectedTimeTextView.setText(String.format("%02d:%02d", hourOfDay, minute1));
        },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private void resetDaysOfWeek() {
        int[] dayIds = new int[] {
                R.id.daySunday, R.id.dayMonday, R.id.dayTuesday,
                R.id.dayWednesday, R.id.dayThursday,
                R.id.dayFriday, R.id.daySaturday
        };

        for (int dayId : dayIds) {
            TextView dayTextView = findViewById(dayId);
            dayTextView.setSelected(false);
            dayTextView.setTextColor(Color.BLACK); // Ajusta a cor do texto conforme necessário
        }
    }


    private void addReminder() {
        String title = reminderTitleEditText.getText().toString();
        String date = selectedDateTextView.getText().toString();
        String time = selectedTimeTextView.getText().toString();

        if (title.isEmpty() || date.equals("Escolha a data") || time.equals("Escolha o horário")) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get days of the week
        boolean[] daysOfWeek = new boolean[7];
        daysOfWeek[0] = findViewById(R.id.daySunday).isSelected();
        daysOfWeek[1] = findViewById(R.id.dayMonday).isSelected();
        daysOfWeek[2] = findViewById(R.id.dayTuesday).isSelected();
        daysOfWeek[3] = findViewById(R.id.dayWednesday).isSelected();
        daysOfWeek[4] = findViewById(R.id.dayThursday).isSelected();
        daysOfWeek[5] = findViewById(R.id.dayFriday).isSelected();
        daysOfWeek[6] = findViewById(R.id.daySaturday).isSelected();

        Reminder reminder = new Reminder(title, date, time, daysOfWeek);
        reminders.add(reminder);
        reminderAdapter.notifyDataSetChanged();
        saveReminders();

        // Schedule notifications for each selected day
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i]) {
                scheduleNotification(reminder, i);
            }
        }

        Toast.makeText(this, "Lembrete adicionado", Toast.LENGTH_SHORT).show();

        // Reset fields
        reminderTitleEditText.setText("");
        selectedDateTextView.setText("Escolha a data");
        selectedTimeTextView.setText("Escolha o horário");

        // Reset days of the week to unselected
        resetDaysOfWeek();
    }


    private void scheduleNotification(Reminder reminder, int dayOfWeek) {
        Calendar now = Calendar.getInstance();
        Calendar notificationTime = (Calendar) calendar.clone();
        notificationTime.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1);

        if (notificationTime.before(now)) {
            notificationTime.add(Calendar.WEEK_OF_YEAR, 1);
        }

        long delay = notificationTime.getTimeInMillis() - now.getTimeInMillis();

        Data data = new Data.Builder()
                .putString("title", reminder.getTitle())
                .putString("message", "É hora do seu lembrete!")
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(this).enqueue(notificationWork);
    }

    private void saveReminders() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(reminders);
        editor.putString("reminder_list", json);
        editor.apply();
    }

    private List<Reminder> loadReminders() {
        String json = sharedPreferences.getString("reminder_list", null);
        Type type = new TypeToken<ArrayList<Reminder>>() {}.getType();
        List<Reminder> loadedReminders = gson.fromJson(json, type);
        return loadedReminders == null ? new ArrayList<>() : loadedReminders;
    }

    @Override
    public void onReminderRemoved(Reminder reminder) {
        cancelReminderNotification(reminder);
        reminders.remove(reminder); // Remove o lembrete da lista
        reminderAdapter.notifyDataSetChanged();
        saveReminders();
    }

    private void cancelReminderNotification(Reminder reminder) {
        // Cancele a notificação para cada dia da semana associado ao lembrete
        for (int i = 0; i < reminder.getDaysOfWeek().length; i++) {
            if (reminder.getDaysOfWeek()[i]) {
                cancelNotificationForDay(reminder, i);
            }
        }
    }

    private void cancelNotificationForDay(Reminder reminder, int dayOfWeek) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                reminder.hashCode() + dayOfWeek,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        pendingIntent.cancel(); // Cancela o PendingIntent associado à notificação
    }

    private void setupDayOfWeekSelection(final TextView dayView) {
        dayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = dayView.isSelected();
                dayView.setSelected(!isSelected);
                dayView.setTextColor(isSelected ? ContextCompat.getColor(getApplicationContext(), android.R.color.black) : ContextCompat.getColor(getApplicationContext(), android.R.color.white));
            }
        });
    }

    private void initializeDayOfWeekSelection() {
        setupDayOfWeekSelection(findViewById(R.id.daySunday));
        setupDayOfWeekSelection(findViewById(R.id.dayMonday));
        setupDayOfWeekSelection(findViewById(R.id.dayTuesday));
        setupDayOfWeekSelection(findViewById(R.id.dayWednesday));
        setupDayOfWeekSelection(findViewById(R.id.dayThursday));
        setupDayOfWeekSelection(findViewById(R.id.dayFriday));
        setupDayOfWeekSelection(findViewById(R.id.daySaturday));
    }

    public void voltarParaPaginaInicial(View view) {
        Intent intent = new Intent(this, MainActivity.class); // Substitua "MainActivity" pelo nome da sua classe principal, se for diferente.
        startActivity(intent);
    }
}
