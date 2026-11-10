package com.example.sosi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PerfilActivity extends BaseActivity {

    // Componentes usados em vários métodos
    private TextView txtAvatar, txtSubtitulo, txtNome, txtEmail, txtCelular, txtCpf;
    private EditText edtNome, edtCelular, edtCpf;
    private LinearLayout layoutVisualizar, layoutEditar;
    private Button btnEditarPerfil, btnSair;

    private DatabaseReference refCidadao;   // cidadaos/<uid>
    private Cidadao cidadaoAtual;           // dados que estão na tela agora
    private boolean editando = false;       // está no modo "editar"?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        configurarTopo();

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            finish();
            return;
        }

        // pegar os componentes do XML
        txtAvatar = findViewById(R.id.txtAvatar);
        txtSubtitulo = findViewById(R.id.txtSubtitulo);
        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        txtCelular = findViewById(R.id.txtCelular);
        txtCpf = findViewById(R.id.txtCpf);
        edtNome = findViewById(R.id.edtNome);
        edtCelular = findViewById(R.id.edtCelular);
        edtCpf = findViewById(R.id.edtCpf);
        layoutVisualizar = findViewById(R.id.layoutVisualizar);
        layoutEditar = findViewById(R.id.layoutEditar);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnSair = findViewById(R.id.btnSair);

        refCidadao = FirebaseDatabase.getInstance(URL_BANCO)
                .getReference("cidadaos")
                .child(usuario.getUid());

        //  botão da esquerda: "Editar Perfil" ou "Salvar"
        btnEditarPerfil.setOnClickListener(v -> {
            if (editando) {
                salvar();
            } else {
                mostrarModoEditar();
            }
        });

        //  botão da direita: "Sair" ou "Cancelar"
        btnSair.setOnClickListener(v -> {
            if (editando) {
                mostrarModoVer();   // cancela sem salvar
            } else {
                sair();
            }
        });

        carregarDados();
    }

    /** Busca os dados no banco e mostra no modo "ver". */
    private void carregarDados() {
        refCidadao.get().addOnSuccessListener(dados -> {
            Cidadao cidadao = dados.getValue(Cidadao.class);
            if (cidadao == null) {
                return;
            }
            cidadaoAtual = cidadao;

            txtNome.setText(cidadao.nome);
            txtEmail.setText(cidadao.email);
            txtCelular.setText(textoOuNaoInformado(cidadao.celular));
            txtCpf.setText(textoOuNaoInformado(cidadao.cpf));

            if (cidadao.nome != null && !cidadao.nome.isEmpty()) {
                txtAvatar.setText(cidadao.nome.substring(0, 1).toUpperCase());
            }
        });
    }

    /** Troca a tela para o modo "editar": esconde os textos e mostra os campos. */
    private void mostrarModoEditar() {
        if (cidadaoAtual == null) {
            return;   // os dados ainda não chegaram do banco
        }
        editando = true;

        edtNome.setText(cidadaoAtual.nome);
        edtCelular.setText(cidadaoAtual.celular);
        edtCpf.setText(cidadaoAtual.cpf);

        layoutVisualizar.setVisibility(View.GONE);
        layoutEditar.setVisibility(View.VISIBLE);
        txtSubtitulo.setText("Altere seus dados e toque em Salvar");
        btnEditarPerfil.setText("Salvar");
        btnSair.setText("Cancelar");
    }

    /** Volta para o modo "ver". */
    private void mostrarModoVer() {
        editando = false;

        layoutEditar.setVisibility(View.GONE);
        layoutVisualizar.setVisibility(View.VISIBLE);
        txtSubtitulo.setText(R.string.meu_perfil_txt);
        btnEditarPerfil.setText(R.string.editar_perfil);
        btnEditarPerfil.setEnabled(true);
        btnSair.setText(R.string.sair);
    }

    /** Grava nome, celular e CPF no banco. */
    private void salvar() {
        String nome = edtNome.getText().toString().trim();
        String celular = edtCelular.getText().toString().trim();
        String cpf = edtCpf.getText().toString().trim();

        if (nome.isEmpty() || celular.isEmpty() || cpf.isEmpty()) {
            Toast.makeText(this, "Preencha nome, celular e CPF.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> mudancas = new HashMap<>();
        mudancas.put("nome", nome);
        mudancas.put("celular", celular);
        mudancas.put("cpf", cpf);

        btnEditarPerfil.setEnabled(false);
        btnEditarPerfil.setText("Salvando...");

        refCidadao.updateChildren(mudancas)
                .addOnSuccessListener(ok -> {
                    Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                    mostrarModoVer();
                    carregarDados();   // mostra os dados novos
                })
                .addOnFailureListener(erro -> {
                    Toast.makeText(this, "Erro ao salvar: " + erro.getMessage(), Toast.LENGTH_LONG).show();
                    btnEditarPerfil.setEnabled(true);
                    btnEditarPerfil.setText("Salvar");
                });
    }

    /** "" ou null vira "Não informado". */
    private String textoOuNaoInformado(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "Não informado";
        }
        return texto;
    }
}