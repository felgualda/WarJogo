package com.gruposete.war.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Inteligência Artificial para o jogo War.
 * Comportamento: Reativo-Conservador.
 * Baseado em Sistema de Pontuação (Scores) para Tomada de Decisão.
 */
public class IABot {

    private final ControladorDePartida controlador;
    private final Jogador eu; // O jogador IA
    private final Mapa mapa;

    // --- CONSTANTES DE COMPORTAMENTO (HEURÍSTICAS) ---
    private static final float FATOR_DEFESA_ALIADOS = 2.0f; // Quanto ter amigos perto ajuda na defesa
    private static final float FATOR_ATAQUE_ESMAGADOR = 4.0f; // Vantagem necessária para ataques oportunistas
    private static final float SCORE_ATAQUE_MINIMO = 2.0f; // Ratio mínimo para considerar um ataque viável

    // Tempos para simular "pensamento" (em segundos)
    private static final float DELAY_DISTRIBUICAO = 1.0f;
    private static final float DELAY_ATAQUE = 3.0f;
    private static final float DELAY_MOVIMENTO = 3.0f;

    public IABot(ControladorDePartida controlador, Jogador jogadorIA) {
        this.controlador = controlador;
        this.eu = jogadorIA;
        this.mapa = controlador.getMapa();
    }

    /**
     * Método principal chamado pelo Controlador para a IA jogar seu turno.
     * Usa Timers para não travar a thread principal e mostrar as ações sequencialmente.
     */
    public void executarTurno() {
        Gdx.app.log("IA", ">>> INICIANDO TURNO DA IA (" + eu.getCor() + ") <<<");

        // 1. Tenta trocar cartas imediatamente
        tentaTrocarCartas();

        // 2. Agenda a Fase de Distribuição
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                faseDistribuicao();
            }
        }, DELAY_DISTRIBUICAO);

        // 3. Agenda a Fase de Ataque (dá tempo da distribuição acontecer)
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                faseAtaque();
            }
        }, DELAY_ATAQUE);

        // 4. Agenda a Fase de Movimentação e Fim de Turno
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                faseMovimentacao();
                Gdx.app.log("IA", "<<< ENCERRANDO TURNO DA IA >>>");
                controlador.passarAVez();
            }
        }, DELAY_MOVIMENTO);
    }

    // ==================================================================================
    //                                      FASE 1: CARTAS
    // ==================================================================================

    private void tentaTrocarCartas() {
        List<Carta> mao = eu.getCartas();

        // Se tiver menos de 3, nem tenta
        if (mao.size() < 3) return;

        Gdx.app.log("IA", "Analisando " + mao.size() + " cartas para troca...");

        // Algoritmo simples de combinação: Tenta achar qualquer trio válido
        for (int i = 0; i < mao.size(); i++) {
            for (int j = i + 1; j < mao.size(); j++) {
                for (int k = j + 1; k < mao.size(); k++) {
                    Carta c1 = mao.get(i);
                    Carta c2 = mao.get(j);
                    Carta c3 = mao.get(k);

                    // Usa o ServicoDeCartas para validar
                    if (ServicoDeCartas.isCombinacaoValida(c1, c2, c3)) {
                        Gdx.app.log("IA", "Troca encontrada! Realizando troca.");
                        List<Carta> troca = new ArrayList<>();
                        troca.add(c1);
                        troca.add(c2);
                        troca.add(c3);

                        controlador.tentarTrocaDeCartas(troca);
                        return; // Realiza uma troca por turno (pode ser melhorado para loopar)
                    }
                }
            }
        }
        Gdx.app.log("IA", "Nenhuma combinação válida encontrada.");
    }

    // ==================================================================================
    //                                      FASE 2: DISTRIBUIÇÃO
    // ==================================================================================

    private void faseDistribuicao() {
        // Garante que o controlador sabe que estamos nesta fase
        // (Caso o Timer tenha atrasado ou algo assim, embora o controlador gerencie o estado)

        int tropasDisponiveis = controlador.getTropasADistribuir();
        Gdx.app.log("IA", "Distribuindo " + tropasDisponiveis + " tropas.");

        while (controlador.getTropasADistribuir() > 0) {
            // Recalcula scores a cada iteração para balancear a distribuição
            Territorio melhorAtaque = getMelhorTerritorioPorScore(true);
            Territorio melhorDefesa = getMelhorTerritorioPorScore(false);

            float scoreAtk = (melhorAtaque != null) ? calcularScoreAtaque(melhorAtaque) : -1;
            float scoreDef = (melhorDefesa != null) ? calcularScoreDefesa(melhorDefesa) : -1;

            // Lógica de Decisão:
            // Prioriza ataque se tiver um bom ratio (> 2.0), senão prioriza defesa
            if (melhorAtaque != null && scoreAtk > scoreDef && scoreAtk > SCORE_ATAQUE_MINIMO) {
                // Tenta colocar 1 tropa por vez para recalcular scores
                controlador.alocarTropas(melhorAtaque, 1);
            } else if (melhorDefesa != null) {
                controlador.alocarTropas(melhorDefesa, 1);
            } else {
                // Fallback: coloca no território com mais tropas (reforça o forte)
                Territorio t = getTerritorioComMaisTropas();
                if (t != null) controlador.alocarTropas(t, 1);
                else break; // Segurança
            }
        }

        // Informa ao controlador para mudar de fase
        controlador.proximaFaseTurno();
    }

    // ==================================================================================
    //                                      FASE 3: ATAQUE
    // ==================================================================================

    private void faseAtaque() {
        boolean conquistouCarta = false;

        // Lista de territórios aptos a atacar (mais de 1 exército)
        // Ordenada do mais forte para o mais fraco
        List<Territorio> meusTerritorios = new ArrayList<>();
        for (Territorio t : eu.getTerritorios()) {
            if (t.getTropas() > 1) meusTerritorios.add(t);
        }
        meusTerritorios.sort((t1, t2) -> Integer.compare(t2.getTropas(), t1.getTropas()));

        for (Territorio origem : meusTerritorios) {
            // Se já ficou fraco devido a batalhas anteriores, pula
            if (origem.getTropas() <= 1) continue;

            // Acha o vizinho mais fraco
            Territorio alvo = getVizinhoInimigoMaisFraco(origem);
            if (alvo == null) continue;

            // Decisão de Engajamento
            boolean atacar = false;

            if (!conquistouCarta) {
                // Prioridade: Conquistar carta. Ataca se tiver vantagem razoável (> +2 tropas)
                if (origem.getTropas() >= alvo.getTropas() + 2) {
                    atacar = true;
                }
            } else {
                // Oportunismo: Só ataca se a vantagem for esmagadora (4x)
                if (origem.getTropas() >= alvo.getTropas() * FATOR_ATAQUE_ESMAGADOR) {
                    atacar = true;
                }
            }

            if (atacar) {
                Gdx.app.log("IA", "ATAQUE: " + origem.getNome() + " (" + origem.getTropas() + ") -> " + alvo.getNome() + " (" + alvo.getTropas() + ")");

                // Loop de combate até conquistar ou desistir
                while (origem.getTropas() > 1 && origem.getTropas() > alvo.getTropas()) {

                    AtaqueEstado resultado = controlador.realizarAtaque(origem, alvo);

                    if (resultado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
                        Gdx.app.log("IA", "Sucesso! Conquistou " + alvo.getNome());
                        conquistouCarta = true;

                        // Regra de Movimentação Pós-Conquista (Conservadora)
                        // Move metade ou máx 3, deixando retaguarda.
                        int disponivel = origem.getTropas() - 1;
                        int mover = Math.min(disponivel / 2, 3);
                        if (mover < 1) mover = 1; // Mínimo 1

                        controlador.moverTropasAposConquista(origem, alvo, mover);

                        // Após conquistar, para de atacar com este território e avalia o próximo da lista
                        break;
                    }
                }
            }
        }

        controlador.proximaFaseTurno();
    }

    // ==================================================================================
    //                                      FASE 4: MOVIMENTAÇÃO
    // ==================================================================================

    private void faseMovimentacao() {
        // A IA tenta fazer UMA movimentação estratégica para fortificar uma fronteira.
        // Procura a melhor troca: Origem (Segura) -> Destino (Perigo)

        Territorio melhorOrigem = null;
        Territorio melhorDestino = null;
        float maiorGanhoDefensivo = -999f;

        for (Territorio origem : eu.getTerritorios()) {
            // Regra: Só pode mover o que tinha no início da fase.
            // O controlador tem um método 'getTropasIniciaisMovimentacao', mas a IA pode
            // estimar simplesmente não movendo se tiver <= 1.
            if (origem.getTropas() <= 1) continue;

            float scoreOrigem = calcularScoreDefesa(origem);
            Array<Territorio> aliados = mapa.getAlidadosAdj(origem);

            for (Territorio destino : aliados) {
                float scoreDestino = calcularScoreDefesa(destino);

                // O ganho é: Tirar tropas de onde o score é baixo (0 = seguro)
                // e colocar onde o score é alto (muitos inimigos)
                float ganho = scoreDestino - scoreOrigem;

                if (ganho > maiorGanhoDefensivo) {
                    maiorGanhoDefensivo = ganho;
                    melhorOrigem = origem;
                    melhorDestino = destino;
                }
            }
        }

        // Só move se valer a pena (diferença de perigo significativa)
        if (melhorOrigem != null && melhorDestino != null && maiorGanhoDefensivo > 5.0f) {
            // Pega o limite permitido pelo controlador (snapshot)
            int limiteSnapshot = controlador.getTropasIniciaisMovimentacao(melhorOrigem);
            int disponivelReal = melhorOrigem.getTropas();

            // Move o máximo permitido (deixando 1)
            int mover = Math.min(limiteSnapshot, disponivelReal) - 1;

            if (mover > 0) {
                Gdx.app.log("IA", "MOVIMENTO: " + mover + " tropas de " + melhorOrigem.getNome() + " (Seguro) para " + melhorDestino.getNome() + " (Perigo)");
                controlador.moverTropasEstrategicas(melhorOrigem, melhorDestino, mover);
            }
        } else {
            Gdx.app.log("IA", "Nenhuma movimentação estratégica vantajosa encontrada.");
        }

        // Não precisa chamar proximaFaseTurno(), o Timer do executarTurno() chamará passarAVez()
    }

    // ==================================================================================
    //                                      SCORES & HELPERS
    // ==================================================================================

    /**
     * Score Defesa: Inimigos Vizinhos * (Fator + Aliados Vizinhos)
     * Alto = Perigo (Precisa de reforço) / Baixo = Seguro
     */
    private float calcularScoreDefesa(Territorio t) {
        Array<Territorio> inimigos = mapa.getInimigosAdj(t);
        Array<Territorio> aliados = mapa.getAlidadosAdj(t);

        if (inimigos.size == 0) return 0; // Interior seguro

        int tropasInimigas = 0;
        for (Territorio ini : inimigos) {
            tropasInimigas += ini.getTropas();
        }

        float score = tropasInimigas * (FATOR_DEFESA_ALIADOS + aliados.size);
        // Fator aleatório pequeno para evitar loops infinitos de decisão
        return score + MathUtils.random(0.1f);
    }

    /**
     * Score Ataque: (Minhas Tropas + Reserva) / Vizinho Mais Fraco
     * Alto = Grande chance de vitória
     */
    private float calcularScoreAtaque(Territorio t) {
        Territorio alvo = getVizinhoInimigoMaisFraco(t);
        if (alvo == null) return 0;

        float minhaForca = t.getTropas() + controlador.getTropasADistribuir();
        float forcaInimiga = alvo.getTropas();

        if (forcaInimiga == 0) forcaInimiga = 0.1f; // Evita divisão por zero

        return (minhaForca / forcaInimiga) + MathUtils.random(0.1f);
    }

    private Territorio getMelhorTerritorioPorScore(boolean buscarAtaque) {
        Territorio melhor = null;
        float maxScore = -1f;

        for (Territorio t : eu.getTerritorios()) {
            float score = buscarAtaque ? calcularScoreAtaque(t) : calcularScoreDefesa(t);
            if (score > maxScore) {
                maxScore = score;
                melhor = t;
            }
        }
        return melhor;
    }

    private Territorio getVizinhoInimigoMaisFraco(Territorio origem) {
        Array<Territorio> inimigos = mapa.getInimigosAdj(origem);
        if (inimigos.size == 0) return null;

        Territorio maisFraco = null;
        int minTropas = Integer.MAX_VALUE;

        for (Territorio t : inimigos) {
            if (t.getTropas() < minTropas) {
                minTropas = t.getTropas();
                maisFraco = t;
            }
        }
        return maisFraco;
    }

    private Territorio getTerritorioComMaisTropas() {
        Territorio max = null;
        int maxTropas = -1;
        for(Territorio t : eu.getTerritorios()) {
            if(t.getTropas() > maxTropas) {
                maxTropas = t.getTropas();
                max = t;
            }
        }
        return max;
    }
}
