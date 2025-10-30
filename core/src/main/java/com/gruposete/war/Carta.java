package com.gruposete.war;

/**
 * PLACEHOLDER
*/

public class Carta {
   
    // Um enum para o tipo da carta
    public enum TipoCarta {
        OBJETIVO,
        TERRITORIO,
        CURINGA
    }

    private String descricao;
    private TipoCarta tipo;
    // private Imagem icone;
    // private Territorio territorioAssociado;

    public Carta(String descricao, TipoCarta tipo) {
        this.descricao = descricao;
        this.tipo = tipo;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public TipoCarta getTipo() {
        return this.tipo;
    }
}