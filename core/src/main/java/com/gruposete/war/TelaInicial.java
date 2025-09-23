package com.gruposete.war;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaInicial {
    public Stage stage;
    private Skin skin;
    private Texture background;
    private Runnable jogarCallback;

    public TelaInicial(Runnable jogarCallback) {
        this.jogarCallback = jogarCallback;
        stage = new Stage(new FitViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);

        // Carrega skin e fundo
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        background = new Texture(Gdx.files.internal("TelaInicialBackground.png")); // coloque em assets/

        //aumentar a fonte
        BitmapFont fontGrande = new BitmapFont(); // fonte padrão
        fontGrande.getData().setScale(2f); // aumenta 2x

        TextButton.TextButtonStyle styleGrande = new TextButton.TextButtonStyle();
        styleGrande.up = skin.getDrawable("buttonUp");    // drawable existente
        styleGrande.down = skin.getDrawable("buttonDown");
        styleGrande.over = skin.getDrawable("buttonOver");
        styleGrande.font = fontGrande;
        // Cria botões
        TextButton btnJogar = new TextButton("Jogar", styleGrande);
        TextButton btnConfigurar = new TextButton("Configurar", styleGrande);
        TextButton btnRegras = new TextButton("Regras", styleGrande);
        TextButton btnSair = new TextButton("Sair", styleGrande);

        // Tamanho e posição
        // Configurações de layout dos botões
        float btnWidth = 400;
        float btnHeight = 50;
        float btnX = 120;
        float btnYStart = 360;   // posição Y do primeiro botão
        float btnSpacing = 80;   // espaço entre os botões

        btnJogar.setSize(btnWidth, btnHeight);
        btnJogar.setPosition(btnX, btnYStart);

        btnConfigurar.setSize(btnWidth, btnHeight);
        btnConfigurar.setPosition(btnX, btnYStart - btnSpacing);

        btnRegras.setSize(btnWidth, btnHeight);
        btnRegras.setPosition(btnX, btnYStart - btnSpacing * 2);

        btnSair.setSize(btnWidth, btnHeight);
        btnSair.setPosition(btnX, btnYStart - btnSpacing * 3);

        // Eventos
        btnJogar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (jogarCallback != null) jogarCallback.run();
            }
        });

        btnConfigurar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Configurar clicado!");
            }
        });

        btnSair.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        // Adiciona ao stage
        stage.addActor(btnJogar);
        stage.addActor(btnConfigurar);
        stage.addActor(btnRegras);
        stage.addActor(btnSair);
    }

    public void render(float delta) {
        // Desenha o fundo
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight());
        stage.getBatch().end();

        // Atualiza e desenha stage
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
