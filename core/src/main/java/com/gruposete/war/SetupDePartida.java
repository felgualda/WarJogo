package com.gruposete.war;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;

public class SetupDePartida {
    private List<Jogador> jogadores;
    private List<Territorio> todosOsTerritorios;
    private List<Carta> deckDeObjetivos;

    // Lista de cores padrão para os jogadores (até 6 jogadores)
    private static final Color[] CORES_JOGADORES = {
        Color.BLUE,
        Color.RED,
        Color.GREEN,
        Color.YELLOW,
        Color.CYAN,
        Color.MAGENTA
    };

    public SetupDePartida(List<String> nomesDosJogadores) {
        criarJogadores(nomesDosJogadores); // cria jogadores
        carregarRecursosDoJogo(); // carrega territorios e cartas
        definirOrdemDosTurnos(); // define a ordem dos turnos
        distribuirObjetivos(); // sorteia e distribui os objetivos
        distribuirTerritorios(); // sorteia e distribui os territorios
    }

    private void criarJogadores(List<String> nomes) {
        this.jogadores = new ArrayList<>();
        for (int i = 0; i < nomes.size(); i++) {
            String nome = nomes.get(i);
            Color cor = CORES_JOGADORES[i % CORES_JOGADORES.length];
            
            this.jogadores.add(new Jogador(i + 1, nome, cor)); 
        }
        System.out.println("SETUP: Jogadores criados: " + nomes);
    }

    private void carregarRecursosDoJogo() {
        // Carrega Territórios 
        this.todosOsTerritorios = Utils.carregarTodosOsTerritorios();
        System.out.println("SETUP: " + this.todosOsTerritorios.size() + " territórios carregados do Utils."); // debug

        
        // Carrega Deck de Objetivos (usando o placeholder)
        this.deckDeObjetivos = new ArrayList<>();
        this.deckDeObjetivos.add(new Carta("Conquistar a America do Sul", Carta.TipoCarta.OBJETIVO));
        this.deckDeObjetivos.add(new Carta("Destruir o jogador AZUL", Carta.TipoCarta.OBJETIVO));
        this.deckDeObjetivos.add(new Carta("Conquistar 24 territorios", Carta.TipoCarta.OBJETIVO));
        this.deckDeObjetivos.add(new Carta("Conquistar a Asia", Carta.TipoCarta.OBJETIVO));
        this.deckDeObjetivos.add(new Carta("Conquistar a Europa", Carta.TipoCarta.OBJETIVO));
        this.deckDeObjetivos.add(new Carta("Destruir o jogador VERMELHO", Carta.TipoCarta.OBJETIVO));

        System.out.println("SETUP: Cartas de objetivo (placeholder) carregadas."); // debug
    }

    // define a sequência de turnos (embaralha a lista de jogadores)
    private void definirOrdemDosTurnos() {
        System.out.println("SETUP: Embaralhando ordem dos turnos...");
        Collections.shuffle(this.jogadores);
        
        // debug
        System.out.print("SETUP: Ordem definida: ");
        for (int i = 0; i < this.jogadores.size(); i++) {
            System.out.print((i+1) + "º: " + this.jogadores.get(i).getNome() + " | ");
        }
        System.out.println(); // Nova linha
    }

    // sorteia e distribui as cartas objetivo
    private void distribuirObjetivos() {
        System.out.println("SETUP: Distribuindo objetivos...");
        
        // Embaralha o deck de objetivos
        Collections.shuffle(this.deckDeObjetivos);

        // Dá uma carta para cada jogador
        for (Jogador jogador : this.jogadores) {
            if (this.deckDeObjetivos.isEmpty()) {
                System.err.println("ERRO DE SETUP: Não há cartas de objetivo suficientes para todos os jogadores!");
                break;
            }
            
            // Pega a carta do topo do deck (índice 0) e a remove
            Carta objetivoSorteado = this.deckDeObjetivos.remove(0);
            
            // Entrega a carta ao jogador (usando o método do placeholder)
            jogador.setObjetivo(objetivoSorteado);
        }
    }

    // sorteia e distribui os territórios
    private void distribuirTerritorios() {
        System.out.println("SETUP: Distribuindo territórios...");
        // 1. Embaralha a lista 
        Collections.shuffle(this.todosOsTerritorios);
        int numJogadores = this.jogadores.size();
        
        // 2. Distribui um território por vez para cada jogador em ordem
        for (int i = 0; i < this.todosOsTerritorios.size(); i++) {
            Territorio territorio = this.todosOsTerritorios.get(i);
            Jogador jogadorDaVez = this.jogadores.get(i % numJogadores);

            // 3. Chama 'jogador.adicionarTerritorio(territorioSorteado)'
            jogadorDaVez.adicionarTerritorio(territorio);

            // 4. Atualiza o objeto Territorio com o dono e 1 tropa
            territorio.setPlayerId(jogadorDaVez.getId()); // Define o ID do dono
            territorio.setColor(jogadorDaVez.getCor()); // Define a cor do dono
            territorio.incrementarTropas(); // Adiciona 1 tropa inicial
        }
        
        System.out.println("SETUP: Distribuição de territórios CONCLUÍDA.");
    }

    public List<Jogador> getJogadoresPreparados() {
        return this.jogadores;
    }

    public List<Territorio> getTodosOsTerritorios() {
        return this.todosOsTerritorios;
    }
}