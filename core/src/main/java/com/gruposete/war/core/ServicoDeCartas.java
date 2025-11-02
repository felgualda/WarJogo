package com.gruposete.war.core;

import java.util.ArrayList;
import java.util.List;

public class ServicoDeCartas {

    public static boolean isTrocaObrigatoria(Jogador jogador) {
        // Ao possuir 5 ou mais cartas no início do seu turno, ele é obrigado a realizar pelo menos uma troca.
        return jogador.getCartas().size() >= 5;
    }

    public static int calcularBonusTroca(int numTrocasEfetuadas) {
        // Tabela 2:
        if (numTrocasEfetuadas == 1) return 4;  // 1ª troca: +4 exércitos. 
        if (numTrocasEfetuadas == 2) return 6;  // 2ª troca: +6 exércitos. 
        if (numTrocasEfetuadas == 3) return 8;  // 3ª troca: +8 exércitos. 
        if (numTrocasEfetuadas == 4) return 10; // 4ª troca: +10 exércitos. 
        if (numTrocasEfetuadas == 5) return 12; // 5ª troca: +12 exércitos. 
        if (numTrocasEfetuadas == 6) return 15; // 6ª troca: +15 exércitos. 

        // A partir da 7ª troca, incrementar 5 exércitos no número da anterior
        if (numTrocasEfetuadas >= 7) {
            return 15 + (numTrocasEfetuadas - 6) * 5;
        }
        return 0;
    }
}