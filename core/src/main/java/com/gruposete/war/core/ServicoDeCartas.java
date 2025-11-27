package com.gruposete.war.core; // Ajuste o pacote se necessário

import java.util.List;

/**
 * Classe de utilidade (lógica pura) para regras de troca de cartas.
 * Não armazena estado, apenas valida combinações e calcula bônus.
 */
public class ServicoDeCartas {

    /**
     * Verifica se o jogador é obrigado a trocar (tem 5 ou mais cartas).
     */
    public static boolean isTrocaObrigatoria(Jogador jogador) {
        return jogador.getCartas().size() >= 5;
    }

    /**
     * Calcula o bônus de exércitos com base no contador global de trocas.
     */
    public static int calcularBonusTroca(int numTrocasEfetuadas) {
        if (numTrocasEfetuadas <= 0) return 0;
        if (numTrocasEfetuadas == 1) return 4;
        if (numTrocasEfetuadas == 2) return 6;
        if (numTrocasEfetuadas == 3) return 8;
        if (numTrocasEfetuadas == 4) return 10;
        if (numTrocasEfetuadas == 5) return 12;
        if (numTrocasEfetuadas == 6) return 15;
        // A partir da 7ª troca, incrementa 5
        return 15 + (numTrocasEfetuadas - 6) * 5;
    }

    /**
     * Verifica se 3 cartas formam uma combinação válida (3 iguais ou 3 diferentes).
     * Curingas são considerados em ambas as combinações.
     */
    public static boolean isCombinacaoValida(Carta c1, Carta c2, Carta c3) {
        if (c1 == null || c2 == null || c3 == null) {
            return false;
        }

        int countCuringa = 0;
        int countCirculo = 0;
        int countQuadrado = 0;
        int countTriangulo = 0;

        // Conta os símbolos
        for (Carta carta : List.of(c1, c2, c3)) {
            switch (carta.getSimbolo()) {
                case CURINGA:   countCuringa++;   break;
                case CIRCULO:   countCirculo++;   break;
                case QUADRADO:  countQuadrado++;  break;
                case TRIANGULO: countTriangulo++; break;
            }
        }

        // --- Checagem 1: Três Iguais ---
        // (Ex: 3 Círculos) OU (2 Círculos + 1 Curinga) OU (1 Círculo + 2 Curingas)
        if (countCirculo + countCuringa == 3) return true;
        if (countQuadrado + countCuringa == 3) return true;
        if (countTriangulo + countCuringa == 3) return true;
        // (O caso de 3 Curingas é coberto por qualquer uma das checagens acima)

        // --- Checagem 2: Três Diferentes ---
        // (Ex: 1C, 1Q, 1T) OU (1C, 1Q, 1 Curinga) OU (1C, 2 Curingas)
        int numSimbolosUnicos = 0;
        if (countCirculo > 0) numSimbolosUnicos++;
        if (countQuadrado > 0) numSimbolosUnicos++;
        if (countTriangulo > 0) numSimbolosUnicos++;

        // Se o número de símbolos únicos + curingas for 3 ou mais, é uma troca válida.
        if (numSimbolosUnicos + countCuringa >= 3) {
            return true;
        }

        return false;
    }
}
