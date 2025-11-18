package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.gruposete.war.core.Objetivo;

public class DialogoObjetivo extends Dialog {

    // --- CONSTANTES DE LAYOUT ---
    private static final float TARGET_HEIGHT = 550f; // Altura desejada para a carta
    private static final float PADDING_CONTENT = 10f;
    private static final float PADDING_LABEL_FALLBACK = 20f;

    // Botão OK
    private static final float BTN_WIDTH = 200f;
    private static final float BTN_HEIGHT = 60f;
    private static final float BTN_PADDING = 10f;
    private static final float FONT_SCALE_BTN = 1.5f;

    // --- VARIÁVEIS ---
    private Texture texturaObjetivo;

    public DialogoObjetivo(Objetivo objetivo, Skin skin) {
        super("", skin);

        // Configurações da Janela
        setModal(true);
        setMovable(true);

        Table content = getContentTable();
        content.clear();
        content.pad(PADDING_CONTENT);

        // Montagem da UI
        configurarConteudo(content, objetivo, skin);
        configurarBotao(skin);
    }

    private void configurarConteudo(Table content, Objetivo objetivo, Skin skin) {
        boolean imagemCarregada = false;

        // Tenta carregar a imagem se o path existir
        if (objetivo.getAssetPath() != null && !objetivo.getAssetPath().isEmpty()) {
            try {
                texturaObjetivo = new Texture(Gdx.files.internal(objetivo.getAssetPath()));
                Image imgObjetivo = new Image(texturaObjetivo);

                // Cálculo de Proporção (Aspect Ratio)
                float texW = texturaObjetivo.getWidth();
                float texH = texturaObjetivo.getHeight();
                float aspectRatio = texW / texH;
                float targetWidth = TARGET_HEIGHT * aspectRatio;

                content.add(imgObjetivo).size(targetWidth, TARGET_HEIGHT);
                imagemCarregada = true;

            } catch (Exception e) {
                Gdx.app.error("DialogoObjetivo", "Erro ao carregar imagem: " + objetivo.getAssetPath());
                // Falha silenciosa, cai no fallback abaixo
            }
        }

        // Fallback: Se não carregou imagem, mostra texto
        if (!imagemCarregada) {
            content.add(new Label(objetivo.getDescricao(), skin)).pad(PADDING_LABEL_FALLBACK);
        }
    }

    private void configurarBotao(Skin skin) {
        TextButton btnOk = new TextButton("OK", skin);
        btnOk.getLabel().setFontScale(FONT_SCALE_BTN);

        btnOk.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        // Adiciona à tabela de botões do Dialog
        getButtonTable().add(btnOk).width(BTN_WIDTH).height(BTN_HEIGHT).pad(BTN_PADDING);
    }

    @Override
    public void hide() {
        super.hide();
        if (texturaObjetivo != null) {
            texturaObjetivo.dispose();
        }
    }
}
