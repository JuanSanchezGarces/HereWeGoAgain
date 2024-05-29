package com.example.idosos;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;

public class SobreNos extends AppCompatActivity {

    public QuedaDetect quedaDetect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre_nos);

        quedaDetect = new QuedaDetect(this);
    }

    // Ative a detecção de quedas quando a tela "Sobre nós" for exibida
    @Override
    protected void onResume() {
        super.onResume();
        quedaDetect.iniciarDetecao();
    }

    // Desative a detecção de quedas quando sair da tela "Sobre nós"
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        quedaDetect.pararDetecao();
    }

    public void voltarParaPaginaInicial(View view) {
        Intent intent = new Intent(this, MainActivity.class); // Substitua "MainActivity" pelo nome da sua classe principal, se for diferente.
        startActivity(intent);
    }
}
