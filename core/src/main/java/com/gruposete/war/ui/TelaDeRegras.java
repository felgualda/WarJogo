package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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

    // --- CONSTANTES DE CONFIGURAÇÃO ---
    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    // --- CONSTANTES DE CAMINHOS ---
    private static final String PATH_SKIN = "ui/uiskin.json";
    private static final String PATH_BACKGROUND = "TelaRegrasBackground.png";

    // --- CONSTANTES DE LAYOUT ---
    // Título
    private static final float TITULO_X = 525f;
    private static final float TITULO_Y = 620f;
    private static final float FONT_SCALE_TITULO = 2.0f;

    // Texto Explicativo
    private static final float TEXTO_X = 330f; // (480 - 150)
    private static final float TEXTO_Y = 280f;
    private static final float TEXTO_WIDTH = 600f;
    private static final float FONT_SCALE_TEXTO = 1.3f;

    // Botão Voltar
    private static final float BTN_X = 460f;
    private static final float BTN_Y = 100f;
    private static final float BTN_WIDTH = 300f;
    private static final float BTN_HEIGHT = 50f;

    // Conteúdo do Texto (Mantido original)
    private static final String TEXTO_CONTEUDO = "\n\n EXÉRCITOS \n\nCada jogador escolhe o exército da cor que lhe agrade dentro das 6 possíveis \n (branco, preto, vermelho, azul, amarelo e verde).\n\n OBJETIVOS \n\nEm seguida à distribuição dos exércitos é feito o sorteio dos objetivos, recebendo cada jogador 1 objetivo dentre os 14 existentes, tomando conhecimento do seu teor e evitando revelá-lo aos seus adversários.\n\nInicialmente, cada jogador terá 1 exército da sua cor em cada um dos territórios recebidos durante o sorteio. Ao final desta operação todos os territórios estarão ocupados por um exército de algum dos participantes\n[Demonstrativo]";

    // --- VARIÁVEIS DA CLASSE ---
    public Stage stage;
    private Skin skin;
    private Texture background;
    private Runnable voltarCallback;

    // Fontes (Armazenadas para dispose)
    private BitmapFont fontTitulo;
    private BitmapFont fontTexto;

    public TelaDeRegras(Runnable voltarCallback) {
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

        // Cria fontes manualmente para aplicar escalas específicas
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(FONT_SCALE_TITULO);

        fontTexto = new BitmapFont();
        fontTexto.getData().setScale(FONT_SCALE_TEXTO);
    }

    private void construirInterface() {
        // --- Título ---
        Label.LabelStyle tituloStyle = new Label.LabelStyle(fontTitulo, skin.getColor("white"));
        Label titulo = new Label("Regras do Jogo", tituloStyle);
        titulo.setPosition(TITULO_X, TITULO_Y);
        titulo.setAlignment(Align.center);
        stage.addActor(titulo);

        // --- Texto Explicativo ---
        Label.LabelStyle textoStyle = new Label.LabelStyle(fontTexto, skin.getColor("white"));
        Label texto = new Label(TEXTO_CONTEUDO, textoStyle);
        texto.setPosition(TEXTO_X, TEXTO_Y);
        texto.setAlignment(Align.center);
        texto.setWidth(TEXTO_WIDTH);
        texto.setWrap(true);
        stage.addActor(texto);

        // --- Botão Voltar ---
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.getDrawable("buttonUp");
        btnStyle.down = skin.getDrawable("buttonDown");
        // FIX: Adicionado estado 'over' para highlight funcionar
        btnStyle.over = skin.getDrawable("buttonOver");
        btnStyle.font = fontTexto;

        TextButton btnVoltar = new TextButton("Voltar", btnStyle);
        btnVoltar.setSize(BTN_WIDTH, BTN_HEIGHT);
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
        // Limpeza de tela (Boa prática)
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

        // Limpeza das fontes criadas manualmente
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontTexto != null) fontTexto.dispose();
    }
}
