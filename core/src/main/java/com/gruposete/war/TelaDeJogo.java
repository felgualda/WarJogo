package com.gruposete.war;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
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

public class TelaDeJogo {

    public Stage stage;
    private Array<Territorio> territorios;
    private BitmapFont font;
    private Skin skin;
    private Texture background;
    private ShapeRenderer shapeRenderer;
    private Runnable voltarParaMenu;

    public TelaDeJogo(Runnable voltarParaMenu) {
        this.voltarParaMenu = voltarParaMenu;

        shapeRenderer = new ShapeRenderer();

        // Cria stage e define viewport
        stage = new Stage(new FitViewport(1280, 720));

        // Carrega skin e fundo
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        background = new Texture(Gdx.files.internal("TelaDeJogoBackground.png"));

        territorios = new Array<>();

        // Territórios com formas mais interessantes (não apenas quadrados)
        territorios.add(new Territorio("Alaska", new float[]{
            100, 600, 150, 620, 140, 580, 110, 570
        }));
        territorios.add(new Territorio("Alberta", new float[]{
            160, 590, 210, 610, 200, 570, 170, 560
        }));
        territorios.add(new Territorio("Mackenzie", new float[]{
            120, 550, 170, 570, 160, 530, 130, 520
        }));

        // Configuração da fonte
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);

        // Configuração do botão Voltar
        TextButton btnVoltar = criarBotaoVoltar();
        stage.addActor(btnVoltar);

        // Configuração do InputMultiplexer para detectar cliques nos territórios
        configurarInput();
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

    private void configurarInput() {
        InputMultiplexer multiplexer = new InputMultiplexer();

        // Primeiro o stage (para os botões da UI)
        multiplexer.addProcessor(stage);

        // Depois o detector de cliques nos territórios
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Converte coordenadas da tela para coordenadas do mundo do stage
                Vector2 worldCoords = stage.getViewport().unproject(new Vector2(screenX, screenY));

                // Verifica cada território
                for (Territorio t : territorios) {
                    if (t.contains(worldCoords.x, worldCoords.y)) {
                        t.incrementarTropas();
                        System.out.println(t.getNome() + " agora tem " + t.getTropas() + " tropas!");
                        return true; // Clique foi tratado
                    }
                }
                return false; // Clique não foi em nenhum território
            }
        });

        Gdx.input.setInputProcessor(multiplexer);
    }

    public void render(float delta) {
        // Limpa a tela
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        // Desenha o fundo
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight());
        stage.getBatch().end();

        // Desenha os contornos dos territórios
        desenharContornosTerritorios();

        // Desenha os números das tropas
        desenharNumerosTropas();

        // Atualiza e desenha a UI
        stage.act(delta);
        stage.draw();
    }

    private void desenharContornosTerritorios() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        for (Territorio t : territorios) {
            float[] vertices = t.getArea().getTransformedVertices();
            for (int i = 0; i < vertices.length - 2; i += 2) {
                shapeRenderer.line(vertices[i], vertices[i + 1],
                    vertices[i + 2], vertices[i + 3]);
            }
            // Fecha o polígono (último ponto com o primeiro)
            shapeRenderer.line(vertices[vertices.length - 2], vertices[vertices.length - 1],
                vertices[0], vertices[1]);
        }

        shapeRenderer.end();
    }

    private void desenharNumerosTropas() {
        stage.getBatch().begin();
        for (Territorio t : territorios) {
            Vector2 centro = t.getCentro();
            // Desenha um fundo escuro para melhor legibilidade
            font.setColor(Color.BLACK);
            font.draw(stage.getBatch(), String.valueOf(t.getTropas()), centro.x - 1, centro.y - 1);
            font.draw(stage.getBatch(), String.valueOf(t.getTropas()), centro.x - 1, centro.y + 1);
            font.draw(stage.getBatch(), String.valueOf(t.getTropas()), centro.x + 1, centro.y - 1);
            font.draw(stage.getBatch(), String.valueOf(t.getTropas()), centro.x + 1, centro.y + 1);

            // Texto principal
            font.setColor(Color.WHITE);
            font.draw(stage.getBatch(), String.valueOf(t.getTropas()), centro.x, centro.y);
        }
        stage.getBatch().end();
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
    }
}
