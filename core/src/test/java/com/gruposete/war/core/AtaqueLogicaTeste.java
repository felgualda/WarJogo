package com.gruposete.war.core;

import com.badlogic.gdx.graphics.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.badlogic.gdx.utils.Array;

import static org.junit.jupiter.api.Assertions.*;

class AtaqueLogicaTest {

    private Jogador atacante;
    private Jogador defensor;
    private Territorio tAtacante;
    private Territorio tDefensor;
    private Mapa mapa;

    @BeforeEach
    void setUp() {
        atacante = new Jogador("Atacante", CorJogador.VERDE, 1, false);
        defensor = new Jogador("Defensor", CorJogador.VERMELHO, 2, false);

        // Cria territórios com arrays de floats vazios (apenas para não quebrar o construtor)
        tAtacante = new Territorio("Brasil", Color.GREEN, new float[]{0,0, 10,0, 10,10});
        tDefensor = new Territorio("Argentina", Color.RED, new float[]{0,0, 10,0, 10,10});

        // Configura posse inicial
        atacante.adicionarTerritorio(tAtacante);
        tAtacante.setPlayerId(atacante.getPlayerId());
        
        defensor.adicionarTerritorio(tDefensor);
        tDefensor.setPlayerId(defensor.getPlayerId());

        // Cria um mapa simples contendo apenas esses dois territórios
        Array<Territorio> listaTerritorios = new Array<>();
        listaTerritorios.add(tAtacante);
        listaTerritorios.add(tDefensor);
        
        // O Mapa calcula adjacências baseado em nomes hardcoded. 
        // Como usamos "Brasil" e "Argentina", o Mapa deve reconhecê-los como vizinhos.
        mapa = new Mapa(listaTerritorios);
    }

    @Test
    void testAtaqueInvalido_SemTropasSuficientes() {
        // Cenário: Atacante tem apenas 1 tropa (Regra: Precisa de > 1 para atacar)
        tAtacante.setTropas(1);
        tDefensor.setTropas(1);

        AtaqueLogica logica = new AtaqueLogica(tAtacante, tDefensor, atacante, defensor, mapa);
        ResultadoCombate resultado = logica.executarUmaRodada();

        assertEquals(AtaqueEstado.TROPAS_INSUFICIENTES, resultado.estado, 
            "Não deve ser possível atacar com apenas 1 tropa.");
    }

    @Test
    void testAtaqueInvalido_NaoAdjacente() {
        // Cria um território distante
        Territorio tJapao = new Territorio("Japão", Color.RED, new float[]{0,0});
        tJapao.setTropas(3);
        
        tAtacante.setTropas(5);

        // O Mapa não vai reconhecer Japão como vizinho do Brasil (baseado na sua classe Mapa)
        AtaqueLogica logica = new AtaqueLogica(tAtacante, tJapao, atacante, defensor, mapa);
        ResultadoCombate resultado = logica.executarUmaRodada();

        assertEquals(AtaqueEstado.SEM_ADJACENCIA, resultado.estado, 
            "Não deve ser possível atacar territórios não adjacentes.");
    }

    @Test
    void testConquistaDeTerritorio() {
        // Cenário: Ataque massivo até conquistar
        // Brasil (100 tropas) vs Argentina (1 tropa)
        tAtacante.setTropas(100);
        tDefensor.setTropas(1);

        AtaqueLogica logica = new AtaqueLogica(tAtacante, tDefensor, atacante, defensor, mapa);
        
        boolean conquistou = false;
        
        // Tenta atacar até 50 vezes (para evitar loop infinito se algo der errado)
        for (int i = 0; i < 50; i++) {
            ResultadoCombate resultado = logica.executarUmaRodada();
            if (resultado.estado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
                conquistou = true;
                break;
            }
        }

        assertTrue(conquistou, "O atacante deveria ter conquistado o território com vantagem numérica esmagadora.");
        assertEquals(atacante.getPlayerId(), tDefensor.getPlayerId(), 
            "O ID do dono do território defensor deve mudar para o do atacante após a conquista.");
        assertEquals(0, tDefensor.getTropas(), 
            "O território conquistado deve estar momentaneamente com 0 tropas (antes do movimento).");
    }
}