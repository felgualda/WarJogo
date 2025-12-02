package com.gruposete.war.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.graphics.Color;

public class CartaTest {

    @Test
    public void testConstrutoresEGetters() {
        Territorio t = new Territorio("T", Color.WHITE, new float[]{0f,0f,5f,0f,5f,5f,0f,5f});
        Carta c1 = new Carta(10, "asset/path.png");
        assertEquals(10, c1.getId());
        assertEquals("asset/path.png", c1.getAssetPath());
        assertNull(c1.getTerritorio());

    Carta c2 = new Carta(11, SimboloCarta.CIRCULO, t, "outro.png");
    assertEquals(11, c2.getId());
    assertEquals(SimboloCarta.CIRCULO, c2.getSimbolo());
        assertSame(t, c2.getTerritorio());
        assertEquals("outro.png", c2.getAssetPath());
    }
}
