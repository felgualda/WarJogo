package com.gruposete.war.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.gruposete.war.core.Mapa;
import com.gruposete.war.core.Territorio;
import com.gruposete.war.core.VerificadorObjetivos;
import com.gruposete.war.utils.Utils;

// imports para jogo configurado
import com.gruposete.war.core.Jogador;
import java.util.List;

// imports para preenchimento de territórios
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.ShortArray;

public class TelaDeJogo {

    public Stage stage;
    private Array<Territorio> territorios;
    private Mapa mapa;
    private List<Jogador> jogadores;
    private VerificadorObjetivos verificadorObjetivos;
    private BitmapFont font;
    private Skin skin;
    private Texture background;
    private ShapeRenderer shapeRenderer;
    private Runnable voltarParaMenu;
    private InputMultiplexer multiplexer;
    private InputAdapter inputAdapter;
    private com.badlogic.gdx.math.EarClippingTriangulator triangulator = new com.badlogic.gdx.math.EarClippingTriangulator(); // "encontra triangulos"

    public TelaDeJogo(Runnable voltarParaMenu, List<Jogador> jogadores, Array<Territorio> territoriosProntos, Mapa mapaAdjacenciaPronto) {
        this.voltarParaMenu = voltarParaMenu;
        this.jogadores = jogadores; // Agora 'jogadores' vem do parâmetro
        this.territorios = territoriosProntos; // Agora 'territoriosProntos' vem do parâmetro
        this.mapa = mapaAdjacenciaPronto;
        this.verificadorObjetivos = new VerificadorObjetivos(jogadores, territoriosProntos);    // Classe usada para gerenciar a lógica de Verificar acompletude de um Objetivo

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

                         // Exemplo de uso do mapa
                        Array<Territorio> adj = mapa.getTerritoriosAdj(t);
                        System.out.println("Adjacentes de " + t.getNome() + ":");
                        for (Territorio a : adj) {
                            System.out.println(" - " + a.getNome());
                        }

                        Array<Territorio> inimigos = mapa.getInimigosAdj(t);
                        System.out.println("Inimigos de " + t.getNome() + ":");
                        for (Territorio a : inimigos) {
                            System.out.println(" - " + a.getNome());
                        }

                        Array<Territorio> aliados = mapa.getAlidadosAdj(t);
                        System.out.println("Aliados de " + t.getNome() + ":");
                        for (Territorio a : aliados) {
                            System.out.println(" - " + a.getNome());
                        }

                        verificarVitoria();     // Teste de vitória, APENAS PARA TESTES RETIRAR POSTERIORMENTE

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
        font.getData().setScale(1.5f); // aumenta a fonte em 50%
        shapeRenderer = new ShapeRenderer();
        
        // Não é carregado mais os territórios do Utils aqui. Eles vêm prontos.

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage); // mantém a UI funcionando
        multiplexer.addProcessor(inputAdapter);

        Gdx.input.setInputProcessor(multiplexer);

        // Configuração do botão Voltar
        TextButton btnVoltar = criarBotaoVoltar();
        stage.addActor(btnVoltar);
    }

    public void novoJogo(){
        for(Territorio t : territorios){
            t.resetarParaNovoJogo(); // Zera tropas e playerId
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

    // Método usado para verificar dentre todos os Objetivos se algum foi concluído
    public void verificarVitoria() {
        Jogador vencedor = verificadorObjetivos.verificarTodosObjetivos();
        if (vencedor != null) {
            System.out.println("JOGADOR VENCEU: " + vencedor.getNome());
            // Aqui você pode mostrar uma tela de vitória, etc.
        }
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

        // desenha os territórios preenchidos
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Territorio t : territorios) {
            com.badlogic.gdx.graphics.Color corDoJogador = t.getColor();
            shapeRenderer.setColor(corDoJogador.r, corDoJogador.g, corDoJogador.b, 0.7f);
            
            // Pega os vértices do polígono (ex: [x1, y1, x2, y2, ...])
            float[] vertices = t.getArea().getTransformedVertices();
            
            // Roda triangulador para descobrir os triângulos
            ShortArray indicesDosTriangulos = triangulator.computeTriangles(vertices);
            
            // Desenha cada triângulo que o triangulador encontrou
            for (int i = 0; i < indicesDosTriangulos.size; i += 3) {
                // Pega os índices dos 3 pontos do triângulo
                // (multiplica por 2 pois os vértices são x,y)
                int p1 = indicesDosTriangulos.get(i) * 2;
                int p2 = indicesDosTriangulos.get(i + 1) * 2;
                int p3 = indicesDosTriangulos.get(i + 2) * 2;

                // Desenha o triângulo preenchido
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
        for (Territorio t : territorios) {
            shapeRenderer.setColor(Color.GRAY); 
            shapeRenderer.polygon(t.getArea().getTransformedVertices());
        }
        shapeRenderer.end();

        // desenha os números de tropas
        stage.getBatch().begin();
        for (Territorio t : territorios) {
            t.desenharTexto(font, stage.getBatch()); // Ler as tropas (1, como definidas pelo setup)
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
    }
}