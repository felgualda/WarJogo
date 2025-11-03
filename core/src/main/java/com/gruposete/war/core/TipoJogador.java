package com.gruposete.war.core;

/**
 * Enum que representa o tipo de um jogador em um slot de seleção.
 * Contém a lógica de transição de estado (ciclo).
 */
public enum TipoJogador {
    NENHUM("NENHUM"), // Representa o ícone "Stop"
    HUMANO("HUMANO"), // Representa o ícone "Humano"
    IA("IA");         // Representa o ícone "Robô"

    private final String texto;

    TipoJogador(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }

    /**
     * Retorna o próximo estado no ciclo (Ex: NENHUM -> HUMANO)
     */
    public TipoJogador proximo() {
        switch (this) {
            case NENHUM: return HUMANO;
            case HUMANO: return IA;
            case IA:     return NENHUM;
            default:     return NENHUM;
        }
    }

    /**
     * Retorna o estado anterior no ciclo (Ex: NENHUM -> IA)
     */
    public TipoJogador anterior() {
        switch (this) {
            case NENHUM: return IA;
            case HUMANO: return NENHUM;
            case IA:     return HUMANO;
            default:     return NENHUM;
        }
    }
}
