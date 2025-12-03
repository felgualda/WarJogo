package com.gruposete.war.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class GerenciadorAudio {
    private static GerenciadorAudio instancia;
    
    // Assets
    private Music musicaFundo;
    private Sound somDados;
    private Sound somConquista; // (Opcional, se tiver)

    // Estado
    private float volumeAtual;
    private static final String PREFS_NAME = "WarJogoConfigs";
    private static final String KEY_VOLUME = "volume";

    private GerenciadorAudio() {
        // Carrega volume salvo ou usa 0.5 (50%) como padrão
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        this.volumeAtual = prefs.getFloat(KEY_VOLUME, 0.1f);
    }

    public static GerenciadorAudio getInstance() {
        if (instancia == null) {
            instancia = new GerenciadorAudio();
        }
        return instancia;
    }

    public void inicializar() {
        try {
            // Carrega MÚSICA
            musicaFundo = Gdx.audio.newMusic(Gdx.files.internal("Audio/Hundred_Years_War.mp3"));
            musicaFundo.setLooping(true);
            atualizarVolumeMusica(); // Aplica o volume inicial
            musicaFundo.play();

            // Carrega SONS
            somDados = Gdx.audio.newSound(Gdx.files.internal("Audio/dados.mp3"));
            // somConquista = Gdx.audio.newSound(Gdx.files.internal("audio/conquista.mp3"));
            
        } catch (Exception e) {
            Gdx.app.error("GerenciadorAudio", "Erro ao carregar áudio: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE CONTROLE ---

    public void setVolume(float novoVolume) {
        this.volumeAtual = novoVolume;
        
        // 1. Atualiza a música tocando agora
        atualizarVolumeMusica();
        
        // 2. Salva na memória permanente
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putFloat(KEY_VOLUME, volumeAtual);
        prefs.flush();
    }

    public float getVolume() {
        return volumeAtual;
    }

    private void atualizarVolumeMusica() {
        if (musicaFundo != null) {
            musicaFundo.setVolume(volumeAtual);
        }
    }

    // --- MÉTODOS DE PLAY ---

    public void tocarDados() {
        if (somDados != null) {
            somDados.play(volumeAtual); // Toca com o volume configurado
        }
    }
    
    // public void tocarConquista() { if (somConquista != null) somConquista.play(volumeAtual); }

    public void dispose() {
        if (musicaFundo != null) musicaFundo.dispose();
        if (somDados != null) somDados.dispose();
        // if (somConquista != null) somConquista.dispose();
    }
}