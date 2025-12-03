package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.gruposete.war.core.GerenciadorAudio;

public class TelaDeConfig {

    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    private static final String PATH_SKIN = "ui/uiskin.json";
    private static final String PATH_BACKGROUND = "TelaRegrasBackground.png";

    // Tamanhos fixos para elementos
    private static final float SLIDER_W = 400f;
    private static final float BTN_W = 300f;
    private static final float BTN_H = 50f;

    private static final float FONT_SCALE_TITULO = 2.0f;
    private static final float FONT_SCALE_TEXTO = 1.5f;

    public Stage stage;
    private Skin skin;
    private Texture background;
    private Runnable voltarCallback;

    private BitmapFont fontTitulo;
    private BitmapFont fontTexto;

    public TelaDeConfig(Runnable voltarCallback) {
        this.voltarCallback = voltarCallback;

        // 1. ExtendViewport
        stage = new Stage(new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        carregarAssets();
        construirInterface();
    }

    private void carregarAssets() {
        skin = new Skin(Gdx.files.internal(PATH_SKIN));
        background = new Texture(Gdx.files.internal(PATH_BACKGROUND));

        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(FONT_SCALE_TITULO);

        fontTexto = new BitmapFont();
        fontTexto.getData().setScale(FONT_SCALE_TEXTO);
    }

    private void construirInterface() {
        // Tabela Raiz
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        // --- Título ---
        Label.LabelStyle tituloStyle = new Label.LabelStyle(fontTitulo, skin.getColor("white"));
        Label titulo = new Label("Configurações", tituloStyle);
        root.add(titulo).padBottom(80).row();

        // --- Volume ---
        Label.LabelStyle textoStyle = new Label.LabelStyle(fontTexto, skin.getColor("white"));
        root.add(new Label("Volume", textoStyle)).padBottom(10).row();

        float volInicial = GerenciadorAudio.getInstance().getVolume();

        Slider sliderVolume = new Slider(0f, 0.1f, 0.001f, false, skin);
        sliderVolume.setValue(volInicial);
        root.add(sliderVolume).width(SLIDER_W).padBottom(40).row();

        sliderVolume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float novoVol = sliderVolume.getValue();
                GerenciadorAudio.getInstance().setVolume(novoVol);
            }
        });

        root.add(sliderVolume).width(SLIDER_W).padBottom(40).row();

        // --- Daltonismo ---

        final CheckBox checkDaltonismo = new CheckBox("   Modo Daltonismo", skin);
        checkDaltonismo.getLabel().setFontScale(FONT_SCALE_TEXTO);
        
        // 1. Carrega o estado salvo (Padrão: false)
        boolean isDaltonico = Gdx.app.getPreferences("WarJogoConfigs").getBoolean("daltonismo", false);
        checkDaltonismo.setChecked(isDaltonico);

        // 2. Salva quando clicar
        checkDaltonismo.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean ativado = checkDaltonismo.isChecked();
                Gdx.app.getPreferences("WarJogoConfigs").putBoolean("daltonismo", ativado);
                Gdx.app.getPreferences("WarJogoConfigs").flush(); // Salva no disco
            }
        });

        // Ajuste fino na célula da tabela para centralizar visualmente
        root.add(checkDaltonismo).padBottom(100).row();

        // --- Botão Voltar ---
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.getDrawable("buttonUp");
        btnStyle.down = skin.getDrawable("buttonDown");
        btnStyle.over = skin.getDrawable("buttonOver");
        btnStyle.font = fontTexto;

        TextButton btnVoltar = new TextButton("Voltar", btnStyle);
        btnVoltar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (voltarCallback != null) voltarCallback.run();
            }
        });
        
        root.add(btnVoltar).size(BTN_W, BTN_H);
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getViewport().apply();
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);

        stage.getBatch().begin();
        
        // Fundo Responsivo
        float screenW = stage.getViewport().getWorldWidth();
        float screenH = stage.getViewport().getWorldHeight();
        float bgW = background.getWidth();
        float bgH = background.getHeight();
        float scale = Math.max(screenW / bgW, screenH / bgH);
        float drawW = bgW * scale;
        float drawH = bgH * scale;
        float drawX = (screenW - drawW) / 2;
        float drawY = (screenH - drawH) / 2;

        stage.getBatch().draw(background, drawX, drawY, drawW, drawH);
        
        stage.getBatch().end();

        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontTexto != null) fontTexto.dispose();
    }
}