package com.gruposete.war.core;
// Como SetupDaPartida utiliza a cor gráfica da gdx (tal qual territorio), ajustei essa classe para além de Labels, também implementar as cores em si
import com.badlogic.gdx.graphics.Color;

public enum CorJogador {
    PRETO(Color.DARK_GRAY),
    VERDE(Color.GREEN),
    AZUL(Color.CYAN),
    VERMELHO(Color.RED),
    AMARELO(Color.YELLOW),
    BRANCO(Color.WHITE);

    // Atributo privado para guardar a cor do LibGDX
    private final Color gdxColor;

    CorJogador(Color gdxColor){
        this.gdxColor = gdxColor;
    }

    public Color getGdxColor(){ return this.gdxColor; }
}
