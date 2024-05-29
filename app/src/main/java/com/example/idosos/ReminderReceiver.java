package com.example.idosos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extrai a mensagem do lembrete do intent
        String reminderMessage = intent.getStringExtra("reminder_message");

        // Exibe a mensagem do lembrete usando um Toast
        Toast.makeText(context, reminderMessage, Toast.LENGTH_SHORT).show();
    }
}

