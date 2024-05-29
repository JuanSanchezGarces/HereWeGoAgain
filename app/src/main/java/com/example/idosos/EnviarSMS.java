package com.example.idosos;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class EnviarSMS {
    private String mensagemBase = "LOOKOUT! O Dispositivo detectou uma possível queda!";
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
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.SEND_SMS}, 1);
            } else {
                enviarMensagemDeAlerta(numeroDestino1);
                enviarMensagemDeAlerta(numeroDestino2);
                obterLocalizacaoEEnviarLink(numeroDestino1);
                obterLocalizacaoEEnviarLink(numeroDestino2);
            }
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
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String linkLocalizacao = "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude();
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

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
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
