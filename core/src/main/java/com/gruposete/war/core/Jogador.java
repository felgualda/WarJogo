package com.gruposete.war.core;

import java.util.ArrayList;
import java.util.List;

public class Jogador {
    private String nome;
    private CorJogador cor;

    private List<Territorio> territorios;
    private List<Carta> cartas;
    private Objetivo objetivo;
    private int playerId;

    private int exercitosDisponiveis;

    public Jogador(String nome, CorJogador cor, int playerId) {
        this.nome = nome;
        this.cor = cor;
        this.territorios = new ArrayList<>();
        this.cartas = new ArrayList<>();
        this.exercitosDisponiveis = 0;
        this.playerId = playerId;
    }

    public void adicionarExercitosDisponiveis(int quantidade) {
        if (quantidade > 0) {
            this.exercitosDisponiveis += quantidade;
        }
    }

    public void removerExercitosDisponiveis(int quantidade) {
        if (quantidade > 0) {
            this.exercitosDisponiveis -= quantidade;
            if (this.exercitosDisponiveis < 0) {
                this.exercitosDisponiveis = 0;
            }
        }
    }

    // GETTERS E SETTERS

    public String getNome() {
        return nome;
    }

    public CorJogador getCor() {
        return cor;
    }

    public List<Territorio> getTerritorios() {
        return territorios;
    }

    public List<Carta> getCartas() {
        return cartas;
    }

    public Carta getCartaByIndex(int index) {
        return cartas.get(index);
    }

    public int getExercitosDisponiveis() {
        return exercitosDisponiveis;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    public void setExercitosDisponiveis(int e) {
        this.exercitosDisponiveis = e;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int p) {
        this.playerId = p;
    }

    // OUTROS

    public int adicionarTerritorio(Territorio t) {
        territorios.add(t);
        return territorios.size();
    }

    public int removerTerritorio(Territorio t) {
        territorios.remove(t);
        return territorios.size();
    }

    public void adicionarCarta(Carta c) {
        cartas.add(c);
    }

    public void removerCarta(Carta c) {
        cartas.remove(c);
    }
}
