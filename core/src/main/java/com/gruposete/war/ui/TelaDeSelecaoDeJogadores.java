package com.gruposete.war.ui; // << Verifique seu pacote

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gruposete.war.core.CorJogador; // Importe suas classes
import com.gruposete.war.core.Jogador;
import com.gruposete.war.core.TipoJogador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nova tela de seleção, seguindo o padrão de UI e Callbacks.
 */
public class TelaDeSelecaoDeJogadores {

    public Stage stage;
    private Skin skin;
    private Texture background; // Reutilizando a textura de fundo da TelaInicial

    private Runnable voltarCallback;
    private Runnable iniciarCallback; // Callback para o Main

    private Map<CorJogador, TipoJogador> estadosDosJogadores;
    private Map<CorJogador, Label> labelsDeEstado;
    private Label errorLabel;

    public TelaDeSelecaoDeJogadores(Runnable voltarCallback, Runnable iniciarCallback) {
        this.voltarCallback = voltarCallback;
        this.iniciarCallback = iniciarCallback;

        stage = new Stage(new FitViewport(1280, 720));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        background = new Texture(Gdx.files.internal("TelaInicialBackground.png"));

        // Inicializa os mapas de controle
        this.estadosDosJogadores = new HashMap<>();
        this.labelsDeEstado = new HashMap<>();
        for (CorJogador cor : CorJogador.values()) {
            estadosDosJogadores.put(cor, TipoJogador.NENHUM);
        }

        // Tabela principal para organizar a UI
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20);
        mainTable.defaults().pad(10);

        mainTable.add(new Label("Selecione os Jogadores", skin)).colspan(4).center().padBottom(30);
        mainTable.row();

        // Cria os 6 slots
        for (final CorJogador cor : CorJogador.values()) {
            Label corLabel = new Label(cor.toString(), skin);

            TextButton leftButton = new TextButton("<", skin); // Placeholder (use ImageButton aqui)
            leftButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    cycleState(cor, false);
                }
            });

            final Label estadoLabel = new Label(TipoJogador.NENHUM.toString(), skin);
            estadoLabel.setAlignment(Align.center);
            labelsDeEstado.put(cor, estadoLabel); // Salva referência para atualizar

            TextButton rightButton = new TextButton(">", skin); // Placeholder (use ImageButton aqui)
            rightButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    cycleState(cor, true);
                }
            });

            mainTable.add(corLabel).expandX().right();
            mainTable.add(leftButton).width(60);
            mainTable.add(estadoLabel).width(120);
            mainTable.add(rightButton).width(60).expandX().left();
            mainTable.row();
        }

        // Label de Erro
        errorLabel = new Label("", skin);
        errorLabel.setColor(1, 0, 0, 1); // Vermelho
        mainTable.add(errorLabel).colspan(4).center().padTop(20);
        mainTable.row();

        // Botões de Navegação
        Table navTable = new Table();
        TextButton backButton = new TextButton("Voltar", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (voltarCallback != null) voltarCallback.run();
            }
        });

        TextButton startButton = new TextButton("Iniciar Jogo", skin);
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

        navTable.add(backButton).pad(20).width(200);
        navTable.add(startButton).pad(20).width(200);
        mainTable.add(navTable).colspan(4).center().padTop(30);

        stage.addActor(mainTable);
    }

    /**
     * Método público para o Main.java pegar os jogadores selecionados.
     */
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

    // --- Métodos de Lógica Interna ---

    private void cycleState(CorJogador cor, boolean proximo) {
        TipoJogador estadoAtual = estadosDosJogadores.get(cor);
        TipoJogador novoEstado = proximo ? estadoAtual.proximo() : estadoAtual.anterior();
        estadosDosJogadores.put(cor, novoEstado);
        labelsDeEstado.get(cor).setText(novoEstado.toString());
        errorLabel.setText(""); // Limpa o erro
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

    // --- Métodos de Renderização (padrão) ---

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

    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
    }
}
