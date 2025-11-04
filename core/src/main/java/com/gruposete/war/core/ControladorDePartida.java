package com.gruposete.war.core; // Ajuste o pacote se necessário

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.Random; // Não é mais necessário, AtaqueLogica tem o seu

/**
 * Gerencia o estado da partida, fluxo de turnos e regras do jogo.
 * ATUALIZADO: Usa a classe AtaqueLogica oficial e o termo 'Tropas'.
 */
public class ControladorDePartida {

    // --- Estado do Jogo ---
    private List<Jogador> jogadores;
    private Array<Territorio> territorios;
    private Mapa mapa;
    private SetupPartida setupLogic;

    private int indiceJogadorAtual;
    private Jogador jogadorAtual;
    private int tropasADistribuir;
    private Map<Territorio, Integer> tropasInicioMovimentacao = new HashMap<>();
    // --- Estado do Turno ---
    public enum EstadoTurno {
        DISTRIBUINDO,
        ATACANDO,
        MOVIMENTANDO
    }

    private EstadoTurno estadoTurno;
    private boolean conquistouTerritorioNesteTurno;
    private int contadorGlobalDeTrocas;


    /**
     * Construtor. Recebe jogadores da TelaDeSelecao.
     */
    public ControladorDePartida(List<Jogador> jogadoresSelecionados) {
        this.jogadores = jogadoresSelecionados;
    }

    /**
     * Executa o setup inicial do jogo.
     * Chamado pela Main ao iniciar o jogo.
     */
    public void iniciarPartida() {
        this.setupLogic = new SetupPartida(this.jogadores);

        this.jogadores = setupLogic.getJogadoresPreparados();
        this.contadorGlobalDeTrocas = 0;
        this.territorios = setupLogic.getTodosOsTerritorios();
        this.mapa = setupLogic.getMapaAdjacencias();

        BaralhoDeTroca.getInstance().inicializarBaralho(this.territorios);

        this.indiceJogadorAtual = 0;
        this.jogadorAtual = this.jogadores.get(this.indiceJogadorAtual);

        this.estadoTurno = EstadoTurno.DISTRIBUINDO;
        this.conquistouTerritorioNesteTurno = false;

        calcularTropasDoTurno();
    }

    // --- LÓGICA DE FLUXO DE JOGO ---
    public boolean tentarTrocaDeCartas(List<Carta> cartasSelecionadas) {
        if (cartasSelecionadas == null || cartasSelecionadas.size() != 3) {
            return false; // Deve selecionar exatamente 3 cartas
        }

        Carta c1 = cartasSelecionadas.get(0);
        Carta c2 = cartasSelecionadas.get(1);
        Carta c3 = cartasSelecionadas.get(2);

        // 1. Valida a combinação usando o serviço
        if (!ServicoDeCartas.isCombinacaoValida(c1, c2, c3)) {
            Gdx.app.log("Controlador", "Troca falhou: Combinação inválida.");
            return false;
        }

        // --- SUCESSO ---

        // 2. Calcular bônus de exércitos e incrementar o contador global
        this.contadorGlobalDeTrocas++;
        int bonusExercitos = ServicoDeCartas.calcularBonusTroca(this.contadorGlobalDeTrocas);

        // Adiciona o bônus principal às tropas de distribuição
        this.tropasADistribuir += bonusExercitos;

        Gdx.app.log("Controlador", "Troca #" + this.contadorGlobalDeTrocas + " concluída. Bônus: " + bonusExercitos);

        // 3. Processar bônus de território
        int indiceJogador = this.jogadores.indexOf(this.jogadorAtual);
        for (Carta carta : cartasSelecionadas) {
            Territorio t = carta.getTerritorio();

            // Verifica se a carta é de território e se o jogador o possui
            if (t != null && t.getPlayerId() - 1 == indiceJogador) {
                // (Assumindo que Territorio tem setTropas ou similar)
                t.setTropas(t.getTropas() + 2);
                Gdx.app.log("Controlador", "Bônus de Território: +2 tropas em " + t.getNome());
            }

            // 4. Remove da mão do jogador
            this.jogadorAtual.getCartas().remove(carta);
        }

        // 5. Devolve as cartas ao Baralho (Singleton)
        BaralhoDeTroca.getInstance().receberTroca(cartasSelecionadas);

        return true;
    }
    /**
     * Avança para o próximo jogador e aplica regras de fim de turno.
     */
    public void passarAVez() {
        if (this.conquistouTerritorioNesteTurno) {
            darCartaAoJogadorAtual();
        }

        this.indiceJogadorAtual = (this.indiceJogadorAtual + 1) % this.jogadores.size();
        this.jogadorAtual = this.jogadores.get(this.indiceJogadorAtual);

        this.estadoTurno = EstadoTurno.DISTRIBUINDO;
        this.conquistouTerritorioNesteTurno = false;
        calcularTropasDoTurno();
    }

    /**
     * Avança para a próxima fase do turno.
     */
    public void proximaFaseTurno() {
        switch (this.estadoTurno) {
            case DISTRIBUINDO:
                if (ServicoDeCartas.isTrocaObrigatoria(this.jogadorAtual)) {
                    Gdx.app.log("Controlador", "Troca obrigatória. Não pode avançar de fase.");
                    // (A UI deve mostrar um aviso)
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
                tropasInicioMovimentacao.clear();
                for (Territorio t : territorios) {
                    if (t.getPlayerId()-1 == jogadores.indexOf(jogadorAtual)) {
                        tropasInicioMovimentacao.put(t, t.getTropas());
                    }
                }
                break;
            case MOVIMENTANDO:
                passarAVez();
                break;
        }
    }
    private void calcularTropasDoTurno() {
        // TODO: Implementar a lógica real min(Territorios / 2 + Bônus de Continente, 3)
        // Por enquanto, damos 5 tropas de bônus para teste.
        this.tropasADistribuir = 5;
    }
    public boolean alocarTropas(Territorio territorio, int quantidade) {
        int indiceJogador = this.jogadores.indexOf(this.jogadorAtual);

        // Validação
        if (this.estadoTurno != EstadoTurno.DISTRIBUINDO) {
            Gdx.app.log("Controlador", "Alocação falhou: Não está na fase de distribuição.");
            return false;
        }
        if (territorio.getPlayerId() - 1 != indiceJogador) {
            Gdx.app.log("Controlador", "Alocação falhou: Território não é seu.");
            return false;
        }
        if (quantidade > this.tropasADistribuir) {
            Gdx.app.log("Controlador", "Alocação falhou: Tropas insuficientes.");
            return false; // Não tem tropas suficientes
        }
        if (quantidade < 1) {
            Gdx.app.log("Controlador", "Alocação falhou: Mínimo 1.");
            return false; // Mínimo de 1
        }

        // Sucesso
        Gdx.app.log("Controlador", "Alocando " + quantidade + " tropas em " + territorio.getNome());
        territorio.setTropas(territorio.getTropas() + quantidade);
        this.tropasADistribuir -= quantidade;
        return true;
    }
    /**
     * Executa UMA rodada de combate usando AtaqueLogica.
     * @param atacante Território de origem do ataque.
     * @param defensor Território alvo.
     * @return O AtaqueEstado (ex: TERRITORIO_CONQUISTADO, TROPAS_INSUFICIENTES)
     */
    public AtaqueEstado realizarAtaque(Territorio atacante, Territorio defensor) {

        // 1. Obter o jogador defensor
        int jogadorDefensorID = defensor.getPlayerId();

        // 2. Criar a lógica de ataque para esta rodada
        AtaqueLogica logica = new AtaqueLogica(atacante, defensor, this.jogadorAtual, jogadores.get(jogadorDefensorID-1), this.mapa);
        // 3. Executar a rodada (AtaqueLogica atualiza as tropas nos objetos)
        AtaqueEstado resultado = logica.executarUmaRodada();

        // 4. Atualizar o estado do turno se houve conquista
        if (resultado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
            this.conquistouTerritorioNesteTurno = true;

            // NOTA: O AtaqueLogica já define o 'playerId' no território.
            // A TelaDeJogo agora deve perguntar "Quantas tropas mover?"
            // e então chamar moverTropasAposConquista().
        }

        return resultado; // Retorna o enum
    }

    /**
     * Move as tropas após uma conquista.
     * A TelaDeJogo deve chamar este método após perguntar ao usuário a quantidade.
     * @param origem O território que atacou.
     * @param destino O território conquistado (que agora tem 0 tropas).
     * @param tropasParaMover A quantidade que o usuário escolheu.
     */
    public boolean moverTropasAposConquista(Territorio origem, Territorio destino, int tropasParaMover) {

        // Validação 1: Regra do War (mínimo de 1)
        if (tropasParaMover < 1) {
            Gdx.app.log("Controlador", "Movimento falhou: Mínimo 1 tropa.");
            return false;
        }

        // Validação 2: (O FIX) Garante que a origem fica com pelo menos 1 tropa.
        int tropasDisponiveisParaMover = origem.getTropas() - 1;

        if (tropasParaMover > tropasDisponiveisParaMover) {
            Gdx.app.log("Controlador", "Movimento falhou: Não pode mover mais do que o disponível.");
            return false;
        }

        // Validação 3: Limita a 3 tropas
        if (tropasParaMover > 3) {
            Gdx.app.log("Controlador", "Movimento falhou: Máximo de 3 tropas pós-ataque.");
            return false;
        }

        // Sucesso: Realiza a movimentação
        Gdx.app.log("Controlador", "Movendo " + tropasParaMover + " tropas.");
        destino.setTropas(tropasParaMover); // (Será 0 + tropas)
        origem.setTropas(origem.getTropas() - tropasParaMover);
        return true;
    }

    public boolean moverTropasEstrategicas(Territorio origem, Territorio destino, int tropasParaMover) {
        // Validação 1: Dono e Adjacência
        int indiceJogador = this.jogadores.indexOf(this.jogadorAtual);
        if (origem.getPlayerId() - 1 != indiceJogador || destino.getPlayerId() - 1 != indiceJogador) {
            Gdx.app.log("Controlador", "Mov. Estratégico falhou: Territórios não são do jogador.");
            return false;
        }
        if (!this.mapa.isAdjacente(origem, destino)) {
            Gdx.app.log("Controlador", "Mov. Estratégico falhou: Territórios não adjacentes.");
            return false;
        }

        // Validação 2 (Constraint 3): Pega o limite de tropas do "snapshot"
        Integer tropasIniciais = tropasInicioMovimentacao.get(origem);
        if (tropasIniciais == null) {
            tropasIniciais = origem.getTropas();
        }

        // O máximo que ele pode mover é o que tinha no início
        int tropasDisponiveisParaMoverDoSnapshot = tropasIniciais;

        if (tropasParaMover > tropasDisponiveisParaMoverDoSnapshot) {
            Gdx.app.log("Controlador", "Mov. Estratégico falhou: Só pode mover tropas que iniciaram a fase no território.");
            return false;
        }
        // Validação 3: Garante que as tropas *atuais* também são suficientes
        if (tropasParaMover > (origem.getTropas() - 1)) {
            Gdx.app.log("Controlador", "Mov. Estratégico falhou: Tropas insuficientes.");
            return false;
        }
        if (tropasParaMover < 1) {
            Gdx.app.log("Controlador", "Mov. Estratégico falhou: Mínimo 1 tropa.");
            return false;
        }

        // --- SUCESSO ---

        // 1. Realiza a movimentação
        origem.setTropas(origem.getTropas() - tropasParaMover);
        destino.setTropas(destino.getTropas() + tropasParaMover);

        // Reduz o "crédito" de movimentação daquele território
        int novoLimite = tropasIniciais - tropasParaMover;
        tropasInicioMovimentacao.put(origem, novoLimite);

        Gdx.app.log("Controlador", "Mov. Estratégico feito. Limite de " + origem.getNome() + " agora é " + (novoLimite ));

        // 3. REMOVE o 'passarAVez()'
        // O jogador agora pode fazer mais movimentos ou clicar em "Encerrar Turno".

        return true;
    }

    /**
     * Adiciona uma carta (mock) ao jogador atual.
     */
    private void darCartaAoJogadorAtual() {
        // Pega a instância do singleton e compra uma carta
        Carta novaCarta = BaralhoDeTroca.getInstance().comprarCarta();

        if (novaCarta != null) {
            this.jogadorAtual.getCartas().add(novaCarta);
            Gdx.app.log("Controlador", jogadorAtual.getNome() + " comprou a carta: " + novaCarta.getSimbolo());
        } else {
            Gdx.app.error("Controlador", "Falha ao comprar carta. O baralho está vazio?");
        }
    }


    // --- GETTERS (Para a TelaDeJogo ler o estado) ---
    public int getTropasIniciaisMovimentacao(Territorio t) {
        return tropasInicioMovimentacao.getOrDefault(t, t.getTropas());
    }
    public List<Jogador> getJogadores() { return jogadores; }
    public Array<Territorio> getTerritorios() { return territorios; }
    public Mapa getMapa() { return mapa; }
    public Jogador getJogadorAtual() { return jogadorAtual; }
    public EstadoTurno getEstadoTurno() { return estadoTurno; }
    public int getTropasADistribuir() { return tropasADistribuir; }
    // A classe interna 'ResultadoCombate' foi removida pois é desnecessária.
    // A TelaDeJogo deve reagir ao 'AtaqueEstado' retornado por 'realizarAtaque'.
}
