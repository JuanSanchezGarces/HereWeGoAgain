package com.example.idosos;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_layout);

        createNotificationChannel();

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

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
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

        Reminder reminder = new Reminder(title, date, time);
        reminders.add(reminder);
        reminderAdapter.notifyDataSetChanged();
        saveReminders();

        // Calculate delay
        long currentTimeMillis = System.currentTimeMillis();
        long delay = calendar.getTimeInMillis() - currentTimeMillis;

        // Schedule notification
        scheduleNotification(title, "É hora do seu lembrete!", delay);

        Toast.makeText(this, "Lembrete adicionado", Toast.LENGTH_SHORT).show();
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

    private void scheduleNotification(String title, String message, long delay) {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (hasPermissions() && isNotificationChannelEnabled()) {
                    sendNotification(title, message);
                }
            }
        };
        handler.postDelayed(runnable, delay);
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isNotificationChannelEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel = notificationManager.getNotificationChannel("reminder_channel");
                return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
            }
        }
        return true; // Se não estiver no Android Oreo ou posterior, supõe-se que as notificações estão habilitadas
    }

    private void sendNotification(String title, String message) {
        try {
            // Tentar enviar a notificação
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "reminder_channel")
                    .setSmallIcon(R.drawable.ic_notification2)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            // Lidar com a exceção de segurança
            e.printStackTrace();
            // Por exemplo, mostrar uma mensagem de erro ao usuário
            Toast.makeText(this, "Erro ao enviar notificação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminder Channel";
            String description = "Channel for Reminder Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("reminder_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida
                Toast.makeText(this, "Permissão concedida. Notificação será enviada.", Toast.LENGTH_SHORT).show();
            } else {
                // Permissão negada
                Toast.makeText(this, "Permissão negada. Não será possível enviar a notificação.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
