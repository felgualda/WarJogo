package com.gruposete.war;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.gruposete.war.ui.TelaDeConfig;
import com.gruposete.war.ui.TelaDeJogo;
import com.gruposete.war.ui.TelaDeRegras;
import com.gruposete.war.ui.TelaInicial;

public class Main extends ApplicationAdapter {
    private TelaInicial telaInicial;
    private TelaDeJogo telaDeJogo;
    private TelaDeRegras telaDeRegras;
    private TelaDeConfig telaDeConfig;


    private enum TelaAtiva { INICIAL, JOGO, REGRAS, CONFIG }
    private TelaAtiva telaAtual;

    @Override
    public void create() {
        // Cria tela de jogo com callback para voltar ao menu
        telaDeJogo = new TelaDeJogo(() -> {
            telaAtual = TelaAtiva.INICIAL;
            Gdx.input.setInputProcessor(telaInicial.stage);
        });

        // Cria tela inicial com callback para iniciar o jogo
        telaInicial = new TelaInicial(
            () -> {
            telaAtual = TelaAtiva.JOGO;
            telaDeJogo.novoJogo();
            Gdx.input.setInputProcessor(telaDeJogo.getMultiplexer());
            },
            () -> {
                telaAtual = TelaAtiva.REGRAS;
                Gdx.input.setInputProcessor(telaDeRegras.stage);
            },
            () -> {
                telaAtual = TelaAtiva.CONFIG;
                Gdx.input.setInputProcessor(telaDeConfig.stage);
            }
        );

        telaDeRegras = new TelaDeRegras(() -> {
            telaAtual = TelaAtiva.INICIAL;
            Gdx.input.setInputProcessor(telaInicial.stage);
        });

        telaDeConfig = new TelaDeConfig(() -> {
            telaAtual = TelaAtiva.INICIAL;
            Gdx.input.setInputProcessor(telaInicial.stage);
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
                case REGRAS:
                    telaDeRegras.render(delta);
                    break;
            case CONFIG:
                telaDeConfig.render(delta);
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        telaInicial.resize(width, height);
        telaDeJogo.resize(width, height);
        telaDeRegras.resize(width, height);
        telaDeConfig.resize(width, height);
    }

    @Override
    public void dispose() {
        telaInicial.dispose();
        telaDeJogo.dispose();
        telaDeRegras.dispose();
        telaDeConfig.dispose();
    }
}
