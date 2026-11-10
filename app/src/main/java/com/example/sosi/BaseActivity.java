package com.example.sosi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Classe "mãe" das telas do SOSI.
 * Quem faz  "extends BaseActivity"  ganha a barra do topo e o menu lateral.
 */
public class BaseActivity extends AppCompatActivity {

    protected static final String URL_BANCO = "https://sosi-31c4e-default-rtdb.firebaseio.com";

    // Quem entra com este e-mail é a prefeitura
    protected static final String EMAIL_PREFEITURA = "prefeitura@sosi.com";

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
    }

    /**
     * Toda tela chama setContentView(R.layout.xxx).
     * Aqui a gente "embrulha" essa tela dentro da gaveta (activity_base.xml).
     */
    @Override
    public void setContentView(int layoutDaTela) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout conteudoTela = drawerLayout.findViewById(R.id.conteudoTela);
        getLayoutInflater().inflate(layoutDaTela, conteudoTela, true);
        super.setContentView(drawerLayout);
    }

    /** true se a pessoa logada agora é a prefeitura. */
    protected boolean ehPrefeitura() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        return usuario != null && EMAIL_PREFEITURA.equalsIgnoreCase(usuario.getEmail());
    }

    /** Chame logo depois do setContentView, em cada tela. */
    protected void configurarTopo() {
        ajustarBordas();
        configurarMenuLateral();

        // Etiqueta "Prefeitura" no topo
        TextView txtTagPrefeitura = findViewById(R.id.txtTagPrefeitura);
        txtTagPrefeitura.setVisibility(ehPrefeitura() ? View.VISIBLE : View.GONE);

        // ☰ abre o menu
        findViewById(R.id.btnMenu).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        // Logo leva para a tela inicial de cada um
        findViewById(R.id.logoSosi).setOnClickListener(v -> {
            if (ehPrefeitura()) {
                irPara(PainelPrefeituraActivity.class);
            } else {
                irPara(HomeActivity.class);
            }
        });
    }

    /** Mostra só as opções certas e diz o que cada uma faz. */
    private void configurarMenuLateral() {
        NavigationView menuLateral = findViewById(R.id.menuLateral);
        Menu menu = menuLateral.getMenu();

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        boolean logado = usuario != null;
        boolean prefeitura = ehPrefeitura();
        boolean cidadao = logado && !prefeitura;

        // ===== quem vê o quê =====
        menu.findItem(R.id.navInicio).setVisible(!prefeitura);
        menu.findItem(R.id.navNovaOcorrencia).setVisible(cidadao);
        menu.findItem(R.id.navMeusFormularios).setVisible(cidadao);
        menu.findItem(R.id.navPerfil).setVisible(cidadao);
        menu.findItem(R.id.navPainel).setVisible(prefeitura);
        menu.findItem(R.id.navEntrar).setVisible(!logado);
        menu.findItem(R.id.navSair).setVisible(logado);

        // ===== cabeçalho (parte vinho) =====
        View cabecalho = menuLateral.getHeaderView(0);
        TextView txtNomeMenu = cabecalho.findViewById(R.id.txtNomeMenu);
        TextView txtTipoMenu = cabecalho.findViewById(R.id.txtTipoMenu);

        if (prefeitura) {
            txtNomeMenu.setText("Prefeitura de Indaiatuba");
            txtTipoMenu.setVisibility(View.GONE);    // ← esconde a linha de baixo
        } else if (cidadao) {
            txtTipoMenu.setText("Cidadão");
            FirebaseDatabase.getInstance(URL_BANCO)
                    .getReference("cidadaos")
                    .child(usuario.getUid())
                    .child("nome")
                    .get()
                    .addOnSuccessListener(dados -> {
                        String nome = dados.getValue(String.class);
                        if (nome != null) {
                            txtNomeMenu.setText(nome);
                        }
                    });
        } else {
            txtNomeMenu.setText("Visitante");
            txtTipoMenu.setText("Entre para abrir chamados");
        }

        // ===== ao tocar numa opção =====
        menuLateral.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();

            if (id == R.id.navInicio) {
                irPara(HomeActivity.class);
            } else if (id == R.id.navNovaOcorrencia) {
                irPara(FormularioActivity.class);
            } else if (id == R.id.navMeusFormularios) {
                irPara(MeusFormulariosActivity.class);
            } else if (id == R.id.navPainel) {
                irPara(PainelPrefeituraActivity.class);
            } else if (id == R.id.navPerfil) {
                irPara(PerfilActivity.class);
            } else if (id == R.id.navSobre) {
                irPara(SobreActivity.class);
            } else if (id == R.id.navContato) {
                irPara(ContatoActivity.class);
            } else if (id == R.id.navEntrar) {
                irPara(LoginActivity.class);
            } else if (id == R.id.navSair) {
                sair();
            }
            return true;
        });

        // ===== botão "voltar" do celular fecha o menu (se estiver aberto) =====
        OnBackPressedCallback fecharMenu = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, fecharMenu);

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(@NonNull View gaveta) {
                fecharMenu.setEnabled(true);
            }

            @Override
            public void onDrawerClosed(@NonNull View gaveta) {
                fecharMenu.setEnabled(false);
            }
        });
    }

    /** Abre outra tela (se já não estiver nela). */
    protected void irPara(Class<?> tela) {
        if (getClass() == tela) {
            return;
        }
        startActivity(new Intent(this, tela));
    }

    /** Sai da conta e volta para o Login, apagando o histórico de telas. */
    protected void sair() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /** Coloca a barra preta atrás do relógio/bateria e o conteúdo acima do teclado. */
    private void ajustarBordas() {
        View conteudo = findViewById(android.R.id.content);
        View topo = findViewById(R.id.topo);
        int espacoOriginal = topo.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(conteudo, (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());

            topo.setPadding(topo.getPaddingLeft(), espacoOriginal + barras.top,
                    topo.getPaddingRight(), topo.getPaddingBottom());
            view.setPadding(barras.left, 0, barras.right, Math.max(barras.bottom, teclado.bottom));
            return insets;
        });
    }
}