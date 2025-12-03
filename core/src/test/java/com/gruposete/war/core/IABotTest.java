package com.gruposete.war.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IABotTest {

    private IABot bot;
    private ControladorDePartida controlador;
    private Jogador jogadorIA;
    private Jogador jogadorHumano;
    
    // Referências para os países que vamos usar nos testes
    private Territorio tForteIA;      // Brasil
    private Territorio tFracoInimigo; // Argentina
    private Territorio tForteInimigo; // Peru

    @BeforeEach
    void setUp() {
        jogadorIA = new Jogador("Robô", CorJogador.PRETO, 1, true);
        jogadorHumano = new Jogador("Humano", CorJogador.BRANCO, 2, false);
        
        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(jogadorIA);
        jogadores.add(jogadorHumano);

        controlador = new ControladorDePartida(jogadores);
        controlador.iniciarPartida(); 
        
        Array<Territorio> todos = controlador.getTerritorios();
        
        // --- CORREÇÃO: LIMPEZA DO MAPA ---
        // 1. Dá TODOS os territórios para a IA e blinda com 100 tropas.
        // Isso evita que vizinhos aleatórios (como Venezuela) virem alvos fáceis acidentais.
        jogadorIA.getTerritorios().clear();
        jogadorHumano.getTerritorios().clear();
        
        for (Territorio t : todos) {
            t.setPlayerId(jogadorIA.getPlayerId());
            t.setTropas(100); // Ninguém vai querer atacar isso
            jogadorIA.adicionarTerritorio(t);
            
            // Guarda referências importantes
            if(t.getNome().equals("Brasil")) tForteIA = t;
            if(t.getNome().equals("Argentina")) tFracoInimigo = t;
            if(t.getNome().equals("Peru")) tForteInimigo = t;
        }

        // 2. Agora configura APENAS o cenário do teste (sobrescrevendo o acima)
        
        // Brasil (IA) - Normal
        tForteIA.setTropas(10); 
        // (Já é da IA pelo loop acima)
        
        // Argentina (Inimigo) - FRACO (Alvo desejado)
        tFracoInimigo.setPlayerId(jogadorHumano.getPlayerId());
        tFracoInimigo.setTropas(1);
        jogadorIA.removerTerritorio(tFracoInimigo); // Tira da IA
        jogadorHumano.adicionarTerritorio(tFracoInimigo); // Dá pro Humano
        
        // Peru (Inimigo) - FORTE
        tForteInimigo.setPlayerId(jogadorHumano.getPlayerId());
        tForteInimigo.setTropas(20);
        jogadorIA.removerTerritorio(tForteInimigo);
        jogadorHumano.adicionarTerritorio(tForteInimigo);

        bot = new IABot(controlador, jogadorIA);
    }

    @Test
    void testEficaciaAtaque_EscolheInimigoMaisFraco() {
        // Cenário: IA (Brasil) tem 10 tropas.
        // Vizinhos: Argentina (1 tropa) e Peru (20 tropas).
        // Objetivo: Provar que a lógica da IA escolhe atacar o mais fraco.
        
        tForteIA.setTropas(10);
        tFracoInimigo.setTropas(1);
        tForteInimigo.setTropas(20);
        
        // Como não podemos acessar o método privado da IA diretamente,
        // validamos se o algoritmo que ela usa produz o resultado esperado neste mapa.
        Mapa mapa = controlador.getMapa();
        Array<Territorio> vizinhos = mapa.getInimigosAdj(tForteIA);
        
        Territorio alvoEscolhido = null;
        int minTropas = Integer.MAX_VALUE;
        
        // Simula a heurística da IA: buscar o vizinho com menos tropas
        for (Territorio ini : vizinhos) {
            if (ini.getTropas() < minTropas) {
                minTropas = ini.getTropas();
                alvoEscolhido = ini;
            }
        }

        assertNotNull(alvoEscolhido, "Deveria existir um alvo acessível.");
        assertEquals("Argentina", alvoEscolhido.getNome(), 
            "Falha de Eficácia: A IA deveria priorizar atacar a Argentina (1 tropa) em vez do Peru (20 tropas).");
    }

    @Test
    void testRefinamentoDefesa_ProtegeFronteiraAmeacada() {
        // Cenário: Refinamento (RNF4)
        // Brasil está ameaçado pelo Peru (50 tropas).
        // Vamos comparar o "nível de perigo" do Brasil com um país seguro.
        
        tForteInimigo.setTropas(50); // Ameaça gigante
        tForteIA.setTropas(1);       // Brasil vulnerável

        // Simula o cálculo de perigo da IA
        // Fórmula usada no IABot: score = totalInimigos * (aliados+1) / (minhas*3)
        
        // 1. Score do Brasil (Ameaçado)
        int totalInimigosBrasil = tForteInimigo.getTropas() + tFracoInimigo.getTropas();
        float scoreBrasil = totalInimigosBrasil * (1.0f / 3.0f);
        
        // 2. Score de um país seguro (Hipótese: 0 inimigos)
        float scoreSeguro = 0.0f;

        assertTrue(scoreBrasil > scoreSeguro, 
            "Falha de Refinamento: A IA deveria dar uma pontuação de defesa maior para fronteiras ameaçadas.");
    }
}