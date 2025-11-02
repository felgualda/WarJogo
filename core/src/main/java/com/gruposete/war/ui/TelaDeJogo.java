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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gruposete.war.core.*;
import com.gruposete.war.core.ControladorDePartida.EstadoTurno; // Importa o Enum

// imports para preenchimento de territórios
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.ShortArray;

public class TelaDeJogo {

    private ControladorDePartida controlador;
    public Stage stage;
    private BitmapFont font;
    private Skin skin;
    private Texture background;
    private ShapeRenderer shapeRenderer;
    private Runnable voltarParaMenu;
    private InputMultiplexer multiplexer;
    private InputAdapter inputAdapter;
    private EarClippingTriangulator triangulator = new EarClippingTriangulator();

    private Territorio territorioAtacante = null;
    private Territorio territorioOrigemMovimento = null;

    private Texture bannerBackground;
    private Texture texArrowRight, texHuman, texAI, texIconBorder;
    private Drawable drawArrowRight, drawHuman, drawAI, drawIconBorder;
    private ImageButton btnProximaFase;
    private Image iconeJogador;
    private Label tropasLabel;

    public TelaDeJogo(Runnable voltarParaMenu, ControladorDePartida controlador) {
        this.voltarParaMenu = voltarParaMenu;
        this.controlador = controlador;

        stage = new Stage(new FitViewport(1280, 720));

        // --- CORREÇÃO: InputAdapter agora usa o ControladorDePartida ---
        inputAdapter = new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector2 worldCoords = new Vector2(screenX, screenY);
                stage.getViewport().unproject(worldCoords);
                EstadoTurno fase = controlador.getEstadoTurno();

                Territorio t = null;
                for (Territorio territorio : controlador.getTerritorios()) {
                    if (territorio.contains(worldCoords.x, worldCoords.y)) {
                        t = territorio;
                        break;
                    }
                }

                if (t == null) {
                    if (button == Input.Buttons.RIGHT) {
                        territorioAtacante = null;
                        territorioOrigemMovimento = null;
                        System.out.println("Seleção cancelada.");
                    }
                    return false;
                }

                // --- Lógica de DISTRIBUIÇÃO (Sem mudança) ---
                if (fase == EstadoTurno.DISTRIBUINDO) {
                    if (button == Input.Buttons.LEFT) {
                        boolean alocou = controlador.alocarTropa(t);
                        if (alocou) {
                            System.out.println("Alocou 1 tropa em " + t.getNome() + ". Restam: " + controlador.getTropasADistribuir());
                        }
                    }
                }

                // --- Lógica de ATAQUE (Atualizada) ---
                else if (fase == EstadoTurno.ATACANDO) {
                    if (button == Input.Buttons.LEFT) {
                        // 1. Selecionando Atacante
                        if (territorioAtacante == null) {
                            if (controlador.getJogadores().get(t.getPlayerId()-1) == controlador.getJogadorAtual() && t.getTropas() > 1) {
                                territorioAtacante = t;
                                System.out.println("Atacante selecionado: " + t.getNome());
                            }
                        }
                        // 2. Selecionando Defensor
                        else {
                            if (controlador.getJogadores().get(t.getPlayerId()-1) != controlador.getJogadorAtual()) {
                                Territorio territorioDefensor = t;
                                if (controlador.getMapa().isAdjacente(territorioAtacante, territorioDefensor)) {
                                    System.out.println("Atacando " + territorioDefensor.getNome() + " de " + territorioAtacante.getNome());

                                    AtaqueEstado resultado = controlador.realizarAtaque(territorioAtacante, territorioDefensor);
                                    System.out.println("Resultado: " + resultado.toString());

                                    if (resultado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
                                        System.out.println("Território Conquistado! Abrindo diálogo...");
                                        mostrarDialogoMovimento(territorioAtacante, territorioDefensor, TipoMovimento.ATAQUE);
                                    }
                                    territorioAtacante = null; // Reseta seleção após ataque (bem-sucedido ou não)
                                } else {
                                    System.out.println("Ataque falhou: " + territorioDefensor.getNome() + " não é adjacente a " + territorioAtacante.getNome());
                                    territorioAtacante = null;
                                }

                            } else {
                                territorioAtacante = t;
                                System.out.println("Trocado atacante para: " + t.getNome());
                            }
                        }
                    } else if (button == Input.Buttons.RIGHT) {
                        territorioAtacante = null;
                        System.out.println("Ataque cancelado.");
                    }
                }

                // --- Lógica de MOVIMENTAÇÃO (Atualizada) ---
                else if (fase == EstadoTurno.MOVIMENTANDO) {
                    if (button == Input.Buttons.LEFT) {
                        // 1. Selecionando Origem
                        if (territorioOrigemMovimento == null) {
                            if (controlador.getJogadores().get(t.getPlayerId()-1) == controlador.getJogadorAtual() && t.getTropas() > 1) {
                                territorioOrigemMovimento = t;
                                System.out.println("Movimentação: Origem: " + t.getNome());
                            }
                        }
                        // 2. Selecionando Destino
                        else if(!(territorioOrigemMovimento.equals(t))){
                            // --- CORREÇÃO: VERIFICAÇÕES DE ADJACÊNCIA E DONO FALTANTES ---

                            // Checa se o destino também é do jogador
                            if (t.getPlayerId()  == territorioOrigemMovimento.getPlayerId()) {
                                // Checa se o destino é adjacente
                                if (controlador.getMapa().isAdjacente(territorioOrigemMovimento, t)) {
                                    System.out.println("Movimentação: Destino: " + t.getNome());
                                    mostrarDialogoMovimento(territorioOrigemMovimento, t, TipoMovimento.ESTRATEGICO);
                                    territorioOrigemMovimento = null; // Reseta seleção
                                } else {
                                    System.out.println("Movimento falhou: " + t.getNome() + " não é adjacente a " + territorioOrigemMovimento.getNome());
                                    territorioOrigemMovimento = null; // Reseta seleção
                                }
                            } else {
                                System.out.println("Movimento falhou: " + t.getNome() + " não pertence a você.");
                                territorioOrigemMovimento = null; // Reseta seleção
                            }
                        }
                    } else if (button == Input.Buttons.RIGHT) {
                        territorioOrigemMovimento = null; // Cancela seleção
                        System.out.println("Movimentação cancelada.");
                    }
                }
                return true;
            }
        };
        // --- FIM DA CORREÇÃO ---

        // Carrega skin e fundo
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        background = new Texture(Gdx.files.internal("TelaDeJogoBackground.png"));

        bannerBackground = new Texture(Gdx.files.internal("ui/banner_600x100.png"));
        texArrowRight = new Texture(Gdx.files.internal("ui/UIRightArrow.png"));
        texHuman = new Texture(Gdx.files.internal("ui/UIHumanPlayerIcon.png"));
        texAI = new Texture(Gdx.files.internal("ui/UIAIPlayerIcon.png"));
        texIconBorder = new Texture(Gdx.files.internal("ui/UIPlayerIconBorder.png"));

        drawArrowRight = new TextureRegionDrawable(new TextureRegion(texArrowRight));
        drawHuman = new TextureRegionDrawable(new TextureRegion(texHuman));
        drawAI = new TextureRegionDrawable(new TextureRegion(texAI));
        drawIconBorder = new TextureRegionDrawable(new TextureRegion(texIconBorder));;

        btnProximaFase = new ImageButton(drawArrowRight);
        btnProximaFase.setSize(64, 64);

        font = new BitmapFont();
        font.getData().setScale(1.5f); // aumenta a fonte em 50%
        shapeRenderer = new ShapeRenderer();

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputAdapter);

        buildUIStage();

        // Configuração do botão Voltar
        TextButton btnVoltar = criarBotaoVoltar();
        stage.addActor(btnVoltar);

    }
    private void mostrarDialogoMovimento(final Territorio origem, final Territorio destino, final TipoMovimento tipo) {

        // 1. Calcular o máximo de tropas que podem ser movidas
        int maxTropas;

        if (tipo == TipoMovimento.ATAQUE) {
            // Regra Pós-Ataque: Máximo de 3 (conforme sua regra),
            // mas não mais do que (total de tropas - 1).
            int maxDisponivel = origem.getTropas() - 1;
            maxTropas = Math.min(3, maxDisponivel);
        } else {
            // Regra Estratégica: Máximo de (tropas no início da fase).
            // Usamos o novo getter do controlador.
            int tropasIniciais = controlador.getTropasIniciaisMovimentacao(origem);
            maxTropas = tropasIniciais;
        }

        // Validação: Se não pode mover nenhuma tropa, nem abre o diálogo.
        if (maxTropas < 1) {
            Gdx.app.log("TelaDeJogo", "Nenhuma tropa disponível para mover.");
            // Se foi um ataque, força a mover o mínimo possível (0) se a origem ficou com 1
            if (tipo == TipoMovimento.ATAQUE && origem.getTropas() == 1) {
                // Ele atacou com 3, mas só tinha 3. Conquistou e ficou com 1. Não pode mover.
                // (Regra do War diz que tem que mover no mínimo 1... isso precisa ser validado no ataque)
                // Por enquanto, vamos assumir que o caso `maxTropas < 1` em ataque é um erro de lógica
                // que deve ser tratado.
            }
            return;
        }

        // 2. Criar a UI da Subjanela
        final Dialog dialog = new Dialog("Mover Tropas", skin);
        dialog.setModal(true); // Escurece o fundo

        String texto = (tipo == TipoMovimento.ATAQUE) ?
            "Mover para " + destino.getNome() + " (Max: 3)" :
            "Mover para " + destino.getNome() + " (Max: " + maxTropas + ")";
        dialog.text(texto);

        // Tabela interna para organizar o Slider e o Label
        Table content = dialog.getContentTable();
        content.pad(20);
        final SpinBox spinBox = new SpinBox(1, maxTropas, skin);

        content.row();
        content.add(spinBox); // Adiciona o widget SpinBox

        // 3. Botão de Confirmação
        TextButton btnConfirmar = new TextButton("Confirmar", skin);
        btnConfirmar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int tropasParaMover = spinBox.getValue();
                // Chama o método correto do controlador
                if (tipo == TipoMovimento.ATAQUE) {
                    controlador.moverTropasAposConquista(origem, destino, tropasParaMover);
                } else {
                    controlador.moverTropasEstrategicas(origem, destino, tropasParaMover);
                }
                dialog.hide(); // Fecha o diálogo
            }
        });

        dialog.button(btnConfirmar); // Adiciona o botão
        dialog.show(stage); // Exibe a subjanela
    }
    private void buildUIStage() {
        // Tabela principal (bottom-aligned)
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.bottom();

        // O Banner (Tabela interna)
        Table banner = new Table(skin);
        banner.setBackground(new TextureRegionDrawable(bannerBackground));
        banner.pad(10);
        banner.defaults().pad(0, 15, 0, 15); // Espaçamento entre colunas

        // --- 1. Ícone do Jogador (col 1) ---
        iconeJogador = new Image(drawHuman); // Default (será atualizado)
        Image iconeBorda = new Image(drawIconBorder);
        iconeBorda.setColor(Color.BLACK);

        Stack iconeStack = new Stack();
        iconeStack.add(iconeBorda);
        iconeStack.add(iconeJogador);

        // Seta o tamanho (64x64) e centraliza o ícone dentro da borda (assumindo borda 1.2x)
        iconeJogador.setSize(64, 64);
        float borderSize = 64 * 1.2f;
        iconeBorda.setSize(borderSize, borderSize);
        iconeJogador.setPosition(
            (iconeBorda.getWidth() - iconeJogador.getWidth()) / 2f,
            (iconeBorda.getHeight() - iconeJogador.getHeight()) / 2f
        );

        // --- 2. Contador de Tropas (col 2) ---
        tropasLabel = new Label("Tropas: 0", skin);
        tropasLabel.setColor(Color.BLACK);
        tropasLabel.setFontScale(2.0f);
        // --- 4. Botão Próxima Fase (col 4 - "ultima coluna") ---
        btnProximaFase = new ImageButton(drawArrowRight);
        btnProximaFase.getImage().setColor(Color.BLACK);
        btnProximaFase.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                territorioAtacante = null;
                territorioOrigemMovimento = null;
                controlador.proximaFaseTurno();
            }
        });

        // Adiciona os 4 elementos na única linha do banner
        banner.add(iconeStack).size(borderSize);    // Col 1
        banner.add(tropasLabel).expandX().left();   // Col 2
        //banner.add(btnVoltar).width(120);           // Col 3
        banner.add(btnProximaFase).size(64, 64); // Col 4 (Última)

        // Adiciona o banner (600x100) à tabela principal
        uiTable.add(banner).prefSize(600, 100);
        stage.addActor(uiTable);
    }
    private void atualizarUI() {
        Jogador jogador = controlador.getJogadorAtual();
        EstadoTurno fase = controlador.getEstadoTurno();
        if (jogador == null) return; // Segurança

        iconeJogador.setDrawable(drawHuman);
        // Tonaliza o ícone
        iconeJogador.setColor(jogador.getCor().getGdxColor()); // (Requer getGdxColor() em CorJogador)

        // 2. Atualizar Contador de Tropas
        if (fase == EstadoTurno.DISTRIBUINDO) {
            tropasLabel.setText("Tropas: " + controlador.getTropasADistribuir());
            tropasLabel.setVisible(true);
        } else {
            tropasLabel.setVisible(false); // Esconde o contador se não estiver distribuindo
        }

        // 3. Tonalizar Botão de Fase
        btnProximaFase.setColor(jogador.getCor().getGdxColor());

        // 4. Desativar botão de fase (se estiver distribuindo tropas)
        btnProximaFase.setDisabled(fase == EstadoTurno.DISTRIBUINDO && controlador.getTropasADistribuir() > 0);
    }

    public void novoJogo(){
        for(Territorio t : controlador.getTerritorios()){
            t.resetarParaNovoJogo(); // Zera tropas e playerId
        }
    }

    public InputMultiplexer getMultiplexer() {
        return multiplexer;
    }

    private TextButton criarBotaoVoltar() {
        float btnWidth = 150;
        float btnHeight = 50;
        float btnX = 1230 - btnWidth;
        float btnY = 20;

        BitmapFont buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.5f);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = skin.getDrawable("buttonUp");
        style.down = skin.getDrawable("buttonDown");
        style.font = buttonFont;

        TextButton btnVoltar = new TextButton("Voltar", style);
        btnVoltar.setSize(btnWidth, btnHeight);
        btnVoltar.setPosition(btnX, btnY);

        btnVoltar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (voltarParaMenu != null) {
                    voltarParaMenu.run();
                }
            }
        });

        return btnVoltar;
    }

    public void render(float delta) {
        // limpa a tela
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        // desenha o fundo
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight());
        stage.getBatch().end();
        // atualiza e desenha a UI (Botão Voltar)



        // desenha os territórios preenchidos
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Territorio t : controlador.getTerritorios()) {

            // --- CORREÇÃO: Pega a cor do Jogador usando o ID ---
            int playerId = t.getPlayerId();
            Jogador dono = controlador.getJogadores().get(playerId-1);
            // (Assumindo que Jogador tem .getCor() e CorJogador tem .getGdxColor())
            Color corDoJogador = dono.getCor().getGdxColor();
            // --- FIM DA CORREÇÃO ---

            shapeRenderer.setColor(corDoJogador.r, corDoJogador.g, corDoJogador.b, 0.7f);

            // Pega os vértices do polígono
            float[] vertices = t.getArea().getTransformedVertices();

            // Roda triangulador
            ShortArray indicesDosTriangulos = triangulator.computeTriangles(vertices);

            // Desenha cada triângulo
            for (int i = 0; i < indicesDosTriangulos.size; i += 3) {
                int p1 = indicesDosTriangulos.get(i) * 2;
                int p2 = indicesDosTriangulos.get(i + 1) * 2;
                int p3 = indicesDosTriangulos.get(i + 2) * 2;

                shapeRenderer.triangle(
                    vertices[p1],     // Ponto 1 - x
                    vertices[p1 + 1], // Ponto 1 - y
                    vertices[p2],     // Ponto 2 - x
                    vertices[p2 + 1], // Ponto 2 - y
                    vertices[p3],     // Ponto 3 - x
                    vertices[p3 + 1]  // Ponto 3 - y
                );
            }
        }
        shapeRenderer.end();

        // desenha os contornos dos territórios
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Territorio t : controlador.getTerritorios()) {
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.polygon(t.getArea().getTransformedVertices());
        }
        shapeRenderer.end();

        // desenha os números de tropas
        stage.getBatch().begin();
        for (Territorio t : controlador.getTerritorios()) {
            t.desenharTexto(font, stage.getBatch()); // Agora lê as tropas atualizadas
        }
        stage.getBatch().end();
        //desenhaUI
        stage.act(delta);
        stage.draw();
        atualizarUI();
    }


    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        font.dispose();
        bannerBackground.dispose();
        texArrowRight.dispose();
        texHuman.dispose();
        texAI.dispose();
        texIconBorder.dispose();
        // shapeRenderer é descartado pelo stage? Não.
        shapeRenderer.dispose();
    }
}
