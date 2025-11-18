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

        // 2. Inicialização de Sistemas
        this.contadorGlobalDeTrocas = 0;
        BaralhoDeTroca.getInstance().inicializarBaralho(this.territorios);
        this.verificadorObjetivos = new VerificadorObjetivos(this.jogadores, this.territorios, this);

        // 3. Configuração do Primeiro Turno
        this.indiceJogadorAtual = 0;
        this.jogadorAtual = this.jogadores.get(this.indiceJogadorAtual);
        this.estadoTurno = EstadoTurno.DISTRIBUINDO;
        this.conquistouTerritorioNesteTurno = false;

        calcularTropasDoTurno();

        // Debug
        imprimirObjetivosJogadores();
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

        calcularTropasDoTurno();
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
                    if (t.getPlayerId() - 1 == jogadores.indexOf(jogadorAtual)) {
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
        int indiceJogador = this.jogadores.indexOf(this.jogadorAtual);

        // Validações
        if (this.estadoTurno != EstadoTurno.DISTRIBUINDO) return false;
        if (territorio.getPlayerId() - 1 != indiceJogador) return false;
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
        int indiceJogador = this.jogadores.indexOf(this.jogadorAtual);
        for (Carta carta : cartasSelecionadas) {
            Territorio t = carta.getTerritorio();
            if (t != null && t.getPlayerId() - 1 == indiceJogador) {
                t.setTropas(t.getTropas() + 2);
                Gdx.app.log("Controlador", "Bônus de Território: +2 tropas em " + t.getNome());
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
            // return AtaqueEstado.ERRO; // Opcional
        }

        // Prepara dados
        int jogadorDefensorID = defensor.getPlayerId();
        Jogador jogadorDefensor = jogadores.get(jogadorDefensorID - 1);

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
        int indiceJogador = this.jogadores.indexOf(this.jogadorAtual);

        // Validação 1: Posse
        if (origem.getPlayerId() - 1 != indiceJogador || destino.getPlayerId() - 1 != indiceJogador) return false;

        // Validação 2: Adjacência
        if (!this.mapa.isAdjacente(origem, destino)) return false;

        // Validação 3: Limite do Snapshot (Constraint 3)
        Integer tropasIniciais = tropasInicioMovimentacao.get(origem);
        if (tropasIniciais == null) tropasIniciais = origem.getTropas();

        int maxPermitidoPeloSnapshot = tropasIniciais; // Pode mover tudo que tinha, desde que sobre 1 no final

        if (tropasParaMover > maxPermitidoPeloSnapshot) return false; // Tentou mover mais do que tinha no começo
        if (tropasParaMover > (origem.getTropas() - 1)) return false; // Tentou deixar a origem vazia
        if (tropasParaMover < 1) return false;

        // Execução
        origem.setTropas(origem.getTropas() - tropasParaMover);
        destino.setTropas(destino.getTropas() + tropasParaMover);

        // Atualiza o snapshot para movimentos futuros
        int novoLimite = tropasIniciais - tropasParaMover;
        tropasInicioMovimentacao.put(origem, novoLimite);

        Gdx.app.log("Controlador", "Movimento estratégico: " + tropasParaMover + ". Novo limite origem: " + novoLimite);
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

    Jogador getEliminadorDe(Jogador jogadorEliminado) {
        return historicoDeEliminacoes.get(jogadorEliminado);
    }

    private void atualizarObjetivosAposEliminacao(Jogador eliminado, Jogador eliminador) {
        for (Jogador jogador : this.jogadores) {
            Objetivo objetivo = jogador.getObjetivo();

            // Se o objetivo era eliminar quem morreu...
            if (objetivo.getTipo() == Objetivo.TipoDeObjetivo.ELIMINAR_JOGAOR &&
                objetivo.getCorJogadorAlvo() == eliminado.getCor()) {

                // ...e não foi este jogador que matou (foi um terceiro)
                if (jogador != eliminador) {
                    Gdx.app.log("Controlador", "Objetivo alterado para " + jogador.getNome() + " (Alvo perdido).");
                    // TODO: Mudar path da imagem para uma carta válida ou null
                    Objetivo novoObjetivo = new Objetivo(99, "Conquistar 24 territorios", "assets/Carta/52.png", 24);
                    jogador.setObjetivo(novoObjetivo);
                }
            }
        }
    }

    public Jogador verificarVitoria() {
        Jogador vencedor = verificadorObjetivos.verificarTodosObjetivos();
        if (vencedor != null) {
            Gdx.app.log("Controlador", "🎉 VITÓRIA! " + vencedor.getNome() + " venceu.");
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
