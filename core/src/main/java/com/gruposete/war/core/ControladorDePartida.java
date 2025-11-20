package com.gruposete.war.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gerencia o estado da partida, fluxo de turnos, regras de negócio e condições de vitória.
 * Atua como o "cérebro" central do jogo.
 */
public class ControladorDePartida {

    // --- ENUMS ---
    public enum EstadoTurno {
        DISTRIBUINDO,
        ATACANDO,
        MOVIMENTANDO
    }

    // --- ESTADO DO JOGO (DADOS) ---
    private List<Jogador> jogadores;
    private Array<Territorio> territorios;
    private Mapa mapa;

    // --- ESTADO DO TURNO ATUAL ---
    private int indiceJogadorAtual;
    private Jogador jogadorAtual;
    private EstadoTurno estadoTurno;
    private int tropasADistribuir;
    private boolean conquistouTerritorioNesteTurno;

    // --- REGRAS E LÓGICA AUXILIAR ---
    private SetupPartida setupLogic;
    private VerificadorObjetivos verificadorObjetivos;
    private int contadorGlobalDeTrocas;

    // --- HISTÓRICOS E SNAPSHOTS ---
    // Snapshot de tropas no início da fase de movimentação (para validação)
    private Map<Territorio, Integer> tropasInicioMovimentacao = new HashMap<>();
    // Histórico de eliminações (Vítima -> Assassino) para verificação de objetivos
    private Map<Jogador, Jogador> historicoDeEliminacoes = new HashMap<>();

    // --- CONSTRUTOR ---
    public ControladorDePartida(List<Jogador> jogadoresSelecionados) {
        this.jogadores = jogadoresSelecionados;
    }

    // --- INICIALIZAÇÃO ---

    /**
     * Executa o setup inicial do jogo: carrega mapa, distribui territórios,
     * sorteia objetivos e inicializa o baralho.
     */
    public void iniciarPartida() {
        // 1. Setup Básico
        this.setupLogic = new SetupPartida(this.jogadores);
        this.jogadores = setupLogic.getJogadoresPreparados();
        this.territorios = setupLogic.getTodosOsTerritorios();
        this.mapa = setupLogic.getMapaAdjacencias();

        // 2. FORÇA A SINCRONIA: ID = Index + 1 (Lógica Unificada)
        for (int i = 0; i < this.jogadores.size(); i++) {
            this.jogadores.get(i).setPlayerId(i + 1);
            for (Territorio t : this.jogadores.get(i).getTerritorios()) {
                t.setPlayerId(i + 1);
            }
        }

        // 3. Inicialização de Sistemas
        this.contadorGlobalDeTrocas = 0;
        BaralhoDeTroca.getInstance().inicializarBaralho(this.territorios);
        this.verificadorObjetivos = new VerificadorObjetivos(this.jogadores, this.territorios, this);

        // 4. Configuração do Primeiro Turno
        this.indiceJogadorAtual = 0;
        this.jogadorAtual = this.jogadores.get(this.indiceJogadorAtual);
        this.estadoTurno = EstadoTurno.DISTRIBUINDO;
        this.conquistouTerritorioNesteTurno = false;

        calcularTropasDoTurno();

        // Debug
        imprimirObjetivosJogadores();

        // Verifica se o primeiro é IA
        verificarTurnoIA();
    }

    /**
     * ÚNICO ponto de conversão de ID para Jogador.
     * Garante que ID 1 = Index 0, ID 2 = Index 1, etc.
     */
    public Jogador getJogadorPorId(int id) {
        if (id <= 0 || id > jogadores.size()) return null;
        return jogadores.get(id - 1);
    }

    // --- CONTROLE DE FLUXO (TURNOS E FASES) ---

    /**
     * Avança para o próximo jogador e reinicia o ciclo do turno.
     */
    public void passarAVez() {
        // Entrega carta se houve conquista
        if (this.conquistouTerritorioNesteTurno) {
            darCartaAoJogadorAtual();
        }

        // Avança índice (circular)
        this.indiceJogadorAtual = (this.indiceJogadorAtual + 1) % this.jogadores.size();
        this.jogadorAtual = this.jogadores.get(this.indiceJogadorAtual);

        // Reseta estado
        this.estadoTurno = EstadoTurno.DISTRIBUINDO;
        this.conquistouTerritorioNesteTurno = false;

        if(this.jogadorAtual.getTerritorios().isEmpty()){
            passarAVez();
        }

        calcularTropasDoTurno();
        verificarTurnoIA();
    }

    private void verificarTurnoIA() {
        if (this.jogadorAtual.getIsAI()) {
            Gdx.app.log("Controlador", ">>> Turno da IA (" + jogadorAtual.getNome() + ") iniciado.");
            IABot bot = new IABot(this, this.jogadorAtual);
            bot.executarTurno();
        }
    }

    /**
     * Avança para a próxima fase dentro do turno de um jogador.
     */
    public void proximaFaseTurno() {
        switch (this.estadoTurno) {
            case DISTRIBUINDO:
                if (ServicoDeCartas.isTrocaObrigatoria(this.jogadorAtual)) {
                    Gdx.app.log("Controlador", "Troca obrigatória. Não pode avançar.");
                    return;
                }
                if (this.tropasADistribuir > 0) {
                    Gdx.app.log("Controlador", "Ainda há tropas para distribuir.");
                    return;
                }
                this.estadoTurno = EstadoTurno.ATACANDO;
                break;

            case ATACANDO:
                this.estadoTurno = EstadoTurno.MOVIMENTANDO;
                // Tira snapshot das tropas para validar movimentação estratégica
                tropasInicioMovimentacao.clear();
                for (Territorio t : territorios) {
                    // Uso da lógica unificada: Compara objeto Dono com Jogador Atual
                    Jogador dono = getJogadorPorId(t.getPlayerId());
                    if (dono.equals(this.jogadorAtual)) {
                        tropasInicioMovimentacao.put(t, t.getTropas());
                    }
                }
                break;

            case MOVIMENTANDO:
                passarAVez();
                break;
        }
    }

    // --- AÇÕES DO JOGADOR: DISTRIBUIÇÃO E TROCA ---

    private void calcularTropasDoTurno() {
        int reforcosBase = ServicoDeReforco.calcularTotalReforcos(this.jogadorAtual, this.mapa);
        this.jogadorAtual.setExercitosDisponiveis(reforcosBase);
        this.tropasADistribuir = this.jogadorAtual.getExercitosDisponiveis();

        Gdx.app.log("Controlador", "Jogador " + jogadorAtual.getNome() + " recebe " + tropasADistribuir + " tropas.");
    }

    public boolean alocarTropas(Territorio territorio, int quantidade) {
        // Validação de Posse Unificada
        Jogador dono = getJogadorPorId(territorio.getPlayerId());
        if (!dono.equals(this.jogadorAtual)) {
            Gdx.app.log("Controlador", "Alocação falhou: Território não é seu.");
            return false;
        }

        // Validações de Regra
        if (this.estadoTurno != EstadoTurno.DISTRIBUINDO) return false;
        if (quantidade > this.tropasADistribuir) return false;
        if (quantidade < 1) return false;

        // Execução
        territorio.setTropas(territorio.getTropas() + quantidade);
        this.tropasADistribuir -= quantidade;

        Gdx.app.log("Controlador", "Alocou " + quantidade + " em " + territorio.getNome());
        return true;
    }

    public boolean tentarTrocaDeCartas(List<Carta> cartasSelecionadas) {
        if (cartasSelecionadas == null || cartasSelecionadas.size() != 3) return false;

        // 1. Validação
        if (!ServicoDeCartas.isCombinacaoValida(cartasSelecionadas.get(0), cartasSelecionadas.get(1), cartasSelecionadas.get(2))) {
            Gdx.app.log("Controlador", "Troca falhou: Combinação inválida.");
            return false;
        }

        // 2. Cálculo de Bônus
        this.contadorGlobalDeTrocas++;
        int bonusExercitos = ServicoDeCartas.calcularBonusTroca(this.contadorGlobalDeTrocas);
        this.tropasADistribuir += bonusExercitos;

        Gdx.app.log("Controlador", "Troca #" + this.contadorGlobalDeTrocas + " efetuada. Bônus: " + bonusExercitos);

        // 3. Aplicação de Bônus de Território e Remoção
        for (Carta carta : cartasSelecionadas) {
            Territorio t = carta.getTerritorio();

            // Validação de Posse Unificada
            if (t != null) {
                Jogador donoTerritorio = getJogadorPorId(t.getPlayerId());
                if (donoTerritorio != null && donoTerritorio.equals(this.jogadorAtual)) {
                    t.setTropas(t.getTropas() + 2);
                    Gdx.app.log("Controlador", "Bônus de Território: +2 tropas em " + t.getNome());
                }
            }
            this.jogadorAtual.getCartas().remove(carta);
        }

        // 4. Retorno ao Baralho
        BaralhoDeTroca.getInstance().receberTroca(cartasSelecionadas);
        return true;
    }

    // --- AÇÕES DO JOGADOR: ATAQUE ---

    public AtaqueEstado realizarAtaque(Territorio atacante, Territorio defensor) {
        if (this.estadoTurno != EstadoTurno.ATACANDO) {
            // return AtaqueEstado.ERRO;
        }

        // Lógica Unificada: Pega o defensor pelo ID
        Jogador jogadorDefensor = getJogadorPorId(defensor.getPlayerId());

        // Executa lógica de dados
        AtaqueLogica logica = new AtaqueLogica(atacante, defensor, this.jogadorAtual, jogadorDefensor, this.mapa);
        AtaqueEstado resultado = logica.executarUmaRodada();

        // Processa Conquista
        if (resultado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
            this.conquistouTerritorioNesteTurno = true;

            // Transferência de Posse (Lógica de Negócio)
            jogadorDefensor.removerTerritorio(defensor);
            this.jogadorAtual.adicionarTerritorio(defensor);

            // Checa Eliminação de Jogador
            if (jogadorDefensor.getTerritorios().isEmpty()) {
                Gdx.app.log("Controlador", "JOGADOR ELIMINADO: " + jogadorDefensor.getNome() + " por " + this.jogadorAtual.getNome());
                historicoDeEliminacoes.put(jogadorDefensor, this.jogadorAtual);
                atualizarObjetivosAposEliminacao(jogadorDefensor, this.jogadorAtual);
            }
        }

        // Checa Vitória do Jogo
        verificarVitoria();

        return resultado;
    }

    // --- AÇÕES DO JOGADOR: MOVIMENTAÇÃO ---

    /**
     * Move tropas imediatamente após conquistar um território.
     */
    public boolean moverTropasAposConquista(Territorio origem, Territorio destino, int tropasParaMover) {
        if (tropasParaMover < 1) return false;

        // Validação: Origem deve manter pelo menos 1 tropa
        int disponivel = origem.getTropas() - 1;
        if (tropasParaMover > disponivel) return false;

        // Validação: Limite máximo de 3 (regra de ataque)
        if (tropasParaMover > 3) return false;

        // Execução
        destino.setTropas(tropasParaMover); // (Destino estava zerado)
        origem.setTropas(origem.getTropas() - tropasParaMover);

        Gdx.app.log("Controlador", "Moveu " + tropasParaMover + " após conquista.");
        return true;
    }

    /**
     * Move tropas na fase de movimentação estratégica.
     */
    public boolean moverTropasEstrategicas(Territorio origem, Territorio destino, int tropasParaMover) {
        // Validação de Posse Unificada
        Jogador donoOrigem = getJogadorPorId(origem.getPlayerId());
        Jogador donoDestino = getJogadorPorId(destino.getPlayerId());

        if (!donoOrigem.equals(this.jogadorAtual) || !donoDestino.equals(this.jogadorAtual)) {
            Gdx.app.log("Controlador", "Movimento falhou: Territórios não são seus.");
            return false;
        }

        // Validação de Adjacência
        if (!this.mapa.isAdjacente(origem, destino)) return false;

        // Validação de Snapshot
        Integer tropasIniciais = tropasInicioMovimentacao.get(origem);
        if (tropasIniciais == null) tropasIniciais = origem.getTropas();

        int maxPermitidoPeloSnapshot = tropasIniciais;

        if (tropasParaMover > maxPermitidoPeloSnapshot) return false;
        if (tropasParaMover > (origem.getTropas() - 1)) return false;
        if (tropasParaMover < 0) return false;

        // Execução
        origem.setTropas(origem.getTropas() - tropasParaMover);
        destino.setTropas(destino.getTropas() + tropasParaMover);

        // Atualiza o snapshot
        int novoLimite = tropasIniciais - tropasParaMover;
        tropasInicioMovimentacao.put(origem, novoLimite);

        Gdx.app.log("Controlador", "Movimento estratégico: " + tropasParaMover);
        return true;
    }

    // --- MÉTODOS AUXILIARES E DE REGRAS ---

    private void darCartaAoJogadorAtual() {
        Carta novaCarta = BaralhoDeTroca.getInstance().comprarCarta();
        if (novaCarta != null) {
            this.jogadorAtual.getCartas().add(novaCarta);
            Gdx.app.log("Controlador", "Carta recebida: " + novaCarta.getSimbolo());
        } else {
            Gdx.app.error("Controlador", "Erro: Baralho vazio.");
        }
    }

    private void atualizarObjetivosAposEliminacao(Jogador eliminado, Jogador eliminador) {
        for (Jogador jogador : this.jogadores) {
            Objetivo objetivo = jogador.getObjetivo();

            if (objetivo.getTipo() == Objetivo.TipoDeObjetivo.ELIMINAR_JOGAOR &&
                objetivo.getCorJogadorAlvo() == eliminado.getCor()) {

                if (jogador != eliminador) {
                    Gdx.app.log("Controlador", "Objetivo alterado para " + jogador.getNome() + " (Alvo perdido).");
                    Objetivo novoObjetivo = new Objetivo(99+jogador.getPlayerId(), "Conquistar 24 territorios", "assets/Carta/52.png", 24);
                    jogador.setObjetivo(novoObjetivo);
                }
            }
        }
    }

    public Jogador verificarVitoria() {
        Jogador vencedor = verificadorObjetivos.verificarTodosObjetivos();
        if (vencedor != null) {
            Gdx.app.log("Controlador", "VITÓRIA! " + vencedor.getNome() + " venceu.");
        }
        return vencedor;
    }

    private void imprimirObjetivosJogadores() {
        Gdx.app.log("DEBUG", "=== OBJETIVOS ===");
        for (Jogador j : jogadores) {
            String desc = (j.getObjetivo() != null) ? j.getObjetivo().getDescricao() : "SEM OBJETIVO";
            Gdx.app.log("DEBUG", j.getNome() + " (" + j.getCor() + "): " + desc);
        }
    }

    public Jogador getEliminadorDe(Jogador jogadorEliminado) {
        return historicoDeEliminacoes.get(jogadorEliminado);
    }

    // --- GETTERS ---

    public List<Jogador> getJogadores() { return jogadores; }
    public Array<Territorio> getTerritorios() { return territorios; }
    public Mapa getMapa() { return mapa; }
    public Jogador getJogadorAtual() { return jogadorAtual; }
    public EstadoTurno getEstadoTurno() { return estadoTurno; }
    public int getTropasADistribuir() { return tropasADistribuir; }

    public int getTropasIniciaisMovimentacao(Territorio t) {
        return tropasInicioMovimentacao.getOrDefault(t, t.getTropas());
    }
}
