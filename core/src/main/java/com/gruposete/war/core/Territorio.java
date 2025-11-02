package com.gruposete.war.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gruposete.war.utils.Utils;

public class Territorio {
    private String nome;
    private Polygon area;
    private int tropas;
    private int playerId;
    private Color color;

    public Territorio(String nome,Color color, float[] vertices){
        this.nome = nome;
        float[] corrected_vertices = Utils.multiplicarPontos(vertices, 1.3f);
        this.area = new Polygon(corrected_vertices);
        this.area.setOrigin(0, 0);
        this.tropas = 0;
        this.color = color;
        this.playerId = 0;
    }

    public void resetarParaNovoJogo(){
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

    public void decrementarTropas(){
        if (this.tropas <= 0) {return;}
        this.tropas--;
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

    public void setPlayerId(int playerId){
        this.playerId = playerId;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public String getNome(){ return this.nome; }
    public int getTropas(){ return this.tropas; }
    public Polygon getArea(){ return this.area; }
    public float[] getVertices(){ return this.area.getVertices(); }
    public Color getColor(){ return this.color; }
    public int getPlayerId(){ return this.playerId; };
}
