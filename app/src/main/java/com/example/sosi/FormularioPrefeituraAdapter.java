package com.example.sosi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FormularioPrefeituraAdapter
        extends RecyclerView.Adapter<FormularioPrefeituraAdapter.ItemViewHolder> {

    //O que fazer quando clicam num cartão (quem decide é a Activity). //
    public interface AoClicar {
        void clicou(Ocorrencia ocorrencia);
    }

    private final List<Ocorrencia> lista;
    private final AoClicar aoClicar;

    public FormularioPrefeituraAdapter(List<Ocorrencia> lista, AoClicar aoClicar) {
        this.lista = lista;
        this.aoClicar = aoClicar;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtMotivoCategoria, txtStatus, txtEndereco, txtInfo;

        public ItemViewHolder(@NonNull View item) {
            super(item);
            txtMotivoCategoria = item.findViewById(R.id.txtMotivoCategoria);
            txtStatus = item.findViewById(R.id.txtStatus);
            txtEndereco = item.findViewById(R.id.txtEndereco);
            txtInfo = item.findViewById(R.id.txtInfo);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_formulario_prefeitura, parent, false);
        return new ItemViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder cartao, int posicao) {
        Ocorrencia ocorrencia = lista.get(posicao);

        cartao.txtMotivoCategoria.setText(ocorrencia.motivo + " — " + ocorrencia.categoria);
        cartao.txtEndereco.setText(ocorrencia.endereco);

        // "Maiara · (19) 99999-0000 · Ocorrido em 28/10/2026"
        String info = ocorrencia.nomeCidadao;
        if (ocorrencia.celularCidadao != null && !ocorrencia.celularCidadao.isEmpty()) {
            info = info + " · " + ocorrencia.celularCidadao;
        }
        info = info + " · Ocorrido em " + ocorrencia.dataOcorrido;
        cartao.txtInfo.setText(info);

        // Etiqueta de status (mesmas cores do OcorrenciaAdapter)
        cartao.txtStatus.setText(ocorrencia.status);
        int fundo;
        int corTexto;
        if ("Concluído".equals(ocorrencia.status)) {
            fundo = R.drawable.bg_badge_concluido;
            corTexto = R.color.status_concluido_texto;
        } else if ("Em andamento".equals(ocorrencia.status)) {
            fundo = R.drawable.bg_badge_andamento;
            corTexto = R.color.status_andamento_texto;
        } else {
            fundo = R.drawable.bg_badge_analise;
            corTexto = R.color.status_analise_texto;
        }
        cartao.txtStatus.setBackgroundResource(fundo);
        cartao.txtStatus.setTextColor(ContextCompat.getColor(cartao.itemView.getContext(), corTexto));

        // Clique no cartão inteiro //
        cartao.itemView.setOnClickListener(v -> aoClicar.clicou(ocorrencia));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}