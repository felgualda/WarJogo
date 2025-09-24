package com.gruposete.war;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

public class Main extends ApplicationAdapter {
    private TelaInicial telaInicial;
    private TelaDeJogo telaDeJogo;

    private enum TelaAtiva { INICIAL, JOGO }
    private TelaAtiva telaAtual;

    @Override
    public void create() {
        // Cria tela de jogo com callback para voltar ao menu
        telaDeJogo = new TelaDeJogo(() -> {
            telaAtual = TelaAtiva.INICIAL;
            Gdx.input.setInputProcessor(telaInicial.stage);
        });

        // Cria tela inicial com callback para iniciar o jogo
        telaInicial = new TelaInicial(() -> {
            telaAtual = TelaAtiva.JOGO;
            Gdx.input.setInputProcessor(telaDeJogo.getMultiplexer());
        });

        telaAtual = TelaAtiva.INICIAL;
        Gdx.input.setInputProcessor(telaInicial.stage);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        switch (telaAtual) {
            case INICIAL:
                telaInicial.render(delta);
                break;
            case JOGO:
                telaDeJogo.render(delta);
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        telaInicial.resize(width, height);
        telaDeJogo.resize(width, height);
    }

    @Override
    public void dispose() {
        telaInicial.dispose();
        telaDeJogo.dispose();
    }
}
