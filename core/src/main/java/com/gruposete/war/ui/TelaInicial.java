package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class TelaInicial {

    // --- CONSTANTES ---
    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    private static final String PATH_SKIN = "ui/uiskin.json";
    private static final String PATH_BACKGROUND = "TelaInicialBackground.png";
    private static final String PATH_LOGO = "logo.png";

    private static final float BTN_WIDTH = 400f;
    private static final float BTN_HEIGHT = 60f; // Um pouco maior para toque
    private static final float FONT_SCALE = 2.0f;

    // --- VARIÁVEIS ---
    public Stage stage;
    private Skin skin;
    private Texture background;
    private Texture logoTexture;

    private final Runnable jogarCallback;
    private final Runnable regrasCallback;
    private final Runnable configCallback;

    // Fontes para dispose
    private BitmapFont fontBotao;

    public TelaInicial(Runnable jogarCallback, Runnable regrasCallback, Runnable configCallback) {
        this.jogarCallback = jogarCallback;
        this.regrasCallback = regrasCallback;
        this.configCallback = configCallback;

        // 1. Viewport Responsivo
        stage = new Stage(new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        // 2. Assets
        skin = new Skin(Gdx.files.internal(PATH_SKIN));
        background = new Texture(Gdx.files.internal(PATH_BACKGROUND));
        logoTexture = new Texture(Gdx.files.internal(PATH_LOGO));

        // 3. Estilo
        fontBotao = new BitmapFont();
        fontBotao.getData().setScale(FONT_SCALE);
        
        TextButtonStyle style = new TextButtonStyle();
        style.up = skin.getDrawable("buttonUp");
        style.down = skin.getDrawable("buttonDown");
        style.over = skin.getDrawable("buttonOver");
        style.font = fontBotao;

        // 4. Construção com TABLE (Layout Responsivo)
        construirInterface(style);
    }

    private void construirInterface(TextButtonStyle style) {
        // Cria uma tabela raiz que preenche toda a tela
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // --- Logo ---
        Image logoImage = new Image(logoTexture);
        
        // --- Botões ---
        TextButton btnJogar = criarBotao("Jogar", style, jogarCallback);
        TextButton btnConfig = criarBotao("Configurar", style, configCallback);
        TextButton btnRegras = criarBotao("Regras", style, regrasCallback);
        TextButton btnSair = criarBotao("Sair", style, () -> Gdx.app.exit());

        // --- Montagem do Layout ---
        // Adiciona logo no topo
        rootTable.add(logoImage).padBottom(50).row();

        // Adiciona botões em pilha
        rootTable.add(btnJogar).size(BTN_WIDTH, BTN_HEIGHT).padBottom(20).row();
        rootTable.add(btnConfig).size(BTN_WIDTH, BTN_HEIGHT).padBottom(20).row();
        rootTable.add(btnRegras).size(BTN_WIDTH, BTN_HEIGHT).padBottom(20).row();
        rootTable.add(btnSair).size(BTN_WIDTH, BTN_HEIGHT).padBottom(20).row();
        
        // (Opcional) Debug lines para ver o alinhamento
        // rootTable.setDebug(true);
    }

    private TextButton criarBotao(String texto, TextButtonStyle style, final Runnable action) {
        TextButton btn = new TextButton(texto, style);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (action != null) action.run();
            }
        });
        return btn;
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getViewport().apply();
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
        
        stage.getBatch().begin();
        
        // --- Background Responsivo (Preenche tudo) ---
        float screenW = stage.getViewport().getWorldWidth();
        float screenH = stage.getViewport().getWorldHeight();
        
        float bgW = background.getWidth();
        float bgH = background.getHeight();
        float scale = Math.max(screenW / bgW, screenH / bgH);
        
        float drawW = bgW * scale;
        float drawH = bgH * scale;
        // Centraliza o background
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
        logoTexture.dispose();
        fontBotao.dispose();
    }
}