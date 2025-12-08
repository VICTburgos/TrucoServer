package trucoarg.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import trucoarg.network.GameController;
import trucoarg.network.ServerThread;
import trucoarg.personajesDosJugadores.JuegoTruco;
import trucoarg.personajesDosJugadores.JugadorBase;
import trucoarg.personajesSolitario.CartaSolitario;
import trucoarg.utiles.Recursos;
import trucoarg.utiles.Render;

public class PantallaDosJugadores implements Screen, GameController {

    public ServerThread server;
    private JuegoTruco juego;
    private JugadorBase jugador1;
    private JugadorBase jugador2;

    private int puntosParaGanar = 15;
    private boolean juegoTerminado = false;
    private float tiempoVictoria = 0f;
    private static final float TIEMPO_MOSTRAR_VICTORIA = 3f;

    public PantallaDosJugadores(int puntosParaGanar, ServerThread server) {
        this.server = server;
        this.puntosParaGanar = puntosParaGanar;

        juego = new JuegoTruco(puntosParaGanar, server);
        jugador1 = juego.getJugador1();
        jugador2 = juego.getJugador2();

        System.out.println(" PantallaDosJugadores creada - Juego inicializado");
    }

    @Override
    public void show() {
        System.out.println(" PantallaDosJugadores.show() llamado - SERVIDOR SOLO LÓGICA");
    }

    private void verificarVictoria() {
        if (juego.hayGanador() && !juegoTerminado) {
            juegoTerminado = true;
            tiempoVictoria = 0f;
            int ganador = juego.getGanadorFinal();
            System.out.println("🏆 VICTORIA: J" + ganador);
        }
    }

    @Override
    public void render(float delta) {
        //  SOLO LÓGICA - Sin renderizado
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            volverAlMenuConMusica();
            return;
        }

        if (juegoTerminado) {
            tiempoVictoria += delta;
            if (tiempoVictoria >= TIEMPO_MOSTRAR_VICTORIA) {
                volverAlMenuConMusica();
                return;
            }
        }
    }

    private void volverAlMenuConMusica() {
        if (Recursos.MUSICA_JUEGO != null) {
            Recursos.MUSICA_JUEGO.stop();
            Recursos.MUSICA_JUEGO.setPosition(0);
        }

        if (Recursos.MUSICA_GENERAL != null) {
            Recursos.MUSICA_GENERAL.play();
        }

        dispose();
        Render.app.setScreen(new PantallaMenu());
    }

    public void enviarEstadoBotonesATodos() {
        enviarEstadoBotones(1);
        enviarEstadoBotones(2);
    }

    public void enviarEstadoBotones(int jugador) {
        String botones = calcularBotonesVisibles(jugador);

        for (trucoarg.network.Client client : server.getClients()) {
            if (client.getNum() == jugador) {
                server.sendMessage("ActualizarBotones:" + botones, client.getIp(), client.getPort());
                System.out.println(" Enviado a J" + jugador + ": ActualizarBotones:" + botones);
                break;
            }
        }
    }

    private String calcularBotonesVisibles(int jugador) {
        if (juegoTerminado) {
            return "ninguno";
        }

        // ========== PRIORIDAD 1: TRUCO PENDIENTE ==========
        if (juego.getGestorTruco().estaEsperandoRespuesta()) {
            int jugadorQueDebeResponder = juego.getJugadorQueDebeResponder();

            if (jugador == jugadorQueDebeResponder) {
                StringBuilder sb = new StringBuilder("quiero,noquiero");

                //  Puede subir el truco mientras responde
                String cantoActual = juego.getGestorTruco().getCantoActual();

                if (cantoActual.equals("truco")) {
                    sb.append(",retruco");
                } else if (cantoActual.equals("retruco")) {
                    sb.append(",vale4");
                }
                // Si es VALE 4, no hay más para subir

                return sb.toString();
            } else {
                return "ninguno"; // El otro jugador espera
            }
        }

        // ========== PRIORIDAD 2: ENVIDO PENDIENTE ==========
        if (juego.getGestorEnvido().estaEsperandoRespuesta()) {
            int jugadorQueDebeResponder = juego.getJugadorQueDebeResponder();

            if (jugador == jugadorQueDebeResponder) {
                StringBuilder sb = new StringBuilder("quiero,noquiero");

                if (juego.getGestorEnvido().puedeSubirConEnvido()) {
                    sb.append(",envido");
                }
                if (juego.getGestorEnvido().puedeSubirConRealEnvido()) {
                    sb.append(",real");
                }
                if (juego.getGestorEnvido().puedeSubirConFaltaEnvido()) {
                    sb.append(",falta");
                }

                return sb.toString();
            } else {
                return "ninguno";
            }
        }

        // ========== TURNO NORMAL (sin cantos pendientes) ==========
        int turnoActual = juego.getTurnoActual();

        if (turnoActual != jugador) {
            return "ninguno";
        }

        boolean manoTerminada = juego.isManoTerminada();
        if (manoTerminada) {
            return "ninguno";
        }

        int tiradaActual = juego.getTiradaActual();
        StringBuilder sb = new StringBuilder();

        sb.append("mazo");

        // Botones de TRUCO
        String cantoActual = juego.getGestorTruco().getCantoActual();
        if (cantoActual == null || cantoActual.isEmpty()) {
            sb.append(",truco");
        } else if (cantoActual.equals("truco") && juego.getGestorTruco().isCantoAceptado()) {
            sb.append(",retruco");
        } else if (cantoActual.equals("retruco") && juego.getGestorTruco().isCantoAceptado()) {
            sb.append(",vale4");
        }

        // Botones de ENVIDO (solo en primera tirada)
        if (tiradaActual == 1 && !juego.isEnvidoYaResuelto()) {
            sb.append(",envido,real,falta");
        }

        return sb.toString();
    }

    public void cerrarServidor() {
        if (server != null) {
            System.out.println("Cerrando Servidor");
            server.disconnectClients();
            server.terminate();
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        cerrarServidor();
    }

    @Override
    public void startGame() {
        System.out.println(" startGame() llamado");
        server.sendMessageToAll("Turno:" + juego.getTurnoActual());
        enviarEstadoBotonesATodos();
    }

    @Override
    public void setearPuntosIniciales(int puntos) {
        System.out.println(" Seteando puntos iniciales: " + puntos);
        this.puntosParaGanar = puntos;

        juego = new JuegoTruco(puntos, server);
        jugador1 = juego.getJugador1();
        jugador2 = juego.getJugador2();

        server.sendMessageToAll("Iniciar_Partida:" + puntos);
        server.sendMessageToAll("Turno:" + juego.getTurnoActual());
        enviarEstadoBotonesATodos();
    }

    @Override
    public void procesarJugada(int jugador, int idCarta) {
        System.out.println("\n ========== SERVIDOR PROCESA JUGADA ==========");
        System.out.println("   Jugador: J" + jugador);
        System.out.println("   Carta ID: " + idCarta);

        if (!juego.puedeJugar(jugador)) {
            System.out.println(" RECHAZADO: No es el turno del jugador " + jugador);
            return;
        }

        JugadorBase jugadorActual = (jugador == 1) ? jugador1 : jugador2;
        CartaSolitario cartaAJugar = null;

        for (CartaSolitario c : jugadorActual.getMano()) {
            if (c.getId() == idCarta) {
                cartaAJugar = c;
                break;
            }
        }

        if (cartaAJugar == null || cartaAJugar.getYaJugadas()) {
            System.out.println(" RECHAZADO: Carta no válida");
            return;
        }


        server.sendMessageToAll("CartaJugada:" + jugador + ":" + idCarta);
        boolean jugadaExitosa = juego.jugarCarta(jugador, cartaAJugar);


        if (jugadaExitosa) {
            int cartasJugadasJ1 = 0;
            int cartasJugadasJ2 = 0;

            for (CartaSolitario c : jugador1.getMano()) {
                if (c.getYaJugadas()) cartasJugadasJ1++;
            }

            for (CartaSolitario c : jugador2.getMano()) {
                if (c.getYaJugadas()) cartasJugadasJ2++;
            }

            if (cartasJugadasJ1 == cartasJugadasJ2 && cartasJugadasJ1 == juego.getTiradaActual()) {
                System.out.println("\n🎲 Ambos jugaron - Procesando tirada");

                juego.procesarTirada();

                // ✅ ENVIAR PUNTOS ACTUALIZADOS
                server.sendMessageToAll("Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());

                verificarVictoria();
                if (juegoTerminado) {
                    int ganador = juego.getGanadorFinal();
                    server.sendMessageToAll("Victoria:" + ganador);
                    return; // ← SALIR INMEDIATAMENTE, NO ENVIAR MÁS MENSAJES
                }

                if (juego.isManoTerminada()) {
                    System.out.println(" Mano terminada - Iniciando nueva mano");
                    server.sendMessageToAll("NuevaMano");

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    juego.reiniciarManoSiCorresponde();
                    jugador1 = juego.getJugador1();
                    jugador2 = juego.getJugador2();

                    server.sendMessageToAll("Turno:" + juego.getTurnoActual());
                } else {
                    System.out.println("➡ Mano continúa - Siguiente tirada");
                    server.sendMessageToAll("Turno:" + juego.getTurnoActual());
                }
            } else {
                server.sendMessageToAll("Turno:" + juego.getTurnoActual());
            }
        }

        enviarEstadoBotonesATodos();
        System.out.println("========================================\n");
    }
    @Override
    public boolean cantarTruco(int jugador, String tipoCanto) {
        if (juegoTerminado) return false;

        boolean exito = juego.cantar(jugador, tipoCanto);

        if (exito) {
            System.out.println(" J" + jugador + " canta " + tipoCanto.toUpperCase());
        }

        return exito;
    }

    @Override
    public boolean cantarEnvido(int jugador, String tipoEnvido) {
        if (juegoTerminado) return false;

        boolean exito = juego.cantarEnvido(jugador, tipoEnvido);

        if (exito) {
            System.out.println(" J" + jugador + " canta " + tipoEnvido.toUpperCase());
        }

        return exito;
    }

    @Override
    public int responderCanto(int jugador, boolean quiero) {
        System.out.println("\n ========== PROCESANDO RESPUESTA A CANTO ==========");
        System.out.println("   Jugador: J" + jugador);
        System.out.println("   Respuesta: " + (quiero ? "QUIERO" : "NO QUIERO"));

        int resultado = -1;

        if (juego.getGestorTruco().estaEsperandoRespuesta()) {
            System.out.println("   Tipo: TRUCO");
            resultado = juego.responderCanto(jugador, quiero);

            if (resultado > 0) {
                System.out.println("    NO QUIERO - Ganador: J" + resultado);
                server.sendMessageToAll("Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());

                verificarVictoria();
                if (juegoTerminado) {
                    int ganadorFinal = juego.getGanadorFinal();
                    server.sendMessageToAll("Victoria:" + ganadorFinal);
                    return resultado;
                }

                server.sendMessageToAll("NuevaMano");

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                juego.reiniciarManoSiCorresponde();
                jugador1 = juego.getJugador1();
                jugador2 = juego.getJugador2();

                server.sendMessageToAll("Turno:" + juego.getTurnoActual());

            } else if (resultado == 0) {
                System.out.println("    QUIERO - Continúa el juego");
            }

        } else if (juego.getGestorEnvido().estaEsperandoRespuesta()) {
            System.out.println("   Tipo: ENVIDO");
            resultado = juego.responderEnvido(jugador, quiero);

            server.sendMessageToAll("Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());

            if (resultado > 0) {
                System.out.println("    NO QUIERO - Ganador: J" + resultado);
            } else if (resultado == 0) {
                System.out.println("    QUIERO ENVIDO");
            }

            verificarVictoria();
            if (juegoTerminado) {
                int ganadorFinal = juego.getGanadorFinal();
                server.sendMessageToAll("Victoria:" + ganadorFinal);
                return resultado;
            }
        }

        enviarEstadoBotonesATodos();
        System.out.println("===================================================\n");

        return resultado;
    }

    @Override
    public void irAlMazo(int jugador) {
        System.out.println("\n ========== PROCESANDO IR AL MAZO ==========");
        System.out.println("   Jugador: J" + jugador);

        juego.terminarManoAlMazo();

        int ganador = (jugador == 1) ? 2 : 1;
        int puntosTruco = juego.getGestorTruco().getPuntos();

        if (ganador == 1) {
            juego.agregarPuntosJ1(puntosTruco);
        } else {
            juego.agregarPuntosJ2(puntosTruco);
        }

        server.sendMessageToAll("Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());

        verificarVictoria();
        if (juegoTerminado) {
            int ganadorFinal = juego.getGanadorFinal();
            server.sendMessageToAll("Victoria:" + ganadorFinal);
            return;
        }

        server.sendMessageToAll("NuevaMano");

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        juego.reiniciarManoSiCorresponde();
        jugador1 = juego.getJugador1();
        jugador2 = juego.getJugador2();

        server.sendMessageToAll("Turno:" + juego.getTurnoActual());
        enviarEstadoBotonesATodos();

        System.out.println("=======================================\n");
    }

    @Override
    public int getPuntosJ1() {
        return juego.getPuntosJ1();
    }

    @Override
    public int getPuntosJ2() {
        return juego.getPuntosJ2();
    }
}
