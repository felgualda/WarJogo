package com.gruposete.war.core;

public class Carta {
    private int id;
    private SimboloCarta simbolo;
    private Territorio territorio;
    private String assetPath;

    public Carta(int id, String assetPath) {
        this.id = id;
        this.simbolo = SimboloCarta.CURINGA;
        this.territorio = null;
        this.assetPath = assetPath;
    }

    public Carta(int id, SimboloCarta simbolo, Territorio territorio, String assetPath) {
        this.id = id;
        this.simbolo = simbolo;
        this.territorio = territorio;
        this.assetPath = assetPath;
    }

    public int getId() {
        return id;
    }

    public SimboloCarta getSimbolo() {
        return simbolo;
    }

    public Territorio getTerritorio() {
        return territorio;
    }

    public String getAssetPath() {
        return assetPath;
    }
}
