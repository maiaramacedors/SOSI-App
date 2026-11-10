package com.example.sosi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ResponderFormularioActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder_formulario);
        configurarTopo();

        // Só a prefeitura pode responder
        if (!ehPrefeitura()) {
            finish();
            return;
        }

        // Qual ocorrência? O painel manda o "id" junto com o Intent
        String idOcorrencia = getIntent().getStringExtra("id");
        if (idOcorrencia == null) {
            finish();
            return;
        }

        //  pegar os componentes do XML
        TextView txtMotivoCategoria = findViewById(R.id.txtMotivoCategoria);
        TextView txtCidadao = findViewById(R.id.txtCidadao);
        TextView txtCelular = findViewById(R.id.txtCelular);
        TextView txtLocal = findViewById(R.id.txtLocal);
        TextView txtData = findViewById(R.id.txtData);
        TextView txtDescricao = findViewById(R.id.txtDescricao);
        Spinner spStatus = findViewById(R.id.spStatus);
        EditText edtResposta = findViewById(R.id.edtResposta);
        Button btnSalvar = findViewById(R.id.btnSalvar);

        // Endereço desta ocorrência no banco:  formularios/<id>
        DatabaseReference refOcorrencia = FirebaseDatabase.getInstance(URL_BANCO)
                .getReference("formularios")
                .child(idOcorrencia);

        //  buscar os dados e preencher a tela
        refOcorrencia.get().addOnSuccessListener(dados -> {
            Ocorrencia ocorrencia = dados.getValue(Ocorrencia.class);
            if (ocorrencia == null) {
                Toast.makeText(this, "Ocorrência não encontrada.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            txtMotivoCategoria.setText(ocorrencia.motivo + " — " + ocorrencia.categoria);
            txtCidadao.setText(ocorrencia.nomeCidadao);
            txtLocal.setText(ocorrencia.endereco);
            txtData.setText(ocorrencia.dataOcorrido);
            txtDescricao.setText(ocorrencia.descricao);

            if (ocorrencia.celularCidadao == null || ocorrencia.celularCidadao.isEmpty()) {
                txtCelular.setText("Não informado");
            } else {
                txtCelular.setText(ocorrencia.celularCidadao);
            }

            // Deixa o Spinner já no status atual
            String[] opcoes = getResources().getStringArray(R.array.status_lista);
            for (int i = 0; i < opcoes.length; i++) {
                if (opcoes[i].equals(ocorrencia.status)) {
                    spStatus.setSelection(i);
                }
            }

            edtResposta.setText(ocorrencia.resposta);
        });

        // salvar status e resposta
        btnSalvar.setOnClickListener(v -> {
            String status = spStatus.getSelectedItem().toString();
            String resposta = edtResposta.getText().toString().trim();

            // Só os campos que mudaram
            Map<String, Object> mudancas = new HashMap<>();
            mudancas.put("status", status);
            mudancas.put("resposta", resposta);

            btnSalvar.setEnabled(false);
            btnSalvar.setText("Salvando...");

            refOcorrencia.updateChildren(mudancas)
                    .addOnSuccessListener(ok -> {
                        Toast.makeText(this, "Alterações salvas!", Toast.LENGTH_SHORT).show();
                        finish();   // volta para o painel
                    })
                    .addOnFailureListener(erro -> {
                        Toast.makeText(this, "Erro ao salvar: " + erro.getMessage(), Toast.LENGTH_LONG).show();
                        btnSalvar.setEnabled(true);
                        btnSalvar.setText("Salvar Alterações");
                    });
        });
    }
}