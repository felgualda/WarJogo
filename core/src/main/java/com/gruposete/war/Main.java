package com.gruposete.war;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.gruposete.war.ui.TelaDeConfig;
import com.gruposete.war.ui.TelaDeJogo;
import com.gruposete.war.ui.TelaDeRegras;
import com.gruposete.war.ui.TelaInicial;

// Imports adicionados para a nova lógica de setup
import com.badlogic.gdx.utils.Array;
import com.gruposete.war.core.CorJogador;
import com.gruposete.war.core.Jogador;
import com.gruposete.war.core.SetupPartida;
import com.gruposete.war.core.Territorio;
import com.gruposete.war.core.Mapa;
import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    private TelaInicial telaInicial;
    private TelaDeJogo telaDeJogo; // A telaDeJogo agora começa null
    private TelaDeRegras telaDeRegras;
    private TelaDeConfig telaDeConfig;


    private enum TelaAtiva { INICIAL, JOGO, REGRAS, CONFIG }
    private TelaAtiva telaAtual;

    @Override
    public void create() {
        // A telaDeJogo não é mais criada aqui.
        // Ela será criada sob demanda (quando o jogo iniciar)
        // pois depende dos dados do setup.

        // Cria tela inicial com callback para iniciar o jogo
        telaInicial = new TelaInicial(
            () -> { // Callback "Iniciar Jogo"
                // (SIMULAÇÃO) Cria a lista de jogadores.
                // No futuro, isso virá de uma Tela De Setup.
                List<Jogador> jogadores = new ArrayList<>();
                jogadores.add(new Jogador("Jogador 1", CorJogador.VERMELHO));
                jogadores.add(new Jogador("Jogador 2", CorJogador.AZUL));
                jogadores.add(new Jogador("Jogador 3", CorJogador.VERDE));

                // Roda a lógica de Setup para preparar a partida
                SetupPartida setup = new SetupPartida(jogadores);
                List<Jogador> jogadoresProntos = setup.getJogadoresPreparados();
                Array<Territorio> territoriosProntos = setup.getTodosOsTerritorios();
                Mapa mapaAdjacenciaPronto = setup.getMapaAdjacencias();

                // Cria tela de jogo com callback para voltar ao menu
                Runnable voltarCallback = () -> {
                    telaAtual = TelaAtiva.INICIAL;
                    Gdx.input.setInputProcessor(telaInicial.stage);
                    // Descarta a tela de jogo antiga para liberar memória
                    if (telaDeJogo != null) {
                        telaDeJogo.dispose();
                        telaDeJogo = null;
                    }
                };

                // Cria a nova TelaDeJogo com os dados prontos
                telaDeJogo = new TelaDeJogo(voltarCallback, jogadoresProntos, territoriosProntos, mapaAdjacenciaPronto);

                // Muda o estado do jogo
                telaAtual = TelaAtiva.JOGO;
                Gdx.input.setInputProcessor(telaDeJogo.getMultiplexer());
            },
            () -> { // Callback "Regras" 
                telaAtual = TelaAtiva.REGRAS;
                Gdx.input.setInputProcessor(telaDeRegras.stage);
            },
            () -> { // Callback "Config" 
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
                // Adicionada checagem para evitar crash, pois telaDeJogo pode ser null
                if (telaDeJogo != null) {
                    telaDeJogo.render(delta);
                }
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
        // Adicionada checagem para evitar crash
        if (telaDeJogo != null) telaDeJogo.resize(width, height);
        telaDeRegras.resize(width, height);
        telaDeConfig.resize(width, height);
    }

    @Override
    public void dispose() {
        telaInicial.dispose();
        // Adicionada checagem para evitar crash
        if (telaDeJogo != null) telaDeJogo.dispose();
        telaDeRegras.dispose();
        telaDeConfig.dispose();
    }
}