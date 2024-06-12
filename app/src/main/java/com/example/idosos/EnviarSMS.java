package com.example.idosos;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class EnviarSMS {
    private String mensagemBase = "Alerta aplicativo LOOKOUT! Uma possível queda foi detectada.";
    private Context context;
    private LocationManager locationManager;

    public EnviarSMS(Context context) {
        this.context = context;
    }

    public void enviarMensagemDeSocorro() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MeuAppPreferences", Context.MODE_PRIVATE);
        String numeroDestino1 = sharedPreferences.getString("telefone_contato_1", null);
        String numeroDestino2 = sharedPreferences.getString("telefone_contato_2", null);

        if (numeroDestino1 != null || numeroDestino2 != null) {
            enviarMensagemDeAlerta(numeroDestino1);
            enviarMensagemDeAlerta(numeroDestino2);
            obterLocalizacaoEEnviarLink(numeroDestino1);
            obterLocalizacaoEEnviarLink(numeroDestino2);
        } else {
            Toast.makeText(context, "Número de destino não cadastrado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarMensagemDeAlerta(String numeroDestino) {
        if (numeroDestino != null) {
            enviarSMS(numeroDestino, mensagemBase);
        }
    }

    private void obterLocalizacaoEEnviarLink(final String numeroDestino) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permissão de localização não concedida.", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                String linkLocalizacao = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                enviarSMS(numeroDestino, linkLocalizacao);
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private void enviarSMS(String numeroDestino, String mensagem) {
        if (numeroDestino != null) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(numeroDestino, null, mensagem, null, null);
                Toast.makeText(context, "SMS enviado para " + numeroDestino, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Erro ao enviar mensagem para " + numeroDestino, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
