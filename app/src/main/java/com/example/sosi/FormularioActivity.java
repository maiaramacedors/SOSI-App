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
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FormularioActivity extends BaseActivity {

    // Dados de quem está enviando (lidos do banco quando a tela abre)
    private String nomeCidadao = "";
    private String celularCidadao = "";

    private static final int REQUEST_LOCATION = 100;

    private FusedLocationProviderClient
            fusedLocationClient;

    private EditText edtRua;
    private EditText edtBairro;

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
        edtRua = findViewById(R.id.edtRua);
        edtBairro = findViewById(R.id.edtBairro);
        EditText edtData = findViewById(R.id.edtData);
        EditText edtDescricao = findViewById(R.id.edtDescricao);
        View areaUpload = findViewById(R.id.areaUpload);
        Button btnEnviar = findViewById(R.id.btnEnviar);
        TextView txtMeusFormularios = findViewById(R.id.txtMeusFormularios);

        FirebaseDatabase banco = FirebaseDatabase.getInstance(URL_BANCO);

        // Inicializar serviço de localização
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

// Solicitar localização ao abrir a página
        verificarPermissaoLocalizacao();


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

    private void obterLocalizacao() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {

            if (location != null) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                buscarEndereco(latitude, longitude);

            } else {

                Toast.makeText(
                        this,
                        "Não foi possível obter sua localização.",
                        Toast.LENGTH_LONG
                ).show();
            }

        }).addOnFailureListener(erro -> {

            Toast.makeText(
                    this,
                    "Erro ao obter localização: " + erro.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        });
    }

    private void verificarPermissaoLocalizacao() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {

            obterLocalizacao();

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_LOCATION) {

            boolean permitido = false;

            for (int resultado : grantResults) {

                if (resultado == PackageManager.PERMISSION_GRANTED) {
                    permitido = true;
                    break;
                }
            }

            if (permitido) {

                obterLocalizacao();

            } else {

                Toast.makeText(
                        this,
                        "Permissão de localização negada. Preencha o endereço manualmente.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void buscarEndereco(double latitude, double longitude) {

        new Thread(() -> {

            Geocoder geocoder = new Geocoder(
                    this,
                    new Locale("pt", "BR")
            );

            try {

                List<Address> enderecos =
                        geocoder.getFromLocation(latitude, longitude, 1);

                if (enderecos != null && !enderecos.isEmpty()) {

                    Address endereco = enderecos.get(0);

                    String rua = endereco.getThoroughfare();
                    String bairro = endereco.getSubLocality();

                    // Atualizar os campos na interface
                    new Handler(Looper.getMainLooper()).post(() -> {

                        if (rua != null && !rua.isEmpty()) {
                            edtRua.setText(rua);
                        }

                        if (bairro != null && !bairro.isEmpty()) {
                            edtBairro.setText(bairro);
                        }

                        Toast.makeText(
                                this,
                                "Endereço localizado!",
                                Toast.LENGTH_SHORT
                        ).show();

                    });

                }

            } catch (IOException e) {

                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(
                                this,
                                "Erro ao consultar endereço.",
                                Toast.LENGTH_SHORT
                        ).show()
                );

            }

        }).start();
    }
}