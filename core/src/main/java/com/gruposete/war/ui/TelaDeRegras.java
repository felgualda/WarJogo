package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class TelaDeRegras {

    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    private static final String PATH_SKIN = "ui/uiskin.json";
    private static final String PATH_BACKGROUND = "TelaRegrasBackground.png";

    private static final float TEXTO_WIDTH = 800f; // Um pouco mais largo para telas modernas
    private static final float FONT_SCALE_TITULO = 2.0f;
    private static final float FONT_SCALE_TEXTO = 1.3f;

    private static final String TEXTO_CONTEUDO = "\n\n EXÉRCITOS \n\nCada jogador escolhe o exército da cor que lhe agrade dentro das 6 possíveis \n (branco, preto, vermelho, azul, amarelo e verde).\n\n OBJETIVOS \n\nEm seguida à distribuição dos exércitos é feito o sorteio dos objetivos, recebendo cada jogador 1 objetivo dentre os 14 existentes, tomando conhecimento do seu teor e evitando revelá-lo aos seus adversários.\n\nInicialmente, cada jogador terá 1 exército da sua cor em cada um dos territórios recebidos durante o sorteio. Ao final desta operação todos os territórios estarão ocupados por um exército de algum dos participantes\n[Demonstrativo]";

    public Stage stage;
    private Skin skin;
    private Texture background;
    private Runnable voltarCallback;

    private BitmapFont fontTitulo;
    private BitmapFont fontTexto;

    public TelaDeRegras(Runnable voltarCallback) {
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
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        // Título
        Label.LabelStyle tituloStyle = new Label.LabelStyle(fontTitulo, skin.getColor("white"));
        Label titulo = new Label("Regras do Jogo", tituloStyle);
        root.add(titulo).padBottom(40).row();

        // Texto
        Label.LabelStyle textoStyle = new Label.LabelStyle(fontTexto, skin.getColor("white"));
        Label texto = new Label(TEXTO_CONTEUDO, textoStyle);
        texto.setAlignment(Align.center);
        texto.setWrap(true);
        
        // Adiciona texto com largura limitada
        root.add(texto).width(TEXTO_WIDTH).padBottom(60).row();

        // Botão Voltar
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
        
        root.add(btnVoltar).width(300).height(50);
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
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