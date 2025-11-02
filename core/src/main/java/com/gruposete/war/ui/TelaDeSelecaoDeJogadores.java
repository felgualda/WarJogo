package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color; // Importado
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont; // Importado
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack; // Importado
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gruposete.war.core.CorJogador;
import com.gruposete.war.core.Jogador;
import com.gruposete.war.core.TipoJogador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelaDeSelecaoDeJogadores {

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

        stage = new Stage(new FitViewport(1280, 720));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        background = new Texture(Gdx.files.internal("TelaDeJogoBackground.png"));

        texArrowLeft = new Texture(Gdx.files.internal("ui/UILeftArrow.png"));
        texArrowRight = new Texture(Gdx.files.internal("ui/UIRightArrow.png"));

        texNoPlayer = new Texture(Gdx.files.internal("ui/UINoPlayerIcon.png"));
        texHuman = new Texture(Gdx.files.internal("ui/UIHumanPlayerIcon.png"));
        texAI = new Texture(Gdx.files.internal("ui/UIAIPlayerIcon.png"));
        texIconBorder = new Texture(Gdx.files.internal("ui/UIPlayerIconBorder.png"));

        // Criar Drawables
        drawArrowLeft = new TextureRegionDrawable(new TextureRegion(texArrowLeft));
        drawArrowRight = new TextureRegionDrawable(new TextureRegion(texArrowRight));
        drawNoPlayer = new TextureRegionDrawable(new TextureRegion(texNoPlayer));
        drawHuman = new TextureRegionDrawable(new TextureRegion(texHuman));
        drawAI = new TextureRegionDrawable(new TextureRegion(texAI));
        drawIconBorder = new TextureRegionDrawable(new TextureRegion(texIconBorder));


        // Fonte para nomes das cores (1.5x)
        fontCor = new BitmapFont();
        fontCor.getData().setScale(1.5f);
        Label.LabelStyle labelStyleCor = new Label.LabelStyle(fontCor, Color.WHITE);
        skin.add("cor-label-style", labelStyleCor);

        // Fonte para Título (2.0x)
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(2.0f);
        Label.LabelStyle labelStyleTitulo = new Label.LabelStyle(fontTitulo, Color.WHITE);
        skin.add("titulo-style", labelStyleTitulo);

        // Fonte para Botões "Voltar" e "Iniciar" (1.5x)
        fontBotaoNav = new BitmapFont();
        fontBotaoNav.getData().setScale(1.5f);
        TextButton.TextButtonStyle buttonStyleNav = new TextButton.TextButtonStyle(
            skin.getDrawable("buttonUp"), // Pega os 9-patch do skin padrão
            skin.getDrawable("buttonDown"),
            skin.getDrawable("buttonOver"),
            fontBotaoNav // Usa a fonte nova
        );
        skin.add("nav-button-style", buttonStyleNav);
        // Inicializa os mapas de controle
        this.estadosDosJogadores = new HashMap<>();
        this.iconesDeEstado = new HashMap<>();

        inicializarMapaLogico();

        CorJogador[] cores = CorJogador.values();

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20).defaults().pad(15);

        mainTable.add(new Label("Selecione os Jogadores", skin, "titulo-style")).colspan(3).center().padBottom(30);
        mainTable.row();

        // Grade 2x3
        // Linha 1
        mainTable.add(createPlayerPod(cores[0])).expand().fill();
        mainTable.add(createPlayerPod(cores[1])).expand().fill();
        mainTable.add(createPlayerPod(cores[2])).expand().fill();
        mainTable.row();

        // Linha 2
        mainTable.add(createPlayerPod(cores[3])).expand().fill();
        mainTable.add(createPlayerPod(cores[4])).expand().fill();
        mainTable.add(createPlayerPod(cores[5])).expand().fill();

        // Label de Erro
        mainTable.row();
        errorLabel = new Label("", skin);
        errorLabel.setColor(1, 0, 0, 1);
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
                    errorLabel.setText("Selecione no minimo 3 jogadores (Humanos ou IA)");
                }
            }
        });

        // Adiciona a tabela final ao stage
        stage.addActor(mainTable);
        navTable.add(backButton).pad(20).width(250); // Aumentei a largura
        navTable.add(startButton).pad(20).width(250); // Aumentei a largura
        mainTable.add(navTable).colspan(3).center().padTop(30);

        stage.addActor(mainTable);

        // Aplica o estado visual inicial
        resetarVisuais();
    }

    /**
     * NOVO MÉTODO HELPER
     * Cria uma Tabela (um "pod") para um slot de jogador.
     * Isso organiza o layout e a criação dos ícones.
     */
    private Table createPlayerPod(final CorJogador cor) {
        Table pod = new Table();
        pod.defaults().pad(10); // Espaçamento dentro do pod

        // 1. Label da Cor (com fonte maior)
        Label corLabel = new Label(cor.toString(), skin, "cor-label-style"); // Usa o novo estilo
        pod.add(corLabel).colspan(3).center(); // Colspan 3 para centralizar
        pod.row();

        // 2. Botão Esquerda
        ImageButton leftButton = new ImageButton(drawArrowLeft);
        leftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                cycleState(cor, false);
            }
        });

        // 3. Ícone de Estado (com Contorno)
        TipoJogador estadoInicial = estadosDosJogadores.get(cor);
        Drawable iconeInicial;

        switch (estadoInicial) {
            case HUMANO:
                iconeInicial = drawHuman;
                break;
            case IA:
                iconeInicial = drawAI;
                break;
            case NENHUM:
            default:
                iconeInicial = drawNoPlayer;
                break;
        }

        // 1. Criar os atores Ícone e Borda
        final Image estadoIcone = new Image(iconeInicial);

        estadoIcone.setSize(128, 128); // Define o tamanho do ícone
        estadoIcone.setColor(cor.getGdxColor());
        iconesDeEstado.put(cor, estadoIcone); // Salva referência

        Image contornoIcone = new Image(drawIconBorder);
        float borderSize = 128 * 1.2f; // Borda 1.2x maior (153.6)
        contornoIcone.setSize(borderSize, borderSize);

        // 2. Criar o Stack
        Stack iconStack = new Stack();
        iconStack.add(contornoIcone); // Adiciona a borda (maior)
        iconStack.add(estadoIcone);  // Adiciona o ícone (menor)

        // 3. Centralizar o ícone (128) dentro da borda (153.6)
        estadoIcone.setPosition(
            (contornoIcone.getWidth() - estadoIcone.getWidth()) / 2f,
            (contornoIcone.getHeight() - estadoIcone.getHeight()) / 2f
        );

        // 4. Botão Direita
        ImageButton rightButton = new ImageButton(drawArrowRight);
        rightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                cycleState(cor, true);
            }
        });

        // Adiciona os 3 controles na segunda linha do pod
        pod.add(leftButton).size(64, 64); // Escala aumentada
        pod.add(iconStack).width(borderSize).height(borderSize);
        pod.add(rightButton).size(64, 64); // Escala aumentada

        return pod;
    }
    // --- FIM DA MUDANÇA DE LAYOUT ---


    public List<Jogador> getJogadoresSelecionados() {
        List<Jogador> lista = new ArrayList<>();
        for (Map.Entry<CorJogador, TipoJogador> entry : estadosDosJogadores.entrySet()) {
            if (entry.getValue() != TipoJogador.NENHUM) {
                CorJogador cor = entry.getKey();
                boolean isIA = (entry.getValue() == TipoJogador.IA);
                lista.add(new Jogador("Jogador " + (lista.size() + 1), cor));
            }
        }
        return lista;
    }


    // --- MUDANÇA: Lógica de restrição de 3 jogadores ---
    private void cycleState(CorJogador cor, boolean proximo) {
        TipoJogador estadoAtual = estadosDosJogadores.get(cor);
        TipoJogador novoEstado = proximo ? estadoAtual.proximo() : estadoAtual.anterior();

        // --- LÓGICA DE RESTRIÇÃO ---
        // Verifica se o novo estado é NENHUM
        if (novoEstado == TipoJogador.NENHUM) {
            // Conta quantos NENHUMs existem *agora*
            int nenhumCount = 0;
            for (TipoJogador tipo : estadosDosJogadores.values()) {
                if (tipo == TipoJogador.NENHUM) {
                    nenhumCount++;
                }
            }

            // Se já existem 3 NENHUMs, E este clique é de um slot que NÃO era NENHUM
            // (ou seja, estamos tentando adicionar um 4º NENHUM), então bloqueia.
            if (nenhumCount >= 3 && estadoAtual != TipoJogador.NENHUM) {
                // errorLabel.setText("Minimo de 3 jogadores e necessario"); // Opcional: mostrar erro
                return; // Não faz nada
            }
        }
        // --- FIM DA LÓGICA DE RESTRIÇÃO ---

        // Atualiza o estado
        estadosDosJogadores.put(cor, novoEstado);

        // Atualiza o ícone (Drawable)
        Image icone = iconesDeEstado.get(cor);
        switch (novoEstado) {
            case NENHUM:
                icone.setDrawable(drawNoPlayer);
                break;
            case HUMANO:
                icone.setDrawable(drawHuman);
                break;
            case IA:
                icone.setDrawable(drawAI);
                break;
        }

        // Re-aplica a cor (Tonalização)
        icone.setColor(cor.getGdxColor());

        errorLabel.setText("");
    }
    public void resetarEstado() {
        inicializarMapaLogico();
        resetarVisuais();
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    private void inicializarMapaLogico() {
        CorJogador[] cores = CorJogador.values();
        for (int i = 0; i < cores.length; i++) {
            CorJogador cor = cores[i];
            if (i < 3) {
                estadosDosJogadores.put(cor, TipoJogador.HUMANO);
            } else {
                estadosDosJogadores.put(cor, TipoJogador.NENHUM);
            }
        }
    }

    private void resetarVisuais() {
        // Garante que os ícones foram criados antes de tentar acessá-los
        if (iconesDeEstado == null || iconesDeEstado.isEmpty()) {
            return;
        }

        for (CorJogador cor : CorJogador.values()) {
            TipoJogador estado = estadosDosJogadores.get(cor);
            Image icone = iconesDeEstado.get(cor);
            if (icone == null) continue; // Segurança

            switch (estado) {
                case HUMANO: icone.setDrawable(drawHuman); break;
                case IA: icone.setDrawable(drawAI); break;
                case NENHUM:
                default: icone.setDrawable(drawNoPlayer); break;
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
        return contadorJogadores >= 3;
    }

    public void render(float delta) {
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

    // Corrigido (sem @Override)
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();

        // --- Dispose de todas as texturas carregadas ---
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
