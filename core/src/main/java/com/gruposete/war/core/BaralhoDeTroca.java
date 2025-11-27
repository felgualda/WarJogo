package com.gruposete.war.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

/**
 * Singleton que gerencia o baralho de cartas do jogo.
 * ATUALIZADO: Usa o mapeamento 1-44 (1-2 Curingas, 3-44 Territórios + Símbolos).
 */
public class BaralhoDeTroca {

    private static BaralhoDeTroca instance;
    private List<Carta> baralho;
    private boolean isInicializado = false;
    private Random random;

    /**
     * Construtor privado (Singleton)
     */
    private BaralhoDeTroca() {
        this.baralho = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * O acessador global (Singleton)
     */
    public static BaralhoDeTroca getInstance() {
        if (instance == null) {
            instance = new BaralhoDeTroca();
        }
        return instance;
    }

    // --- 1. A ESTRUTURA DE MAPEAMENTO (CORRIGIDA) ---

    /**
     * Classe interna para guardar os dados do mapeamento (IDs 3-44).
     */
    private static class CardData {
        public final int id;
        public final String nomeTerritorio;
        public final SimboloCarta simbolo;

        public CardData(int id, String nomeTerritorio, char simboloChar) {
            this.id = id;
            this.nomeTerritorio = nomeTerritorio;
            this.simbolo = charToSimbolo(simboloChar);
        }

        private static SimboloCarta charToSimbolo(char c) {
            switch (c) {
                case 'C': return SimboloCarta.CIRCULO;
                case 'Q': return SimboloCarta.QUADRADO;
                case 'T': return SimboloCarta.TRIANGULO;
                default: return SimboloCarta.CURINGA; // Fallback
            }
        }
    }

    /**
     * O mapeamento em código, baseado na sua lista (IDs 3-44).
     */
    private static final CardData[] TERRITORY_MAP = {
        // Europa
        new CardData(3, "Alemanha", 'C'),
        new CardData(4, "França", 'Q'),
        new CardData(5, "Inglaterra", 'C'),
        new CardData(6, "Islândia", 'T'), // (Ajuste "Islândia" se o nome no Territorio.java tiver acento)
        new CardData(7, "Moscou", 'T'),
        new CardData(8, "Polônia", 'Q'), // (Ajuste "Polônia")
        new CardData(9, "Suécia", 'C'), // (Ajuste "Suécia")
        // América do Sul
        new CardData(10, "Argentina", 'Q'),
        new CardData(11, "Brasil", 'C'),
        new CardData(12, "Peru", 'T'),
        new CardData(13, "Venezuela", 'T'),
        // África
        new CardData(14, "Africa do Sul", 'T'), // (Ajuste "África do Sul")
        new CardData(15, "Argelia", 'C'), // (Ajuste "Argélia")
        new CardData(16, "Congo", 'Q'),
        new CardData(17, "Egito", 'T'),
        new CardData(18, "Madagascar", 'C'),
        new CardData(19, "Sudão", 'Q'), // (Ajuste "Sudão")
        // Oceania
        new CardData(20, "Australia", 'T'), // (Ajuste "Austrália")
        new CardData(21, "Borneo", 'Q'),
        new CardData(22, "Nova Guiné", 'C'), // (Ajuste "Nova Guiné")
        new CardData(23, "Sumatra", 'Q'),
        // América do Norte
        new CardData(24, "Alasca", 'T'),
        new CardData(25, "California", 'Q'), // (Ajuste "Califórnia")
        new CardData(26, "Groenlândia", 'C'), // (Ajuste "Groenlândia")
        new CardData(27, "Labrador", 'Q'),
        new CardData(28, "Mackenzie", 'C'),
        new CardData(29, "Mexico", 'Q'), // (Ajuste "México")
        new CardData(30, "Nova Iorque", 'T'),
        new CardData(31, "Ottawa", 'C'),
        new CardData(32, "Vancouver", 'T'),
        // Ásia
        new CardData(33, "Aral", 'T'),
        new CardData(34, "China", 'C'),
        new CardData(35, "Dudinka", 'C'),
        new CardData(36, "India", 'Q'), // (Ajuste "índia")
        new CardData(37, "Mongólia", 'C'), // (Ajuste "Mongólia")
        new CardData(38, "Omsk", 'Q'),
        new CardData(39, "Oriente Médio", 'Q'), // (Ajuste "Oriente Médio")
        new CardData(40, "Sibéria", 'T'), // (Ajuste "Sibéria")
        new CardData(41, "Tchita", 'T'),
        new CardData(42, "Vietnã", 'T'), // (Ajuste "Vietnã")
        new CardData(43, "Vladvostok", 'C'),
        new CardData(44, "Japão", 'T') // (Ajuste "Japão")
    };

    // --- FIM DA ESTRUTURA DE MAPEAMENTO ---

    /**
     * ATUALIZADO: Preenche o baralho usando a estrutura de mapeamento 1-44.
     *
     * @param todosOsTerritorios A lista de territórios vinda do SetupPartida.
     */
    public void inicializarBaralho(Array<Territorio> todosOsTerritorios) {
        if (isInicializado) return;
        baralho.clear();

        // 1. Otimização: Mapeia nomes de territórios para os objetos Territorio
        Map<String, Territorio> territorioMap = new HashMap<>();
        for (Territorio t : todosOsTerritorios) {
            // (IMPORTANTE: Assumindo que t.getNome() remove acentos, ex: "Polonia", "Africa do Sul")
            territorioMap.put(t.getNome(), t);
        }

        // 2. Cria os 2 Curingas (IDs 1 e 2)
        baralho.add(new Carta(1, "Carta/1.png"));
        baralho.add(new Carta(2, "Carta/2.png"));

        // 3. Cria as 42 cartas de território (IDs 3-44)
        for (CardData data : TERRITORY_MAP) {
            // Encontra o objeto Territorio correspondente
            Territorio territorio = territorioMap.get(data.nomeTerritorio);

            if (territorio == null) {
                Gdx.app.error("BaralhoDeTroca", "Falha ao mapear ID " + data.id + ". Não foi possível encontrar o Território: '" + data.nomeTerritorio + "'");
                continue;
            }

            // O asset path é "Carta/[id].png"
            String assetPath = "Carta/" + data.id + ".png";

            baralho.add(new Carta(data.id, data.simbolo, territorio, assetPath));
        }

        // 4. Embaralha
        embaralhar();
        isInicializado = true;

        Gdx.app.log("BaralhoDeTroca", "Baralho inicializado com " + baralho.size() + " cartas.");
    }

    public Carta comprarCarta() {
        if (baralho.isEmpty()) {
            Gdx.app.error("BaralhoDeTroca", "Baralho vazio! Não há cartas para comprar.");
            return null;
        }
        return baralho.remove(0);
    }

    /**
     * Recebe cartas trocadas e as re-insere no baralho.
     */
    public void receberTroca(List<Carta> cartasTrocadas) {
        for (Carta carta : cartasTrocadas) {
            int randomIndex = random.nextInt(baralho.size() + 1);
            baralho.add(randomIndex, carta);
        }
        Gdx.app.log("BaralhoDeTroca", cartasTrocadas.size() + " cartas retornadas. Tamanho atual: " + baralho.size());
    }

    private void embaralhar() {
        Collections.shuffle(baralho);
    }

    public int getTamanhoBaralho() {
        return baralho.size();
    }
}
