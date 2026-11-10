package com.example.sosi;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MeusFormulariosActivity extends BaseActivity {

    private static final String URL_BANCO = "https://sosi-31c4e-default-rtdb.firebaseio.com";

    private final List<Ocorrencia> lista = new ArrayList<>();
    private OcorrenciaAdapter adapter;
    private TextView txtStatusLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_formularios);
        configurarTopo();

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            finish();
            return;
        }

        txtStatusLista = findViewById(R.id.txtStatusLista);

        // preparar a lista
        RecyclerView rvMeusFormularios = findViewById(R.id.rvMeusFormularios);
        rvMeusFormularios.setLayoutManager(new LinearLayoutManager(this));   // um item embaixo do outro
        adapter = new OcorrenciaAdapter(lista);
        rvMeusFormularios.setAdapter(adapter);

        // buscar só as ocorrências DESTE cidadão
        FirebaseDatabase.getInstance(URL_BANCO)
                .getReference("formularios")
                .orderByChild("uidCidadao")
                .equalTo(usuario.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dados) {
                        lista.clear();
                        for (DataSnapshot item : dados.getChildren()) {
                            Ocorrencia ocorrencia = item.getValue(Ocorrencia.class);
                            if (ocorrencia != null) {
                                lista.add(0, ocorrencia);   // posição 0 = a mais nova fica em cima
                            }
                        }
                        adapter.notifyDataSetChanged();   // avisa a lista que os dados mudaram

                        if (lista.isEmpty()) {
                            txtStatusLista.setText("Você ainda não enviou nenhuma ocorrência.");
                            txtStatusLista.setVisibility(View.VISIBLE);
                        } else {
                            txtStatusLista.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError erro) {
                        txtStatusLista.setText("Erro ao carregar: " + erro.getMessage());
                        txtStatusLista.setVisibility(View.VISIBLE);
                    }
                });
    }
}