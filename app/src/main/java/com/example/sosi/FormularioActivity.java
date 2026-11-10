package com.example.sosi;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class FormularioActivity extends BaseActivity {

    // Dados de quem está enviando (lidos do banco quando a tela abre)
    private String nomeCidadao = "";
    private String celularCidadao = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);
        configurarTopo();

        // Quem está logado? Se ninguém, fecha a tela.
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            finish();
            return;
        }

        // pegar os componentes do XML
        TextView txtUsuario = findViewById(R.id.txtUsuario);
        RadioGroup rgMotivo = findViewById(R.id.rgMotivo);
        Spinner spSituacao = findViewById(R.id.spSituacao);
        EditText edtRua = findViewById(R.id.edtRua);
        EditText edtBairro = findViewById(R.id.edtBairro);
        EditText edtData = findViewById(R.id.edtData);
        EditText edtDescricao = findViewById(R.id.edtDescricao);
        View areaUpload = findViewById(R.id.areaUpload);
        Button btnEnviar = findViewById(R.id.btnEnviar);
        TextView txtMeusFormularios = findViewById(R.id.txtMeusFormularios);

        FirebaseDatabase banco = FirebaseDatabase.getInstance(URL_BANCO);

        // mostrar quem está enviando (lê  cidadaos do banco)
        banco.getReference("cidadaos")
                .child(usuario.getUid())
                .get()
                .addOnSuccessListener(dados -> {
                    Cidadao cidadao = dados.getValue(Cidadao.class);
                    if (cidadao != null) {
                        nomeCidadao = cidadao.nome != null ? cidadao.nome : "";
                        celularCidadao = cidadao.celular != null ? cidadao.celular : "";

                        String texto = "👤  " + nomeCidadao;
                        if (!celularCidadao.isEmpty()) {
                            texto = texto + "  ·  " + celularCidadao;
                        }
                        txtUsuario.setText(texto);
                    }
                });

        // ao tocar na Data: abre um calendário
        edtData.setOnClickListener(v -> {
            Calendar hoje = Calendar.getInstance();

            DatePickerDialog calendario = new DatePickerDialog(
                    this,
                    (view, ano, mes, dia) -> {
                        // O mês começa em 0 (janeiro = 0), por isso o "+ 1"
                        String data = String.format("%02d/%02d/%04d", dia, mes + 1, ano);
                        edtData.setText(data);
                    },
                    hoje.get(Calendar.YEAR),
                    hoje.get(Calendar.MONTH),
                    hoje.get(Calendar.DAY_OF_MONTH)
            );

            // Não deixa escolher uma data no futuro (a ocorrência já aconteceu)
            calendario.getDatePicker().setMaxDate(System.currentTimeMillis());
            calendario.show();
        });

        // foto: fica para depois
        areaUpload.setOnClickListener(v ->
                Toast.makeText(this, "Envio de foto: em breve!", Toast.LENGTH_SHORT).show());

        // ver meus formulários
        txtMeusFormularios.setOnClickListener(v ->
                startActivity(new Intent(this, MeusFormulariosActivity.class)));

        // ao clicar em Enviar
        btnEnviar.setOnClickListener(v -> {

            // Qual motivo foi marcado?
            int idMarcado = rgMotivo.getCheckedRadioButtonId();
            String motivo = "";
            if (idMarcado == R.id.rbElogio) {
                motivo = "Elogio";
            } else if (idMarcado == R.id.rbReclamacao) {
                motivo = "Reclamação";
            } else if (idMarcado == R.id.rbSugestao) {
                motivo = "Sugestão";
            }

            // Qual situação foi escolhida? (posição 0 = "Onde se encaixa?")
            String situacao = "";
            if (spSituacao.getSelectedItemPosition() > 0) {
                situacao = spSituacao.getSelectedItem().toString();
            }

            // Ler os campos de texto
            String rua = edtRua.getText().toString().trim();
            String bairro = edtBairro.getText().toString().trim();
            String data = edtData.getText().toString().trim();
            String descricao = edtDescricao.getText().toString().trim();

            // Validar
            if (motivo.isEmpty() || situacao.isEmpty() || rua.isEmpty()
                    || bairro.isEmpty() || data.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Montar a ocorrência
            Ocorrencia ocorrencia = new Ocorrencia();
            ocorrencia.uidCidadao = usuario.getUid();
            ocorrencia.nomeCidadao = nomeCidadao;
            ocorrencia.celularCidadao = celularCidadao;
            ocorrencia.motivo = motivo;
            ocorrencia.categoria = situacao;
            ocorrencia.endereco = rua + " - " + bairro;
            ocorrencia.dataOcorrido = data;
            ocorrencia.descricao = descricao;
            ocorrencia.status = "Em análise";
            ocorrencia.resposta = "";
            ocorrencia.dataEnvio = System.currentTimeMillis();

            btnEnviar.setEnabled(false);
            btnEnviar.setText("Enviando...");

            // Gravar em  formularios/<código novo>
            DatabaseReference novaOcorrencia = banco.getReference("formularios").push();
            ocorrencia.id = novaOcorrencia.getKey();

            novaOcorrencia.setValue(ocorrencia)
                    .addOnSuccessListener(ok -> {
                        Toast.makeText(this, "Ocorrência enviada com sucesso!", Toast.LENGTH_LONG).show();
                        finish();   // volta para a Home
                    })
                    .addOnFailureListener(erro -> {
                        Toast.makeText(this, "Erro ao enviar: " + erro.getMessage(), Toast.LENGTH_LONG).show();
                        btnEnviar.setEnabled(true);
                        btnEnviar.setText("✈️  Enviar Formulário");
                    });
        });
    }
}