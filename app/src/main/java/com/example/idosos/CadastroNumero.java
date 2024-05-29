package com.example.idosos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CadastroNumero extends AppCompatActivity {
    private EditText contactNameEditText1;
    private EditText contactPhoneEditText1;
    private EditText contactNameEditText2;
    private EditText contactPhoneEditText2;
    private TextView statusTextView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_numero);

        // Inicializar as views
        contactNameEditText1 = findViewById(R.id.contactNameEditText1);
        contactPhoneEditText1 = findViewById(R.id.contactPhoneEditText1);
        contactNameEditText2 = findViewById(R.id.contactNameEditText2);
        contactPhoneEditText2 = findViewById(R.id.contactPhoneEditText2);
        statusTextView = findViewById(R.id.statusTextView);

        // Inicialize o SharedPreferences
        sharedPreferences = getSharedPreferences("MeuAppPreferences", MODE_PRIVATE);

        // Recuperar os dados salvos (se existirem)
        recuperarDadosSalvos();

        // Configurar o botão de salvar
        Button salvarNumeroButton = findViewById(R.id.salvarNumeroButton);
        salvarNumeroButton.setOnClickListener(view -> salvarContatos());

        // Configurar o botão de voltar à página inicial
        Button voltarButton = findViewById(R.id.voltarParaPaginaInicial);
        voltarButton.setOnClickListener(this::voltarParaPaginaInicial);
    }

    private void recuperarDadosSalvos() {
        String nomeContato1 = sharedPreferences.getString("nome_contato_1", null);
        String telefoneContato1 = sharedPreferences.getString("telefone_contato_1", null);
        String nomeContato2 = sharedPreferences.getString("nome_contato_2", null);
        String telefoneContato2 = sharedPreferences.getString("telefone_contato_2", null);

        if (nomeContato1 != null) {
            contactNameEditText1.setText(nomeContato1);
        }
        if (telefoneContato1 != null) {
            contactPhoneEditText1.setText(telefoneContato1);
        }
        if (nomeContato2 != null) {
            contactNameEditText2.setText(nomeContato2);
        }
        if (telefoneContato2 != null) {
            contactPhoneEditText2.setText(telefoneContato2);
        }
    }

    private void salvarContatos() {
        String nomeContato1 = contactNameEditText1.getText().toString().trim();
        String telefoneContato1 = contactPhoneEditText1.getText().toString().trim();
        String nomeContato2 = contactNameEditText2.getText().toString().trim();
        String telefoneContato2 = contactPhoneEditText2.getText().toString().trim();

        // Verifique se todos os campos estão preenchidos
        if (nomeContato1.isEmpty() || telefoneContato1.isEmpty() || nomeContato2.isEmpty() || telefoneContato2.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Salve os dados no SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nome_contato_1", nomeContato1);
        editor.putString("telefone_contato_1", telefoneContato1);
        editor.putString("nome_contato_2", nomeContato2);
        editor.putString("telefone_contato_2", telefoneContato2);
        editor.apply();


        // Mostrar uma mensagem de sucesso
        Toast.makeText(this, "Contatos salvos com sucesso!", Toast.LENGTH_SHORT).show();
    }

    public void voltarParaPaginaInicial(View view) {
        Intent intent = new Intent(this, MainActivity.class); // Substitua "MainActivity" pelo nome da sua classe principal, se for diferente.
        startActivity(intent);
    }
}
