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
import com.gruposete.war.core.CorJogador;
import com.gruposete.war.core.Jogador;
import com.gruposete.war.core.TipoJogador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelaDeSelecaoDeJogadores {

    // --- CONSTANTES: CONFIGURAÇÕES DA TELA ---
    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    // --- CONSTANTES: CAMINHOS DOS ASSETS ---
    private static final String PATH_SKIN = "ui/uiskin.json";
    private static final String PATH_BACKGROUND = "TelaDeJogoBackground.png";
    private static final String PATH_ICON_BORDER = "ui/UIPlayerIconBorder.png";
    private static final String PATH_ARROW_LEFT = "ui/UILeftArrow.png";
    private static final String PATH_ARROW_RIGHT = "ui/UIRightArrow.png";
    private static final String PATH_ICON_NONE = "ui/UINoPlayerIcon.png";
    private static final String PATH_ICON_HUMAN = "ui/UIHumanPlayerIcon.png";
    private static final String PATH_ICON_AI = "ui/UIAIPlayerIcon.png";

    // --- CONSTANTES: DIMENSÕES E LAYOUT ---
    private static final float PAD_OUTER = 20f;      // Espaçamento da borda da tela
    private static final float PAD_INNER = 15f;      // Espaçamento entre elementos
    private static final float BUTTON_NAV_WIDTH = 250f; // Largura dos botões Iniciar/Voltar

    // Tamanhos dos elementos do "Pod" de jogador
    private static final float ICON_SIZE = 128f;     // Tamanho base do ícone (Humano/Robô)
    private static final float ARROW_SIZE = 64f;     // Tamanho das setas
    private static final float BORDER_SCALE = 1.2f;  // Quanto a borda é maior que o ícone (1.2x)

    // --- CONSTANTES: ESTILOS E FONTES ---
    private static final float FONT_SCALE_NORMAL = 1.5f;
    private static final float FONT_SCALE_TITLE = 2.0f;
    private static final Color COLOR_ERROR = new Color(1, 0, 0, 1); // Vermelho

    // --- CONSTANTES: REGRAS DE NEGÓCIO (Lógica da Tela) ---
    private static final int MIN_JOGADORES = 3;
    private static final int DEFAULT_HUMANOS = 3; // Quantos começam como Humanos por padrão

    // --- VARIÁVEIS DA CLASSE ---
    public Stage stage;
    private Skin skin;
    private Texture background;

    private Runnable voltarCallback;
    private Runnable iniciarCallback;

    private Map<CorJogador, TipoJogador> estadosDosJogadores;
    private Map<CorJogador, Image> iconesDeEstado;
    private Label errorLabel;

    // Assets carregados
    private Texture texArrowLeft, texArrowRight, texNoPlayer, texHuman, texAI, texIconBorder;
    private Drawable drawArrowLeft, drawArrowRight, drawNoPlayer, drawHuman, drawAI, drawIconBorder;

    // Fontes
    private BitmapFont fontCor;
    private BitmapFont fontTitulo;
    private BitmapFont fontBotaoNav;

    public TelaDeSelecaoDeJogadores(Runnable voltarCallback, Runnable iniciarCallback) {
        this.voltarCallback = voltarCallback;
        this.iniciarCallback = iniciarCallback;

        // Usa as constantes de viewport e paths
        stage = new Stage(new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        skin = new Skin(Gdx.files.internal(PATH_SKIN));
        background = new Texture(Gdx.files.internal(PATH_BACKGROUND));

        carregarAssets();
        configurarFontes();

        // Inicializa lógica
        this.estadosDosJogadores = new HashMap<>();
        this.iconesDeEstado = new HashMap<>();
        inicializarMapaLogico();

        // Constrói a UI
        construirUI();

        // Aplica o estado visual inicial
        resetarVisuais();
    }

    private void carregarAssets() {
        // Carrega texturas usando constantes
        texArrowLeft = new Texture(Gdx.files.internal(PATH_ARROW_LEFT));
        texArrowRight = new Texture(Gdx.files.internal(PATH_ARROW_RIGHT));
        texNoPlayer = new Texture(Gdx.files.internal(PATH_ICON_NONE));
        texHuman = new Texture(Gdx.files.internal(PATH_ICON_HUMAN));
        texAI = new Texture(Gdx.files.internal(PATH_ICON_AI));
        texIconBorder = new Texture(Gdx.files.internal(PATH_ICON_BORDER));

        // Cria drawables
        drawArrowLeft = new TextureRegionDrawable(new TextureRegion(texArrowLeft));
        drawArrowRight = new TextureRegionDrawable(new TextureRegion(texArrowRight));
        drawNoPlayer = new TextureRegionDrawable(new TextureRegion(texNoPlayer));
        drawHuman = new TextureRegionDrawable(new TextureRegion(texHuman));
        drawAI = new TextureRegionDrawable(new TextureRegion(texAI));
        drawIconBorder = new TextureRegionDrawable(new TextureRegion(texIconBorder));
    }

    private void configurarFontes() {
        // Fonte para nomes das cores
        fontCor = new BitmapFont();
        fontCor.getData().setScale(FONT_SCALE_NORMAL);
        Label.LabelStyle labelStyleCor = new Label.LabelStyle(fontCor, Color.WHITE);
        skin.add("cor-label-style", labelStyleCor);

        // Fonte para Título
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(FONT_SCALE_TITLE);
        Label.LabelStyle labelStyleTitulo = new Label.LabelStyle(fontTitulo, Color.WHITE);
        skin.add("titulo-style", labelStyleTitulo);

        // Fonte para Botões
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
        mainTable.pad(PAD_OUTER).defaults().pad(PAD_INNER);

        mainTable.add(new Label("Selecione os Jogadores", skin, "titulo-style")).colspan(3).center().padBottom(30);
        mainTable.row();

        CorJogador[] cores = CorJogador.values();

        // Linha 1 (3 pods)
        mainTable.add(createPlayerPod(cores[0])).expand().fill();
        mainTable.add(createPlayerPod(cores[1])).expand().fill();
        mainTable.add(createPlayerPod(cores[2])).expand().fill();
        mainTable.row();

        // Linha 2 (3 pods)
        mainTable.add(createPlayerPod(cores[3])).expand().fill();
        mainTable.add(createPlayerPod(cores[4])).expand().fill();
        mainTable.add(createPlayerPod(cores[5])).expand().fill();

        // Label de Erro
        mainTable.row();
        errorLabel = new Label("", skin);
        errorLabel.setColor(COLOR_ERROR);
        mainTable.add(errorLabel).colspan(3).center().padTop(20);
        mainTable.row();

        // Botões de Navegação
        Table navTable = new Table();
        TextButton backButton = new TextButton("Voltar", skin, "nav-button-style");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (voltarCallback != null) voltarCallback.run();
            }
        });

        TextButton startButton = new TextButton("Iniciar Jogo", skin, "nav-button-style");
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (validarSelecao()) {
                    if (iniciarCallback != null) iniciarCallback.run();
                } else {
                    errorLabel.setText("Selecione no minimo " + MIN_JOGADORES + " jogadores (Humanos ou IA)");
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

        // 1. Label da Cor
        Label corLabel = new Label(cor.toString(), skin, "cor-label-style");
        pod.add(corLabel).colspan(3).center();
        pod.row();

        // 2. Botão Esquerda
        ImageButton leftButton = new ImageButton(drawArrowLeft);
        leftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                cycleState(cor, false);
            }
        });

        // 3. Ícone de Estado
        TipoJogador estadoInicial = estadosDosJogadores.get(cor);
        Drawable iconeInicial;

        switch (estadoInicial) {
            case HUMANO: iconeInicial = drawHuman; break;
            case IA:     iconeInicial = drawAI; break;
            case NENHUM:
            default:     iconeInicial = drawNoPlayer; break;
        }

        final Image estadoIcone = new Image(iconeInicial);
        estadoIcone.setSize(ICON_SIZE, ICON_SIZE);
        estadoIcone.setColor(cor.getGdxColor());
        iconesDeEstado.put(cor, estadoIcone);

        Image contornoIcone = new Image(drawIconBorder);

        // Calcula tamanho da borda baseado na constante de escala
        float borderSize = ICON_SIZE * BORDER_SCALE;
        contornoIcone.setSize(borderSize, borderSize);

        Stack iconStack = new Stack();
        iconStack.add(contornoIcone);
        iconStack.add(estadoIcone);

        // Centraliza ícone dentro da borda
        estadoIcone.setPosition(
            (borderSize - ICON_SIZE) / 2f,
            (borderSize - ICON_SIZE) / 2f
        );

        // 4. Botão Direita
        ImageButton rightButton = new ImageButton(drawArrowRight);
        rightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                cycleState(cor, true);
            }
        });

        // Adiciona ao pod usando as constantes de tamanho
        pod.add(leftButton).size(ARROW_SIZE, ARROW_SIZE);
        pod.add(iconStack).width(borderSize).height(borderSize);
        pod.add(rightButton).size(ARROW_SIZE, ARROW_SIZE);

        return pod;
    }

    // --- LÓGICA ---

    public List<Jogador> getJogadoresSelecionados() {
        List<Jogador> lista = new ArrayList<>();

        // Reinicia o ID para 1 para os jogadores ativos
        int idSequencial = 1;

        for (CorJogador cor : CorJogador.values()) {
            TipoJogador tipo = estadosDosJogadores.get(cor);

            if (tipo != TipoJogador.NENHUM) {
                boolean isIA = (tipo == TipoJogador.IA);

                // Cria jogador com ID sequencial (1, 2, 3...)
                lista.add(new Jogador(
                    "Jogador " + idSequencial, // Nome
                    cor,                       // Cor
                    idSequencial,              // ID
                    isIA                       // Flag IA
                ));

                idSequencial++;
            }
        }
        return lista;
    }

    private void cycleState(CorJogador cor, boolean proximo) {
        TipoJogador estadoAtual = estadosDosJogadores.get(cor);
        TipoJogador novoEstado = proximo ? estadoAtual.proximo() : estadoAtual.anterior();

        // Validação de Restrição (Máx 3 'NENHUM' / Mín 3 Jogadores)
        if (novoEstado == TipoJogador.NENHUM) {
            int nenhumCount = 0;
            for (TipoJogador tipo : estadosDosJogadores.values()) {
                if (tipo == TipoJogador.NENHUM) nenhumCount++;
            }
            // Se já tem 3 Nenhuns (ou seja, só 3 jogadores ativos), bloqueia adicionar mais um
            if (nenhumCount >= (CorJogador.values().length - MIN_JOGADORES) && estadoAtual != TipoJogador.NENHUM) {
                return;
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
        icone.setColor(cor.getGdxColor());
        errorLabel.setText("");
    }

    public void resetarEstado() {
        inicializarMapaLogico();
        resetarVisuais();
        if (errorLabel != null) errorLabel.setText("");
    }

    private void inicializarMapaLogico() {
        CorJogador[] cores = CorJogador.values();
        for (int i = 0; i < cores.length; i++) {
            CorJogador cor = cores[i];
            if (i < DEFAULT_HUMANOS) {
                estadosDosJogadores.put(cor, TipoJogador.HUMANO);
            } else {
                estadosDosJogadores.put(cor, TipoJogador.NENHUM);
            }
        }
    }

    private void resetarVisuais() {
        if (iconesDeEstado == null || iconesDeEstado.isEmpty()) return;

        for (CorJogador cor : CorJogador.values()) {
            TipoJogador estado = estadosDosJogadores.get(cor);
            Image icone = iconesDeEstado.get(cor);
            if (icone == null) continue;

            switch (estado) {
                case HUMANO: icone.setDrawable(drawHuman); break;
                case IA:     icone.setDrawable(drawAI); break;
                case NENHUM:
                default:     icone.setDrawable(drawNoPlayer); break;
            }
            icone.setColor(cor.getGdxColor());
        }
    }

    private boolean validarSelecao() {
        int contadorJogadores = 0;
        for (TipoJogador tipo : estadosDosJogadores.values()) {
            if (tipo != TipoJogador.NENHUM) {
                contadorJogadores++;
            }
        }
        return contadorJogadores >= MIN_JOGADORES;
    }

    // --- CICLO DE VIDA ---

    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
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

        texArrowLeft.dispose();
        texArrowRight.dispose();
        texNoPlayer.dispose();
        texHuman.dispose();
        texAI.dispose();
        texIconBorder.dispose();

        fontCor.dispose();
        fontTitulo.dispose();
        fontBotaoNav.dispose();
    }
}
