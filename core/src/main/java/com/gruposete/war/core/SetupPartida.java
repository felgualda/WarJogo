package com.gruposete.war.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.badlogic.gdx.utils.Array;

import com.gruposete.war.core.Jogador;
import com.gruposete.war.core.Territorio;
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

        // Carrega Deck de Objetivos 
        this.deckDeObjetivos = new ArrayList<>();
        // Temporário
        this.deckDeObjetivos.add(new Objetivo(1, "Conquistar a America do Sul", "path/obj1.png"));
        this.deckDeObjetivos.add(new Objetivo(2, "Destruir o jogador AZUL", "path/obj2.png"));
        this.deckDeObjetivos.add(new Objetivo(3, "Conquistar 24 territorios", "path/obj3.png"));
        this.deckDeObjetivos.add(new Objetivo(4, "Conquistar a Asia", "path/obj4.png"));
        this.deckDeObjetivos.add(new Objetivo(5, "Conquistar a Europa", "path/obj5.png"));
        this.deckDeObjetivos.add(new Objetivo(6, "Destruir o jogador VERMELHO", "path/obj6.png"));

        System.out.println("SETUP: Cartas de objetivo (reais) carregadas.");
    }

    // Define a sequência de turnos embaralhando a lista de jogadores.
    private void definirOrdemDosTurnos() {
        System.out.println("SETUP: Embaralhando ordem dos turnos...");
        Collections.shuffle(this.jogadores);
        
        System.out.print("SETUP: Ordem definida: ");
        for (int i = 0; i < this.jogadores.size(); i++) {
            System.out.print((i+1) + "º: " + this.jogadores.get(i).getNome() + " | ");
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
            
            // Entrega o objetivo ao jogador 
            jogador.setObjetivo(objetivoSorteado);
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
}