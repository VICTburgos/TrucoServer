package trucoarg.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import com.sun.management.internal.GarbageCollectorExtImpl;
import trucoarg.elementos.Imagen;
import trucoarg.network.GameController;
import trucoarg.network.ServerThread;
import trucoarg.personajesDosJugadores.JuegoTruco;
import trucoarg.personajesDosJugadores.JugadorBase;
import trucoarg.personajesSolitario.CartaSolitario;
import trucoarg.ui.Boton;
import trucoarg.ui.EntradaDosJugadores;
import trucoarg.utiles.Configuracion;
import trucoarg.utiles.Recursos;
import trucoarg.utiles.Render;

import java.util.ArrayList;
import java.util.List;

public class PantallaDosJugadores implements Screen, GameController {

    private Imagen fondo;
    private SpriteBatch batch;
    public ServerThread server;
    private JuegoTruco juego;
    private JugadorBase jugador1;
    private JugadorBase jugador2;

    private final List<CartaSolitario> jugadasJ1 = new ArrayList<>();
    private final List<CartaSolitario> jugadasJ2 = new ArrayList<>();

    private int puntosParaGanar = 15;

    private final Vector2[] posicionesJugadasJ1 = new Vector2[3];
    private final Vector2[] posicionesJugadasJ2 = new Vector2[3];

    private Boton btnTruco;
    private Boton btnRetruco;
    private Boton btnValeCuatro;

    private Boton btnEnvido;
    private Boton btnRealEnvido;
    private Boton btnFaltaEnvido;

    private Boton btnQuiero;
    private Boton btnNoQuiero;

    private Boton btnIrAlMazo;

    private BitmapFont fuente;
    private BitmapFont fuenteVictoria;
    private BitmapFont fuenteCanto;

    private String tipoCantoPendiente = null;

    private String mensajeTemporal = "";
    private float tiempoMensajeTemporal = 0f;
    private static final float DURACION_MENSAJE_TEMPORAL = 4f;

    private boolean juegoTerminado = false;
    private float tiempoVictoria = 0f;
    private static final float TIEMPO_MOSTRAR_VICTORIA = 3f;

    public PantallaDosJugadores(int puntosParaGanar, ServerThread server) {
        this.server = server;
        this.puntosParaGanar = puntosParaGanar;

        juego = new JuegoTruco(puntosParaGanar, server);
        jugador1 = juego.getJugador1();
        jugador2 = juego.getJugador2();

        System.out.println("🎮 PantallaDosJugadores creada - Juego inicializado");
    }


    @Override
    public void show() {
        System.out.println("🎮 PantallaDosJugadores.show() llamado");

        fondo = new Imagen(Recursos.FONDODOSJUGADORES);
        fondo.dimensionarImg(Configuracion.ANCHO, Configuracion.ALTO);
        batch = Render.batch;

        // ❌ NO crear juego aquí - ya se creó en el constructor
        // juego = new JuegoTruco(puntosParaGanar, server);

        configurarPosicionesMesa();
        crearBotones();
        posicionarCartasJugadorAbajo(jugador1.getMano());
        posicionarCartasJugadorArriba(jugador2.getMano());

        fuente = new BitmapFont();
        fuente.getData().setScale(2f);
        fuente.setColor(Color.WHITE);

        fuenteVictoria = new BitmapFont();
        fuenteVictoria.getData().setScale(4f);
        fuenteVictoria.setColor(Color.YELLOW);

        fuenteCanto = new BitmapFont();
        fuenteCanto.getData().setScale(5f);
        fuenteCanto.setColor(new Color(1f, 0.8f, 0.2f, 1f));

        actualizarEstadoBotones();

        Gdx.input.setInputProcessor(new EntradaDosJugadores(
            jugador1.getMano(),
            jugador2.getMano(),
            this
        ));

    }

    private void crearBotones() {
        float btnAncho = 150;
        float btnAlto = 50;
        float margen = 20;
        float separacion = 10;

        Color azulArg = new Color(0.4f, 0.6f, 0.85f, 0.9f);
        Color violeta = new Color(0.6f, 0.3f, 0.8f, 0.9f);
        Color blanco = Color.WHITE;
        Color borde = new Color(0.2f, 0.4f, 0.6f, 1f);
        Color verde = new Color(0.2f, 0.7f, 0.3f, 0.9f);
        Color rojo = new Color(0.8f, 0.2f, 0.2f, 0.9f);
        Color naranja = new Color(0.9f, 0.5f, 0.1f, 0.9f); // Para "Ir al Mazo"

        float trucoPosY = Configuracion.ALTO / 2f + 100;
        btnTruco = new Boton("TRUCO", margen, trucoPosY, btnAncho, btnAlto);
        btnRetruco = new Boton("RETRUCO", margen, trucoPosY - btnAlto - separacion, btnAncho, btnAlto);
        btnValeCuatro = new Boton("VALE 4", margen, trucoPosY - (btnAlto + separacion) * 2, btnAncho, btnAlto);

        btnTruco.setColor(azulArg, blanco, borde);
        btnRetruco.setColor(azulArg, blanco, borde);
        btnValeCuatro.setColor(azulArg, blanco, borde);

        float envidoPosY = Configuracion.ALTO / 2f - 50;
        btnEnvido = new Boton("ENVIDO", margen, envidoPosY, btnAncho, btnAlto);
        btnRealEnvido = new Boton("REAL ENVIDO", margen, envidoPosY - btnAlto - separacion, btnAncho, btnAlto);
        btnFaltaEnvido = new Boton("FALTA ENVIDO", margen, envidoPosY - (btnAlto + separacion) * 2, btnAncho, btnAlto);

        btnEnvido.setColor(violeta, blanco, borde);
        btnRealEnvido.setColor(violeta, blanco, borde);
        btnFaltaEnvido.setColor(violeta, blanco, borde);

        float respuestaPosY = Configuracion.ALTO / 2f + 50;
        btnQuiero = new Boton("QUIERO", Configuracion.ANCHO - btnAncho - margen, respuestaPosY, btnAncho, btnAlto);
        btnNoQuiero = new Boton("NO QUIERO", Configuracion.ANCHO - btnAncho - margen, respuestaPosY - btnAlto - separacion, btnAncho, btnAlto);

        btnQuiero.setColor(verde, blanco, borde);
        btnNoQuiero.setColor(rojo, blanco, borde);

        // 🆕 BOTÓN IR AL MAZO - Abajo a la izquierda
        float mazoPosY = 100;
        btnIrAlMazo = new Boton("IR AL MAZO", margen, mazoPosY, btnAncho, btnAlto);
        btnIrAlMazo.setColor(naranja, blanco, borde);
    }

    private void mostrarMensajeTemporal(String mensaje) {
        mensajeTemporal = mensaje;
        tiempoMensajeTemporal = DURACION_MENSAJE_TEMPORAL;
    }

    private void actualizarEstadoBotones() {
        if (juegoTerminado) {
            ocultarTodosLosBotones();
            return;
        }

        boolean hayTrucoPendiente = juego.getGestorTruco().estaEsperandoRespuesta();
        boolean hayEnvidoPendiente = juego.getGestorEnvido().estaEsperandoRespuesta();

        if (hayTrucoPendiente || hayEnvidoPendiente) {
            int jugadorResponde = juego.getJugadorQueDebeResponder();

            if (hayTrucoPendiente) {
                tipoCantoPendiente = "truco";
                String cantoActual = juego.getGestorTruco().getCantoActual();

                btnTruco.setVisible(false);
                btnRetruco.setVisible(false);
                btnValeCuatro.setVisible(false);
                btnEnvido.setVisible(false);
                btnRealEnvido.setVisible(false);
                btnFaltaEnvido.setVisible(false);
                btnIrAlMazo.setVisible(false); // 🆕

                btnQuiero.setVisible(true);
                btnNoQuiero.setVisible(true);
                btnQuiero.setHabilitado(true);
                btnNoQuiero.setHabilitado(true);

            } else {
                tipoCantoPendiente = "envido";
                String cantoActual = juego.getGestorEnvido().getCantoActual();

                btnTruco.setVisible(false);
                btnRetruco.setVisible(false);
                btnValeCuatro.setVisible(false);
                btnIrAlMazo.setVisible(false); // 🆕

                btnQuiero.setVisible(true);
                btnNoQuiero.setVisible(true);
                btnQuiero.setHabilitado(true);
                btnNoQuiero.setHabilitado(true);

                boolean puedeSubirEnvido = juego.getGestorEnvido().puedeSubirConEnvido();
                boolean puedeSubirReal = juego.getGestorEnvido().puedeSubirConRealEnvido();
                boolean puedeSubirFalta = juego.getGestorEnvido().puedeSubirConFaltaEnvido();

                btnEnvido.setVisible(puedeSubirEnvido);
                btnEnvido.setHabilitado(puedeSubirEnvido);

                btnRealEnvido.setVisible(puedeSubirReal);
                btnRealEnvido.setHabilitado(puedeSubirReal);

                btnFaltaEnvido.setVisible(puedeSubirFalta);
                btnFaltaEnvido.setHabilitado(puedeSubirFalta);
            }

        } else {
            tipoCantoPendiente = null;

            btnQuiero.setVisible(false);
            btnNoQuiero.setVisible(false);

            boolean manoTerminada = juego.isManoTerminada();
            int tiradaActual = juego.getTiradaActual();

            btnTruco.setVisible(!manoTerminada);
            btnRetruco.setVisible(!manoTerminada);
            btnValeCuatro.setVisible(!manoTerminada);
            btnIrAlMazo.setVisible(!manoTerminada); // 🆕 Mostrar cuando no hay canto pendiente

            btnTruco.setHabilitado(!manoTerminada);
            btnRetruco.setHabilitado(!manoTerminada && juego.getGestorTruco().isCantoAceptado());
            btnValeCuatro.setHabilitado(!manoTerminada && juego.getGestorTruco().isCantoAceptado());
            btnIrAlMazo.setHabilitado(!manoTerminada); // 🆕 Siempre habilitado

            boolean puedeEnvido = !manoTerminada && tiradaActual == 1 && !juego.isEnvidoYaResuelto();

            btnEnvido.setVisible(puedeEnvido);
            btnRealEnvido.setVisible(puedeEnvido);
            btnFaltaEnvido.setVisible(puedeEnvido);

            btnEnvido.setHabilitado(puedeEnvido);
            btnRealEnvido.setHabilitado(puedeEnvido);
            btnFaltaEnvido.setHabilitado(puedeEnvido);

        }
    }

    private void ocultarTodosLosBotones() {
        btnTruco.setVisible(false);
        btnRetruco.setVisible(false);
        btnValeCuatro.setVisible(false);
        btnEnvido.setVisible(false);
        btnRealEnvido.setVisible(false);
        btnFaltaEnvido.setVisible(false);
        btnQuiero.setVisible(false);
        btnNoQuiero.setVisible(false);
        btnIrAlMazo.setVisible(false); // 🆕
    }

    private void verificarVictoria() {
        if (juego.hayGanador() && !juegoTerminado) {
            juegoTerminado = true;
            tiempoVictoria = 0f;
            int ganador = juego.getGanadorFinal();
            actualizarEstadoBotones();
        }
    }

    public void jugarCarta(CartaSolitario carta, int jugador) {
        if (juegoTerminado) return;

        boolean ok = juego.jugarCarta(jugador, carta);
        if (!ok) return;

        if (jugador == 1) {
            int idx = jugadasJ1.size();
            carta.setPosicion(posicionesJugadasJ1[idx]);
            jugadasJ1.add(carta);
        } else {
            int idx = jugadasJ2.size();
            carta.setPosicion(posicionesJugadasJ2[idx]);
            jugadasJ2.add(carta);
        }

        if (jugadasJ1.size() == jugadasJ2.size()) {
            juego.procesarTirada();

            if (juego.isManoTerminada()) {
                verificarVictoria();
                if (juegoTerminado) return;

                juego.reiniciarManoSiCorresponde();
                jugador1 = juego.getJugador1();
                jugador2 = juego.getJugador2();

                jugadasJ1.clear();
                jugadasJ2.clear();

                posicionarCartasJugadorAbajo(jugador1.getMano());
                posicionarCartasJugadorArriba(jugador2.getMano());

                actualizarInputProcessor();
            }
        }

        actualizarEstadoBotones();
    }

    public void procesarClickBoton(Boton boton) {
        if (juegoTerminado) return;

        System.out.println("⚠️ Servidor no debe usar botones directamente");
    }

    private void reiniciarManoVisual() {
        juego.reiniciarManoSiCorresponde();
        jugador1 = juego.getJugador1();
        jugador2 = juego.getJugador2();
        jugadasJ1.clear();
        jugadasJ2.clear();
        posicionarCartasJugadorAbajo(jugador1.getMano());
        posicionarCartasJugadorArriba(jugador2.getMano());

        actualizarInputProcessor();
    }

    public Boton[] getBotones() {
        return new Boton[]{
            btnTruco, btnRetruco, btnValeCuatro,
            btnEnvido, btnRealEnvido, btnFaltaEnvido,
            btnQuiero, btnNoQuiero, btnIrAlMazo // 🆕
        };
    }

    private void configurarPosicionesMesa() {
        float cx = Configuracion.ANCHO / 2f;
        float cy = Configuracion.ALTO / 2f;
        posicionesJugadasJ1[0] = new Vector2(cx - 300, cy - 120);
        posicionesJugadasJ1[1] = new Vector2(cx - 50, cy - 120);
        posicionesJugadasJ1[2] = new Vector2(cx + 200, cy - 120);

        posicionesJugadasJ2[0] = new Vector2(cx - 300, cy + 40);
        posicionesJugadasJ2[1] = new Vector2(cx - 50, cy + 40);
        posicionesJugadasJ2[2] = new Vector2(cx + 200, cy + 40);
    }

    private void posicionarCartasJugadorAbajo(List<CartaSolitario> mano) {
        float x = Configuracion.ANCHO / 2f - 300;
        float y = Configuracion.ALTO - 650;
        float dx = 250;

        for (int i = 0; i < mano.size(); i++) {
            CartaSolitario c = mano.get(i);
            c.setSize(100, 200);
            c.setPosicion(new Vector2(x + i * dx, y));
            c.setYaJugadas(false);

        }
    }

    private void posicionarCartasJugadorArriba(List<CartaSolitario> mano) {
        float x = Configuracion.ANCHO / 2f - 300;
        float y = Configuracion.ALTO - 220;
        float dx = 250;

        for (int i = 0; i < mano.size(); i++) {
            CartaSolitario c = mano.get(i);
            c.setSize(100, 200);
            c.setPosicion(new Vector2(x + i * dx, y));
            c.setYaJugadas(false);
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            volverAlMenuConMusica();
            return;
        }

        if (tiempoMensajeTemporal > 0) {
            tiempoMensajeTemporal -= delta;
            if (tiempoMensajeTemporal <= 0) {
                mensajeTemporal = "";
            }
        }

        if (juegoTerminado) {
            tiempoVictoria += delta;
            if (tiempoVictoria >= TIEMPO_MOSTRAR_VICTORIA) {
                volverAlMenuConMusica();
                return;
            }
        }

        Render.limpiarPantalla(0, 0, 0);
        batch.begin();

        fondo.dibujar();

        // ✅✅✅ SERVIDOR: Solo dibujar las cartas que están en las MANOS de los jugadores ✅✅✅
        // NO dibujar jugadasJ1 ni jugadasJ2 porque en el servidor no se actualizan bien

        for (CartaSolitario c : jugador1.getMano()) {
            c.dibujar(batch);
        }

        for (CartaSolitario c : jugador2.getMano()) {
            c.dibujar(batch);
        }

        // ❌ NO dibujar las cartas jugadas en la mesa del servidor
        // for (CartaSolitario c : jugadasJ1) c.dibujar(batch); ❌ COMENTAR ESTO
        // for (CartaSolitario c : jugadasJ2) c.dibujar(batch); ❌ COMENTAR ESTO

        // ✅ SOLO PUNTOS Y INFO
        fuente.draw(batch, "SERVIDOR", Configuracion.ANCHO / 2f - 100, Configuracion.ALTO - 50);
        fuente.draw(batch, "J1: " + juego.getPuntosJ1() + " pts", 50, Configuracion.ALTO - 50);
        fuente.draw(batch, "J2: " + juego.getPuntosJ2() + " pts", 50, Configuracion.ALTO - 100);
        fuente.draw(batch, "Turno: J" + juego.getTurnoActual(), 50, Configuracion.ALTO - 150);
        fuente.draw(batch, "Tirada: " + juego.getTiradaActual() + "/3", 50, Configuracion.ALTO - 200);
        fuente.draw(batch, "Mano: " + (juego.getJugador1().esMano() ? "J1" : "J2"), 50, Configuracion.ALTO - 250);
        fuente.draw(batch, "ESC para salir", 50, 650);

        // ✅ Mensajes temporales
        if (!mensajeTemporal.isEmpty()) {
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(fuenteCanto, mensajeTemporal);
            float anchoTexto = layout.width;
            float altoTexto = layout.height;

            fuenteCanto.draw(batch, mensajeTemporal,
                Configuracion.ANCHO / 2f - anchoTexto / 2f,
                Configuracion.ALTO / 2f + altoTexto / 2f);
        }

        // ✅ Victoria
        if (juegoTerminado) {
            int ganador = juego.getGanadorFinal();
            String msgVictoria = "¡GANÓ JUGADOR " + ganador + "!";
            String msgPuntos = juego.getPuntosJ1() + " - " + juego.getPuntosJ2();

            fuenteVictoria.draw(batch, msgVictoria,
                Configuracion.ANCHO / 2f - 300,
                Configuracion.ALTO / 2f + 50);

            fuente.draw(batch, msgPuntos,
                Configuracion.ANCHO / 2f - 100,
                Configuracion.ALTO / 2f - 20);

            fuente.draw(batch, "Volviendo al menú...",
                Configuracion.ANCHO / 2f - 150,
                Configuracion.ALTO / 2f - 80);
        }



        batch.end();
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

    private void actualizarInputProcessor() {
        System.out.println("Actualizando InputProcessor con nuevas cartas");
        Gdx.input.setInputProcessor(new EntradaDosJugadores(
            jugador1.getMano(),
            jugador2.getMano(),
            this
        ));
    }

    public void enviarEstadoBotonesATodos() {
        enviarEstadoBotones(1);
        enviarEstadoBotones(2);
    }

    // Cambiar de private a public
    public void enviarEstadoBotones(int jugador) {
        String botones = calcularBotonesVisibles(jugador);

        for (trucoarg.network.Client client : server.getClients()) {
            if (client.getNum() == jugador) {
                server.sendMessage("ActualizarBotones:" + botones, client.getIp(), client.getPort());
                System.out.println("📤 Enviado a J" + jugador + ": ActualizarBotones:" + botones);
                break;
            }
        }
    }

    private String calcularBotonesVisibles(int jugador) {
        if (juegoTerminado) {
            return "ninguno";
        }

        // ========== SI HAY TRUCO PENDIENTE ==========
        // ✅ PRIORIDAD 1: Si hay truco pendiente, SOLO mostrar respuestas
        if (juego.getGestorTruco().estaEsperandoRespuesta()) {
            int jugadorQueDebeResponder = juego.getJugadorQueDebeResponder();

            if (jugador == jugadorQueDebeResponder) {
                return "quiero,noquiero";
            } else {
                return "ninguno"; // El otro jugador espera
            }
        }

        // ========== SI HAY ENVIDO PENDIENTE ==========
        // ✅ PRIORIDAD 2: Si hay envido pendiente, SOLO mostrar respuestas o subir
        if (juego.getGestorEnvido().estaEsperandoRespuesta()) {
            int jugadorQueDebeResponder = juego.getJugadorQueDebeResponder();

            if (jugador == jugadorQueDebeResponder) {
                StringBuilder sb = new StringBuilder("quiero,noquiero");

                // Puede subir el envido
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
                return "ninguno"; // El otro jugador espera
            }
        }

        // ========== TURNO NORMAL (sin cantos pendientes) ==========

        int turnoActual = juego.getTurnoActual();

        // ✅ Si NO hay cantos pendientes, validar turno
        if (turnoActual != jugador) {
            return "ninguno";
        }

        boolean manoTerminada = juego.isManoTerminada();
        if (manoTerminada) {
            return "ninguno";
        }

        int tiradaActual = juego.getTiradaActual();
        StringBuilder sb = new StringBuilder();

        // Siempre puede ir al mazo
        sb.append("mazo");

        // Botones de TRUCO
        sb.append(",truco");
        if (juego.getGestorTruco().isCantoAceptado()) {
            sb.append(",retruco,vale4");
        }

        // Botones de ENVIDO (solo en primera tirada y si no está resuelto)
        if (tiradaActual == 1 && !juego.isEnvidoYaResuelto()) {
            sb.append(",envido,real,falta");
        }

        return sb.toString();
    }


    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        fondo.dispose();
        btnTruco.dispose();
        btnRetruco.dispose();
        btnValeCuatro.dispose();
        btnEnvido.dispose();
        btnRealEnvido.dispose();
        btnFaltaEnvido.dispose();
        btnQuiero.dispose();
        btnNoQuiero.dispose();
        btnIrAlMazo.dispose(); // 🆕
        fuente.dispose();
        if (fuenteVictoria != null) fuenteVictoria.dispose();
        if (fuenteCanto != null) fuenteCanto.dispose();
        server.terminate();
    }

    @Override
    public void startGame() {
        System.out.println("🎮 startGame() llamado");
        server.sendMessageToAll("Turno:" + juego.getTurnoActual());

        // 🆕 Enviar estado de botones
        enviarEstadoBotonesATodos();
    }

    @Override
    public void setearPuntosIniciales(int puntos) {
        System.out.println("🎯 Seteando puntos iniciales: " + puntos);
        this.puntosParaGanar = puntos;

        juego = new JuegoTruco(puntos, server);
        jugador1 = juego.getJugador1();
        jugador2 = juego.getJugador2();

        server.sendMessageToAll("Iniciar_Partida:" + puntos);
        server.sendMessageToAll("Turno:" + juego.getTurnoActual());

        // 🆕 Enviar estado de botones
        enviarEstadoBotonesATodos();
    }

    @Override
    public void procesarJugada(int jugador, int idCarta) {
        System.out.println("\n🎮 ========== SERVIDOR PROCESA JUGADA ==========");
        System.out.println("   Jugador: J" + jugador);
        System.out.println("   Carta ID: " + idCarta);
        System.out.println("   Turno actual: J" + juego.getTurnoActual());
        System.out.println("   Tirada actual: " + juego.getTiradaActual());

        // ✅ Validar que sea el turno correcto
        if (!juego.puedeJugar(jugador)) {
            System.out.println("❌ RECHAZADO: No es el turno del jugador " + jugador);
            return;
        }

        // ✅ Buscar la carta en la mano del jugador
        JugadorBase jugadorActual = (jugador == 1) ? jugador1 : jugador2;
        CartaSolitario cartaAJugar = null;

        for (CartaSolitario c : jugadorActual.getMano()) {
            if (c.getId() == idCarta) {
                cartaAJugar = c;
                break;
            }
        }

        if (cartaAJugar == null) {
            System.out.println("❌ RECHAZADO: Carta no encontrada");
            return;
        }

        if (cartaAJugar.getYaJugadas()) {
            System.out.println("❌ RECHAZADO: Carta ya fue jugada");
            return;
        }

        System.out.println("✅ VALIDACIÓN EXITOSA");

        // ✅ Enviar a AMBOS clientes que la carta fue jugada
        server.sendMessageToAll("CartaJugada:" + jugador + ":" + idCarta);
        System.out.println("📤 Enviado a clientes: CartaJugada:" + jugador + ":" + idCarta);

        // ✅ Procesar la jugada en el servidor (SOLO LÓGICA)
        boolean jugadaExitosa = juego.jugarCarta(jugador, cartaAJugar);

        if (jugadaExitosa) {
            System.out.println("✅ Jugada procesada en JuegoTruco");

            // ❌ NO mover cartas visualmente en el servidor
            // Las listas jugadasJ1 y jugadasJ2 NO se usan en el servidor
            // Solo se usan para CONTAR cuántas cartas se jugaron

            // ✅ Contar cartas jugadas (sin moverlas visualmente)
            int cartasJugadasJ1 = 0;
            int cartasJugadasJ2 = 0;

            for (CartaSolitario c : jugador1.getMano()) {
                if (c.getYaJugadas()) cartasJugadasJ1++;
            }

            for (CartaSolitario c : jugador2.getMano()) {
                if (c.getYaJugadas()) cartasJugadasJ2++;
            }

            System.out.println("   Cartas jugadas J1: " + cartasJugadasJ1);
            System.out.println("   Cartas jugadas J2: " + cartasJugadasJ2);

            // ✅ Si ambos jugaron, procesar tirada
            if (cartasJugadasJ1 == cartasJugadasJ2 &&
                cartasJugadasJ1 == juego.getTiradaActual()) {

                System.out.println("\n🎲 Ambos jugaron - Procesando tirada " + juego.getTiradaActual());

                int resultadoTirada = juego.procesarTirada();
                System.out.println("📊 Resultado de tirada: " + resultadoTirada);

                // ✅ Enviar puntos actualizados
                server.sendMessageToAll("Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());
                System.out.println("📤 Enviado: Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());

                // ✅ Verificar si la mano terminó
                if (juego.isManoTerminada()) {
                    System.out.println("🏁 Mano terminada");

                    if (juego.hayGanador()) {
                        int ganador = juego.getGanadorFinal();
                        System.out.println("🏆 ¡GANADOR: J" + ganador + "!");
                        server.sendMessageToAll("Victoria:" + ganador);
                    } else {
                        System.out.println("🔄 Iniciando nueva mano");

                        server.sendMessageToAll("NuevaMano");
                        System.out.println("📤 Enviado: NuevaMano");

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        juego.reiniciarManoSiCorresponde();
                        jugador1 = juego.getJugador1();
                        jugador2 = juego.getJugador2();

                        server.sendMessageToAll("Turno:" + juego.getTurnoActual());
                        System.out.println("📤 Enviado: Turno:" + juego.getTurnoActual());
                    }
                } else {
                    System.out.println("➡️ Continúa la mano - Tirada " + juego.getTiradaActual());

                    int nuevoTurno = juego.getTurnoActual();
                    server.sendMessageToAll("Turno:" + nuevoTurno);
                    System.out.println("📤 Enviado: Turno:" + nuevoTurno);
                }
            } else {
                // Solo un jugador jugó
                int nuevoTurno = juego.getTurnoActual();
                server.sendMessageToAll("Turno:" + nuevoTurno);
                System.out.println("📤 Enviado: Turno:" + nuevoTurno);
            }
        }

        System.out.println("========================================\n");
        enviarEstadoBotonesATodos();

    }


    @Override
    public boolean cantarTruco(int jugador, String tipoCanto) {
        if (juegoTerminado) return false;

        boolean exito = juego.cantar(jugador, tipoCanto);

        if (exito) {
            mostrarMensajeTemporal("J" + jugador + " canta " + tipoCanto.toUpperCase());

        }

        return exito;
    }

    @Override
    public boolean cantarEnvido(int jugador, String tipoEnvido) {
        if (juegoTerminado) return false;

        boolean exito = juego.cantarEnvido(jugador, tipoEnvido);

        if (exito) {
            mostrarMensajeTemporal("J" + jugador + " canta " + tipoEnvido.toUpperCase());

        }

        return exito;
    }

    @Override
    public int responderCanto(int jugador, boolean quiero) {
        System.out.println("\n💬 ========== PROCESANDO RESPUESTA A CANTO ==========");
        System.out.println("   Jugador: J" + jugador);
        System.out.println("   Respuesta: " + (quiero ? "QUIERO" : "NO QUIERO"));

        int resultado = -1;

        // Determinar si es truco o envido
        if (juego.getGestorTruco().estaEsperandoRespuesta()) {
            System.out.println("   Tipo: TRUCO");
            resultado = juego.responderCanto(jugador, quiero);

            if (resultado > 0) {
                // ✅ NO QUIERO - Alguien ganó la mano
                System.out.println("   ❌ NO QUIERO - Ganador: J" + resultado);
                mostrarMensajeTemporal("J" + jugador + " dice NO QUIERO");

                // ✅ Enviar puntos actualizados
                server.sendMessageToAll("Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());

                verificarVictoria();
                if (juegoTerminado) {
                    int ganadorFinal = juego.getGanadorFinal();
                    server.sendMessageToAll("Victoria:" + ganadorFinal);
                    return resultado;
                }

                // ✅ Nueva mano
                server.sendMessageToAll("NuevaMano");

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 🆕✅ Reiniciar y repartir nuevas cartas
                juego.reiniciarManoSiCorresponde();
                jugador1 = juego.getJugador1();
                jugador2 = juego.getJugador2();


                server.sendMessageToAll("Turno:" + juego.getTurnoActual());

                jugadasJ1.clear();
                jugadasJ2.clear();
                posicionarCartasJugadorAbajo(jugador1.getMano());
                posicionarCartasJugadorArriba(jugador2.getMano());
                actualizarInputProcessor();

            } else if (resultado == 0) {
                // ✅ QUIERO - Continúa el juego
                System.out.println("   ✅ QUIERO - Continúa el juego");
                mostrarMensajeTemporal("J" + jugador + " dice QUIERO");
            }

        } else if (juego.getGestorEnvido().estaEsperandoRespuesta()) {
            System.out.println("   Tipo: ENVIDO");
            resultado = juego.responderEnvido(jugador, quiero);

            // ✅ Enviar puntos actualizados
            server.sendMessageToAll("Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());

            if (resultado > 0) {
                // ✅ NO QUIERO
                System.out.println("   ❌ NO QUIERO - Ganador: J" + resultado);
                mostrarMensajeTemporal("J" + jugador + " dice NO QUIERO");
            } else if (resultado == 0) {
                // ✅ QUIERO
                System.out.println("   ✅ QUIERO ENVIDO");
                mostrarMensajeTemporal("J" + jugador + " dice QUIERO ENVIDO");
            }

            verificarVictoria();
            if (juegoTerminado) {
                int ganadorFinal = juego.getGanadorFinal();
                server.sendMessageToAll("Victoria:" + ganadorFinal);
                return resultado;
            }
        }

        enviarEstadoBotonesATodos();
        System.out.println("   Resultado: " + resultado);
        System.out.println("===================================================\n");

        return resultado;
    }

    @Override
    public void irAlMazo(int jugador) {
        System.out.println("\n🃏 ========== PROCESANDO IR AL MAZO ==========");
        System.out.println("   Jugador: J" + jugador);

        mostrarMensajeTemporal("¡Jugador " + jugador + " se va al mazo!");

        // ✅ Terminar la mano
        juego.terminarManoAlMazo();

        // ✅ Calcular ganador y puntos
        int ganador = (jugador == 1) ? 2 : 1;
        int puntosTruco = juego.getGestorTruco().getPuntos();

        System.out.println("   Ganador: J" + ganador);
        System.out.println("   Puntos de truco: " + puntosTruco);

        // ✅ Agregar puntos
        if (ganador == 1) {
            juego.agregarPuntosJ1(puntosTruco);
        } else {
            juego.agregarPuntosJ2(puntosTruco);
        }

        // ✅ Enviar puntos actualizados a los clientes
        server.sendMessageToAll("Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());
        System.out.println("📤 Enviado: Puntos:" + juego.getPuntosJ1() + ":" + juego.getPuntosJ2());

        // ✅ Verificar si hay victoria
        verificarVictoria();
        if (juegoTerminado) {
            int ganadorFinal = juego.getGanadorFinal();
            server.sendMessageToAll("Victoria:" + ganadorFinal);
            System.out.println("📤 Enviado: Victoria:" + ganadorFinal);
            return;
        }

        // ✅ Si no hay victoria, iniciar nueva mano
        System.out.println("🔄 Iniciando nueva mano después de ir al mazo");

        // ✅ Limpiar la mesa en los clientes
        server.sendMessageToAll("NuevaMano");
        System.out.println("📤 Enviado: NuevaMano");

        try {
            Thread.sleep(200); // Esperar a que los clientes limpien
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // ✅ Reiniciar la mano en el servidor
        // ⚡ ESTO YA ENVÍA LAS CARTAS automáticamente en el constructor de JugadorBase
        juego.reiniciarManoSiCorresponde();
        jugador1 = juego.getJugador1();
        jugador2 = juego.getJugador2();

        // ❌ ELIMINAR ESTA LÍNEA - Las cartas ya se enviaron automáticamente
        // repartirCartasAClientes(); // ❌ DUPLICADO

        // ✅ Enviar turno
        server.sendMessageToAll("Turno:" + juego.getTurnoActual());
        System.out.println("📤 Enviado: Turno:" + juego.getTurnoActual());

        // ✅ Actualizar estado visual del servidor
        jugadasJ1.clear();
        jugadasJ2.clear();
        posicionarCartasJugadorAbajo(jugador1.getMano());
        posicionarCartasJugadorArriba(jugador2.getMano());
        actualizarInputProcessor();
        enviarEstadoBotonesATodos();

        System.out.println("✅ Nueva mano iniciada correctamente");
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
