package com.gruposete.war.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.graphics.Color;

public class JogadorTest {

    @Test
    public void testAdicionarRemoverExercitos() {
        Jogador j = new Jogador("A", CorJogador.AZUL, 1, false);
        assertEquals(0, j.getExercitosDisponiveis());

        j.adicionarExercitosDisponiveis(5);
        assertEquals(5, j.getExercitosDisponiveis());

        // não deve aceitar valores negativos
        j.adicionarExercitosDisponiveis(-3);
        assertEquals(5, j.getExercitosDisponiveis());

        j.removerExercitosDisponiveis(2);
        assertEquals(3, j.getExercitosDisponiveis());

        // ao remover mais do que disponível, zera
        j.removerExercitosDisponiveis(10);
        assertEquals(0, j.getExercitosDisponiveis());
    }

    @Test
    public void testTerritoriosECartas() {
        Jogador j = new Jogador("B", CorJogador.VERMELHO, 2, false);

        Territorio t = new Territorio("Teste", Color.WHITE, new float[]{0f,0f,10f,0f,10f,10f,0f,10f});
        assertEquals(0, j.getTerritorios().size());
        int size = j.adicionarTerritorio(t);
        assertEquals(1, size);
        assertTrue(j.getTerritorios().contains(t));

        size = j.removerTerritorio(t);
        assertEquals(0, size);

    Carta c = new Carta(1, SimboloCarta.CIRCULO, t, "path");
        j.adicionarCarta(c);
        assertEquals(1, j.getCartas().size());
        assertSame(c, j.getCartaByIndex(0));

        j.removerCarta(c);
        assertEquals(0, j.getCartas().size());
    }

    @Test
    public void testIsAIAndIds() {
        Jogador ai = new Jogador("Bot", CorJogador.AMARELO, 5, true);
        assertTrue(ai.getIsAI());
        ai.setIsAI(false);
        assertFalse(ai.getIsAI());

        assertEquals(5, ai.getPlayerId());
        ai.setPlayerId(7);
        assertEquals(7, ai.getPlayerId());
    }
}
