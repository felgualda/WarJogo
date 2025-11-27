package com.gruposete.war.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import java.util.*;

public class IABot {

    private static class DadosTerritorio {
        int distanciaAoFront = 9999;
        float perigoBase = 0f;
        Territorio territorio;
        public DadosTerritorio(Territorio t) { this.territorio = t; }
    }

    private final ControladorDePartida controlador;
    private final Jogador eu;
    private final Mapa mapa;

    // Constantes de comportamemnto da IA
    private static final float FATOR_DEFESA_ALIADOS = 2.0f;
    private static final float FATOR_ATAQUE_ESMAGADOR = 4.0f;

    // Delays
    private static final float DELAY_DISTRIBUICAO = 0.03f;
    private static final float DELAY_ATAQUE = 0.06f;
    private static final float DELAY_MOVIMENTO = 0.1f;

    public IABot(ControladorDePartida controlador, Jogador jogadorIA) {
        this.controlador = controlador;
        this.eu = jogadorIA;
        this.mapa = controlador.getMapa();
    }

    public void executarTurno() {
        Gdx.app.log("IA", ">>> INICIANDO TURNO DA IA (" + eu.getCor() + ") <<<");

        tentaTrocarCartas();

        Timer.schedule(new Timer.Task() {
            @Override public void run() { faseDistribuicao(); }
        }, DELAY_DISTRIBUICAO);
        if (controlador.isPrimeiraRodada()) return;
        Timer.schedule(new Timer.Task() {
            @Override public void run() { faseAtaque(); }
        }, DELAY_ATAQUE);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                faseMovimentacao();
                Gdx.app.log("IA", "<<< ENCERRANDO TURNO DA IA >>>");
                controlador.passarAVez();
            }
        }, DELAY_MOVIMENTO);
    }

    // --- FASE 1 DA IA: TROCA ---
    private void tentaTrocarCartas() {
        List<Carta> mao = eu.getCartas();
        if (mao.size() < 3) return;

        for (int i = 0; i < mao.size(); i++) {
            for (int j = i + 1; j < mao.size(); j++) {
                for (int k = j + 1; k < mao.size(); k++) {
                    Carta c1 = mao.get(i), c2 = mao.get(j), c3 = mao.get(k);
                    if (ServicoDeCartas.isCombinacaoValida(c1, c2, c3)) {
                        List<Carta> troca = new ArrayList<>(Arrays.asList(c1, c2, c3));
                        controlador.tentarTrocaDeCartas(troca);
                        return;
                    }
                }
            }
        }
    }

    // --- FASE 2 DISTRIBUIÇÂO ---
    private void faseDistribuicao() {

        Gdx.app.log("IA", "Iniciando Distribuição. Total Geral: " + controlador.getTropasADistribuirTotal());

        int safetyCounter = 0;

        while (controlador.getTropasADistribuirTotal() > 0) {

            safetyCounter++;
            if (safetyCounter > 300) {
                Gdx.app.log("IA", "ALERTA: Loop de distribuição travou. Forçando saída.");
                break;
            }

            String restricao = controlador.getRestricaoAtual();

            if (restricao != null) {

                Territorio melhorDef = getMelhorTerritorioDefesaNovaFormula(restricao);

                if (melhorDef != null) {
                    controlador.alocarTropas(melhorDef, 1);
                } else {
                    alocarFallbackRestrito(restricao);
                }

            } else {

                int tropasNoLote = controlador.getTropasADistribuir();
                Territorio melhorAtk = getMelhorTerritorioAtaqueAllIn(tropasNoLote);
                boolean ataqueGarantido = false;

                if (melhorAtk != null) {
                    Territorio vizinhoFraco = getVizinhoInimigoMaisFraco(melhorAtk);
                    if (vizinhoFraco != null && melhorAtk.getTropas() > (3 * vizinhoFraco.getTropas())) {
                        ataqueGarantido = true;
                    }
                }

                if (melhorAtk != null && !ataqueGarantido) {
                    controlador.alocarTropas(melhorAtk, 1);
                } else {
                    Territorio melhorDef = getMelhorTerritorioDefesaNovaFormula(null);

                    if (melhorDef != null) {
                        controlador.alocarTropas(melhorDef, 1);
                    } else {
                        Territorio t = getTerritorioComMaisTropas();
                        if (t != null) controlador.alocarTropas(t, 1);
                    }
                }
            }
        }
        controlador.proximaFaseTurno();
    }

    private Territorio getTerritorioComMaisTropas() {
        Territorio melhor = null;
        int maior = -1;

        for (Territorio t : eu.getTerritorios()) {
            if (t.getTropas() > maior) {
                maior = t.getTropas();
                melhor = t;
            }
        }

        return melhor;
    }


    private void alocarFallbackRestrito(String continenteNome) {
        for (Territorio t : eu.getTerritorios()) {
            if (t.getContinente().equalsIgnoreCase(continenteNome)) {
                controlador.alocarTropas(t, 1);
                return;
            }
        }

        if (!eu.getTerritorios().isEmpty()) {
            controlador.alocarTropas(eu.getTerritorios().get(0), 1);
        }
    }

    private Territorio getMelhorTerritorioDefesaNovaFormula(String filtroContinente) {
        Territorio melhor = null;
        float maiorScore = -1f;

        for (Territorio t : eu.getTerritorios()) {
            if (filtroContinente != null && !t.getContinente().equalsIgnoreCase(filtroContinente)) {
                continue;
            }

            Array<Territorio> inimigos = mapa.getInimigosAdj(t);
            if (inimigos.size == 0) continue;

            int totalInimigos = 0;
            for (Territorio ini : inimigos) totalInimigos += ini.getTropas();
            int totalAliados = mapa.getAlidadosAdj(t).size;
            int minhas = Math.max(1, t.getTropas());

            float score = totalInimigos * ((float)(totalAliados + 1) / (minhas * 3));
            score += MathUtils.random(0.01f);

            if (score > maiorScore) {
                maiorScore = score;
                melhor = t;
            }
        }
        return melhor;
    }

    private Territorio getMelhorTerritorioAtaqueAllIn(int tropasReserva) {
        Territorio melhor = null;
        float maiorScore = -1f;

        for (Territorio t : eu.getTerritorios()) {
            Territorio vizinhoFraco = getVizinhoInimigoMaisFraco(t);
            if (vizinhoFraco != null) {
                float minhaForcaPotencial = t.getTropas() + tropasReserva;
                float forcaInimiga = Math.max(vizinhoFraco.getTropas(), 0.1f);

                float score = (minhaForcaPotencial / forcaInimiga) + MathUtils.random(0.01f);
                if (score > maiorScore) {
                    maiorScore = score;
                    melhor = t;
                }
            }
        }
        return melhor;
    }


    // --- FASE # ATAQUE ---
    private void faseAtaque() {
        boolean conquistouCarta = false;
        List<Territorio> ofensivos = new ArrayList<>();

        for (Territorio t : eu.getTerritorios()) {
            if (t.getTropas() > 1) ofensivos.add(t);
        }

        // Ordenar pela maior vantagem
        ofensivos.sort((t1, t2) -> Integer.compare(getVantagemSobreVizinhoMaisFraco(t2), getVantagemSobreVizinhoMaisFraco(t1)));

        for (Territorio origem : ofensivos) {
            if (origem.getTropas() <= 1) continue;

            Territorio alvo = getVizinhoInimigoMaisFraco(origem);
            if (alvo == null) continue;

            // Logica de decisão: Agressivo até conseguir carta, dps só com vantagem
            boolean atacar = (!conquistouCarta) ? (origem.getTropas() > alvo.getTropas())
                : (origem.getTropas() >= alvo.getTropas() * FATOR_ATAQUE_ESMAGADOR);

            if (atacar) {
                Gdx.app.log("IA", "ATAQUE: " + origem.getNome() + " -> " + alvo.getNome());

                while (origem.getTropas() > 1) {
                    // Condição de parada
                    if (!conquistouCarta) {
                        if (origem.getTropas() <= alvo.getTropas()) break;
                    } else {
                        if (origem.getTropas() < alvo.getTropas() * 3) break;
                    }

                    AtaqueEstado estado = controlador.realizarAtaque(origem, alvo);

                    if (estado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
                        conquistouCarta = true;
                        int disponivel = origem.getTropas() - 1;

                        // Movimentação
                        int mover = (!conquistouCarta) ? 3 : Math.min(disponivel / 2, 3);
                        mover = MathUtils.clamp(mover, 1, disponivel);

                        controlador.moverTropasAposConquista(origem, alvo, mover);
                        break;
                    }
                }
            }
        }
        controlador.proximaFaseTurno();
    }

    private int getVantagemSobreVizinhoMaisFraco(Territorio t) {
        Territorio alvo = getVizinhoInimigoMaisFraco(t);
        return (alvo == null) ? -999 : (t.getTropas() - alvo.getTropas());
    }

    private Territorio getVizinhoInimigoMaisFraco(Territorio t) {
        Array<Territorio> inimigos = mapa.getInimigosAdj(t);
        Territorio fraco = null;
        int min = Integer.MAX_VALUE;
        for (Territorio ini : inimigos) {
            if (ini.getTropas() < min) {
                min = ini.getTropas();
                fraco = ini;
            }
        }
        return fraco;
    }

    // --- Fase 4, Difusão de tropas para as fronteiras ---
    private void faseMovimentacao() {
        Gdx.app.log("IA", "--- Fase de Movimentação ---");
        Map<Territorio, DadosTerritorio> mapaDados = analisarTerreno();

        // Ordena: Interior (Distancia grande) e Fronteira (Distancia 0)
        List<Territorio> meusTerritorios = new ArrayList<>(eu.getTerritorios());
        meusTerritorios.sort((t1, t2) -> Integer.compare(mapaDados.get(t2).distanciaAoFront, mapaDados.get(t1).distanciaAoFront));

        for (Territorio origem : meusTerritorios) {
            DadosTerritorio dadosOrigem = mapaDados.get(origem);
            int maxMover = Math.min(controlador.getTropasIniciaisMovimentacao(origem), origem.getTropas() - 1);

            if (maxMover <= 0) continue;

            if (dadosOrigem.distanciaAoFront > 0) {
                moverInteriorParaFront(origem, maxMover, mapaDados);
            } else {
                moverEntreFronts(origem, maxMover, mapaDados);
            }
        }
    }

    private void moverInteriorParaFront(Territorio origem, int qtdTotal, Map<Territorio, DadosTerritorio> mapaDados) {
        DadosTerritorio dadosOrigem = mapaDados.get(origem);
        Array<Territorio> vizinhos = mapa.getAlidadosAdj(origem);
        Map<Territorio, Float> candidatos = new HashMap<>();
        float somaInversos = 0f;

        for (Territorio vizinho : vizinhos) {
            DadosTerritorio dadosVizinho = mapaDados.get(vizinho);
            if (dadosVizinho.distanciaAoFront < dadosOrigem.distanciaAoFront) {
                float peso = 1.0f / Math.max(0.1f, (float) dadosVizinho.distanciaAoFront);
                candidatos.put(vizinho, peso);
                somaInversos += peso;
            }
        }

        if (somaInversos > 0) {
            int tropasRestantes = qtdTotal;
            for (Map.Entry<Territorio, Float> entry : candidatos.entrySet()) {
                Territorio destino = entry.getKey();
                int qtdParaEste = Math.round(qtdTotal * (entry.getValue() / somaInversos));

                if (qtdParaEste > tropasRestantes) qtdParaEste = tropasRestantes;
                if (qtdParaEste <= 0) continue;

                if (controlador.moverTropasEstrategicas(origem, destino, qtdParaEste)) {
                    tropasRestantes -= qtdParaEste;
                }
            }
        }
    }

    private void moverEntreFronts(Territorio origem, int qtdDisponivel, Map<Territorio, DadosTerritorio> mapaDados) {
        DadosTerritorio dadosOrigem = mapaDados.get(origem);
        Array<Territorio> vizinhos = mapa.getAlidadosAdj(origem);
        List<Territorio> grupoFront = new ArrayList<>();
        grupoFront.add(origem);

        float totalPerigo = dadosOrigem.perigoBase;
        int totalTropas = origem.getTropas();

        for (Territorio vizinho : vizinhos) {
            DadosTerritorio dadosVizinho = mapaDados.get(vizinho);
            if (dadosVizinho.distanciaAoFront == 0) {
                grupoFront.add(vizinho);
                totalPerigo += dadosVizinho.perigoBase;
                totalTropas += vizinho.getTropas();
            }
        }

        if (grupoFront.size() <= 1) return;

        float ratioAlvo = (float) totalTropas / Math.max(1.0f, totalPerigo);
        float meuRatio = (float) origem.getTropas() / Math.max(1.0f, dadosOrigem.perigoBase);
        if (meuRatio <= ratioAlvo) return;

        // Calcula deficit de tropa
        Map<Territorio, Integer> demandas = new HashMap<>();
        int demandaTotal = 0;
        for (Territorio vizinho : grupoFront) {
            if (vizinho == origem) continue;
            int deficit = Math.round(mapaDados.get(vizinho).perigoBase * ratioAlvo) - vizinho.getTropas();
            if (deficit > 0) {
                demandas.put(vizinho, deficit);
                demandaTotal += deficit;
            }
        }

        // Distribui
        if (demandaTotal > 0) {
            int tropasNaMao = qtdDisponivel;
            for (Map.Entry<Territorio, Integer> entry : demandas.entrySet()) {
                int poolDeDoacao = Math.min(tropasNaMao, demandaTotal);
                int qtdEnviar = Math.round(((float) entry.getValue() / demandaTotal) * poolDeDoacao);

                if (qtdEnviar > tropasNaMao) qtdEnviar = tropasNaMao;
                if (qtdEnviar <= 0) continue;

                if (controlador.moverTropasEstrategicas(origem, entry.getKey(), qtdEnviar)) {
                    tropasNaMao -= qtdEnviar;
                }
            }
        }
    }

    // --- MAPEIA (Busca em largura) ---
    private Map<Territorio, DadosTerritorio> analisarTerreno() {
        Map<Territorio, DadosTerritorio> dados = new HashMap<>();
        List<Territorio> meusTerritorios = eu.getTerritorios();
        Queue<Territorio> fila = new LinkedList<>();

        for (Territorio t : meusTerritorios) {
            dados.put(t, new DadosTerritorio(t));
        }

        // 1. Identifica as fronteira
        for (Territorio t : meusTerritorios) {
            Array<Territorio> inimigos = mapa.getInimigosAdj(t);
            if (inimigos.size > 0) {
                DadosTerritorio dt = dados.get(t);
                dt.distanciaAoFront = 0;
                for (Territorio ini : inimigos) dt.perigoBase += ini.getTropas();
                fila.add(t);
            }
        }

        // 2. BFS Distancias
        while (!fila.isEmpty()) {
            Territorio atual = fila.poll();
            DadosTerritorio dadosAtual = dados.get(atual);
            for (Territorio vizinho : mapa.getAlidadosAdj(atual)) {
                DadosTerritorio dadosVizinho = dados.get(vizinho);
                if (dadosVizinho.distanciaAoFront > dadosAtual.distanciaAoFront + 1) {
                    dadosVizinho.distanciaAoFront = dadosAtual.distanciaAoFront + 1;
                    fila.add(vizinho);
                }
            }
        }
        return dados;
    }
}
