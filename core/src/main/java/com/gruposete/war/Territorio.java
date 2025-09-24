package com.gruposete.war;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Territorio {
    private String nome;
    private Polygon area;
    private int tropas;
    private int playerId;

    public Territorio(String nome, float[] vertices){
        this.nome = nome;
        this.area = new Polygon(vertices);
        this.area.setOrigin(0, 0);
        this.tropas = 0;
        this.playerId = 0;
    }

    public boolean contains(float x, float y){
        return area.contains(x, y);
    }

    public Vector2 getCentro() {
        Rectangle bounds = area.getBoundingRectangle();
        return new Vector2(bounds.x + bounds.width/2, bounds.y + bounds.height/2);
    }

    public void incrementarTropas(){
        this.tropas++;
    }

    public void desenharTexto(BitmapFont font, Batch batch) {
        float[] verts = area.getTransformedVertices();
        float centroX = 0, centroY = 0;
        for (int i = 0; i < verts.length; i += 2) {
            centroX += verts[i];
            centroY += verts[i + 1];
        }
        centroX /= (verts.length / 2);
        centroY /= (verts.length / 2);

        font.draw(batch, String.valueOf(tropas), centroX, centroY);
    }


    public String getNome(){ return this.nome; }
    public int getTropas(){ return this.tropas; }
    public Polygon getArea(){ return this.area; }
    public float[] getVertices(){ return this.area.getVertices(); }
}
