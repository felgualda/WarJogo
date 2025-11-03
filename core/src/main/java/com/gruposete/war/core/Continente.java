package com.gruposete.war.core;

public enum Continente {
    AMERICA_NORTE("America do Norte", 9, 5),    // +5 exércitos
    AMERICA_SUL("America do Sul", 4, 2),        // +2 exércitos
    AFRICA("Africa", 6, 3),                     // +3 exércitos
    EUROPA("Europa", 7, 5),                     // +5 exércitos
    ASIA("Asia", 12, 7),                        // +7 exércitos
    OCEANIA("Oceania", 4, 2);                   // +2 exércitos

    private final String nome;
    private final int numTerritorios;
    private final int bonusExercitos;

    Continente(String nome, int numTerritorios, int bonusExercitos) {
        this.nome = nome;
        this.numTerritorios = numTerritorios;
        this.bonusExercitos = bonusExercitos;
    }

    public String getNome() { return nome; }
    public int getNumTerritorios() { return numTerritorios; }
    public int getBonusExercitos() { return bonusExercitos; }
}