package com.gruposete.war.core;


import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Array;

/**
 * Classe responsável por verificar se um jogador atingiu o seu objetivo no jogo WAR.
 * Pode verificar três tipos principais de objetivos:
 * 1. Eliminar um jogador específico.
 * 2. Conquistar determinados continentes.
 * 3. Conquistar um número específico de territórios.
 */

public class VerificadorObjetivos {
    private List<Jogador> jogadores;
    private Array<Territorio> territorios;
    
    public VerificadorObjetivos(List<Jogador> jogadores, Array<Territorio> territorios) {
        this.jogadores = jogadores;
        this.territorios = territorios;
    }

    // Método para verificar se o jogador já completou seu próprio Objetivo
    public boolean verificarObjetivo(Jogador jogador){
        Objetivo objetivo = jogador.getObjetivo();

        switch (objetivo.getTipo()) {
            case ELIMINAR_JOGAOR:
                return verficarEliminacaoJogador(objetivo.getCorJogadorAlvo());
            case CONQUISTAR_CONTINENTE:
                return verificarConquistaContinente(jogador, objetivo.getContinentesAlvo());
            case CONQUISTAR_TERRITORIOS:
                return verificarConquistarTerritorios(jogador, objetivo.getQtdTerritoriosAlvo());
            default:

                System.out.println("ERRO: Não foi possível verificar o tipo do Objetivo.");
                break;
        }


        return true;
    }

    // Verifica se determinado jogador possui a quantidade de territórios suficientes e se cumpre as demandas para vencer
    private boolean verificarConquistarTerritorios(Jogador jogador, int qtdTerritoriosAlvo) {
        int qtdTerritoriosJogador = jogador.getTerritorios().size();
        List<Territorio> territoriosJogador = jogador.getTerritorios();

        if (qtdTerritoriosAlvo == 24 && qtdTerritoriosJogador >= qtdTerritoriosAlvo){
            return true;
        }
        else if (qtdTerritoriosAlvo == 18 && qtdTerritoriosJogador >= qtdTerritoriosAlvo) {
            int territoriosValidos = 0;
            for (Territorio t : territoriosJogador){
                if (t.getTropas() >= 2){
                    territoriosValidos++;
                }
            }

            if (territoriosValidos >= qtdTerritoriosAlvo){
                return true;
            }
        }

        return false;
    }

    // Verifica se determinado jogador conquistou os Continentes necessários para vencer
    private boolean verificarConquistaContinente(Jogador jogador, String[] continentesAlvo) {
        List<String> continentesFixos = new ArrayList<>();
        boolean temCoringa = false;

        // Separa continentes fixos e conta coringas
        for (String continente : continentesAlvo) {
            if ("*".equals(continente)) {
                temCoringa = true;
            } else {
                continentesFixos.add(continente);
            }
        }

        // Verifica continentes fixos
        for (String continente : continentesFixos) {
            if (!verificarContinenteConquistado(jogador, continente)) {
                return false;
            }
        }

        // Verifica continentes coringa
        if (temCoringa) {
            return verificarContinentesCoringa(jogador, continentesFixos);
        }

        return true;
    }

    // Usado para verificar se o jogador conquistou um Continente diferente dos específicados no seu Objetivo
    private boolean verificarContinentesCoringa(Jogador jogador, List<String> continentesFixos) {
        // Lista de todos os continentes possíveis
        String[] todosContinentes = {
            "América do Norte", "América do Sul", "África", 
            "Oceania", "Europa", "Ásia"
        };

        for (String continente : todosContinentes) {
            // Pula os continentes que já são obrigatórios (fixos)
            if (continentesFixos.contains(continente)) {
                continue;
            }

            // Verifica se conquistou este continente extra
            if (verificarContinenteConquistado(jogador, continente)) {
                return true;

            }
        }

        return false;
    }

    // Percorre os territórios do Jogador para verificar se ele conquistou um Continente inteiro
    private boolean verificarContinenteConquistado(Jogador jogador, String continente) {
        List<Territorio> territoriosJogador = jogador.getTerritorios();
        int territoriosNoContinente = 0;
        int territoriosConquistadosNoContinente = 0;

        // Conta territórios totais e conquistados no continente
        for (Territorio t : territorios) {
            if (continente.equals(t.getContinente())) {
                territoriosNoContinente++;
                if (territoriosJogador.contains(t)) {
                    territoriosConquistadosNoContinente++;
                }
            }
        }

        // Verifica se conquistou todos os territórios do continente
        return territoriosConquistadosNoContinente == territoriosNoContinente && territoriosNoContinente > 0;
    }

    // PRECISA FAZER INTEGRAÇÂO COM ControladorDePartida
    // Atualmente apenas verifica se o alvo morreu, não se foi morto pelo Jogador.
    private boolean verficarEliminacaoJogador(CorJogador corJogadorAlvo) {

        for (Jogador j : this.jogadores){
            if (j.getCor() == corJogadorAlvo && j.getTerritorios().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    // Verificar o Objetivo de todos os jogadores de uma só vez.
    public Jogador verificarTodosObjetivos(){
        for (Jogador jogador : jogadores){
            if (verificarObjetivo(jogador)){
                return jogador;
            }

        }

        return null;
    }
}
