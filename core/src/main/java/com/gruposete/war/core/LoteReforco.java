package com.gruposete.war.core;

public class LoteReforco {
    public int quantidade;
    public final Continente restricao;

    public LoteReforco(int quantidade, Continente restricao) {
        this.quantidade = quantidade;
        this.restricao = restricao;
    }
}
