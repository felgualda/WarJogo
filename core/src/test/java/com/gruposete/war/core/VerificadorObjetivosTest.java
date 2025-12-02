package com.gruposete.war.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.List;

public class VerificadorObjetivosTest {

    @Test
    public void testVerificarConquistarTerritorios_24() {
        Jogador j = new Jogador("X", CorJogador.AZUL, 1, false);
        for (int i = 0; i < 24; i++) {
            j.adicionarTerritorio(new Territorio("T" + i, Color.WHITE, new float[]{0f,0f,2f,0f,2f,2f,0f,2f}));
        }

        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j);

        Array<Territorio> territorios = new Array<>();
        VerificadorObjetivos v = new VerificadorObjetivos(jogadores, territorios, new ControladorDePartida(jogadores));

        Objetivo obj = new Objetivo(1, "Conquistar 24", "", 24);
        j.setObjetivo(obj);

        assertTrue(v.verificarObjetivo(j));
    }

    @Test
    public void testVerificarConquistarTerritorios_18_requires_two_troops() {
        Jogador j = new Jogador("Y", CorJogador.AMARELO, 2, false);
        for (int i = 0; i < 18; i++) {
            Territorio t = new Territorio("T" + i, Color.WHITE, new float[]{0f,0f,2f,0f,2f,2f,0f,2f});
            t.setTropas(2);
            j.adicionarTerritorio(t);
        }

        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j);
        Array<Territorio> territorios = new Array<>();

        VerificadorObjetivos v = new VerificadorObjetivos(jogadores, territorios, new ControladorDePartida(jogadores));
        Objetivo obj = new Objetivo(2, "Conquistar 18", "", 18);
        j.setObjetivo(obj);

        assertTrue(v.verificarObjetivo(j));
    }

    @Test
    public void testVerificarConquistaContinente() {
        // América do Sul: Brasil, Peru, Argentina, Venezuela
        Territorio brasil = new Territorio("Brasil", Color.WHITE, new float[]{0,0,1,0,1,1,0,1});
        Territorio peru = new Territorio("Peru", Color.WHITE, new float[]{0,0,1,0,1,1,0,1});
        Territorio argentina = new Territorio("Argentina", Color.WHITE, new float[]{0,0,1,0,1,1,0,1});
        Territorio venezuela = new Territorio("Venezuela", Color.WHITE, new float[]{0,0,1,0,1,1,0,1});

        Jogador j = new Jogador("Z", CorJogador.VERMELHO, 3, false);
        j.adicionarTerritorio(brasil);
        j.adicionarTerritorio(peru);
        j.adicionarTerritorio(argentina);
        j.adicionarTerritorio(venezuela);

        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j);

        Array<Territorio> territorios = new Array<>();
        territorios.addAll(brasil, peru, argentina, venezuela);

        VerificadorObjetivos v = new VerificadorObjetivos(jogadores, territorios, new ControladorDePartida(jogadores));

        Objetivo obj = new Objetivo(3, "Conquistar América do Sul", "", new String[]{"América do Sul"});
        j.setObjetivo(obj);

        assertTrue(v.verificarObjetivo(j));
    }

    @Test
    public void testVerificarEliminacaoJogador() {
        // jogador alvo sem territorios
        Jogador eliminador = new Jogador("E", CorJogador.AZUL, 10, false);
        Jogador alvo = new Jogador("V", CorJogador.VERMELHO, 11, false);

        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(eliminador);
        jogadores.add(alvo);

        Array<Territorio> territorios = new Array<>();

        // cria um controlador que retorna 'eliminador' como quem eliminou o 'alvo'
        ControladorDePartida controlador = new ControladorDePartida(jogadores) {
            @Override
            public Jogador getEliminadorDe(Jogador jogadorEliminado) {
                return eliminador;
            }
        };

        VerificadorObjetivos v = new VerificadorObjetivos(jogadores, territorios, controlador);

        Objetivo obj = new Objetivo(4, "Eliminar alvo", "", CorJogador.VERMELHO);
        eliminador.setObjetivo(obj);

        // o alvo não tem territorios (está eliminado)
        assertTrue(v.verificarObjetivo(eliminador));
    }
}
