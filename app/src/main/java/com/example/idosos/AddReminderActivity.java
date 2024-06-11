package com.example.idosos;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        daysOfWeek[0] = ((CheckBox) findViewById(R.id.checkBoxSunday)).isChecked();
        daysOfWeek[1] = ((CheckBox) findViewById(R.id.checkBoxMonday)).isChecked();
        daysOfWeek[2] = ((CheckBox) findViewById(R.id.checkBoxTuesday)).isChecked();
        daysOfWeek[3] = ((CheckBox) findViewById(R.id.checkBoxWednesday)).isChecked();
        daysOfWeek[4] = ((CheckBox) findViewById(R.id.checkBoxThursday)).isChecked();
        daysOfWeek[5] = ((CheckBox) findViewById(R.id.checkBoxFriday)).isChecked();
        daysOfWeek[6] = ((CheckBox) findViewById(R.id.checkBoxSaturday)).isChecked();

        Reminder reminder = new Reminder(title, date, time, daysOfWeek);
        reminders.add(reminder);
        reminderAdapter.notifyDataSetChanged();
        saveReminders();

        // Schedule notifications for each selected day
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i]) {
                scheduleNotification(title, time, i);
            }
        }

        Toast.makeText(this, "Lembrete adicionado", Toast.LENGTH_SHORT).show();
    }

    private void scheduleNotification(String title, String time, int dayOfWeek) {
        Calendar now = Calendar.getInstance();
        Calendar notificationTime = (Calendar) calendar.clone();
        notificationTime.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1);

        if (notificationTime.before(now)) {
            notificationTime.add(Calendar.WEEK_OF_YEAR, 1);
        }

        long delay = notificationTime.getTimeInMillis() - now.getTimeInMillis();

        Data data = new Data.Builder()
                .putString("title", title)
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
    public void onReminderRemoved() {
        saveReminders();
    }
    public void voltarParaPaginaInicial(View view) {
        Intent intent = new Intent(this, MainActivity.class); // Substitua "MainActivity" pelo nome da sua classe principal, se for diferente.
        startActivity(intent);
    }
}

