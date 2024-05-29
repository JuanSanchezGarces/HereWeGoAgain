package com.example.idosos;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class QuedaDetectService extends Service {
    private QuedaDetect quedaDetect;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("QuedaDetectService", "Serviço criado 1");
        quedaDetect = new QuedaDetect(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("QuedaDetectService", "Serviço iniciado 2");
        quedaDetect.iniciarDetecao();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("QuedaDetectService", "Serviço encerrado 3");
        quedaDetect.pararDetecao();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


