package com.gruposete.war.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.badlogic.gdx.utils.Array;

import com.gruposete.war.core.Jogador;
import com.gruposete.war.core.Territorio;
import com.gruposete.war.core.Objetivo.TipoDeObjetivo;
import com.gruposete.war.core.Mapa;
import com.gruposete.war.core.Objetivo;
import com.gruposete.war.core.CorJogador;
import com.gruposete.war.utils.Utils; 

/**
 * Classe responsável pela configuração inicial da partida:
 * Define ordem de turno, distribui objetivos e territórios.
 */
public class SetupPartida {

    private List<Jogador> jogadores;
    private Array<Territorio> todosOsTerritorios;
    private List<Objetivo> deckDeObjetivos;
    private Mapa mapaAdjacencias; 

    // Construtor, recebe lista de jogadores (criada pela UI)
    public SetupPartida(List<Jogador> jogadores) {
        this.jogadores = jogadores; // Recebe os jogadores 
        
        carregarRecursosDoJogo();
        definirOrdemDosTurnos();
        distribuirObjetivos();
        distribuirTerritorios();
    }

    // Carrega territórios e objetivos
    private void carregarRecursosDoJogo() {
        this.todosOsTerritorios = Utils.geradorTerritorios();
        System.out.println("SETUP: " + this.todosOsTerritorios.size + " territórios carregados do Utils.");

        // Gera o mapa de adjacencias
        this.mapaAdjacencias = new Mapa(todosOsTerritorios);

        // Carrega Deck de Objetivos
        this.deckDeObjetivos = new ArrayList<>();
        // ADD Objetivos de conquista de Continentes
        this.deckDeObjetivos.add(new Objetivo(1, "Conquistar na Totalidade a AMÉRICA DO NORTE e a ÁFRICA", "assets\\Carta\\53.png", new String[]{"América do Norte", "África"}));
        this.deckDeObjetivos.add(new Objetivo(2, "Conquistar na Totalidade a ÁSIA e a ÁFRICA", "assets\\Carta\\54.png", new String[]{"Ásia", "África"}));
        this.deckDeObjetivos.add(new Objetivo(3, "Conquistar na Totalidade a AMÉRICA DO NORTE e a OCEANIA", "assets\\Carta\\55.png", new String[]{"América do Norte", "Oceania"}));
        this.deckDeObjetivos.add(new Objetivo(4, "Conquistar na Totalidade a AMÉRICA DO SUL e a ÁSIA", "assets\\Carta\\56.png", new String[]{"América do Sul", "Ásia"}));
        this.deckDeObjetivos.add(new Objetivo(5, "Conquistar na Totalidade a AMÉRICA DO SUL, A EUROPA e mais um terceiro continente à sua escolha", "assets\\Carta\\57.png", new String[]{"América do Sul", "Europa", "*"}));
        this.deckDeObjetivos.add(new Objetivo(6, "Conquistar na Totalidade a EUROPA e a OCEANIA e mais um terceiro continente à sua escolha", "assets\\Carta\\58.png", new String[]{"Europa", "Oceania", "*"}));
        // ADD Objetivos de Destruir Jogadores
        int idObjetivos = 7;
        for (Jogador j : this.jogadores) {
            this.deckDeObjetivos.add(new Objetivo(idObjetivos, "Destruir totalmente o JOGADOR " + j.getCor() + ", se o jogador que os possui for eliminado por outro jogador, o seu objetivo passa automáticamente a ser: CONQUISTAR 24 TERRIÓRIOS", "assets\\Carta\\45.png", j.getCor()));
            idObjetivos++;
        }
        // ADD Objetivos de Conquistar Territórios
        this.deckDeObjetivos.add(new Objetivo(idObjetivos, "Conquistar 24 territorios", "assets\\Carta\\51.png", 24));
        idObjetivos++;
        this.deckDeObjetivos.add(new Objetivo(idObjetivos, "Conquistar 18 territorios e ocupar cada um deles com pelo menos 2 exércitos", "assets\\Carta\\52.png", 18));

        System.out.println("SETUP: Cartas de objetivo (reais) carregadas.");
    }

    // Define a sequência de turnos embaralhando a lista de jogadores.
    private void definirOrdemDosTurnos() {
    System.out.println("SETUP: Embaralhando ordem dos turnos...");
    Collections.shuffle(this.jogadores); // Embaralha a ordem de turnos

    for (int i = 0; i < this.jogadores.size(); i++) {
        Jogador jogador = this.jogadores.get(i);
        int novoPlayerId = i + 1;
        
        // Atualiza o ID do objeto Jogador (Se a classe Jogador tiver setPlayerId)
        jogador.setPlayerId(novoPlayerId);

        // Atualiza o ID de TODOS os territórios dele no mapa
        for (Territorio t : jogador.getTerritorios()) {
            t.setPlayerId(novoPlayerId); 
        }
    }
    // FIM DA SINCRONIZAÇÃO
    
    System.out.print("SETUP: Ordem definida: ");
    for (int i = 0; i < this.jogadores.size(); i++) {
        System.out.print((i + 1) + "º: " + this.jogadores.get(i).getNome() + " (ID: " + this.jogadores.get(i).getPlayerId() + ") | ");
    }
    System.out.println();
}

    // Sorteia e distribui as cartas de objetivo.
    private void distribuirObjetivos() {
        System.out.println("SETUP: Distribuindo objetivos...");
        
        Collections.shuffle(this.deckDeObjetivos);

        for (Jogador jogador : this.jogadores) {
            if (this.deckDeObjetivos.isEmpty()) {
                System.err.println("ERRO DE SETUP: Não há cartas de objetivo suficientes para todos os jogadores!"); 
                break;
            }
            
            // Pega um Objetivo 
            Objetivo objetivoSorteado = this.deckDeObjetivos.remove(0);

            // Verificação para que um jogador não receba um Objetivo de eliminar a sí mesmo
            if (objetivoSorteado.getTipo() == TipoDeObjetivo.ELIMINAR_JOGAOR && objetivoSorteado.getCorJogadorAlvo() == jogador.getCor()){
                if (this.deckDeObjetivos.isEmpty()) {
                    System.err.println("ERRO DE SETUP: Jogador foi sorteado com um Objetivo para eliminar a sí mesmo e não há mais cartas de objetivo suficientes!"); 
                    break;
                }
                
                Objetivo novoObjetivo = this.deckDeObjetivos.remove(0);
                jogador.setObjetivo(novoObjetivo);

                this.deckDeObjetivos.add(objetivoSorteado);
            }
            else{
                // Entrega o objetivo ao jogador 
                jogador.setObjetivo(objetivoSorteado);
            }
            
            
        }
    }

    // Sorteia e distribui os territórios.
    private void distribuirTerritorios() {
        System.out.println("SETUP: Distribuindo territórios...");
        
        this.todosOsTerritorios.shuffle();
        int numJogadores = this.jogadores.size();
        
        for (int i = 0; i < this.todosOsTerritorios.size; i++) {
            Territorio territorio = this.todosOsTerritorios.get(i);
            
            // Pega o índice e o Jogador correspondente
            int jogadorIndex = i % numJogadores;
            Jogador jogadorDaVez = this.jogadores.get(jogadorIndex);
            
            // Usa índice (1, 2, 3, ...)
            int jogadorId = jogadorIndex + 1; 

            // Adiciona o território ao jogador
            jogadorDaVez.adicionarTerritorio(territorio);

            // Adiciona cor e 1 tropa inicial ao território associado
            territorio.setPlayerId(jogadorId);
            territorio.setColor(jogadorDaVez.getCor().getGdxColor()); 
            territorio.incrementarTropas(); 
        }
        
        System.out.println("SETUP: Distribuição de territórios CONCLUÍDA.");
    }

    
    // Retorna a lista de jogadores prontos (com ordem definida, objetivos e territórios).
    public List<Jogador> getJogadoresPreparados() {
        return this.jogadores;
    }
    
    // Retorna a lista de territórios atualizada (com donos, cores e tropas). 
    public Array<Territorio> getTodosOsTerritorios() {
        return this.todosOsTerritorios;
    }

    public Mapa getMapaAdjacencias(){
        return this.mapaAdjacencias;
    }
}