package com.example.sosi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PainelPrefeituraActivity extends BaseActivity {

    private final List<Ocorrencia> todas = new ArrayList<>();   // tudo que veio do banco
    private final List<Ocorrencia> lista = new ArrayList<>();   // o que aparece (depois do filtro)
    private FormularioPrefeituraAdapter adapter;
    private RadioGroup rgFiltro;
    private TextView txtStatusLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painel_prefeitura);
        configurarTopo();

        // Só a prefeitura pode ver esta tela//
        if (!ehPrefeitura()) {
            finish();
            return;
        }

        rgFiltro = findViewById(R.id.rgFiltro);
        txtStatusLista = findViewById(R.id.txtStatusLista);
        txtStatusLista.setVisibility(View.VISIBLE);   // mostra "Carregando…"

        //  preparar a lista
        RecyclerView rvFormularios = findViewById(R.id.rvFormularios);
        rvFormularios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FormularioPrefeituraAdapter(lista, ocorrencia -> {
            Intent intent = new Intent(this, ResponderFormularioActivity.class);
            intent.putExtra("id", ocorrencia.id);   // manda junto qual ocorrência foi clicada
            startActivity(intent);
        });
        rvFormularios.setAdapter(adapter);

        // filtros: ao trocar, refaz a lista
        rgFiltro.setOnCheckedChangeListener((grupo, idMarcado) -> aplicarFiltro());

        //  buscar TODAS as ocorrências (e continuar escutando)
        FirebaseDatabase.getInstance(URL_BANCO)
                .getReference("formularios")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dados) {
                        todas.clear();
                        for (DataSnapshot item : dados.getChildren()) {
                            Ocorrencia ocorrencia = item.getValue(Ocorrencia.class);
                            if (ocorrencia != null) {
                                todas.add(0, ocorrencia);   // a mais nova em cima
                            }
                        }
                        aplicarFiltro();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError erro) {
                        txtStatusLista.setText("Erro ao carregar: " + erro.getMessage());
                        txtStatusLista.setVisibility(View.VISIBLE);
                    }
                });
    }

    // Copia de "todas" para "lista" só as do status escolhido no filtro.
    private void aplicarFiltro() {
        int idMarcado = rgFiltro.getCheckedRadioButtonId();
        String statusEscolhido = "";   // vazio = Todos
        if (idMarcado == R.id.filtroAnalise) {
            statusEscolhido = "Em análise";
        } else if (idMarcado == R.id.filtroAndamento) {
            statusEscolhido = "Em andamento";
        } else if (idMarcado == R.id.filtroConcluido) {
            statusEscolhido = "Concluído";
        }

        lista.clear();
        for (Ocorrencia ocorrencia : todas) {
            if (statusEscolhido.isEmpty() || statusEscolhido.equals(ocorrencia.status)) {
                lista.add(ocorrencia);
            }
        }
        adapter.notifyDataSetChanged();

        if (lista.isEmpty()) {
            txtStatusLista.setText("Nenhum formulário encontrado.");
            txtStatusLista.setVisibility(View.VISIBLE);
        } else {
            txtStatusLista.setVisibility(View.GONE);
        }
    }
}