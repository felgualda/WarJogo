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
    private static final float DELAY_DISTRIBUICAO = 3.0f;
    private static final float DELAY_ATAQUE = 6.0f;
    private static final float DELAY_MOVIMENTO = 9.0f;

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
    //                                      FASE 2: DISTRIBUIÇÃO (NOVA LÓGICA)
    // ==================================================================================

    private void faseDistribuicao() {
        Gdx.app.log("IA", "Distribuindo " + controlador.getTropasADistribuir() + " tropas.");

        while (controlador.getTropasADistribuir() > 0) {

            // 1. Calcule o território com melhor score de ataque
            // (Usa o método helper existente que calcula basedo na força relativa)
            Territorio melhorAtk = getMelhorTerritorioPorScore(true);

            boolean ataqueGarantido = false;

            if (melhorAtk != null) {
                // 2. Verifique se ele tem tropas > (3 * tropas do vizinho inimigo mais fraco)
                Territorio vizinhoFraco = getVizinhoInimigoMaisFraco(melhorAtk);

                if (vizinhoFraco != null) {
                    int minhasTropas = melhorAtk.getTropas();
                    int tropasInimigo = vizinhoFraco.getTropas();

                    if (minhasTropas > (3 * tropasInimigo)) {
                        ataqueGarantido = true;
                    }
                }
            }

            if (melhorAtk != null && !ataqueGarantido) {
                // 3. Se não tiver a vantagem de 3x, aloca 1 no ataque
                controlador.alocarTropas(melhorAtk, 1);
            } else {
                // 4. Se tiver vantagem (ou não tiver onde atacar), calcula Score de Defesa Novo
                Territorio melhorDef = getMelhorTerritorioDefesaNovaFormula();

                if (melhorDef != null) {
                    controlador.alocarTropas(melhorDef, 1);
                } else {
                    // Fallback de segurança (se não tiver onde defender nem atacar)
                    if (melhorAtk != null) controlador.alocarTropas(melhorAtk, 1);
                    else break;
                }
            }
        }

        controlador.proximaFaseTurno();
    }

    /**
     * Calcula o melhor território para defesa usando a NOVA FÓRMULA:
     * Total Tropas Inimigas * ((Total Aliados Vizinhos + 1) / Total Tropas No Territorio)
     */
    private Territorio getMelhorTerritorioDefesaNovaFormula() {
        Territorio melhor = null;
        float maiorScore = -1f;

        for (Territorio t : eu.getTerritorios()) {
            Array<Territorio> inimigos = mapa.getInimigosAdj(t);
            if (inimigos.size == 0) continue; // Se não tem inimigo, score é 0, ignora

            // Soma tropas inimigas vizinhas
            int totalTropasInimigas = 0;
            for (Territorio ini : inimigos) {
                totalTropasInimigas += ini.getTropas();
            }

            // Total de territórios aliados vizinhos
            int totalAliadosVizinhos = mapa.getAlidadosAdj(t).size;

            // Minhas tropas atuais
            int minhasTropas = t.getTropas();
            if (minhasTropas == 0) minhasTropas = 1; // Evita divisão por zero (tecnicamente impossível no jogo, mas seguro)

            // A FÓRMULA
            float score = totalTropasInimigas * ((float)(totalAliadosVizinhos + 1) / minhasTropas);

            // Adiciona pequeno fator aleatório para desempatar
            score += MathUtils.random(0.01f);

            if (score > maiorScore) {
                maiorScore = score;
                melhor = t;
            }
        }
        return melhor;
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
        int movimentosRealizados = 0;

        // --- MUDANÇA: Limite de movimentos dinâmico ---
        int maxMovimentos = eu.getTerritorios().size() * 2;
        // ----------------------------------------------

        while (movimentosRealizados < maxMovimentos) {
            Territorio melhorOrigem = null;
            Territorio melhorDestino = null;
            float maiorGanho = -999f;

            for (Territorio origem : eu.getTerritorios()) {
                int creditoSnapshot = controlador.getTropasIniciaisMovimentacao(origem);
                int atual = origem.getTropas();

                // --- MUDANÇA: Só pula se crédito for 0 ou se não tiver sobra física ---
                if (atual <= 1 || creditoSnapshot <= 0) continue;

                float scoreOrigem = calcularScoreDefesa(origem);
                Array<Territorio> aliados = mapa.getAlidadosAdj(origem);

                for (Territorio destino : aliados) {
                    float scoreDestino = calcularScoreDefesa(destino);
                    float ganho = scoreDestino - scoreOrigem;

                    if (ganho > maiorGanho) {
                        maiorGanho = ganho;
                        melhorOrigem = origem;
                        melhorDestino = destino;
                    }
                }
            }

            if (melhorOrigem != null && melhorDestino != null && maiorGanho > 5.0f) {
                int credito = controlador.getTropasIniciaisMovimentacao(melhorOrigem);
                int disponivelFisico = melhorOrigem.getTropas() - 1;

                // --- MUDANÇA: O máximo é o menor entre o Crédito e a Sobra Física ---
                int maxMover = Math.min(credito, disponivelFisico);

                if (maxMover > 0) {
                    // (Lógica de balanceamento opcional: mover metade ou tudo)
                    // Aqui movemos tudo que é permitido para simplificar a correção
                    controlador.moverTropasEstrategicas(melhorOrigem, melhorDestino, maxMover);
                    movimentosRealizados++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
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
