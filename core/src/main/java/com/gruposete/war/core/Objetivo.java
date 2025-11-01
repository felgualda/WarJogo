package com.gruposete.war.core;

public class Objetivo {
    private int idObjetivo;
    private String descricao;
    private String assetPath;

    public Objetivo(int idObjetivo, String descricao,  String assetPath ) {
        this.idObjetivo = idObjetivo;
        this.descricao = descricao;
        this.assetPath = assetPath;
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
}
