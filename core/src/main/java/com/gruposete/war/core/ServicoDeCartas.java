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

    public static boolean isCombinacaoValida(Carta c1, Carta c2, Carta c3) {
        if (c1 == null || c2 == null || c3 == null) {
            return false;
        }

        List<SimboloCarta> simbolos = new ArrayList<>();
        simbolos.add(c1.getSimbolo());
        simbolos.add(c2.getSimbolo());
        simbolos.add(c3.getSimbolo());

        int countCuringa = 0;
        int countCirculo = 0;
        int countQuadrado = 0;
        int countTriangulo = 0;

        for (SimboloCarta simbolo : simbolos) {
            if (simbolo == SimboloCarta.CURINGA) {
                countCuringa++;
            } else if (simbolo == SimboloCarta.CIRCULO) {
                countCirculo++;
            } else if (simbolo == SimboloCarta.QUADRADO) {
                countQuadrado++;
            } else if (simbolo == SimboloCarta.TRIANGULO) {
                countTriangulo++;
            }
        }
        
        // Combinação de TRÊS IGUAIS (sem curingas ou com curingas)
        // Se houver 3 do mesmo tipo (incluindo curingas)
        if (countCirculo + countCuringa >= 3 || 
            countQuadrado + countCuringa >= 3 || 
            countTriangulo + countCuringa >= 3) 
        {
             if (countCirculo == 3 || countQuadrado == 3 || countTriangulo == 3) {
                 return true; // 3 cartas iguais (sem curinga)
             } 
             // 2 cartas iguais + 1 curinga
             if (countCirculo == 2 && countCuringa == 1) return true;
             if (countQuadrado == 2 && countCuringa == 1) return true;
             if (countTriangulo == 2 && countCuringa == 1) return true;
             
             // 1 carta igual + 2 curingas (Válido para 3 iguais)
             if (countCirculo == 1 && countCuringa == 2) return true;
             if (countQuadrado == 1 && countCuringa == 2) return true;
             if (countTriangulo == 1 && countCuringa == 2) return true;

             // 3 curingas
             if (countCuringa == 3) return true;
        }


        // Combinação de TRÊS DIFERENTES (com ou sem curingas)
        // Símbolos únicos não-curinga
        int numSimbolosUnicos = 0;
        if (countCirculo > 0) numSimbolosUnicos++;
        if (countQuadrado > 0) numSimbolosUnicos++;
        if (countTriangulo > 0) numSimbolosUnicos++;
        
        if (numSimbolosUnicos + countCuringa >= 3) {
             if (numSimbolosUnicos == 3) {
                return true; // 1 de cada (Circulo, Quadrado, Triangulo)
             }
             // Ex: 1 Círculo, 1 Quadrado, 1 Curinga (o curinga age como Triangulo)
             if (numSimbolosUnicos == 2 && countCuringa == 1) {
                return true;
             }
             // Ex: 1 Círculo, 2 Curingas (Curingas agem como Quadrado e Triangulo)
             if (numSimbolosUnicos == 1 && countCuringa == 2) {
                return true;
             }
        }
        
        return false;
    }
}