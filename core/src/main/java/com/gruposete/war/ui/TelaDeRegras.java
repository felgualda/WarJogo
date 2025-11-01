package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaDeRegras {
    public Stage stage;
    private Skin skin;
    private Texture background;
    private Runnable voltarCallback;

    public TelaDeRegras(Runnable voltarCallback) {
        this.voltarCallback = voltarCallback;
        stage = new Stage(new FitViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        background = new Texture(Gdx.files.internal("TelaRegrasBackground.png")); // opcional

        BitmapFont fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(2f);

        BitmapFont fontTexto = new BitmapFont();
        fontTexto.getData().setScale(1.3f);

        // Título
        Label.LabelStyle tituloStyle = new Label.LabelStyle(fontTitulo, skin.getColor("white"));
        Label titulo = new Label("Regras do Jogo", tituloStyle);
        titulo.setPosition(525, 620);
        titulo.setAlignment(Align.center);
        //titulo.setWidth(1280);

        // Texto explicativo
        Label.LabelStyle textoStyle = new Label.LabelStyle(fontTexto, skin.getColor("white"));
        Label texto = new Label("\n\n EXÉRCITOS \n\nCada jogador escolhe o exército da cor que lhe agrade dentro das 6 possíveis \n (branco, preto, vermelho, azul, amarelo e verde).\n\n OBJETIVOS \n\nEm seguida à distribuição dos exércitos é feito o sorteio dos objetivos, recebendo cada jogador 1 objetivo dentre os 14 existentes, tomando conhecimento do seu teor e evitando revelá-lo aos seus adversários.\n\nInicialmente, cada jogador terá 1 exército da sua cor em cada um dos territórios recebidos durante o sorteio. Ao final desta operação todos os territórios estarão ocupados por um exército de algum dos participantes\n[Demonstrativo]", textoStyle);
        texto.setPosition(480-150, 280);
        texto.setAlignment(Align.center);
        texto.setWidth(600);
        texto.setWrap(true);

        // botão Voltar
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.getDrawable("buttonUp");
        btnStyle.down = skin.getDrawable("buttonDown");
        btnStyle.font = fontTexto;

        TextButton btnVoltar = new TextButton("Voltar", btnStyle);
        btnVoltar.setSize(300, 50);
        btnVoltar.setPosition(460, 100);
        btnVoltar.align(Align.center);

        btnVoltar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (voltarCallback != null) voltarCallback.run();
            }
        });

        // adiciona ao stage
        stage.addActor(titulo);
        stage.addActor(texto);
        stage.addActor(btnVoltar);
    }

    public void render(float delta) {
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight());
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
    }
}
