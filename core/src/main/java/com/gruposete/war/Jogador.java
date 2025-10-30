package com.gruposete.war;
import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.Color;

/**
 * PLACEHOLDER
*/

public class Jogador {
    
    private int id;
    private String nome;
    private Color cor; 
    private Carta objetivo;
    private List<Territorio> territorios;
    private List<Carta> cartasMao; 

    public Jogador(int id, String nome, Color cor) {
        this.id = id;
        this.nome = nome;
        this.cor = cor;
        
        // Inicializa as listas
        this.territorios = new ArrayList<>();
        this.cartasMao = new ArrayList<>();
    }


    // Define o objetivo secreto deste jogador 
    public void setObjetivo(Carta objetivo) {
        this.objetivo = objetivo;
    }

    // Adiciona um território à lista de territórios deste jogador 
    public void adicionarTerritorio(Territorio territorio) {
        this.territorios.add(territorio);
    }

    public int getId() {
        return this.id;
    }
        
    public Color getCor() {
        return this.cor;
    }

    public String getNome() {
        return this.nome;
    }

    public Carta getObjetivo() {
        return this.objetivo;
    }

    public List<Territorio> getTerritorios() {
        return this.territorios;
    }
}