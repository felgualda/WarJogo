package com.gruposete.war;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
// import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Array;

// Imports para teste
// import java.util.List;
// import java.util.ArrayList;

public class TelaDeJogo {

    public Stage stage;
    private Array<Territorio> territorios;
    private BitmapFont font;
    private Skin skin;
    private Texture background;
    private ShapeRenderer shapeRenderer;
    private Runnable voltarParaMenu;
    private InputMultiplexer multiplexer;
    private InputAdapter inputAdapter;

    public TelaDeJogo(Runnable voltarParaMenu) {
        this.voltarParaMenu = voltarParaMenu;

        // Cria stage e define viewport
        stage = new Stage(new FitViewport(1280, 720));
        inputAdapter = new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector2 worldCoords = new Vector2(screenX, screenY);
                stage.getViewport().unproject(worldCoords);

                for (Territorio t : territorios) {
                    if (t.contains(worldCoords.x, worldCoords.y)) {
                        if (button == Input.Buttons.LEFT) {
                            t.incrementarTropas();
                            System.out.println("⬆ Clicou com ESQUERDO: " + t.getNome() + " | Tropas: " + t.getTropas());
                        } else if (button == Input.Buttons.RIGHT) {
                            t.decrementarTropas();
                            System.out.println("⬇ Clicou com DIREITO: " + t.getNome() + " | Tropas: " + t.getTropas());
                        }
                        return true;
                    }
                }
                return false;
            }
        };

        // Carrega skin e fundo
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        background = new Texture(Gdx.files.internal("TelaDeJogoBackground.png"));

        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        this.territorios = new Array<Territorio>(Utils.carregarTodosOsTerritorios().toArray(new Territorio[0]));
        

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage); // mantém a UI funcionando
        //Gdx.input.setInputProcessor(new InputAdapter() {
        multiplexer.addProcessor(inputAdapter);

        Gdx.input.setInputProcessor(multiplexer);

        /* 
        // Inicio do bloco de testes de SetupDePartida
        System.out.println("--- INICIANDO TESTE DO SETUP DA PARTIDA ---");

        // 1. Simula a entrada da tela de configuração (ex: 3 jogadores)
        List<String> nomesJogadores = new ArrayList<>();
        nomesJogadores.add("Jogador 1 (Alice)");
        nomesJogadores.add("Jogador 2 (Bob)");
        nomesJogadores.add("Jogador 3 (Charlie)");

        System.out.println("TESTE: Criando setup para " + nomesJogadores.size() + " jogadores.");

        // 2. Executa a SetupDeParida
        SetupDePartida setup = new SetupDePartida(nomesJogadores);

        // 3. Pega os resultados
        List<Jogador> jogadoresProntos = setup.getJogadoresPreparados();
        List<Territorio> territoriosDoJogo = setup.getTodosOsTerritorios();

        System.out.println("--- Verificando Resultados ---");

        // 4. Verifica os Jogadores (Ordem, Objetivos, Contagem de Territórios)
        int totalTerritoriosDistribuidos = 0;
        System.out.println("ORDEM DE TURNO FINAL (Embaralhada):");
        for (Jogador j : jogadoresProntos) {
            System.out.println("  -> Jogador [ID " + j.getId() + "] " + j.getNome() + " (Cor: " + j.getCor() + ")");
            
            // Verifica Objetivo
            if (j.getObjetivo() != null) {
                System.out.println("     Objetivo: " + j.getObjetivo().getDescricao());
            } else {
                System.out.println("     ERRO: Jogador ficou SEM OBJETIVO!");
            }
            
            // Verifica contagem de territórios
            System.out.println("     Territórios recebidos: " + j.getTerritorios().size());
            totalTerritoriosDistribuidos += j.getTerritorios().size();
        }

        System.out.println("\nTotal de territórios distribuídos: " + totalTerritoriosDistribuidos);
        if (totalTerritoriosDistribuidos == territoriosDoJogo.size()) {
            System.out.println("     SUCESSO: Todos os " + territoriosDoJogo.size() + " territórios foram distribuídos.");
        } else {
            System.out.println("     FALHA: Esperado " + territoriosDoJogo.size() + " territórios, mas foram " + totalTerritoriosDistribuidos);
        }

        // 5. Verifica os Territórios (se todos têm dono e 1 tropa)
        boolean falhaTerritorio = false;
        for (Territorio t : territoriosDoJogo) {
            if (t.getPlayerId() == 0 || t.getTropas() != 1) {
                System.out.println("     FALHA NO TERRITÓRIO: " + t.getNome() + " | PlayerID: " + t.getPlayerId() + " | Tropas: " + t.getTropas());
                falhaTerritorio = true;
            }
        }
        if (!falhaTerritorio) {
            System.out.println("     SUCESSO: Todos os territórios têm 1 tropa e um ID de dono válido.");
        }

        System.out.println("--- FIM DO TESTE DO SETUP ---");
        // fim do bloco de testes
        */

        // Configuração do botão Voltar
        TextButton btnVoltar = criarBotaoVoltar();
        stage.addActor(btnVoltar);

    }

    public void novoJogo(){
        for(Territorio t : territorios){
            t.resetarParaNovoJogo();
        }
    }

    public InputMultiplexer getMultiplexer() {
        return multiplexer;
    }

    private TextButton criarBotaoVoltar() {
        float btnWidth = 300;
        float btnHeight = 50;
        float btnX = 1280 / 2f - btnWidth / 2f;
        float btnY = 50;

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

        // atualiza e desenha a UI
        stage.act(delta);
        stage.draw();

// desenha os contornos dos territórios
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Territorio t : territorios) {
            shapeRenderer.setColor(t.getColor());
            shapeRenderer.polygon(t.getArea().getTransformedVertices());
        }
        shapeRenderer.end();

// desenha os números de tropas
        stage.getBatch().begin();
        for (Territorio t : territorios) {
            t.desenharTexto(font, stage.getBatch());
        }
        stage.getBatch().end();
        shapeRenderer.end();

    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        font.dispose();
    }
}
