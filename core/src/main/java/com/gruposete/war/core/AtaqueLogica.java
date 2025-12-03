package com.gruposete.war.core;

import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class AtaqueLogica {
    // ... (atributos iguais) ...
    private Territorio territorioAtacante;
    private Territorio territorioDefensor;
    private Jogador jogadorAtacante;
    private Jogador jogadorDefensor;
    private Mapa mapa;
    private Random random = new Random();

    public AtaqueLogica(Territorio territorioA, Territorio territorioD, Jogador jogadorA, Jogador jogadorD, Mapa mapa){
        this.territorioAtacante = territorioA;
        this.territorioDefensor = territorioD;
        this.jogadorAtacante = jogadorA;
        this.jogadorDefensor = jogadorD;
        this.mapa = mapa;
    }

    // MUDANÇA: Agora retorna ResultadoCombate em vez de AtaqueEstado
    public ResultadoCombate executarUmaRodada() {
        if (territorioAtacante.getTropas() <= 1) { 
            return new ResultadoCombate(AtaqueEstado.TROPAS_INSUFICIENTES, new Integer[]{}, new Integer[]{}, 0, 0); 
        }
        if (!mapa.isAdjacente(territorioDefensor, territorioAtacante)) { 
            return new ResultadoCombate(AtaqueEstado.SEM_ADJACENCIA, new Integer[]{}, new Integer[]{}, 0, 0); 
        }

        int qntdDadosAtacante = Math.min(territorioAtacante.getTropas() - 1, 3);
        int qntdDadosDefensor = Math.min(territorioDefensor.getTropas(), 3);

        return rolarDados(qntdDadosAtacante, qntdDadosDefensor);
    }

    private ResultadoCombate rolarDados(int qntdDadosAtacante, int qntdDadosDefensor){
        Integer[] dadosAtacante = new Integer[qntdDadosAtacante]; // Array tamanho exato
        Integer[] dadosDefensor = new Integer[qntdDadosDefensor]; // Array tamanho exato

        // Rolagem
        for (int i = 0; i < qntdDadosAtacante; i++) dadosAtacante[i] = random.nextInt(6) + 1;
        for (int i = 0; i < qntdDadosDefensor; i++) dadosDefensor[i] = random.nextInt(6) + 1;

        // Ordenação Decrescente
        Arrays.sort(dadosAtacante, Collections.reverseOrder());
        Arrays.sort(dadosDefensor, Collections.reverseOrder());

        int perdasA = 0;
        int perdasD = 0;
        int comparacoes = Math.min(qntdDadosAtacante, qntdDadosDefensor);

        // Comparação
        for (int i = 0; i < comparacoes; i++){
            if (dadosDefensor[i] < dadosAtacante[i]) {
                perdasD++;
            } else {
                perdasA++;
            }
        }

        // Aplica danos
        for(int i=0; i<perdasD; i++) territorioDefensor.decrementarTropas();
        for(int i=0; i<perdasA; i++) territorioAtacante.decrementarTropas();

        // Define estado final
        AtaqueEstado estadoFinal = AtaqueEstado.CONTINUAR_POSSIVEL;
        if (territorioAtacante.getTropas() == 1) estadoFinal = AtaqueEstado.TROPAS_INSUFICIENTES;
        if (territorioDefensor.getTropas() == 0) {
            territorioDefensor.setPlayerId(territorioAtacante.getPlayerId());
            estadoFinal = AtaqueEstado.TERRITORIO_CONQUISTADO;
        }

        return new ResultadoCombate(estadoFinal, dadosAtacante, dadosDefensor, perdasA, perdasD);
    }
}