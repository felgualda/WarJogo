package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gruposete.war.core.*;
import com.gruposete.war.core.ControladorDePartida.EstadoTurno;

import java.util.function.Consumer;

public class TelaDeJogo {

    // --- CONSTANTES ---
    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;
    private static final String LOG_TAG = "TelaDeJogo";

    private static final float FONT_SCALE = 1.5f;
    private static final float ICON_SIZE = 64f;
    private static final float ICON_BORDER_SCALE = 1.2f;
    private static final float BANNER_WIDTH = 600f;
    private static final float BANNER_HEIGHT = 100f;
    private static final float BTN_WIDTH_STD = 110f;
    private static final float FONT_SCALE_PHASE = 2.0f;
    private static final Color COLOR_PHASE_TEXT = Color.YELLOW;

    // --- ASSETS ---
    private static final String SKIN_PATH = "ui/uiskin.json";
    private static final String BG_PATH = "TelaDeJogoBackground.png";
    private static final String BANNER_BG_PATH = "ui/banner_600x100.png";
    private static final String ICON_ARROW_PATH = "ui/UIRightArrow.png";
    private static final String ICON_HUMAN_PATH = "ui/UIHumanPlayerIcon.png";
    private static final String ICON_AI_PATH = "ui/UIAIPlayerIcon.png";
    private static final String ICON_BORDER_PATH = "ui/UIPlayerIconBorder.png";

    // --- CORE ---
    private final ControladorDePartida controlador;
    private final Runnable voltarParaMenu;
    private final Consumer<Jogador> vitoriaCallback;
    private final EarClippingTriangulator triangulator;

    // --- ESTADO LOCAL ---
    private Territorio territorioAtacante = null;
    private Territorio territorioOrigemMovimento = null;
    private enum TipoMovimento { ATAQUE, ESTRATEGICO, DISTRIBUICAO }

    // --- CÂMERAS E VIEWPORTS (SEPARADOS) ---
    public Stage stage; // Câmera da UI (Fixa)
    
    private OrthographicCamera gameCamera; // Câmera do Jogo (Zoom/Pan)
    private Viewport gameViewport;         // Viewport do Jogo

    // --- RENDER ---
    private final InputMultiplexer multiplexer;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Skin skin;

    private final Texture background;
    private final Texture bannerBackground;
    private final Texture texArrowRight, texHuman, texAI, texIconBorder;
    private final Drawable drawArrowRight, drawHuman, drawAI, drawIconBorder;

    // --- UI ACTORS ---
    private ImageButton btnProximaFase;
    private Image iconeJogador;
    private Label tropasLabel;
    private Label phaseLabel;

    // --- CONTROLE DE GESTOS ---
    private GestureDetector gestureDetector;
    private float zoomMin = 0.5f;
    private float zoomMax = 1.0f; 
    private float mapWidth = 1280f; 
    private float mapHeight = 720f;
    private Vector2 lastPinchCenter = new Vector2();
    private boolean isPinching = false;

    public TelaDeJogo(Runnable voltarParaMenu, Consumer<Jogador> vitoriaCallback, ControladorDePartida controlador) {
        this.voltarParaMenu = voltarParaMenu;
        this.controlador = controlador;
        this.triangulator = new EarClippingTriangulator();
        this.vitoriaCallback = vitoriaCallback;

        // 1. Configuração da UI (Stage) - Câmera Fixa
        // Usa ExtendViewport para que a UI se adapte aos cantos da tela em qualquer resolução
        this.stage = new Stage(new ExtendViewport(mapWidth, mapHeight));

        // 2. Configuração do Mapa (Game Camera) - Câmera Móvel
        // Instanciamos uma câmera separada para poder dar Zoom/Pan sem afetar os botões
        this.gameCamera = new OrthographicCamera();
        this.gameViewport = new ExtendViewport(mapWidth, mapHeight, gameCamera);

        // 3. Renderizadores e Fontes
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(FONT_SCALE);

        // 4. Carregamento de Assets (Texturas e Skins)
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

        // 5. Configuração de Input (Controles)
        InputAdapter inputAdapter = criarInputAdapter();       // Lida com cliques nos territórios e atalhos de teclado
        this.gestureDetector = criarGestureDetector();         // Lida com Zoom (Pinça) e Pan (Dois dedos)

        this.multiplexer = new InputMultiplexer();
        
        // A ORDEM AQUI É CRUCIAL:
        this.multiplexer.addProcessor(stage);           // 1º Prioridade: Clicar em Botões da UI
        this.multiplexer.addProcessor(inputAdapter);    // 2º Prioridade: Clicar em Territórios
        this.multiplexer.addProcessor(gestureDetector); // 3º Prioridade: Mover a câmera (se não clicou em nada)

        // 6. Monta a Interface Visual (Botões, Faixas, Ícones)
        buildUIStage();

        // 7. CORREÇÃO CRÍTICA (Tela Cinza/Preta):
        // Força o cálculo inicial do viewport e da câmera antes do primeiro frame ser desenhado.
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // --- INPUT HANDLING ---

    private InputAdapter criarInputAdapter() {
        return new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (controlador.getJogadorAtual().getIsAI()) return false;

                // IMPORTANTE: Unproject usando o gameViewport, não o stage!
                Vector2 worldCoords = new Vector2(screenX, screenY);
                gameViewport.unproject(worldCoords);

                EstadoTurno fase = controlador.getEstadoTurno();
                Territorio territorioClicado = null;
                for (Territorio t : controlador.getTerritorios()) {
                    if (t.contains(worldCoords.x, worldCoords.y)) {
                        territorioClicado = t;
                        break;
                    }
                }

                if (territorioClicado == null) {
                    if (button == Input.Buttons.RIGHT) {
                        limparSelecoes();
                    }
                    return false; 
                }

                Jogador dono = controlador.getJogadorPorId(territorioClicado.getPlayerId());
                boolean isMeu = dono.equals(controlador.getJogadorAtual());

                switch (fase) {
                    case DISTRIBUINDO: tratarCliqueDistribuicao(territorioClicado, button, isMeu); break;
                    case ATACANDO:     tratarCliqueAtaque(territorioClicado, button, isMeu); break;
                    case MOVIMENTANDO: tratarCliqueMovimentacao(territorioClicado, button, isMeu); break;
                }
                return true;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                // Manipula a gameCamera, UI fica parada
                boolean isCtrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || 
                                        Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

                if (isCtrlPressed) {
                    float zoomSpeed = 0.1f;
                    gameCamera.zoom += amountY * zoomSpeed;
                    gameCamera.zoom = MathUtils.clamp(gameCamera.zoom, zoomMin, zoomMax);
                } else {
                    float panSpeed = 20f * gameCamera.zoom;
                    if (amountY != 0) gameCamera.translate(0, -amountY * panSpeed);
                    if (amountX != 0) gameCamera.translate(amountX * panSpeed, 0);
                }
                limitarCamera(gameCamera);
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                boolean alterou = false;
                float moveSpeed = 20f * gameCamera.zoom;

                if (keycode == Input.Keys.Z) { gameCamera.zoom -= 0.1f; alterou = true; }
                if (keycode == Input.Keys.X) { gameCamera.zoom += 0.1f; alterou = true; }
                if (keycode == Input.Keys.LEFT) { gameCamera.translate(-moveSpeed, 0); alterou = true; }
                if (keycode == Input.Keys.RIGHT) { gameCamera.translate(moveSpeed, 0); alterou = true; }
                if (keycode == Input.Keys.UP) { gameCamera.translate(0, moveSpeed); alterou = true; }
                if (keycode == Input.Keys.DOWN) { gameCamera.translate(0, -moveSpeed); alterou = true; }

                if (alterou) {
                    gameCamera.zoom = MathUtils.clamp(gameCamera.zoom, zoomMin, zoomMax);
                    limitarCamera(gameCamera);
                    return true;
                }
                return false;
            }
        };
    }

    private GestureDetector criarGestureDetector() {
        return new GestureDetector(new GestureAdapter() {
            @Override
            public boolean zoom(float initialDistance, float distance) {
                if (controlador.getJogadorAtual().getIsAI()) return false;
                float ratio = initialDistance / distance;
                float newZoom = gameCamera.zoom * ratio;
                gameCamera.zoom = MathUtils.clamp(newZoom, zoomMin, zoomMax);
                limitarCamera(gameCamera);
                return true;
            }

            @Override
            public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
                if (controlador.getJogadorAtual().getIsAI()) return false;
                float currentCenterX = (pointer1.x + pointer2.x) / 2f;
                float currentCenterY = (pointer1.y + pointer2.y) / 2f;

                if (!isPinching) {
                    lastPinchCenter.set(currentCenterX, currentCenterY);
                    isPinching = true;
                } else {
                    float deltaX = currentCenterX - lastPinchCenter.x;
                    float deltaY = currentCenterY - lastPinchCenter.y;
                    gameCamera.translate(-deltaX * gameCamera.zoom, deltaY * gameCamera.zoom);
                    lastPinchCenter.set(currentCenterX, currentCenterY);
                    limitarCamera(gameCamera);
                }
                return true;
            }

            @Override
            public void pinchStop() { isPinching = false; }
            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) { return false; }
        });
    }

    // --- UI LAYOUT REFORMULADO (TABELAS) ---

    private void buildUIStage() {
        // Tabela Principal que ocupa a tela toda
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // --- TOPO (Objetivo + Título + Voltar) ---
        Table topContainer = new Table();
        
        // Botão Objetivo
        TextButton btnObjetivo = new TextButton("Objetivo", skin);
        btnObjetivo.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Objetivo obj = controlador.getJogadorAtual().getObjetivo();
                if (obj != null) new DialogoObjetivo(obj, skin).show(stage);
            }
        });

        // Título Fase
        phaseLabel = new Label("FASE", skin);
        phaseLabel.setFontScale(FONT_SCALE_PHASE);
        phaseLabel.setColor(COLOR_PHASE_TEXT);
        phaseLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        // Botão Voltar
        TextButton btnVoltar = new TextButton("Voltar", skin);
        btnVoltar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (voltarParaMenu != null) voltarParaMenu.run();
            }
        });

        // Adiciona à tabela do topo (Esquerda - Centro - Direita)
        topContainer.add(btnObjetivo).width(120).height(50).pad(20).left();
        topContainer.add(phaseLabel).expandX().center().padTop(20);
        topContainer.add(btnVoltar).width(120).height(50).pad(20).right();

        // --- FUNDO (Banner do Jogador) ---
        Table bottomContainer = new Table();
        Table banner = new Table(skin);
        banner.setBackground(new TextureRegionDrawable(new TextureRegion(bannerBackground)));
        banner.pad(10); // Margem interna do banner

        // Configuração do Banner (Mantida a sua lógica original)
        if(!controlador.getJogadorAtual().getIsAI()){ iconeJogador = new Image(drawHuman);}
        else {iconeJogador = new Image(drawAI);}
        Image iconeBorda = new Image(drawIconBorder);
        iconeBorda.setColor(Color.BLACK);
        Stack iconeStack = new Stack();
        iconeStack.add(iconeBorda);
        iconeStack.add(iconeJogador);
        
        tropasLabel = new Label("Tropas: 0", skin);
        tropasLabel.setColor(Color.BLACK);
        
        TextButton btnCartas = new TextButton("Cartas", skin);
        btnCartas.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { new DialogoCartas(controlador, skin).show(stage); } });
        
        btnProximaFase = new ImageButton(drawArrowRight);
        btnProximaFase.getImage().setColor(Color.BLACK);
        btnProximaFase.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { limparSelecoes(); controlador.proximaFaseTurno(); } });

        float borderSize = ICON_SIZE * ICON_BORDER_SCALE;
        banner.add(iconeStack).size(borderSize).padRight(20);
        banner.add(tropasLabel).width(150).left();
        banner.add().expandX(); // Espaço vazio elástico
        banner.add(btnCartas).width(BTN_WIDTH_STD).padRight(20);
        banner.add(btnProximaFase).size(ICON_SIZE, ICON_SIZE);

        bottomContainer.add(banner).width(BANNER_WIDTH).height(BANNER_HEIGHT).bottom();

        // --- MONTAGEM FINAL DA ROOT TABLE ---
        rootTable.add(topContainer).growX().top().row(); // Topo ocupa largura total e fica em cima
        rootTable.add().expand().row();                  // Espaço vazio no meio (onde fica o mapa)
        rootTable.add(bottomContainer).growX().bottom(); // Fundo ocupa largura total e fica em baixo
    }

    // --- MÉTODOS DE JOGO (Lógica mantida, apenas chamando os diálogos) ---
    // (Mantive os métodos tratarClique... e processarAtaque... iguais, 
    //  apenas omiti aqui para poupar espaço, pois não mudam com a refatoração da câmera)
    private void tratarCliqueDistribuicao(Territorio t, int button, boolean isMeu) {
        if (button == Input.Buttons.LEFT && isMeu) mostrarDialogoMovimento(t, null, TipoMovimento.DISTRIBUICAO);
    }
    private void tratarCliqueAtaque(Territorio t, int button, boolean isMeu) {
        // Cancelamento com Botão Direito (Mantido)
        if (button == Input.Buttons.RIGHT) {
            territorioAtacante = null;
            Gdx.app.log(LOG_TAG, "Ataque cancelado (Botão Direito).");
            return;
        }

        if (button != Input.Buttons.LEFT) return;

        // 1. Selecionando Atacante
        if (territorioAtacante == null) {
            if (isMeu && t.getTropas() > 1) {
                territorioAtacante = t;
                Gdx.app.log(LOG_TAG, "Atacante selecionado: " + t.getNome());
            }
        }
        // 2. Já existe um selecionado...
        else {
            // --- NOVO: SE CLICAR NO MESMO, CANCELA ---
            if (territorioAtacante.equals(t)) {
                territorioAtacante = null;
                Gdx.app.log(LOG_TAG, "Seleção cancelada (Toggle).");
                return;
            }

            // Se clicou em OUTRO território seu, troca a seleção
            if (isMeu) {
                if (t.getTropas() > 1) {
                    territorioAtacante = t;
                    Gdx.app.log(LOG_TAG, "Trocado atacante para: " + t.getNome());
                }
            } 
            // Se clicou em inimigo, ataca
            else {
                processarAtaque(t);
            }
        }
    }
    private void processarAtaque(Territorio defensor) {
        if (controlador.getMapa().isAdjacente(territorioAtacante, defensor)) {
            
            // ATENÇÃO: O controlador agora deve retornar ResultadoCombate
            ResultadoCombate resultado = controlador.realizarAtaque(territorioAtacante, defensor);
            
            // --- MOSTRA O DIÁLOGO DOS DADOS ---
            // Só mostramos se for Humano atacando (para não spamar a tela na vez da IA)
            if (!controlador.getJogadorAtual().getIsAI()) {
                new DialogoResultadoDados(resultado, skin).show(stage);
            }

            Gdx.app.log(LOG_TAG, "Resultado: " + resultado.estado);

            if (resultado.estado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
                // Se conquistou, mostra o diálogo de mover tropas DEPOIS que fechar o dos dados
                // (Ou podemos mostrar junto, mas pode ficar confuso. O ideal é o jogador dar OK nos dados e aí abrir a movimentação)
                mostrarDialogoMovimento(territorioAtacante, defensor, TipoMovimento.ATAQUE);
            }
            
            // Se NÃO conquistou, limpa a seleção para o jogador escolher de novo
            if (resultado.estado != AtaqueEstado.TERRITORIO_CONQUISTADO) {
                 territorioAtacante = null;
            }
            
        } else {
            Gdx.app.log(LOG_TAG, "Ataque falhou: Não é adjacente.");
        }
    }
    private void tratarCliqueMovimentacao(Territorio t, int button, boolean isMeu) {
        if (button == Input.Buttons.RIGHT) { territorioOrigemMovimento = null; return; }
        if (button != Input.Buttons.LEFT) return;

        if (territorioOrigemMovimento == null) {
             // ... (Lógica de seleção original) ...
             if (isMeu && t.getTropas() > 1 && controlador.getTropasIniciaisMovimentacao(t) > 0) 
                 territorioOrigemMovimento = t;
        } 
        else {
            // --- NOVO: DESSELECIONAR SE CLICAR NO MESMO ---
            if (territorioOrigemMovimento.equals(t)) {
                territorioOrigemMovimento = null;
                return;
            }
            
            // Lógica de mover
            if (isMeu && controlador.getMapa().isAdjacente(territorioOrigemMovimento, t)) {
                mostrarDialogoMovimento(territorioOrigemMovimento, t, TipoMovimento.ESTRATEGICO);
                territorioOrigemMovimento = null;
            }
        }
    }
    private void limparSelecoes() { territorioAtacante = null; territorioOrigemMovimento = null; }
    private void logDebugAdjacencia(Territorio t) { }

    private void mostrarDialogoMovimento(final Territorio origem, final Territorio destino, final TipoMovimento tipo) {
        // (Lógica do diálogo mantida igual ao seu código original)
        int maxTropas = 0;
        String titulo = "";
        String textoDialogo = "";
        int minTropas = (tipo == TipoMovimento.ATAQUE) ? 1 : 0;

        if (tipo == TipoMovimento.ATAQUE) {
            titulo = "Mover Tropas (Conquista)";
            maxTropas = Math.min(3, origem.getTropas() - 1);
            textoDialogo = "Mover para " + destino.getNome();
        } else if (tipo == TipoMovimento.ESTRATEGICO) {
            titulo = "Mover (Estratégico)";
            maxTropas = Math.min(controlador.getTropasIniciaisMovimentacao(origem), origem.getTropas() - 1);
            textoDialogo = "Mover para " + destino.getNome();
        } else {
            titulo = "Alocar Tropas";
            maxTropas = controlador.getTropasADistribuir();
            textoDialogo = "Alocar em " + origem.getNome();
        }

        if (maxTropas < 1) return;

        final Dialog dialog = new Dialog(titulo, skin);
        dialog.text(textoDialogo);
        final SpinBox spinBox = new SpinBox(minTropas, maxTropas, skin);
        dialog.getContentTable().add(spinBox).pad(20);
        
        TextButton btnConfirmar = new TextButton("Confirmar", skin);
        btnConfirmar.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                int qtd = spinBox.getValue();
                if (tipo == TipoMovimento.ATAQUE) controlador.moverTropasAposConquista(origem, destino, qtd);
                else if (tipo == TipoMovimento.ESTRATEGICO) controlador.moverTropasEstrategicas(origem, destino, qtd);
                else controlador.alocarTropas(origem, qtd);
                dialog.hide();
            }
        });
        dialog.button(btnConfirmar);
        dialog.show(stage);
    }

    private void atualizarUI() {
        Jogador jogador = controlador.getJogadorAtual();
        if (jogador == null) return;
        boolean isAI = jogador.getIsAI();

        if (isAI) {
            btnProximaFase.setDisabled(true);
            btnProximaFase.setColor(Color.GRAY);
            tropasLabel.setText("IA Jogando");
            iconeJogador.setDrawable(drawAI);
        } else {
            btnProximaFase.setColor(Color.BLACK);
            iconeJogador.setDrawable(drawHuman);
            if (controlador.getEstadoTurno() == EstadoTurno.DISTRIBUINDO) {
                tropasLabel.setText("Tropas: " + controlador.getTropasADistribuirTotal());
                tropasLabel.setVisible(true);
                btnProximaFase.setDisabled(controlador.getTropasADistribuir() > 0);
            } else {
                tropasLabel.setVisible(false);
                btnProximaFase.setDisabled(false);
            }
        }
        iconeJogador.setColor(jogador.getCor().getGdxColor());
        
        String textoFase = "";
        switch (controlador.getEstadoTurno()) {
            case DISTRIBUINDO: textoFase = "FASE DE DISTRIBUIÇÃO"; break;
            case ATACANDO: textoFase = "FASE DE ATAQUE"; break;
            case MOVIMENTANDO: textoFase = "FASE DE MOVIMENTAÇÃO"; break;
        }
        phaseLabel.setText(textoFase);
    }

    // --- LOOP E RENDER ---

    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        // 1. Renderiza o MAPA com a GameCamera
        gameViewport.apply();
        gameCamera.update();
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        stage.getBatch().setProjectionMatrix(gameCamera.combined);

        // Fundo Infinito
        stage.getBatch().begin();
        stage.getBatch().setColor(Color.WHITE);
        float camW = gameCamera.viewportWidth * gameCamera.zoom;
        float camH = gameCamera.viewportHeight * gameCamera.zoom;
        float drawX = gameCamera.position.x - camW / 2f;
        float drawY = gameCamera.position.y - camH / 2f;
        stage.getBatch().draw(background, drawX, drawY, camW, camH);
        stage.getBatch().end();

        // Territórios
        desenharMapa();

        // Textos dos Territórios
        stage.getBatch().begin();
        for (Territorio t : controlador.getTerritorios()) {
            t.desenharTexto(font, stage.getBatch());
        }
        stage.getBatch().end();

        // 2. Renderiza a UI com a Stage Camera (Fixa)
        stage.getViewport().apply();
        atualizarUI();
        stage.act(delta);
        stage.draw();

        // Vitória
        Jogador vencedor = controlador.verificarVitoria();
        if (vencedor != null) {
            Timer.instance().clear();
            if (vitoriaCallback != null) vitoriaCallback.accept(vencedor);
        }
    }

    private void desenharMapa() {
        shapeRenderer.setProjectionMatrix(gameCamera.combined);

        boolean modoDaltonico = Gdx.app.getPreferences("WarJogoConfigs").getBoolean("daltonismo", false);
        
        // 1. DESENHO BASE (Preenchimento colorido dos países)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Territorio t : controlador.getTerritorios()) {
            int playerId = t.getPlayerId();
            if (playerId > 0 && playerId <= controlador.getJogadores().size()) {
                Jogador dono = controlador.getJogadorPorId(playerId);
                Color c = dono.getCor().getColor(modoDaltonico);
                shapeRenderer.setColor(c.r, c.g, c.b, 0.7f); // 0.7f de transparência
                
                float[] vertices = t.getArea().getTransformedVertices();
                ShortArray indices = triangulator.computeTriangles(vertices);
                for (int i = 0; i < indices.size; i += 3) {
                    int p1 = indices.get(i) * 2;
                    int p2 = indices.get(i + 1) * 2;
                    int p3 = indices.get(i + 2) * 2;
                    shapeRenderer.triangle(vertices[p1], vertices[p1 + 1], vertices[p2], vertices[p2 + 1], vertices[p3], vertices[p3 + 1]);
                }
            }
        }
        shapeRenderer.end();

        // 2. CONTORNOS PADRÃO (Cinza fino)
        Gdx.gl.glLineWidth(1); // Espessura normal
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Territorio t : controlador.getTerritorios()) {
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.polygon(t.getArea().getTransformedVertices());
        }
        shapeRenderer.end();

        // 3. --- LÓGICA DE DESTAQUE (HIGHLIGHTS) ---
        // Verifica se há algum território selecionado (seja para ataque ou movimento)
        Territorio selecionado = (territorioAtacante != null) ? territorioAtacante : territorioOrigemMovimento;

        if (selecionado != null) {
            Gdx.gl.glLineWidth(4); // Aumenta a espessura da linha para destacar
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            // A. Destaca a ORIGEM (Branco)
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.polygon(selecionado.getArea().getTransformedVertices());

            // B. Destaca os VIZINHOS VÁLIDOS
            for (Territorio t : controlador.getTerritorios()) {
                // Pula a própria origem
                if (t.equals(selecionado)) continue;

                // Só processa se for adjacente (vizinho)
                if (controlador.getMapa().isAdjacente(selecionado, t)) {
                    
                    boolean desenharDestaque = false;

                    // Regras de Cor dependendo da Fase
                    if (controlador.getEstadoTurno() == EstadoTurno.ATACANDO) {
                        // No ataque, vizinho deve ser INIMIGO
                        boolean isInimigo = (t.getPlayerId() != selecionado.getPlayerId());
                        if (isInimigo) {
                            shapeRenderer.setColor(Color.RED); // Alvo de Ataque
                            desenharDestaque = true;
                        }
                    } 
                    else if (controlador.getEstadoTurno() == EstadoTurno.MOVIMENTANDO) {
                        // Na movimentação, vizinho deve ser ALIADO (meu)
                        boolean isAliado = (t.getPlayerId() == selecionado.getPlayerId());
                        if (isAliado) {
                            shapeRenderer.setColor(Color.YELLOW); // Alvo de Movimento
                            desenharDestaque = true;
                        }
                    }

                    if (desenharDestaque) {
                        shapeRenderer.polygon(t.getArea().getTransformedVertices());
                    }
                }
            }
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1); // Reseta a espessura para não afetar outras coisas
        }
    }

    public void resize(int width, int height) {
        // UI: Centraliza (True)
        stage.getViewport().update(width, height, true);
        
        // MAPA: Não centraliza automático (False) pois controlamos manualmente
        gameViewport.update(width, height, false);
        gameCamera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        limitarCamera(gameCamera);
    }

    private void limitarCamera(OrthographicCamera camera) {
        float effectiveW = camera.viewportWidth * camera.zoom;
        float effectiveH = camera.viewportHeight * camera.zoom;

        if (effectiveW < mapWidth) {
            float minX = effectiveW / 2f;
            float maxX = mapWidth - effectiveW / 2f;
            camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
        } else {
            camera.position.x = mapWidth / 2f;
        }

        if (effectiveH < mapHeight) {
            float minY = effectiveH / 2f;
            float maxY = mapHeight - effectiveH / 2f;
            camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
        } else {
            camera.position.y = mapHeight / 2f;
        }
        camera.update();
    }

    public InputMultiplexer getMultiplexer() { return multiplexer; }
    public void novoJogo() { for(Territorio t : controlador.getTerritorios()) t.resetarParaNovoJogo(); }
    public void dispose() {
        Timer.instance().clear();
        stage.dispose(); skin.dispose(); background.dispose(); font.dispose(); shapeRenderer.dispose();
        bannerBackground.dispose(); texArrowRight.dispose(); texHuman.dispose(); texAI.dispose(); texIconBorder.dispose();
    }
}