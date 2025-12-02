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
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaDeConfig {

    // --- CONSTANTES DE CONFIGURAÇÃO ---
    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    // --- CONSTANTES DE CAMINHOS ---
    private static final String PATH_SKIN = "ui/uiskin.json";
    private static final String PATH_BACKGROUND = "TelaRegrasBackground.png"; // (Mantido conforme seu original)

    // --- CONSTANTES DE LAYOUT ---
    // Título
    private static final float TITULO_X = 525f;
    private static final float TITULO_Y = 620f;
    private static final float FONT_SCALE_TITULO = 2.0f;

    // Volume
    private static final float LABEL_VOL_X = 440f;
    private static final float LABEL_VOL_Y = 500f;
    private static final float SLIDER_X = 440f;
    private static final float SLIDER_Y = 450f;
    private static final float SLIDER_W = 400f;
    private static final float SLIDER_H = 40f;

    // Daltonismo
    private static final float CHECK_X = 440f;
    private static final float CHECK_Y = 380f;

    // Botão Voltar
    private static final float BTN_X = 460f;
    private static final float BTN_Y = 100f;
    private static final float BTN_W = 300f;
    private static final float BTN_H = 50f;

    // Estilo Geral
    private static final float FONT_SCALE_TEXTO = 1.5f;

    // --- VARIÁVEIS DA CLASSE ---
    public Stage stage;
    private Skin skin;
    private Texture background;
    private Runnable voltarCallback;

    // Fontes (Armazenadas para dar dispose depois)
    private BitmapFont fontTitulo;
    private BitmapFont fontTexto;

    public TelaDeConfig(Runnable voltarCallback) {
        this.voltarCallback = voltarCallback;

        // 1. Inicialização
        stage = new Stage(new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        // 2. Carregamento
        carregarAssets();

        // 3. Construção da UI
        construirInterface();
    }

    private void carregarAssets() {
        skin = new Skin(Gdx.files.internal(PATH_SKIN));
        background = new Texture(Gdx.files.internal(PATH_BACKGROUND));

        // Cria as fontes manualmente (Seguro se o skin não tiver nomes padrão)
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(FONT_SCALE_TITULO);

        fontTexto = new BitmapFont();
        fontTexto.getData().setScale(FONT_SCALE_TEXTO);
    }

    private void construirInterface() {
        // --- Título ---
        Label.LabelStyle tituloStyle = new Label.LabelStyle(fontTitulo, skin.getColor("white"));
        Label titulo = new Label("Configurações", tituloStyle);
        titulo.setPosition(TITULO_X, TITULO_Y);
        titulo.setAlignment(Align.center);
        stage.addActor(titulo);

        // --- Label Volume ---
        Label.LabelStyle textoStyle = new Label.LabelStyle(fontTexto, skin.getColor("white"));
        Label labelVolume = new Label("Volume", textoStyle);
        labelVolume.setPosition(LABEL_VOL_X, LABEL_VOL_Y);
        stage.addActor(labelVolume);

        // --- Slider Volume ---
        Slider sliderVolume = new Slider(0f, 1f, 0.01f, false, skin);
        sliderVolume.setValue(0.5f);
        sliderVolume.setSize(SLIDER_W, SLIDER_H);
        sliderVolume.setPosition(SLIDER_X, SLIDER_Y);
        stage.addActor(sliderVolume);

        // --- Checkbox Daltonismo ---
        CheckBox checkDaltonismo = new CheckBox("   Modo Daltonismo", skin);
        checkDaltonismo.getLabel().setFontScale(FONT_SCALE_TEXTO);
        checkDaltonismo.setPosition(CHECK_X, CHECK_Y);
        stage.addActor(checkDaltonismo);

        // --- Botão Voltar ---
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.getDrawable("buttonUp");
        btnStyle.down = skin.getDrawable("buttonDown");
        btnStyle.over = skin.getDrawable("buttonOver");
        btnStyle.font = fontTexto;

        TextButton btnVoltar = new TextButton("Voltar", btnStyle);
        btnVoltar.setSize(BTN_W, BTN_H);
        btnVoltar.setPosition(BTN_X, BTN_Y);
        btnVoltar.align(Align.center);

        btnVoltar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (voltarCallback != null) voltarCallback.run();
            }
        });
        stage.addActor(btnVoltar);
    }

    public void render(float delta) {
        // Limpeza de tela (Prevenção de artefatos visuais)
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
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

        // IMPORTANTE: Limpar as fontes que criamos manualmente
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontTexto != null) fontTexto.dispose();
    }
}
