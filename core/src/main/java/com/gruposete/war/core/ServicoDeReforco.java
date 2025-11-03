// core/ServicoDeReforco.java (NOVA CLASSE)

package com.gruposete.war.core;

import java.util.List;
import java.util.Map;
import com.badlogic.gdx.utils.Array;

public class ServicoDeReforco {

    public static int calcularTotalReforcos(Jogador jogador, Mapa mapa) {
        int totalReforcos = 0;

        int numTerritorios = jogador.getTerritorios().size();

        System.out.println("Total de territorios" + numTerritorios);
        
        int bonusTerritorios = (int) Math.floor(numTerritorios / 3.0);
        
        //System.out.println("Total de bonusterritorios" + bonusTerritorios);

        // O jogador recebe no mínimo 3 territórios
        if (bonusTerritorios < 3) {
            bonusTerritorios = 3;
        }
        totalReforcos += bonusTerritorios;

        //System.out.println("Total de totalreforços" + totalReforcos);

        // Bônus por continente
        totalReforcos += calcularBonusContinentes(jogador, mapa);

        //System.out.println("Total de reforcosbase pos contineente" + totalReforcos);

        // O total deve ser adicionado ao jogador no Controller:
        // jogador.adicionarExercitosDisponiveis(totalReforcos);
        
        return totalReforcos;
    }

    // Calcula bônus por continente dominado
    private static int calcularBonusContinentes(Jogador jogador, Mapa mapa) {
        int bonusTotal = 0;
        
        Map<Continente, List<Territorio>> mapaContinentes = mapa.getTerritoriosPorContinente();

        for (Map.Entry<Continente, List<Territorio>> entry : mapaContinentes.entrySet()) {
            Continente cont = entry.getKey();
            List<Territorio> territoriosDoContinente = entry.getValue();

            // Verifica se o jogador domina a totalidade dos territórios do continente.
            boolean domina = territoriosDoContinente.stream()
                .allMatch(t -> t.getPlayerId() == jogador.getPlayerId());

            if (domina) {
                // Adiciona o bônus de exércitos por continente dominado
                bonusTotal += cont.getBonusExercitos();
            }
        }
        return bonusTotal;
    }
}