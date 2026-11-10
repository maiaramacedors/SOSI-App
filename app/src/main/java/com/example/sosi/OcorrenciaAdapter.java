package com.example.sosi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OcorrenciaAdapter extends RecyclerView.Adapter<OcorrenciaAdapter.ItemViewHolder> {

    private final List<Ocorrencia> lista;

    public OcorrenciaAdapter(List<Ocorrencia> lista) {
        this.lista = lista;
    }

    // ViewHolder: guarda os componentes de UM cartão
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtMotivo, txtStatus, txtEndereco, txtData, txtResposta;
        LinearLayout layoutResposta;

        public ItemViewHolder(@NonNull View item) {
            super(item);
            txtMotivo = item.findViewById(R.id.txtMotivo);
            txtStatus = item.findViewById(R.id.txtStatus);
            txtEndereco = item.findViewById(R.id.txtEndereco);
            txtData = item.findViewById(R.id.txtData);
            txtResposta = item.findViewById(R.id.txtResposta);
            layoutResposta = item.findViewById(R.id.layoutResposta);
        }
    }

    // Cria um cartão novo a partir do XML
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meu_formulario, parent, false);
        return new ItemViewHolder(item);
    }

    // Preenche o cartão com os dados de uma ocorrência
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder cartao, int posicao) {
        Ocorrencia ocorrencia = lista.get(posicao);

        cartao.txtMotivo.setText(ocorrencia.motivo + " · " + ocorrencia.categoria);
        cartao.txtEndereco.setText(ocorrencia.endereco);
        cartao.txtData.setText("Ocorrido em: " + ocorrencia.dataOcorrido);

        // Etiqueta de status com as cores do site
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

        // Resposta da prefeitura: só aparece se existir
        if (ocorrencia.resposta != null && !ocorrencia.resposta.isEmpty()) {
            cartao.txtResposta.setText(ocorrencia.resposta);
            cartao.layoutResposta.setVisibility(View.VISIBLE);
        } else {
            cartao.layoutResposta.setVisibility(View.GONE);
        }
    }

    // Quantos itens tem a lista
    @Override
    public int getItemCount() {
        return lista.size();
    }
}