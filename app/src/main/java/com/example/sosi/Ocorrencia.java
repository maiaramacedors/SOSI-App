package com.example.sosi;

public class Ocorrencia {

    public String id;
    public String uidCidadao;
    public String nomeCidadao;
    public String celularCidadao;
    public String motivo;
    public String categoria;
    public String endereco;
    public String dataOcorrido;
    public String descricao;
    public String status;
    public String resposta;
    public long dataEnvio;

    // Construtor vazio: o Firebase exige para ler os dados depois
    public Ocorrencia() {
    }
}