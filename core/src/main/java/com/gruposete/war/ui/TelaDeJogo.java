package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gruposete.war.core.*;
import com.gruposete.war.core.ControladorDePartida.EstadoTurno;

public class TelaDeJogo {

    // --- CONSTANTES (Configurações Globais da Tela) ---
    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;
    private static final String LOG_TAG = "TelaDeJogo";

    // --- CONSTANTES (UI e Layout) ---
    private static final float FONT_SCALE = 1.5f;
    private static final float ICON_SIZE = 64f;
    private static final float ICON_BORDER_SCALE = 1.2f;
    private static final float BANNER_WIDTH = 600f;
    private static final float BANNER_HEIGHT = 100f;
    private static final float BTN_WIDTH_STD = 110f; // Botões padrão do banner
    private static final float BTN_OBJETIVO_W = 100f;
    private static final float BTN_OBJETIVO_H = 50f;
    private static final float BTN_OBJETIVO_X = 50f;
    private static final float BTN_OBJETIVO_Y = 20f;

    // --- CONSTANTES (Caminhos de Assets) ---
    private static final String SKIN_PATH = "ui/uiskin.json";
    private static final String BG_PATH = "TelaDeJogoBackground.png";
    private static final String BANNER_BG_PATH = "ui/banner_600x100.png";
    private static final String ICON_ARROW_PATH = "ui/UIRightArrow.png";
    private static final String ICON_HUMAN_PATH = "ui/UIHumanPlayerIcon.png";
    private static final String ICON_AI_PATH = "ui/UIAIPlayerIcon.png";
    private static final String ICON_BORDER_PATH = "ui/UIPlayerIconBorder.png";

    // --- CORE & LÓGICA ---
    private final ControladorDePartida controlador;
    private final Runnable voltarParaMenu;
    private final EarClippingTriangulator triangulator;

    // --- ESTADO LOCAL (Seleção) ---
    private Territorio territorioAtacante = null;
    private Territorio territorioOrigemMovimento = null;
    private enum TipoMovimento { ATAQUE, ESTRATEGICO, DISTRIBUICAO }

    // --- LIBGDX ENGINE ---
    public Stage stage;
    private final InputMultiplexer multiplexer;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Skin skin;

    // --- ASSETS GRÁFICOS ---
    private final Texture background;
    private final Texture bannerBackground;
    private final Texture texArrowRight, texHuman, texAI, texIconBorder;
    private final Drawable drawArrowRight, drawHuman, drawAI, drawIconBorder;

    // --- ATORES DA UI (Referências para atualização) ---
    private ImageButton btnProximaFase;
    private TextButton btnObjetivo;
    private Image iconeJogador;
    private Label tropasLabel;

    public TelaDeJogo(Runnable voltarParaMenu, ControladorDePartida controlador) {
        this.voltarParaMenu = voltarParaMenu;
        this.controlador = controlador;
        this.triangulator = new EarClippingTriangulator();

        // 1. Inicialização Gráfica
        this.stage = new Stage(new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(FONT_SCALE);

        // 2. Carregamento de Assets
        this.skin = new Skin(Gdx.files.internal(SKIN_PATH));
        this.background = new Texture(Gdx.files.internal(BG_PATH));

        this.bannerBackground = new Texture(Gdx.files.internal(BANNER_BG_PATH));
        this.texArrowRight = new Texture(Gdx.files.internal(ICON_ARROW_PATH));
        this.texHuman = new Texture(Gdx.files.internal(ICON_HUMAN_PATH));
        this.texAI = new Texture(Gdx.files.internal(ICON_AI_PATH));
        this.texIconBorder = new Texture(Gdx.files.internal(ICON_BORDER_PATH));

        this.drawArrowRight = new TextureRegionDrawable(new TextureRegion(texArrowRight));
        this.drawHuman = new TextureRegionDrawable(new TextureRegion(texHuman));
        this.drawAI = new TextureRegionDrawable(new TextureRegion(texAI));
        this.drawIconBorder = new TextureRegionDrawable(new TextureRegion(texIconBorder));

        // 3. Configuração de Input
        InputAdapter inputAdapter = criarInputAdapter();
        this.multiplexer = new InputMultiplexer();
        this.multiplexer.addProcessor(stage);
        this.multiplexer.addProcessor(inputAdapter);

        // 4. Construção da Interface
        buildUIStage();
    }

    // --- INPUT HANDLING (Lógica de Cliques Organizada) ---

    private InputAdapter criarInputAdapter() {
        return new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector2 worldCoords = new Vector2(screenX, screenY);
                stage.getViewport().unproject(worldCoords);

                EstadoTurno fase = controlador.getEstadoTurno();
                int indiceJogador = controlador.getJogadores().indexOf(controlador.getJogadorAtual());

                // Detecta território clicado
                Territorio territorioClicado = null;
                for (Territorio t : controlador.getTerritorios()) {
                    if (t.contains(worldCoords.x, worldCoords.y)) {
                        territorioClicado = t;
                        break;
                    }
                }

                // Clique fora de território (Cancelamento)
                if (territorioClicado == null) {
                    if (button == Input.Buttons.RIGHT) {
                        limparSelecoes();
                        Gdx.app.log(LOG_TAG, "Seleção cancelada.");
                    }
                    return false;
                }

                // Debug (mantido da equipe)
                logDebugAdjacencia(territorioClicado);

                // Delega a lógica baseada na fase
                switch (fase) {
                    case DISTRIBUINDO:
                        tratarCliqueDistribuicao(territorioClicado, button, indiceJogador);
                        break;
                    case ATACANDO:
                        tratarCliqueAtaque(territorioClicado, button, indiceJogador);
                        break;
                    case MOVIMENTANDO:
                        tratarCliqueMovimentacao(territorioClicado, button, indiceJogador);
                        break;
                }
                return true;
            }
        };
    }

    private void tratarCliqueDistribuicao(Territorio t, int button, int indiceJogador) {
        if (button == Input.Buttons.LEFT) {
            if (t.getPlayerId() - 1 == indiceJogador) {
                mostrarDialogoMovimento(t, null, TipoMovimento.DISTRIBUICAO);
            } else {
                Gdx.app.log(LOG_TAG, "Este território não é seu.");
            }
        }
    }

    private void tratarCliqueAtaque(Territorio t, int button, int indiceJogador) {
        if (button == Input.Buttons.RIGHT) {
            territorioAtacante = null;
            Gdx.app.log(LOG_TAG, "Ataque cancelado.");
            return;
        }

        if (button != Input.Buttons.LEFT) return;

        // 1. Selecionando Atacante
        if (territorioAtacante == null) {
            if (t.getPlayerId() - 1 == indiceJogador && t.getTropas() > 1) {
                territorioAtacante = t;
                Gdx.app.log(LOG_TAG, "Atacante selecionado: " + t.getNome());
            }
        }
        // 2. Selecionando Defensor
        else {
            if (t.getPlayerId() - 1 != indiceJogador) {
                processarAtaque(t);
            } else {
                // Troca de atacante (clicou em outro seu)
                territorioAtacante = t;
                Gdx.app.log(LOG_TAG, "Trocado atacante para: " + t.getNome());
            }
        }
    }

    private void processarAtaque(Territorio defensor) {
        if (controlador.getMapa().isAdjacente(territorioAtacante, defensor)) {
            Gdx.app.log(LOG_TAG, "Atacando " + defensor.getNome() + " de " + territorioAtacante.getNome());

            AtaqueEstado resultado = controlador.realizarAtaque(territorioAtacante, defensor);
            Gdx.app.log(LOG_TAG, "Resultado: " + resultado.toString());

            if (resultado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
                Gdx.app.log(LOG_TAG, "Território Conquistado! Abrindo diálogo...");
                mostrarDialogoMovimento(territorioAtacante, defensor, TipoMovimento.ATAQUE);
            }
            territorioAtacante = null;
        } else {
            Gdx.app.log(LOG_TAG, "Ataque falhou: Não é adjacente.");
        }
    }

    private void tratarCliqueMovimentacao(Territorio t, int button, int indiceJogador) {
        if (button == Input.Buttons.RIGHT) {
            territorioOrigemMovimento = null;
            Gdx.app.log(LOG_TAG, "Movimentação cancelada.");
            return;
        }

        if (button != Input.Buttons.LEFT) return;

        // 1. Selecionando Origem
        if (territorioOrigemMovimento == null) {
            int tropasIniciais = controlador.getTropasIniciaisMovimentacao(t);
            if (t.getPlayerId() - 1 == indiceJogador && t.getTropas() > 1 && tropasIniciais > 1) {
                territorioOrigemMovimento = t;
                Gdx.app.log(LOG_TAG, "Movimentação: Origem: " + t.getNome());
            } else {
                Gdx.app.log(LOG_TAG, "Movimentação: Não pode mover deste território.");
            }
        }
        // 2. Selecionando Destino
        else if (!territorioOrigemMovimento.equals(t)) {
            if (t.getPlayerId() - 1 == indiceJogador) {
                if (controlador.getMapa().isAdjacente(territorioOrigemMovimento, t)) {
                    Gdx.app.log(LOG_TAG, "Movimentação: Destino: " + t.getNome());
                    mostrarDialogoMovimento(territorioOrigemMovimento, t, TipoMovimento.ESTRATEGICO);
                    territorioOrigemMovimento = null;
                } else {
                    Gdx.app.log(LOG_TAG, "Movimento falhou: Não é adjacente.");
                }
            } else {
                Gdx.app.log(LOG_TAG, "Movimento falhou: Não pertence a você.");
            }
        }
    }

    private void limparSelecoes() {
        territorioAtacante = null;
        territorioOrigemMovimento = null;
    }

    private void logDebugAdjacencia(Territorio t) {
        // Método auxiliar apenas para manter o debug da equipe limpo no código
        Array<Territorio> inimigos = controlador.getMapa().getInimigosAdj(t);
        System.out.println("Inimigos de " + t.getNome() + ": " + inimigos.size);
        Array<Territorio> aliados = controlador.getMapa().getAlidadosAdj(t);
        System.out.println("Aliados de " + t.getNome() + ": " + aliados.size);
    }

    // --- UI CONSTRUCTION & LOGIC ---

    private void buildUIStage() {
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.bottom();

        // --- Banner Principal ---
        Table banner = new Table(skin);
        banner.setBackground(new TextureRegionDrawable(bannerBackground));
        banner.pad(10);
        banner.defaults().pad(0, 15, 0, 15); // Padding horizontal entre colunas

        // 1. Ícone Jogador
        iconeJogador = new Image(drawHuman);
        Image iconeBorda = new Image(drawIconBorder);
        iconeBorda.setColor(Color.BLACK);

        Stack iconeStack = new Stack();
        iconeStack.add(iconeBorda);
        iconeStack.add(iconeJogador);

        iconeJogador.setSize(ICON_SIZE, ICON_SIZE);
        float borderSize = ICON_SIZE * ICON_BORDER_SCALE;
        iconeBorda.setSize(borderSize, borderSize);
        // Centraliza
        iconeJogador.setPosition(
            (borderSize - ICON_SIZE) / 2f,
            (borderSize - ICON_SIZE) / 2f
        );

        // 2. Label Tropas
        tropasLabel = new Label("Tropas: 0", skin);
        tropasLabel.setColor(Color.BLACK);

        // 3. Botão Voltar
        TextButton btnVoltar = new TextButton("Voltar", skin);

        // 4. Botão Cartas
        TextButton btnCartas = new TextButton("Cartas", skin);
        btnCartas.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                new DialogoCartas(controlador, skin).show(stage);
            }
        });

        // 5. Botão Próxima Fase (Seta)
        btnProximaFase = new ImageButton(drawArrowRight);
        btnProximaFase.getImage().setColor(Color.BLACK);
        btnProximaFase.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                limparSelecoes();
                controlador.proximaFaseTurno();
            }
        });

        // Adiciona ao Banner
        banner.add(iconeStack).size(borderSize);
        banner.add(tropasLabel).width(150).left(); // Largura fixa para não empurrar
        banner.add().expandX(); // Spacer que empurra o resto para a direita
        banner.add(btnCartas).width(BTN_WIDTH_STD);
        banner.add(btnProximaFase).size(ICON_SIZE, ICON_SIZE);

        uiTable.add(banner).prefSize(BANNER_WIDTH, BANNER_HEIGHT);
        stage.addActor(uiTable);

        // --- Botão Flutuante: Objetivo ---
        btnObjetivo = new TextButton("Objetivo", skin);
        btnObjetivo.setSize(BTN_OBJETIVO_W, BTN_OBJETIVO_H);
        btnObjetivo.setPosition(BTN_OBJETIVO_X, BTN_OBJETIVO_Y);

        btnObjetivo.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Objetivo obj = controlador.getJogadorAtual().getObjetivo();
                if (obj != null) {
                    new DialogoObjetivo(obj, skin).show(stage);
                } else {
                    Gdx.app.log(LOG_TAG, "Jogador sem objetivo!");
                }
            }
        });
        stage.addActor(btnObjetivo);
        // Botão Voltar agora fora do Banner
        btnVoltar.setSize(BTN_OBJETIVO_W, BTN_OBJETIVO_H);
        // Posição: Largura da tela - Poição objetivo - Largura do Botão
        float voltarX = VIEWPORT_WIDTH - (BTN_OBJETIVO_X + BTN_OBJETIVO_W);
        btnVoltar.setPosition(voltarX, BTN_OBJETIVO_Y); //(mesma altura do objetivo)

        btnVoltar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (voltarParaMenu != null) voltarParaMenu.run();
            }
        });
        stage.addActor(btnVoltar);
    }

    private void mostrarDialogoMovimento(final Territorio origem, final Territorio destino, final TipoMovimento tipo) {
        int maxTropas = 0;
        String titulo = "";
        String textoDialogo = "";
        final int minTropas = 1;

        if (tipo == TipoMovimento.ATAQUE) {
            titulo = "Mover Tropas (Conquista)";
            int maxDisponivel = origem.getTropas() - 1;
            maxTropas = Math.min(3, maxDisponivel);
            textoDialogo = "Mover para " + destino.getNome() + " (Max: " + maxTropas + ")";
        }
        else if (tipo == TipoMovimento.ESTRATEGICO) {
            titulo = "Mover Tropas (Estratégico)";
            int tropasIniciais = controlador.getTropasIniciaisMovimentacao(origem);
            maxTropas = tropasIniciais - 1;
            textoDialogo = "Mover para " + destino.getNome() + " (Max: " + maxTropas + ")";
        }
        else { // DISTRIBUICAO
            titulo = "Alocar Tropas";
            maxTropas = controlador.getTropasADistribuir();
            textoDialogo = "Alocar em " + origem.getNome() + " (Max: " + maxTropas + ")";
        }

        if (maxTropas < 1) {
            Gdx.app.log(LOG_TAG, "Nenhuma tropa disponível para esta ação.");
            return;
        }

        final Dialog dialog = new Dialog(titulo, skin);
        dialog.setModal(true);
        dialog.text(textoDialogo);

        Table content = dialog.getContentTable();
        content.pad(20);

        final SpinBox spinBox = new SpinBox(minTropas, maxTropas, skin);
        content.row();
        content.add(spinBox);

        TextButton btnConfirmar = new TextButton("Confirmar", skin);
        btnConfirmar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int quantidade = spinBox.getValue();
                if (tipo == TipoMovimento.ATAQUE) {
                    controlador.moverTropasAposConquista(origem, destino, quantidade);
                } else if (tipo == TipoMovimento.ESTRATEGICO) {
                    controlador.moverTropasEstrategicas(origem, destino, quantidade);
                } else {
                    controlador.alocarTropas(origem, quantidade);
                }
                dialog.hide();
            }
        });

        dialog.button(btnConfirmar);
        dialog.show(stage);
    }

    private void atualizarUI() {
        Jogador jogador = controlador.getJogadorAtual();
        EstadoTurno fase = controlador.getEstadoTurno();
        if (jogador == null) return;

        // Ícone
        if (jogador.getIsAI()) {
            iconeJogador.setDrawable(drawAI);
        } else {
            iconeJogador.setDrawable(drawHuman);
        }
        Color corJogador = jogador.getCor().getGdxColor();
        iconeJogador.setColor(corJogador);

        // Label
        if (fase == EstadoTurno.DISTRIBUINDO) {
            tropasLabel.setText("Tropas: " + controlador.getTropasADistribuir());
            tropasLabel.setVisible(true);
        } else {
            tropasLabel.setVisible(false);
        }

        // Botão Próxima Fase
        btnProximaFase.setDisabled(fase == EstadoTurno.DISTRIBUINDO && controlador.getTropasADistribuir() > 0);
    }

    // --- GAME LOOP ---

    public void render(float delta) {
        // 1. Limpeza da Tela (Essencial)
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        // 2. Atualiza a câmera do stage (Garante que as matrizes estão certas)
        stage.getViewport().apply();

        // --- DESENHO DO BACKGROUND ---
        // Define explicitamente a matriz de projeção para o batch manual
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        stage.getBatch().end();

        // --- DESENHO DO MAPA (SHAPE RENDERER) ---
        // Shapes devem ser desenhados DEPOIS do fundo, mas ANTES da UI
        desenharMapa();

        // --- DESENHO DOS TEXTOS DE TROPAS ---
        stage.getBatch().begin();
        for (Territorio t : controlador.getTerritorios()) {
            t.desenharTexto(font, stage.getBatch());
        }
        stage.getBatch().end();

        // --- DESENHO DA UI (STAGE) ---
        // Atualiza lógica da UI
        atualizarUI();

        // Desenha a UI por cima de tudo
        stage.act(delta);
        stage.draw();

        // Checagem de Vitória
        Jogador vencedor = controlador.verificarVitoria();
        if (vencedor != null) {
            // TODO: Transição para TelaVitoria
        }
    }

    private void desenharMapa() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        // Preenchimento
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Territorio t : controlador.getTerritorios()) {
            int playerId = t.getPlayerId();
            // (Segurança: playerId começa em 1, lista em 0)
            if (playerId > 0 && playerId <= controlador.getJogadores().size()) {
                Jogador dono = controlador.getJogadores().get(playerId - 1);
                Color corDoJogador = dono.getCor().getGdxColor();
                shapeRenderer.setColor(corDoJogador.r, corDoJogador.g, corDoJogador.b, 0.7f);

                float[] vertices = t.getArea().getTransformedVertices();
                ShortArray indices = triangulator.computeTriangles(vertices);

                for (int i = 0; i < indices.size; i += 3) {
                    int p1 = indices.get(i) * 2;
                    int p2 = indices.get(i + 1) * 2;
                    int p3 = indices.get(i + 2) * 2;
                    shapeRenderer.triangle(
                        vertices[p1], vertices[p1 + 1],
                        vertices[p2], vertices[p2 + 1],
                        vertices[p3], vertices[p3 + 1]
                    );
                }
            }
        }
        shapeRenderer.end();

        // Contornos
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Territorio t : controlador.getTerritorios()) {
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.polygon(t.getArea().getTransformedVertices());
        }
        shapeRenderer.end();
    }

    public void novoJogo(){
        for(Territorio t : controlador.getTerritorios()){
            t.resetarParaNovoJogo();
        }
    }

    public InputMultiplexer getMultiplexer() {
        return multiplexer;
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        font.dispose();
        shapeRenderer.dispose();
        bannerBackground.dispose();
        texArrowRight.dispose();
        texHuman.dispose();
        texAI.dispose();
        texIconBorder.dispose();
    }
}
