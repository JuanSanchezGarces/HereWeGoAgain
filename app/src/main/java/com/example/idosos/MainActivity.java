package com.example.idosos;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.view.View;
import androidx.cardview.widget.CardView;
import android.widget.TextView;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.MotionEvent;

import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private TextView statusTextView;
    private SharedPreferences sharedPreferences;
    private String numeroCadastrado;
    public QuedaDetect quedaDetect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(this, QuedaDetectService.class);
        this.startService(serviceIntent);

        quedaDetect = new QuedaDetect(this);
        statusTextView = findViewById(R.id.statusTextView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Botão para ir para a tela Lembretes
        CardView reminderCardView = findViewById(R.id.ReminderView);
        reminderCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReminderReceiver.class);
                startActivity(intent);
            }
        });

        // Botão para ir para a tela CadastroNumero
        CardView cadastrarNumeroCardView = findViewById(R.id.contactCardView);
        cadastrarNumeroCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CadastroNumero.class);
                startActivity(intent);
            }
        });

        // Animação de escala para o botão de pânico
        final Animation scale = AnimationUtils.loadAnimation(this, R.anim.scale);
        final CardView panicCardView = findViewById(R.id.panicCardView);
        final LinearLayout panicAnimationContainer = findViewById(R.id.panicAnimationContainer);

        panicCardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    panicAnimationContainer.startAnimation(scale);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    panicAnimationContainer.clearAnimation();
                    Toast.makeText(this, "Corinthians!", Toast.LENGTH_SHORT).show();
                    enviarMensagemPanico();
                    return true;
                }
                return false;
            }
        });

        if (accelerometer == null) {
            Toast.makeText(this, "Seu dispositivo não suporta o acelerômetro.", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (gyroscope == null) {
            Toast.makeText(this, "Seu dispositivo não suporta giroscópio", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarMensagemPanico() {
        EnviarSMS enviarSMS = new EnviarSMS(this);
        enviarSMS.enviarMensagemDeSocorro();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(quedaDetect, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(quedaDetect, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sharedPreferences = getSharedPreferences("MeuAppPreferences", MODE_PRIVATE);
        numeroCadastrado = sharedPreferences.getString("numero_cadastrado", null);
        quedaDetect.iniciarDetecao();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(quedaDetect);
        quedaDetect.pararDetecao();

        Intent serviceIntent = new Intent(this, QuedaDetectService.class);
        this.stopService(serviceIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
