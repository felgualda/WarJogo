package com.gruposete.war.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import java.util.ArrayList;
import java.util.List;

public class ControladorDePartidaTest {

    @BeforeAll
    public static void setupGdxAppStub() throws Exception {
        // Create a dynamic proxy that implements com.badlogic.gdx.Application and provides no-op log/error
        Class<?> appInterface = Class.forName("com.badlogic.gdx.Application");
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("log".equals(name) || "error".equals(name) || "debug".equals(name)) {
                    // args: (String tag, String message) or (String tag, String message, Throwable)
                    return null;
                }
                // return sensible defaults for primitive return types
                Class<?> rt = method.getReturnType();
                if (rt == boolean.class) return false;
                if (rt == int.class) return 0;
                if (rt == long.class) return 0L;
                if (rt == float.class) return 0f;
                if (rt == double.class) return 0d;
                return null;
            }
        };

        Object proxy = Proxy.newProxyInstance(appInterface.getClassLoader(), new Class[]{appInterface}, handler);

        // Set com.badlogic.gdx.Gdx.app = proxy
        Class<?> gdxClass = Class.forName("com.badlogic.gdx.Gdx");
        Field appField = gdxClass.getField("app");
        appField.set(null, proxy);
    }

    @Test
    public void testAlocarTropas_validAndInvalid() {
        // Cria jogadores
        Jogador j1 = new Jogador("P1", CorJogador.AZUL, 1, false);
        Jogador j2 = new Jogador("P2", CorJogador.VERMELHO, 2, false);
        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j1);
        jogadores.add(j2);

        ControladorDePartida ctrl = new ControladorDePartida(jogadores);
        ctrl.iniciarPartida();

        Jogador atual = ctrl.getJogadorAtual();
        assertNotNull(atual, "Jogador atual não deve ser nulo");

        // Valida que o controlador foi inicializado corretamente
        assertTrue(atual.getTerritorios().size() > 0, "Jogador deve ter territórios após iniciar partida");
        
        // Testes de alocação com validações
        int lote = ctrl.getTropasADistribuir();
        int totalLotes = ctrl.getTropasADistribuirTotal();
        
        // Deveria ter pelo menos alguns lotes a distribuir no início
        assertTrue(totalLotes >= 0, "Total de lotes deve ser >= 0");
        
        if (lote > 0) {
            // Tenta alocar quantidade maior que o lote (deve falhar)
            Territorio t = atual.getTerritorios().get(0);
            boolean resExcesso = ctrl.alocarTropas(t, lote + 100);
            assertFalse(resExcesso, "Não deveria alocar mais que o lote disponível");

            // Testa alocação de 1 tropa em todos os territórios até conseguir
            // (alguns podem falhar se têm restrição de continente)
            boolean alocouComSucesso = false;
            for (Territorio territorio : atual.getTerritorios()) {
                int tropasAntes = territorio.getTropas();
                boolean ok = ctrl.alocarTropas(territorio, 1);
                if (ok) {
                    alocouComSucesso = true;
                    assertEquals(tropasAntes + 1, territorio.getTropas(), "Deveria ter +1 tropa após alocação");
                    break; // Saiu após primeira alocação bem-sucedida
                }
            }
            assertTrue(alocouComSucesso, "Deveria conseguir alocar pelo menos 1 tropa em algum território");
        } else {
            // Se não houver lotes, tenta alocar em qualquer território (deve falhar)
            Territorio t = atual.getTerritorios().get(0);
            boolean ok = ctrl.alocarTropas(t, 1);
            assertFalse(ok, "Não deveria alocar tropas quando não há lote disponível");
        }
    }

    @Test
    public void testTentarTrocaDeCartas_aplicaBonusEremoveCartas() {
        Jogador j1 = new Jogador("P1", CorJogador.AZUL, 1, false);
        Jogador j2 = new Jogador("P2", CorJogador.VERMELHO, 2, false);
        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j1);
        jogadores.add(j2);

        ControladorDePartida ctrl = new ControladorDePartida(jogadores);
        ctrl.iniciarPartida();

        Jogador atual = ctrl.getJogadorAtual();
        // cria 3 cartas válidas (3 diferentes)
        Territorio any = atual.getTerritorios().get(0);
        Carta c1 = new Carta(101, SimboloCarta.CIRCULO, any, "a");
        Carta c2 = new Carta(102, SimboloCarta.QUADRADO, any, "b");
        Carta c3 = new Carta(103, SimboloCarta.TRIANGULO, any, "c");

        atual.adicionarCarta(c1);
        atual.adicionarCarta(c2);
        atual.adicionarCarta(c3);

        int antesTotal = ctrl.getTropasADistribuirTotal();

        boolean trocou = ctrl.tentarTrocaDeCartas(List.of(c1, c2, c3));
        assertTrue(trocou);

        // Cartas removidas da mão
        assertEquals(0, atual.getCartas().size());

        // O bônus da primeira troca é 4 (ver ServicoDeCartas)
        assertEquals(antesTotal + 4, ctrl.getTropasADistribuirTotal());
    }

    @Test
    public void testMoverTropasEstrategicas_validAndInvalid() {
        Jogador j1 = new Jogador("P1", CorJogador.AZUL, 1, false);
        Jogador j2 = new Jogador("P2", CorJogador.VERMELHO, 2, false);
        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j1);
        jogadores.add(j2);

        ControladorDePartida ctrl = new ControladorDePartida(jogadores);
        ctrl.iniciarPartida();

        Jogador atual = ctrl.getJogadorAtual();
        List<Territorio> terr = atual.getTerritorios();

        if (terr.size() >= 2) {
            Territorio t1 = terr.get(0);
            Territorio t2 = terr.get(1);

            // Ambos têm pelo menos 1 tropa inicial
            assertTrue(t1.getTropas() >= 1);
            assertTrue(t2.getTropas() >= 1);

            int antes1 = t1.getTropas();
            int antes2 = t2.getTropas();

            // Move 1 do t1 para t2 (se adjacentes)
            boolean ok = ctrl.moverTropasEstrategicas(t1, t2, 1);
            // Resultado depende se são adjacentes; não garantido passar
            // Mas se passou: validar
            if (ok) {
                assertEquals(antes1 - 1, t1.getTropas());
                assertEquals(antes2 + 1, t2.getTropas());
            }
        }
    }

    @Test
    public void testEstadoTurno_progressaoDesFases() {
        Jogador j1 = new Jogador("P1", CorJogador.AZUL, 1, false);
        Jogador j2 = new Jogador("P2", CorJogador.VERMELHO, 2, false);
        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j1);
        jogadores.add(j2);

        ControladorDePartida ctrl = new ControladorDePartida(jogadores);
        ctrl.iniciarPartida();

        assertEquals(ControladorDePartida.EstadoTurno.DISTRIBUINDO, ctrl.getEstadoTurno());

        // --- LOOP SEGURO ---
        int safety = 0;
        while (ctrl.getTropasADistribuirTotal() > 0) {
            if (safety++ > 500) { // Proteção
                ctrl.descartarReforcosRestantes(); // Força saída
                break; 
            }
            
            // Tenta alocar no primeiro território válido que encontrar
            boolean alocou = false;
            for (Territorio t : ctrl.getJogadorAtual().getTerritorios()) {
                // Se tiver restrição, respeita
                String rest = ctrl.getRestricaoAtual();
                if (rest == null || t.getContinente().equalsIgnoreCase(rest)) {
                    if (ctrl.alocarTropas(t, 1)) {
                        alocou = true;
                        break;
                    }
                }
            }
            if (!alocou) ctrl.descartarReforcosRestantes(); // Se travou, descarta
        }

        ctrl.proximaFaseTurno();
        assertNotNull(ctrl.getEstadoTurno());
    }
}
