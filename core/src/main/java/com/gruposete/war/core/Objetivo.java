package com.gruposete.war.core;

public class Objetivo {
    private int idObjetivo;
    private String descricao;
    private String assetPath;

    // Atributos Relacionados ao Tipo do Objetivo
    private TipoDeObjetivo tipo;
    private CorJogador corJogadorAlvo;
    private String[] continentesAlvo;
    private int qtdTerritoriosAlvo;


    public enum TipoDeObjetivo{
        ELIMINAR_JOGAOR,
        CONQUISTAR_CONTINENTE,
        CONQUISTAR_TERRITORIOS,
    }

    // Construtor para Objetivo de Eliminar Jogadores
    public Objetivo(int idObjetivo, String descricao, String assetPath, CorJogador corAlvo ) {
        this.idObjetivo = idObjetivo;
        this.descricao = descricao;
        this.assetPath = assetPath;
        this.corJogadorAlvo = corAlvo;

        this.tipo = TipoDeObjetivo.ELIMINAR_JOGAOR;
    }

    // Construtor para Objetivo de Conquistar Continentes
    public Objetivo(int idObjetivo, String descricao,  String assetPath, String[] continentesAlvo ) {
        this.idObjetivo = idObjetivo;
        this.descricao = descricao;
        this.assetPath = assetPath;
        this.continentesAlvo = continentesAlvo;

        this.tipo = TipoDeObjetivo.CONQUISTAR_CONTINENTE;
    }

    // Construtor para Objetivo de Conquistar Territórios
    public Objetivo(int idObjetivo, String descricao,  String assetPath, int qtdTerritoriosAlvo ) {
        this.idObjetivo = idObjetivo;
        this.descricao = descricao;
        this.assetPath = assetPath;
        this.qtdTerritoriosAlvo = qtdTerritoriosAlvo;

        this.tipo = TipoDeObjetivo.CONQUISTAR_TERRITORIOS;
    }

    public int getIdObjetivo() {
        return idObjetivo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getAssetPath() {
        return assetPath;
    }

    // Getters para os atributos relacionados ao Tipo de Objetivo
    public CorJogador getCorJogadorAlvo() {
        return corJogadorAlvo;
    }

    public TipoDeObjetivo getTipo() {
        return tipo;
    }

    public int getQtdTerritoriosAlvo() {
        return qtdTerritoriosAlvo;
    }

    public String[] getContinentesAlvo() {
        return continentesAlvo;
    }
}
