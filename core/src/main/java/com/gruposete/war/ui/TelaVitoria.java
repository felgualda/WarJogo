package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport; // Importante

import com.gruposete.war.core.Jogador;

public class TelaVitoria {

    // --- CONSTANTES ---
    private static final float VIEWPORT_W = 1280f;
    private static final float VIEWPORT_H = 720f;
    private static final String PATH_SKIN = "ui/uiskin.json";
    private static final String PATH_BG = "TelaInicialBackground.png";

    private static final float FONT_SCALE_TITLE = 3.0f;
    private static final float FONT_SCALE_TEXT = 1.5f;

    // --- VARIÁVEIS ---
    public Stage stage;
    private Skin skin;
    private Texture background;
    private final Runnable menuCallback;
    private final Jogador vencedor;

    private BitmapFont fontTitulo;
    private BitmapFont fontTexto;

    public TelaVitoria(Runnable menuCallback, Jogador vencedor) {
        this.menuCallback = menuCallback;
        this.vencedor = vencedor;

        // 1. Viewport Responsivo
        stage = new Stage(new ExtendViewport(VIEWPORT_W, VIEWPORT_H));
        Gdx.input.setInputProcessor(stage);

        carregarAssets();
        construirUI();
    }

    private void carregarAssets() {
        skin = new Skin(Gdx.files.internal(PATH_SKIN));
        background = new Texture(Gdx.files.internal(PATH_BG));

        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(FONT_SCALE_TITLE);

        fontTexto = new BitmapFont();
        fontTexto.getData().setScale(FONT_SCALE_TEXT);
    }

    private void construirUI() {
        // Tabela Raiz que centraliza tudo
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center(); // Centraliza o conteúdo na tela
        mainTable.pad(50);

        // 1. Título "VITORIA"
        Label.LabelStyle styleTitulo = new Label.LabelStyle(fontTitulo, Color.YELLOW);
        Label lblTitulo = new Label("VITORIA!", styleTitulo);
        lblTitulo.setAlignment(Align.center);

        // 2. Nome do Jogador
        Label.LabelStyle styleNome = new Label.LabelStyle(fontTitulo, vencedor.getCor().getGdxColor());
        Label lblNome = new Label(vencedor.getNome().toUpperCase(), styleNome);
        lblNome.setAlignment(Align.center);

        // 3. Descrição do Objetivo
        Label.LabelStyle styleTexto = new Label.LabelStyle(fontTexto, Color.WHITE);
        String textoObj = "Objetivo Cumprido:\n" + vencedor.getObjetivo().getDescricao();
        Label lblObjetivo = new Label(textoObj, styleTexto);
        lblObjetivo.setAlignment(Align.center);
        lblObjetivo.setWrap(true);

        // 4. Botão Menu
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.getDrawable("buttonUp");
        btnStyle.down = skin.getDrawable("buttonDown");
        btnStyle.over = skin.getDrawable("buttonOver");
        btnStyle.font = fontTexto;

        TextButton btnMenu = new TextButton("Voltar ao Menu", btnStyle);
        btnMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (menuCallback != null) menuCallback.run();
            }
        });

        // Montagem
        mainTable.add(lblTitulo).padBottom(20).row();
        mainTable.add(lblNome).padBottom(50).row();
        mainTable.add(lblObjetivo).width(800).center().padBottom(60).row();
        mainTable.add(btnMenu).width(300).height(60);

        stage.addActor(mainTable);
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getViewport().apply();
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);

        stage.getBatch().begin();
        
        // --- Fundo Responsivo ---
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
        // True = Centraliza a câmera no mundo extendido
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        fontTitulo.dispose();
        fontTexto.dispose();
    }
}