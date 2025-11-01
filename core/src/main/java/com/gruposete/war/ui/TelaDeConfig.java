package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaDeConfig {
    public Stage stage;
    private Skin skin;
    private Texture background;
    private Runnable voltarCallback;

    public TelaDeConfig(Runnable voltarCallback) {
        this.voltarCallback = voltarCallback;
        stage = new Stage(new FitViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        background = new Texture(Gdx.files.internal("TelaRegrasBackground.png")); // opcional

        BitmapFont fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(2f);

        BitmapFont fontTexto = new BitmapFont();
        fontTexto.getData().setScale(1.5f);

        // Título
        Label.LabelStyle tituloStyle = new Label.LabelStyle(fontTitulo, skin.getColor("white"));
        Label titulo = new Label("Configurações", tituloStyle);
        titulo.setPosition(525, 620);
        titulo.setAlignment(Align.center);
        //titulo.setWidth(1280);

        // slider volume

        Slider sliderVolume = new Slider(0f, 1f, 0.01f, false, skin);
        sliderVolume.setValue(0.5f); // valor inicial
        sliderVolume.setSize(400, 40);
        sliderVolume.setPosition(440, 450);

        Label labelVolume = new Label("Volume", new Label.LabelStyle(fontTexto, skin.getColor("white")));
        labelVolume.setPosition(440, 500);

        //check box daltonismo
        CheckBox checkDaltonismo = new CheckBox("   Modo Daltonismo", skin);
        checkDaltonismo.getLabel().setFontScale(1.5f);
        checkDaltonismo.setPosition(440, 380);

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
        stage.addActor(labelVolume);
        stage.addActor(sliderVolume);
        stage.addActor(checkDaltonismo);
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
