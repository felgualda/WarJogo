package com.gruposete.war.core;

public class ResultadoCombate {
    public AtaqueEstado estado;
    public Integer[] dadosAtacante;
    public Integer[] dadosDefensor;
    public int perdasAtacante;
    public int perdasDefensor;

    public ResultadoCombate(AtaqueEstado estado, Integer[] dadosA, Integer[] dadosD, int perdasA, int perdasD) {
        this.estado = estado;
        this.dadosAtacante = dadosA;
        this.dadosDefensor = dadosD;
        this.perdasAtacante = perdasA;
        this.perdasDefensor = perdasD;
    }
}