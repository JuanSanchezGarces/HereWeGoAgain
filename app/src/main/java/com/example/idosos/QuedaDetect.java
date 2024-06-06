package com.example.idosos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.PendingIntent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuedaDetect implements SensorEventListener {
    private Context context;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SharedPreferences sharedPreferences;
    private long lastFallTime = 0;
    private boolean fallDetected = false;
    private EnviarSMS enviarSMS;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private TextView timerTextView;
    private MediaPlayer mediaPlayer;
    private NotificationManagerCompat notificationManager;

    // Constants for fall detection criteria
    private static final double AR_THRESHOLD = 10; //Aceleração Resultante
    private static final double VA_THRESHOLD = 35; //Variação Angular
    private static final double MA_THRESHOLD = 105.5; //Mudança no Angulo

    private List<Map<AccelerometerAxis, Double>> accelerometerValues = new ArrayList<>();

    public enum AccelerometerAxis {
        X, Y, Z
    }

    public QuedaDetect(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sharedPreferences = context.getSharedPreferences("MeuAppPreferences", Context.MODE_PRIVATE);
        enviarSMS = new EnviarSMS(context);
        timerHandler = new Handler(Looper.getMainLooper());
        mediaPlayer = MediaPlayer.create(context, R.raw.seu_som);
        notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Canal de Notificação";
            String description = "Descrição do Canal";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("canal_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void iniciarDetecao() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void pararDetecao() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Adiciona os valores do acelerômetro à lista accelerometerValues
            Map<AccelerometerAxis, Double> values = new HashMap<>();
            values.put(AccelerometerAxis.X, (double) x);
            values.put(AccelerometerAxis.Y, (double) y);
            values.put(AccelerometerAxis.Z, (double) z);
            accelerometerValues.add(values);

            // Limita o tamanho da lista para manter apenas os últimos valores do acelerômetro
            if (accelerometerValues.size() > 5) {
                accelerometerValues.remove(0);
            }

            // Calcular variação angular e mudança no ângulo
            double acceleration = Math.sqrt(x * x + y * y + z * z);
            double angleVariation = calculateAngleVariation(x, y, z);
            double changeInAngle = calculateChangeInAngle(x, y, z);

            // Condições de detecção de queda: todos os três critérios devem ser atendidos simultaneamente
            if (acceleration > AR_THRESHOLD && angleVariation > VA_THRESHOLD && changeInAngle > MA_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFallTime > 5000) {
                    lastFallTime = currentTime;
                    fallDetected = true;
                    showFallAlertPopup();
                    Log.d("Detecção de Queda", "Aceleração: " + acceleration + ", Variação Angular: " + angleVariation + ", Mudança no Ângulo: " + changeInAngle);
                }
            }
        }
    }

    private double calculateAngleVariation(float x, float y, float z) {
        int size = accelerometerValues.size();
        if (size < 2) {
            return -1;
        }

        Map<AccelerometerAxis, Double> minusTwo = accelerometerValues.get(size - 2);
        Map<AccelerometerAxis, Double> minusOne = accelerometerValues.get(size - 1);

        // Calcular os vetores de aceleração para os dois conjuntos de leituras
        double[] vectorMinusTwo = {
                minusTwo.get(AccelerometerAxis.X),
                minusTwo.get(AccelerometerAxis.Y),
                minusTwo.get(AccelerometerAxis.Z)
        };

        double[] vectorMinusOne = {
                minusOne.get(AccelerometerAxis.X),
                minusOne.get(AccelerometerAxis.Y),
                minusOne.get(AccelerometerAxis.Z)
        };

        // Calcular o produto interno (ou produto escalar) dos vetores
        double dotProduct = 0;
        for (int i = 0; i < 3; i++) {
            dotProduct += vectorMinusTwo[i] * vectorMinusOne[i];
        }

        // Calcular as magnitudes dos vetores
        double magnitudeMinusTwo = Math.sqrt(
                Math.pow(vectorMinusTwo[0], 2) +
                        Math.pow(vectorMinusTwo[1], 2) +
                        Math.pow(vectorMinusTwo[2], 2)
        );

        double magnitudeMinusOne = Math.sqrt(
                Math.pow(vectorMinusOne[0], 2) +
                        Math.pow(vectorMinusOne[1], 2) +
                        Math.pow(vectorMinusOne[2], 2)
        );

        // Calcular o ângulo entre os vetores usando a fórmula do arco cosseno
        double angle = Math.acos(dotProduct / (magnitudeMinusTwo * magnitudeMinusOne));

        // Converter o ângulo de radianos para graus
        return Math.toDegrees(angle);
    }

    private double calculateChangeInAngle(float x, float y, float z) {
        int size = accelerometerValues.size();
        if (size < 4) {
            return -1;
        }

        Map<AccelerometerAxis, Double> first = accelerometerValues.get(0);
        Map<AccelerometerAxis, Double> second = accelerometerValues.get(1);
        Map<AccelerometerAxis, Double> third = accelerometerValues.get(size - 2);
        Map<AccelerometerAxis, Double> fourth = accelerometerValues.get(size - 1);

        // Calcular os vetores de aceleração para os conjuntos de leituras
        double[] vector1 = {
                second.get(AccelerometerAxis.X) - first.get(AccelerometerAxis.X),
                second.get(AccelerometerAxis.Y) - first.get(AccelerometerAxis.Y),
                second.get(AccelerometerAxis.Z) - first.get(AccelerometerAxis.Z)
        };

        double[] vector2 = {
                fourth.get(AccelerometerAxis.X) - third.get(AccelerometerAxis.X),
                fourth.get(AccelerometerAxis.Y) - third.get(AccelerometerAxis.Y),
                fourth.get(AccelerometerAxis.Z) - third.get(AccelerometerAxis.Z)
        };

        // Calcular o produto escalar (dot product) dos vetores
        double dotProduct = vector1[0] * vector2[0] + vector1[1] * vector2[1] + vector1[2] * vector2[2];

        // Calcular as magnitudes dos vetores
        double magnitude1 = Math.sqrt(Math.pow(vector1[0], 2) + Math.pow(vector1[1], 2) + Math.pow(vector1[2], 2));
        double magnitude2 = Math.sqrt(Math.pow(vector2[0], 2) + Math.pow(vector2[1], 2) + Math.pow(vector2[2], 2));

        // Calcular o ângulo entre os vetores usando a fórmula do produto escalar
        double angle = Math.acos(dotProduct / (magnitude1 * magnitude2));

        // Converter o ângulo de radianos para graus
        double angleDegrees = Math.toDegrees(angle);

        // Threshold em graus
        double thresholdDegrees = 65.5;

        // Comparar o ângulo calculado com o threshold
        if (angleDegrees > thresholdDegrees) {
            // Se a mudança angular exceder o threshold, retornar o valor em graus
            return angleDegrees;
        } else {
            // Caso contrário, retornar -1 para indicar que a mudança angular está dentro do limite
            return -1;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void showFallAlertPopup() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "canal_id")
                .setSmallIcon(R.drawable.ic_notification2)
                .setContentTitle("Queda Detectada")
                .setContentText("Toque para mais detalhes")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{android.Manifest.permission.VIBRATE}, 123);
        } else {
            builder.setContentIntent(pendingIntent);
            notificationManager.notify(1, builder.build());
        }

        final Dialog popupDialog = new Dialog(context);
        popupDialog.setContentView(R.layout.popup_layout);

        timerTextView = popupDialog.findViewById(R.id.timerTextView);

        Button confirmButton = popupDialog.findViewById(R.id.confirmButton);
        Button cancelButton = popupDialog.findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(v -> {
            enviarSMS.enviarMensagemDeSocorro();
            Toast.makeText(context, "Mensagem de alerta enviada!", Toast.LENGTH_SHORT).show();
            popupDialog.dismiss();
            timerHandler.removeCallbacks(timerRunnable);
        });

        cancelButton.setOnClickListener(v -> {
            Toast.makeText(context, "Alerta cancelado!", Toast.LENGTH_SHORT).show();
            popupDialog.dismiss();
            timerHandler.removeCallbacks(timerRunnable);
        });

        int popupTimeout = 10000;
        timerRunnable = new Runnable() {
            int remainingTime = popupTimeout / 1000;

            @Override
            public void run() {
                timerTextView.setText("Tempo restante: " + remainingTime + "s");
                remainingTime--;

                if (remainingTime >= 0) {
                    timerHandler.postDelayed(this, 1000);
                } else {
                    enviarSMS.enviarMensagemDeSocorro();
                    Toast.makeText(context, "Mensagem de alerta enviada automaticamente!", Toast.LENGTH_SHORT).show();
                    popupDialog.dismiss();
                }
            }
        };
        timerHandler.post(timerRunnable);

        popupDialog.show();
    }
}
