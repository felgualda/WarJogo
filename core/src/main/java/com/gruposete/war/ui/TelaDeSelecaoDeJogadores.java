package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
// Importante: ExtendViewport para manter a responsividade que já fizemos
import com.badlogic.gdx.utils.viewport.ExtendViewport; 
import com.gruposete.war.core.CorJogador;
import com.gruposete.war.core.Jogador;
import com.gruposete.war.core.TipoJogador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelaDeSelecaoDeJogadores {

    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    private static final String PATH_SKIN = "ui/uiskin.json";
    private static final String PATH_BACKGROUND = "TelaDeJogoBackground.png"; // Reutiliza fundo do jogo
    private static final String PATH_ICON_BORDER = "ui/UIPlayerIconBorder.png";
    private static final String PATH_ARROW_LEFT = "ui/UILeftArrow.png";
    private static final String PATH_ARROW_RIGHT = "ui/UIRightArrow.png";
    private static final String PATH_ICON_NONE = "ui/UINoPlayerIcon.png";
    private static final String PATH_ICON_HUMAN = "ui/UIHumanPlayerIcon.png";
    private static final String PATH_ICON_AI = "ui/UIAIPlayerIcon.png";

    private static final float PAD_OUTER = 20f;
    private static final float PAD_INNER = 15f;
    private static final float BUTTON_NAV_WIDTH = 250f;
    private static final float ICON_SIZE = 128f;
    private static final float ARROW_SIZE = 64f;
    private static final float BORDER_SCALE = 1.2f;
    private static final float FONT_SCALE_NORMAL = 1.5f;
    private static final float FONT_SCALE_TITLE = 2.0f;
    private static final Color COLOR_ERROR = new Color(1, 0, 0, 1);

    private static final int MIN_JOGADORES = 3;
    private static final int DEFAULT_HUMANOS = 3;

    public Stage stage;
    private Skin skin;
    private Texture background;

    private Runnable voltarCallback;
    private Runnable iniciarCallback;

    private Map<CorJogador, TipoJogador> estadosDosJogadores;
    private Map<CorJogador, Image> iconesDeEstado;
    private Label errorLabel;

    private Texture texArrowLeft, texArrowRight, texNoPlayer, texHuman, texAI, texIconBorder;
    private Drawable drawArrowLeft, drawArrowRight, drawNoPlayer, drawHuman, drawAI, drawIconBorder;
    private BitmapFont fontCor;
    private BitmapFont fontTitulo;
    private BitmapFont fontBotaoNav;

    public TelaDeSelecaoDeJogadores(Runnable voltarCallback, Runnable iniciarCallback) {
        this.voltarCallback = voltarCallback;
        this.iniciarCallback = iniciarCallback;

        // Usa ExtendViewport para responsividade (igual às outras telas)
        stage = new Stage(new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        skin = new Skin(Gdx.files.internal(PATH_SKIN));
        background = new Texture(Gdx.files.internal(PATH_BACKGROUND));

        carregarAssets();
        configurarFontes();

        this.estadosDosJogadores = new HashMap<>();
        this.iconesDeEstado = new HashMap<>();
        inicializarMapaLogico();

        construirUI();
        resetarVisuais();
    }
    
    // ... (Métodos carregarAssets e configurarFontes iguais ao anterior) ...
    private void carregarAssets() {
        texArrowLeft = new Texture(Gdx.files.internal(PATH_ARROW_LEFT));
        texArrowRight = new Texture(Gdx.files.internal(PATH_ARROW_RIGHT));
        texNoPlayer = new Texture(Gdx.files.internal(PATH_ICON_NONE));
        texHuman = new Texture(Gdx.files.internal(PATH_ICON_HUMAN));
        texAI = new Texture(Gdx.files.internal(PATH_ICON_AI));
        texIconBorder = new Texture(Gdx.files.internal(PATH_ICON_BORDER));

        drawArrowLeft = new TextureRegionDrawable(new TextureRegion(texArrowLeft));
        drawArrowRight = new TextureRegionDrawable(new TextureRegion(texArrowRight));
        drawNoPlayer = new TextureRegionDrawable(new TextureRegion(texNoPlayer));
        drawHuman = new TextureRegionDrawable(new TextureRegion(texHuman));
        drawAI = new TextureRegionDrawable(new TextureRegion(texAI));
        drawIconBorder = new TextureRegionDrawable(new TextureRegion(texIconBorder));
    }

    private void configurarFontes() {
        fontCor = new BitmapFont();
        fontCor.getData().setScale(FONT_SCALE_NORMAL);
        Label.LabelStyle labelStyleCor = new Label.LabelStyle(fontCor, Color.WHITE);
        skin.add("cor-label-style", labelStyleCor);

        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(FONT_SCALE_TITLE);
        Label.LabelStyle labelStyleTitulo = new Label.LabelStyle(fontTitulo, Color.WHITE);
        skin.add("titulo-style", labelStyleTitulo);

        fontBotaoNav = new BitmapFont();
        fontBotaoNav.getData().setScale(FONT_SCALE_NORMAL);
        TextButton.TextButtonStyle buttonStyleNav = new TextButton.TextButtonStyle();
        buttonStyleNav.font = fontBotaoNav;
        buttonStyleNav.up = skin.getDrawable("buttonUp");
        buttonStyleNav.down = skin.getDrawable("buttonDown");
        buttonStyleNav.over = skin.getDrawable("buttonOver");
        skin.add("nav-button-style", buttonStyleNav);
    }

    private void construirUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center(); // Centraliza responsivamente
        mainTable.pad(PAD_OUTER).defaults().pad(PAD_INNER);

        mainTable.add(new Label("Selecione os Jogadores", skin, "titulo-style")).colspan(3).center().padBottom(30);
        mainTable.row();

        CorJogador[] cores = CorJogador.values();

        mainTable.add(createPlayerPod(cores[0])).expand().fill();
        mainTable.add(createPlayerPod(cores[1])).expand().fill();
        mainTable.add(createPlayerPod(cores[2])).expand().fill();
        mainTable.row();

        mainTable.add(createPlayerPod(cores[3])).expand().fill();
        mainTable.add(createPlayerPod(cores[4])).expand().fill();
        mainTable.add(createPlayerPod(cores[5])).expand().fill();

        mainTable.row();
        errorLabel = new Label("", skin);
        errorLabel.setColor(COLOR_ERROR);
        mainTable.add(errorLabel).colspan(3).center().padTop(20);
        mainTable.row();

        Table navTable = new Table();
        TextButton backButton = new TextButton("Voltar", skin, "nav-button-style");
        backButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (voltarCallback != null) voltarCallback.run();
            }
        });

        TextButton startButton = new TextButton("Iniciar Jogo", skin, "nav-button-style");
        startButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (validarSelecao()) {
                    if (iniciarCallback != null) iniciarCallback.run();
                } else {
                    errorLabel.setText("Selecione no minimo " + MIN_JOGADORES + " jogadores");
                }
            }
        });

        navTable.add(backButton).pad(20).width(BUTTON_NAV_WIDTH);
        navTable.add(startButton).pad(20).width(BUTTON_NAV_WIDTH);
        mainTable.add(navTable).colspan(3).center().padTop(30);

        stage.addActor(mainTable);
    }

    private Table createPlayerPod(final CorJogador cor) {
        Table pod = new Table();
        pod.defaults().pad(10);

        Label corLabel = new Label(cor.toString(), skin, "cor-label-style");
        pod.add(corLabel).colspan(3).center();
        pod.row();

        ImageButton leftButton = new ImageButton(drawArrowLeft);
        leftButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { cycleState(cor, false); }
        });

        // Configuração inicial do ícone
        final Image estadoIcone = new Image(drawNoPlayer);
        estadoIcone.setSize(ICON_SIZE, ICON_SIZE);
        // A cor será definida no resetarVisuais() corretamente
        iconesDeEstado.put(cor, estadoIcone);

        Image contornoIcone = new Image(drawIconBorder);
        float borderSize = ICON_SIZE * BORDER_SCALE;
        contornoIcone.setSize(borderSize, borderSize);

        Stack iconStack = new Stack();
        iconStack.add(contornoIcone);
        iconStack.add(estadoIcone);

        estadoIcone.setPosition((borderSize - ICON_SIZE) / 2f, (borderSize - ICON_SIZE) / 2f);

        ImageButton rightButton = new ImageButton(drawArrowRight);
        rightButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { cycleState(cor, true); }
        });

        pod.add(leftButton).size(ARROW_SIZE, ARROW_SIZE);
        pod.add(iconStack).width(borderSize).height(borderSize);
        pod.add(rightButton).size(ARROW_SIZE, ARROW_SIZE);

        return pod;
    }

    // --- LÓGICA ATUALIZADA PARA DALTONISMO ---

    private void cycleState(CorJogador cor, boolean proximo) {
        TipoJogador estadoAtual = estadosDosJogadores.get(cor);
        TipoJogador novoEstado = proximo ? estadoAtual.proximo() : estadoAtual.anterior();

        // Lógica de pular "Nenhum" se já estiver no mínimo (seu pedido anterior)
        if (novoEstado == TipoJogador.NENHUM) {
            int jogadoresAtivos = 0;
            for (TipoJogador tipo : estadosDosJogadores.values()) {
                if (tipo != TipoJogador.NENHUM) jogadoresAtivos++;
            }
            if (jogadoresAtivos <= MIN_JOGADORES && estadoAtual != TipoJogador.NENHUM) {
                novoEstado = proximo ? novoEstado.proximo() : novoEstado.anterior();
            }
        }

        estadosDosJogadores.put(cor, novoEstado);

        // Atualiza Visual
        Image icone = iconesDeEstado.get(cor);
        switch (novoEstado) {
            case NENHUM: icone.setDrawable(drawNoPlayer); break;
            case HUMANO: icone.setDrawable(drawHuman); break;
            case IA:     icone.setDrawable(drawAI); break;
        }
        
        // --- CORREÇÃO AQUI: Lê a preferência para pintar o ícone ---
        boolean modoDaltonico = Gdx.app.getPreferences("WarJogoConfigs").getBoolean("daltonismo", false);
        icone.setColor(cor.getColor(modoDaltonico));
        
        if (errorLabel != null) errorLabel.setText("");
    }

    private void resetarVisuais() {
        if (iconesDeEstado == null || iconesDeEstado.isEmpty()) return;

        // --- CORREÇÃO AQUI TAMBÉM ---
        boolean modoDaltonico = Gdx.app.getPreferences("WarJogoConfigs").getBoolean("daltonismo", false);

        for (CorJogador cor : CorJogador.values()) {
            TipoJogador estado = estadosDosJogadores.get(cor);
            Image icone = iconesDeEstado.get(cor);
            if (icone == null) continue;

            switch (estado) {
                case HUMANO: icone.setDrawable(drawHuman); break;
                case IA:     icone.setDrawable(drawAI); break;
                case NENHUM: default: icone.setDrawable(drawNoPlayer); break;
            }
            
            // Aplica a cor certa (Normal ou Daltônica)
            icone.setColor(cor.getColor(modoDaltonico));
        }
    }

    // ... (Resto da classe: inicializarMapaLogico, validarSelecao, getJogadoresSelecionados, render, resize, dispose - IGUAIS) ...
    // Vou incluir apenas os métodos faltantes para completar o arquivo se você copiar e colar:
    
    public List<Jogador> getJogadoresSelecionados() {
        List<Jogador> lista = new ArrayList<>();
        int id = 1;
        for (CorJogador cor : CorJogador.values()) {
            TipoJogador tipo = estadosDosJogadores.get(cor);
            if (tipo != TipoJogador.NENHUM) {
                lista.add(new Jogador("Jogador " + id, cor, id, (tipo == TipoJogador.IA)));
                id++;
            }
        }
        return lista;
    }
    
    public void resetarEstado() {
        inicializarMapaLogico();
        resetarVisuais();
        if(errorLabel != null) errorLabel.setText("");
    }

    private void inicializarMapaLogico() {
        CorJogador[] cores = CorJogador.values();
        for (int i = 0; i < cores.length; i++) {
            if (i < DEFAULT_HUMANOS) estadosDosJogadores.put(cores[i], TipoJogador.HUMANO);
            else estadosDosJogadores.put(cores[i], TipoJogador.NENHUM);
        }
    }
    
    private boolean validarSelecao() {
        int count = 0;
        for (TipoJogador t : estadosDosJogadores.values()) if (t != TipoJogador.NENHUM) count++;
        return count >= MIN_JOGADORES;
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        stage.getBatch().begin();
        
        // Fundo Responsivo (Copiado da TelaInicial)
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
        stage.dispose(); skin.dispose(); background.dispose();
        texArrowLeft.dispose(); texArrowRight.dispose(); texNoPlayer.dispose();
        texHuman.dispose(); texAI.dispose(); texIconBorder.dispose();
        fontCor.dispose(); fontTitulo.dispose(); fontBotaoNav.dispose();
    }
}