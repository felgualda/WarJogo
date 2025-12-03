package com.gruposete.war.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.gruposete.war.utils.Utils; // Importante: Usa o gerador oficial
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtaqueLogicaTest {

    private Jogador atacante;
    private Jogador defensor;
    private Territorio tAtacante; // Vamos usar o Brasil
    private Territorio tDefensor; // Vamos usar a Argentina
    private Mapa mapa;

    @BeforeEach
    void setUp() {
        // 1. Configura jogadores básicos
        atacante = new Jogador("Atacante", CorJogador.VERDE, 1, false);
        defensor = new Jogador("Defensor", CorJogador.VERMELHO, 2, false);

        // 2. Carrega o mapa COMPLETO usando seu Utils (Evita o erro de NullPointer)
        Array<Territorio> todosTerritorios = Utils.geradorTerritorios();
        mapa = new Mapa(todosTerritorios);

        // 3. Busca os territórios reais dentro da lista carregada
        for (Territorio t : todosTerritorios) {
            if (t.getNome().equals("Brasil")) tAtacante = t;
            if (t.getNome().equals("Argentina")) tDefensor = t;
        }

        // 4. Configura a posse para o teste (Limpa donos anteriores do gerador)
        tAtacante.setPlayerId(atacante.getPlayerId());
        tAtacante.setTropas(0); // Reseta tropas para configurarmos no teste
        atacante.adicionarTerritorio(tAtacante);
        
        tDefensor.setPlayerId(defensor.getPlayerId());
        tDefensor.setTropas(0); // Reseta tropas
        defensor.adicionarTerritorio(tDefensor);
    }

    @Test
    void testAtaqueInvalido_SemTropasSuficientes() {
        // Regra: Não pode atacar com apenas 1 tropa (exército de ocupação)
        tAtacante.setTropas(1);
        tDefensor.setTropas(1);

        AtaqueLogica logica = new AtaqueLogica(tAtacante, tDefensor, atacante, defensor, mapa);
        ResultadoCombate resultado = logica.executarUmaRodada();

        assertEquals(AtaqueEstado.TROPAS_INSUFICIENTES, resultado.estado,
            "O ataque deveria falhar pois o atacante só tem 1 exército.");
    }

    @Test
    void testAtaqueInvalido_NaoAdjacente() {
        // Busca um país longe (Japão) na lista do mapa já criado
        Territorio tLonge = null;
        for (Territorio t : mapa.getTerritoriosPorContinente().get(Continente.ASIA)) {
            if (t.getNome().equals("Japão")) tLonge = t;
        }
        
        tAtacante.setTropas(5);
        
        // Tenta atacar do Brasil para o Japão
        AtaqueLogica logica = new AtaqueLogica(tAtacante, tLonge, atacante, defensor, mapa);
        ResultadoCombate resultado = logica.executarUmaRodada();

        assertEquals(AtaqueEstado.SEM_ADJACENCIA, resultado.estado,
            "O ataque deveria falhar pois Brasil e Japão não são vizinhos.");
    }

    @Test
    void testConquistaDeTerritorio() {
        // Cenário: Ataque massivo (100 vs 1) para garantir vitória
        tAtacante.setTropas(100);
        tDefensor.setTropas(1);

        AtaqueLogica logica = new AtaqueLogica(tAtacante, tDefensor, atacante, defensor, mapa);
        
        boolean conquistou = false;
        
        // Tenta atacar várias vezes até vencer (pois dados são aleatórios)
        for (int i = 0; i < 100; i++) {
            ResultadoCombate resultado = logica.executarUmaRodada();
            if (resultado.estado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
                conquistou = true;
                break;
            }
        }

        assertTrue(conquistou, "O atacante deveria ter conquistado o território.");
        assertEquals(atacante.getPlayerId(), tDefensor.getPlayerId(), 
            "A posse do território defensor (Argentina) deveria passar para o atacante.");
        assertEquals(0, tDefensor.getTropas(), 
            "O território conquistado deve estar momentaneamente com 0 tropas (antes do movimento).");
    }
}