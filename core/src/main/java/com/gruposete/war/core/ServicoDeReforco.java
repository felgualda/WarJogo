package com.gruposete.war.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.badlogic.gdx.utils.Array;

public class ServicoDeReforco {

    /**
     * Calcula todos os reforços do turno, separando por restrições de continente.
     * Retorna uma lista (fila) de lotes para o Controlador gerenciar.
     */
    public static List<LoteReforco> calcularReforcos(Jogador jogador, Mapa mapa) {
        List<LoteReforco> filaDeReforcos = new ArrayList<>();

        // 1. Bônus por Continente (Prioridade: Restritos primeiro)
        Map<Continente, List<Territorio>> mapaContinentes = mapa.getTerritoriosPorContinente();

        for (Map.Entry<Continente, List<Territorio>> entry : mapaContinentes.entrySet()) {
            Continente continente = entry.getKey();
            List<Territorio> territoriosDoContinente = entry.getValue();

            // Verifica se o jogador possui todos os territórios do continente (pelo ID)
            boolean domina = territoriosDoContinente.stream()
                .allMatch(t -> t.getPlayerId() == jogador.getPlayerId());

            if (domina) {
                filaDeReforcos.add(new LoteReforco(continente.getBonusExercitos(), continente));
            }
        }

        // 2. Reforço Base Global (Sem restrição)
        int numTerritorios = jogador.getTerritorios().size();

        // Regra padrão War: Total / 2 (Arredondado para baixo). Mínimo de 3.
        int bonusBase = numTerritorios / 3;

        if (bonusBase < 3) {
            bonusBase = 3;
        }

        // Adiciona o lote global (restricao = null)
        filaDeReforcos.add(new LoteReforco(bonusBase, null));

        return filaDeReforcos;
    }
}
