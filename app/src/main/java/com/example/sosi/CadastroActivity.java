package com.example.sosi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class CadastroActivity extends AppCompatActivity {
        // endereço do meu banco (firebase)
        private static final String URL_BANCO = "https://sosi-31c4e-default-rtdb.firebaseio.com";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_cadastro);

            // componentes XML
            EditText edtNome = findViewById(R.id.edtNome);
            EditText edtEmail = findViewById(R.id.edtEmail);
            EditText edtCelular = findViewById(R.id.edtCelular);
            EditText edtCpf = findViewById(R.id.edtCpf);
            EditText edtSenha = findViewById(R.id.edtSenha);
            TextView txtErro = findViewById(R.id.txtErro);
            Button btnCadastrar = findViewById(R.id.btnCadastrar);
            TextView txtEntrarLink = findViewById(R.id.txtEntrarLink);

            // ao clicar em Cadastrar
            btnCadastrar.setOnClickListener(v -> {
                String nome = edtNome.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String celular = edtCelular.getText().toString().trim();
                String cpf = edtCpf.getText().toString().trim();
                String senha = edtSenha.getText().toString().trim();

                if (nome.isEmpty() || email.isEmpty() || celular.isEmpty()
                        || cpf.isEmpty() || senha.isEmpty()) {
                    txtErro.setText("Preencha todos os campos.");
                    txtErro.setVisibility(View.VISIBLE);

                } else if (senha.length() < 6) {
                    //firebase exige senha de no mínimo 6 caracteres
                    txtErro.setText("A senha precisa ter pelo menos 6 caracteres.");
                    txtErro.setVisibility(View.VISIBLE);
                } else {
                    txtErro.setVisibility(View.GONE);
                    btnCadastrar.setEnabled(false);
                    btnCadastrar.setText("Cadastrando...");

                    FirebaseAuth auth = FirebaseAuth.getInstance();

                    //cria a conta (email e senha) no Authentication
                    auth.createUserWithEmailAndPassword(email, senha)
                            .addOnSuccessListener(resultado -> {

                                //uid código unico fornecido pelo firebase para cada usuário
                                String uid = resultado.getUser().getUid();
                                Cidadao cidadao = new Cidadao(nome, email, celular, cpf);


                                // Grava os dados em  cidadaos no Realtime Database
                                FirebaseDatabase.getInstance(URL_BANCO)
                                        .getReference("cidadaos")
                                        .child(uid)
                                        .setValue(cidadao)
                                        .addOnSuccessListener(ok -> {
                                            Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show();
                                            auth.signOut();   // pede para a pessoa entrar pelo Login
                                            finish();         // volta para a tela de Login
                                        })
                                        .addOnFailureListener(erro -> {
                                            mostrarErro(txtErro, btnCadastrar, "Erro ao salvar os dados: " + erro.getMessage());
                                        });
                            })
                            .addOnFailureListener(erro -> {
                                mostrarErro(txtErro, btnCadastrar, "Erro ao cadastrar: " + erro.getMessage());
                            });
                }
            });

            // ===== ao clicar em "Entrar": volta para o Login =====
            txtEntrarLink.setOnClickListener(v -> finish());
        }

        // Mostra a mensagem de erro e libera o botão de novo
        private void mostrarErro(TextView txtErro, Button btnCadastrar, String mensagem) {
            txtErro.setText(mensagem);
            txtErro.setVisibility(View.VISIBLE);
            btnCadastrar.setEnabled(true);
            btnCadastrar.setText("Cadastrar");
        }
    }