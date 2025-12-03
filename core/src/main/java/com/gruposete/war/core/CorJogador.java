package com.gruposete.war.core;

import com.badlogic.gdx.graphics.Color;

public enum CorJogador {
    //               (Cor Padrão)        (Cor Daltônico - Alto Contraste)
    PRETO    (Color.DARK_GRAY,   Color.BLACK),           // Preto Puro
    VERDE    (Color.GREEN,       new Color(0x009E73FF)), // Verde Azulado (Teal)
    AZUL     (Color.CYAN,        new Color(0x56B4E9FF)), // Azul Céu
    VERMELHO (Color.RED,         new Color(0xD55E00FF)), // Vermelhão (Vermillion)
    AMARELO  (Color.YELLOW,      new Color(0xF0E442FF)), // Amarelo Claro
    BRANCO   (Color.WHITE,       Color.LIGHT_GRAY);      // Cinza Claro (para não ofuscar)

    private final Color gdxColor;
    private final Color colorBlindColor;

    CorJogador(Color gdxColor, Color colorBlindColor){
        this.gdxColor = gdxColor;
        this.colorBlindColor = colorBlindColor;
    }

    // Método inteligente que retorna a cor certa baseada no modo
    public Color getColor(boolean modoDaltonicoAtivo) {
        return modoDaltonicoAtivo ? this.gdxColor : this.colorBlindColor;
    }
    
    // Mantém o getter antigo para compatibilidade, se precisar
    public Color getGdxColor(){ return this.gdxColor; }
}