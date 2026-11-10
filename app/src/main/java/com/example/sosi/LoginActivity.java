package com.example.sosi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final String URL_BANCO = "https://sosi-31c4e-default-rtdb.firebaseio.com";

    private TextView txtErro;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));   // ícones da barra de status em branco
        setContentView(R.layout.activity_login);

        // Teclado não cobre os campos
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizLogin), (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(barras.left, 0, barras.right, Math.max(barras.bottom, teclado.bottom));
            return insets;
        });

        // ===== pegar os componentes do XML =====
        EditText edtLogin = findViewById(R.id.edtLogin);
        EditText edtSenha = findViewById(R.id.edtSenha);
        txtErro = findViewById(R.id.txtErro);
        btnEntrar = findViewById(R.id.btnEntrar);
        RadioGroup rgAbas = findViewById(R.id.rgAbas);
        TextView txtTituloLogin = findViewById(R.id.txtTituloLogin);
        LinearLayout layoutCadastro = findViewById(R.id.layoutCadastro);
        TextView txtCadastrese = findViewById(R.id.txtCadastrese);
        LinearLayout layoutGoogle = findViewById(R.id.layoutGoogle);
        Button btnGoogle = findViewById(R.id.btnGoogle);
        TextView txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha);

        // ===== trocar de aba =====
        rgAbas.setOnCheckedChangeListener((grupo, idMarcado) -> {
            txtErro.setVisibility(View.GONE);

            if (idMarcado == R.id.abaCidadao) {
                txtTituloLogin.setText("Portal do Cidadão");
                edtLogin.setHint("E-mail");
                layoutCadastro.setVisibility(View.VISIBLE);
                layoutGoogle.setVisibility(View.VISIBLE);
                txtEsqueciSenha.setVisibility(View.VISIBLE);
            } else {
                txtTituloLogin.setText("Painel da Prefeitura");
                edtLogin.setHint("E-mail da prefeitura");
                layoutCadastro.setVisibility(View.GONE);
                layoutGoogle.setVisibility(View.GONE);
                txtEsqueciSenha.setVisibility(View.GONE);
            }
        });

        // ===== ao clicar em Cadastre-se =====
        txtCadastrese.setOnClickListener(v -> {
            Intent intent = new Intent(this, CadastroActivity.class);
            startActivity(intent);
        });

        // ===== ao clicar em Esqueci minha senha =====
        txtEsqueciSenha.setOnClickListener(v -> {
            String email = edtLogin.getText().toString().trim();

            if (email.isEmpty()) {
                mostrarErro("Digite seu e-mail no campo acima e toque de novo em \"Esqueci minha senha\".");
                return;
            }

            FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .addOnSuccessListener(ok -> {
                        txtErro.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Se este e-mail estiver cadastrado, você vai receber um link para criar uma nova senha. Confira também o spam.",
                                Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(erro ->
                            mostrarErro("Não foi possível enviar. Confira se o e-mail está escrito certo."));
        });

        // ===== ao clicar em Entrar (e-mail e senha) =====
        btnEntrar.setOnClickListener(v -> {
            String login = edtLogin.getText().toString().trim();
            String senha = edtSenha.getText().toString().trim();

            if (login.isEmpty() || senha.isEmpty()) {
                mostrarErro("Preencha e-mail/login e senha.");
                return;
            }

            boolean abaPrefeitura = rgAbas.getCheckedRadioButtonId() == R.id.abaPrefeitura;

            txtErro.setVisibility(View.GONE);
            btnEntrar.setEnabled(false);
            btnEntrar.setText("Entrando...");

            FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(login, senha)
                    .addOnSuccessListener(resultado -> {
                        String email = resultado.getUser().getEmail();
                        boolean contaPrefeitura = BaseActivity.EMAIL_PREFEITURA.equalsIgnoreCase(email);

                        if (abaPrefeitura && contaPrefeitura) {
                            abrirTela(PainelPrefeituraActivity.class);
                        } else if (!abaPrefeitura && !contaPrefeitura) {
                            abrirTela(HomeActivity.class);
                        } else {
                            FirebaseAuth.getInstance().signOut();
                            if (abaPrefeitura) {
                                mostrarErro("Esta conta não é da prefeitura.");
                            } else {
                                mostrarErro("Para esta conta, use a aba \"Sou Prefeitura\".");
                            }
                            liberarBotao();
                        }
                    })
                    .addOnFailureListener(erro -> {
                        mostrarErro("E-mail ou senha incorretos.");
                        liberarBotao();
                    });
        });

        // ===== ao clicar em Entrar com Google =====
        btnGoogle.setOnClickListener(v -> entrarComGoogle());
    }

    // ---------------------------------------------------------------
    // LOGIN COM GOOGLE
    // ---------------------------------------------------------------

    /** 1) Pede ao Android para mostrar as contas Google do celular. */
    private void entrarComGoogle() {
        txtErro.setVisibility(View.GONE);

        GetGoogleIdOption opcaoGoogle = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest pedido = new GetCredentialRequest.Builder()
                .addCredentialOption(opcaoGoogle)
                .build();

        CredentialManager gerenciador = CredentialManager.create(this);

        gerenciador.getCredentialAsync(
                this,
                pedido,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse resposta) {
                        receberContaGoogle(resposta.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException erro) {
                        if (erro instanceof GetCredentialCancellationException) {
                            return;
                        }
                        mostrarErro("Não foi possível entrar com Google: " + erro.getMessage());
                    }
                });
    }

    /** 2) Pega o "crachá" (token) que o Google devolveu e entrega ao Firebase. */
    private void receberContaGoogle(Credential credencial) {
        if (credencial instanceof CustomCredential
                && credencial.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

            GoogleIdTokenCredential contaGoogle =
                    GoogleIdTokenCredential.createFrom(((CustomCredential) credencial).getData());

            AuthCredential credencialFirebase =
                    GoogleAuthProvider.getCredential(contaGoogle.getIdToken(), null);

            FirebaseAuth.getInstance()
                    .signInWithCredential(credencialFirebase)
                    .addOnSuccessListener(resultado -> {
                        boolean primeiraVez = resultado.getAdditionalUserInfo() != null
                                && resultado.getAdditionalUserInfo().isNewUser();

                        if (primeiraVez) {
                            salvarCidadaoGoogle(resultado.getUser());
                        } else {
                            abrirTela(HomeActivity.class);
                        }
                    })
                    .addOnFailureListener(erro ->
                            mostrarErro("Erro ao entrar com Google: " + erro.getMessage()));
        } else {
            mostrarErro("Tipo de login não reconhecido.");
        }
    }

    /** 3) Na primeira vez, grava nome e e-mail em  cidadaos/<uid>. */
    private void salvarCidadaoGoogle(FirebaseUser usuario) {
        Cidadao cidadao = new Cidadao(usuario.getDisplayName(), usuario.getEmail(), "", "");

        FirebaseDatabase.getInstance(URL_BANCO)
                .getReference("cidadaos")
                .child(usuario.getUid())
                .setValue(cidadao)
                .addOnSuccessListener(ok -> abrirTela(HomeActivity.class))
                .addOnFailureListener(erro ->
                        mostrarErro("Erro ao salvar os dados: " + erro.getMessage()));
    }

    // ---------------------------------------------------------------

    /** Abre a tela e fecha o Login (o "voltar" não volta para cá). */
    private void abrirTela(Class<?> tela) {
        Toast.makeText(this, "Bem-vindo(a)!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, tela));
        finish();
    }

    private void liberarBotao() {
        btnEntrar.setEnabled(true);
        btnEntrar.setText("Entrar");
    }

    private void mostrarErro(String mensagem) {
        txtErro.setText(mensagem);
        txtErro.setVisibility(View.VISIBLE);
    }
}