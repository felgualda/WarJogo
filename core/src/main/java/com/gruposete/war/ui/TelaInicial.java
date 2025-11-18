package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaInicial {

    // --- CONSTANTES DE CONFIGURAÇÃO ---
    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    // --- CONSTANTES DE CAMINHOS (ASSETS) ---
    private static final String PATH_SKIN = "ui/uiskin.json";
    private static final String PATH_BACKGROUND = "TelaInicialBackground.png";
    private static final String PATH_LOGO = "logo.png";

    // --- CONSTANTES DE LAYOUT (POSICIONAMENTO) ---
    private static final float LOGO_X = 130f;
    private static final float LOGO_Y = 460f;

    private static final float BTN_WIDTH = 400f;
    private static final float BTN_HEIGHT = 50f;
    private static final float BTN_X = 120f;
    private static final float BTN_START_Y = 360f;
    private static final float BTN_SPACING = 80f;

    private static final float FONT_SCALE = 2.0f;

    // --- VARIÁVEIS DE CLASSE ---
    public Stage stage;
    private Skin skin;
    private Texture background;
    private Texture logoTexture; // Promovido a atributo para poder dar dispose

    // Callbacks de navegação
    private final Runnable jogarCallback;
    private final Runnable regrasCallback;
    private final Runnable configCallback;

    public TelaInicial(Runnable jogarCallback, Runnable regrasCallback, Runnable configCallback) {
        this.jogarCallback = jogarCallback;
        this.regrasCallback = regrasCallback;
        this.configCallback = configCallback;

        // 1. Inicialização do Stage
        stage = new Stage(new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        // 2. Carregamento de Assets e Estilos
        skin = new Skin(Gdx.files.internal(PATH_SKIN));
        background = new Texture(Gdx.files.internal(PATH_BACKGROUND));
        logoTexture = new Texture(Gdx.files.internal(PATH_LOGO));

        TextButtonStyle styleGrande = criarEstiloBotao();

        // 3. Construção da UI
        construirInterface(styleGrande);
    }

    /**
     * Configura o estilo do botão (Fonte, Imagens).
     */
    private TextButtonStyle criarEstiloBotao() {
        BitmapFont fontGrande = new BitmapFont(); // Fonte padrão
        fontGrande.getData().setScale(FONT_SCALE);

        TextButtonStyle style = new TextButtonStyle();
        style.up = skin.getDrawable("buttonUp");
        style.down = skin.getDrawable("buttonDown");
        style.over = skin.getDrawable("buttonOver");
        style.font = fontGrande;

        return style;
    }

    /**
     * Cria e posiciona todos os elementos visuais.
     */
    private void construirInterface(TextButtonStyle style) {
        // --- Logo ---
        Image tituloImage = new Image(logoTexture);
        tituloImage.setPosition(LOGO_X, LOGO_Y);
        stage.addActor(tituloImage);

        // --- Botões ---

        // Botão Jogar (Posição 0)
        criarBotao("Jogar", 0, style, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (jogarCallback != null) jogarCallback.run();
            }
        });

        // Botão Configurar (Posição 1)
        criarBotao("Configurar", 1, style, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (configCallback != null) configCallback.run();
            }
        });

        // Botão Regras (Posição 2)
        criarBotao("Regras", 2, style, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (regrasCallback != null) regrasCallback.run();
            }
        });

        // Botão Sair (Posição 3)
        criarBotao("Sair", 3, style, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    /**
     * Método auxiliar para padronizar a criação e posicionamento dos botões.
     */
    private void criarBotao(String texto, int indice, TextButtonStyle style, ChangeListener listener) {
        TextButton btn = new TextButton(texto, style);
        btn.setSize(BTN_WIDTH, BTN_HEIGHT);

        // Calcula a posição Y baseada no índice (0, 1, 2, 3)
        float posY = BTN_START_Y - (BTN_SPACING * indice);
        btn.setPosition(BTN_X, posY);

        btn.addListener(listener);
        stage.addActor(btn);
    }

    // --- CICLO DE VIDA ---

    public void render(float delta) {
        // Desenha o fundo
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        stage.getBatch().end();

        // Atualiza e desenha a UI
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
        logoTexture.dispose(); // Dispose adicionado para evitar vazamento de memória
    }
}
