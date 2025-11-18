package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.gruposete.war.core.Objetivo;

public class DialogoObjetivo extends Dialog {

    private Texture texturaObjetivo;

    public DialogoObjetivo(Objetivo objetivo, Skin skin) {
        super("", skin);

        setModal(true);
        setMovable(true);

        Table content = getContentTable();
        content.clear();
        content.pad(10);

        if (objetivo.getAssetPath() != null && !objetivo.getAssetPath().isEmpty()) {
            try {
                texturaObjetivo = new Texture(Gdx.files.internal(objetivo.getAssetPath()));
                Image imgObjetivo = new Image(texturaObjetivo);

                float texW = texturaObjetivo.getWidth();
                float texH = texturaObjetivo.getHeight();

                float targetHeight = 550f;
                float aspectRatio = texW / texH;
                float targetWidth = targetHeight * aspectRatio;

                content.add(imgObjetivo).size(targetWidth, targetHeight);

            } catch (Exception e) {
                Gdx.app.error("DialogoObjetivo", "Erro ao carregar imagem: " + objetivo.getAssetPath());
                content.add(new Label(objetivo.getDescricao(), skin)).pad(20);
            }
        } else {
            content.add(new Label(objetivo.getDescricao(), skin)).pad(20);
        }

        TextButton btnOk = new TextButton("OK", skin);
        btnOk.getLabel().setFontScale(1.5f);

        // Adiciona manualmente à tabela de botões para forçar tamanho maior
        getButtonTable().add(btnOk).width(200).height(60).pad(10);

        btnOk.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
    }

    @Override
    public void hide() {
        super.hide();
        if (texturaObjetivo != null) {
            texturaObjetivo.dispose();
        }
    }
}
