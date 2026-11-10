package com.example.sosi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        configurarTopo();

        //pegar os componentes do XML
        Button btnAbrirChamado = findViewById(R.id.btnAbrirChamado);
        View cardVias = findViewById(R.id.cardVias);
        View cardIluminacao = findViewById(R.id.cardIluminacao);
        View cardTransito = findViewById(R.id.cardTransito);
        View cardAmbiente = findViewById(R.id.cardAmbiente);

        // todos abrem o formulário
        btnAbrirChamado.setOnClickListener(v -> abrirFormulario());
        cardVias.setOnClickListener(v -> abrirFormulario());
        cardIluminacao.setOnClickListener(v -> abrirFormulario());
        cardTransito.setOnClickListener(v -> abrirFormulario());
        cardAmbiente.setOnClickListener(v -> abrirFormulario());
    }

    // Um método só, usado por 5 botões: evita repetir o mesmo código 5 vezes
    private void abrirFormulario() {
        Intent intent = new Intent(this, FormularioActivity.class);
        startActivity(intent);
    }
}