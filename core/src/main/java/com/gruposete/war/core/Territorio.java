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

    // Método para recuperar o continente de um território, usado em VerificadorObjetivos
    public String getContinente() {
        String nome = this.getNome();

        if (nome.equals("Mexico") || nome.equals("California") || 
            nome.equals("Nova Iorque") || nome.equals("Vancouver") || 
            nome.equals("Ottawa") || nome.equals("Labrador") || 
            nome.equals("Mackenzie") || nome.equals("Alasca") ||
            nome.equals("Groenlândia")) {
            return "América do Norte";
        }

        if (nome.equals("Brasil") || nome.equals("Peru") || 
            nome.equals("Argentina") || nome.equals("Venezuela")) {
            return "América do Sul";
        }

        if (nome.equals("Argelia") || nome.equals("Congo") || 
            nome.equals("Africa do Sul") || nome.equals("Sudão") || 
            nome.equals("Egito") || nome.equals("Madagascar")) {
            return "África";
        }

        if (nome.equals("Australia") || nome.equals("Nova Guiné") || 
            nome.equals("Sumatra") || nome.equals("Borneo")) {
            return "Oceania";
        }

        if (nome.equals("Islândia") || nome.equals("Inglaterra") || 
            nome.equals("França") || nome.equals("Alemanha") || 
            nome.equals("Polônia") || nome.equals("Moscou") || 
            nome.equals("Suécia")) {
            return "Europa";
        }

        if (nome.equals("Oriente Médio") || nome.equals("India") || 
            nome.equals("Aral") || nome.equals("Omsk") || 
            nome.equals("Dudinka") || nome.equals("Mongólia") || 
            nome.equals("Tchita") || nome.equals("China") || 
            nome.equals("Vietnã") || nome.equals("Japão") || 
            nome.equals("Vladvostok") || nome.equals("Sibéria")) {
            return "Ásia";
        }

        return "ERRO: Não foi possível definir o continente do Território: " + nome;
    }
    
    public void setTropas(int tropas) {
        this.tropas = tropas;
    }
}
