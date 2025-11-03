package com.gruposete.war.core; // Ajuste o pacote se necessário

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gruposete.war.core.ServicoDeReforco;
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

    /**
     * Calcula o total de tropas que o jogador receberá no início do seu turno.
     */
    private void calcularTropasDoTurno() {

        tropasADistribuir = 0;
        
        // Calcular o bônus base (Territórios / 3) + Bônus de Continentes
        int reforcosBase = ServicoDeReforco.calcularTotalReforcos(this.jogadorAtual, this.mapa);

        //System.out.println("Total de reforcosbase" + reforcosBase);
        
        // A lógica de troca obrigatória e reforços de continente vem aqui
        
        // Adicionar os exércitos de reforço ao jogadorAtual.
        this.jogadorAtual.setExercitosDisponiveis(reforcosBase);
        
        // Define as tropas A DISTRIBUIR com o valor calculado
        this.tropasADistribuir = this.jogadorAtual.getExercitosDisponiveis();

        System.out.println("Total de tropas a distribuir" + tropasADistribuir);
        
        // DEBUG: Zera o contador do jogador para que ele só possa distribuir o que calculamos.
        // Se você não tem um método para zerar, use:
        // this.jogadorAtual.removerExercitosDisponiveis(this.tropasADistribuir);
        // O código do controlador precisa ser ajustado para usar 'tropasADistribuir' como o saldo.        
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

    public AtaqueEstado realizarAtaque(Territorio atacante, Territorio defensor) {
    // 1. Validação de Estado do Jogo (Ataque deve ser na fase correta)
    if (this.estadoTurno != EstadoTurno.ATACANDO) {
        // Retorna um estado de falha ou erro, assumindo que AtaqueEstado tem essa opção
        // return AtaqueEstado.NAO_NA_FASE; 
    }
    
    // 2. Obter o jogador defensor
    int jogadorDefensorID = defensor.getPlayerId();
    // O PlayerId é 1-based, a lista de Jogadores é 0-based.
    Jogador jogadorDefensor = jogadores.get(jogadorDefensorID - 1); 

    // 3. Criar e Executar a lógica de ataque (RF8)
    // NOTE: Este construtor exige as classes AtaqueLogica e AtaqueEstado
    AtaqueLogica logica = new AtaqueLogica(atacante, defensor, this.jogadorAtual, jogadorDefensor, this.mapa);
    AtaqueEstado resultado = logica.executarUmaRodada(); 

    // 4. Atualizar o estado do turno e os dados do Jogo se houve conquista
    if (resultado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
        this.conquistouTerritorioNesteTurno = true; // Necessário para dar carta (RF20)

        // --- CORREÇÃO DO BUG DE CONTAGEM (Transferência de Posse) ---
        
        // a) O jogador defensor perde o território.
        jogadorDefensor.removerTerritorio(defensor);
        
        // b) O jogador atacante ganha o território.
        this.jogadorAtual.adicionarTerritorio(defensor);

        // c) O território defensor foi eliminado? (RF13c e RF22)
        if (jogadorDefensor.getTerritorios().isEmpty()) {
            // Se o jogador foi eliminado, a lógica de RF22 deve ser acionada (transferência de cartas).
            // A lógica real deve ser implementada em um serviço: ServicoDeVitoria.processarEliminacao(jogadorAtacante, jogadorDefensor);
        }

        // A AtaqueLogica já atualizou o PlayerId e a cor no Território.
        
        // A TelaDeJogo deve agora solicitar a movimentação pós-conquista (RF10).
    }
    
    // 5. Verificar condição de vitória (RF13) - Deve ser feito pelo Controlador após cada conquista.
    // this.verificarVitoria(this.jogadorAtual);

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
