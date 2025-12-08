package trucoarg.pantallas;

import com.badlogic.gdx.Game;
import trucoarg.elementos.Texto;
import trucoarg.network.Client;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import trucoarg.elementos.Imagen;
import trucoarg.network.GameController;
import trucoarg.network.ServerThread;
import trucoarg.ui.Boton;
import trucoarg.utiles.Configuracion;
import trucoarg.utiles.Recursos;
import trucoarg.utiles.Render;

import java.util.ArrayList;

public class PantallaSeleccionPuntos implements Screen, GameController {

    private Imagen fondo;
    private SpriteBatch batch;
    public Texto texto, texto2;
    public int clientesConectados;
    public ServerThread server;
//    private Boton btn15Puntos;
//    private Boton btn30Puntos;

    private final Object gameInstance;

    public PantallaSeleccionPuntos(ServerThread serverThread, Object game) {
        this.server = serverThread;
        this.gameInstance = game;
    }

    @Override
    public void show() {
        fondo = new Imagen(Recursos.FONDODOSJUGADORES);
        fondo.dimensionarImg(Configuracion.ANCHO, Configuracion.ALTO);
        batch = Render.batch;
        texto= new Texto(Recursos.FUENTE_MENU, 50, Color.WHITE, true);
        texto.setPosicion(Configuracion.ALTO/2, (Configuracion.ANCHO- texto.getAncho())/2);
        texto.setTexto("TRUCO EN RED");

        texto2= new Texto(Recursos.FUENTE_MENU, 50, Color.WHITE, true);
        texto2.setPosicion(Configuracion.ALTO/3, (Configuracion.ANCHO- texto.getAncho())/2);
        texto2.setTexto("Clientes conectados: "+ clientesConectados);


//        crearBotones();



//        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
//            @Override
//            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//                float y = Configuracion.ALTO - screenY;
//
//                if (btn15Puntos.fueClickeado(screenX, y)) {
//                    iniciarJuego(15);
//                    return true;
//                }
//
//                if (btn30Puntos.fueClickeado(screenX, y)) {
//                    iniciarJuego(30);
//                    return true;
//                }
//
//                return false;
//            }
//
//            @Override
//            public boolean keyDown(int keycode) {
//                if (keycode == Input.Keys.ESCAPE) {
//                    volverAlMenu();
//                    return true;
//                }
//                return false;
//            }
    //});
    }



//    private void crearBotones() {
//        float btnAncho = 300;
//        float btnAlto = 100;
//        float separacion = 30;
//        float centroY = Configuracion.ALTO / 2f - 50;
//
//        float totalAncho = (btnAncho * 2) + separacion;
//        float inicioX = (Configuracion.ANCHO / 2f) - (totalAncho / 2f);
//
//        Color azulArg = new Color(0.4f, 0.6f, 0.85f, 0.9f);
//        Color amarillo = new Color(1f, 0.8f, 0.2f, 0.9f);
//        Color blanco = Color.WHITE;
//        Color borde = new Color(0.2f, 0.4f, 0.6f, 1f);
//
//        btn15Puntos = new Boton("15 PUNTOS",
//            inicioX,
//            centroY,
//            btnAncho,
//            btnAlto);
//        btn15Puntos.setColor(azulArg, blanco, borde);
//
//        btn30Puntos = new Boton("30 PUNTOS",
//            inicioX + btnAncho + separacion,
//            centroY,
//            btnAncho,
//            btnAlto);
//        btn30Puntos.setColor(amarillo, new Color(0.2f, 0.2f, 0.2f, 1f), borde);
//    }

    private void iniciarJuego(int puntosParaGanar) {
        System.out.println("Iniciando juego a " + puntosParaGanar + " puntos");
        dispose();
        Render.app.setScreen(new PantallaDosJugadores(puntosParaGanar, server));
    }

    // 🆕 Método para volver al menú
    private void volverAlMenu() {
        System.out.println("Volviendo al menú principal...");
        dispose(); // Limpiar recursos antes de cambiar
        Render.app.setScreen(new PantallaMenu());
    }

    @Override
    public void render(float delta) {
        Render.limpiarPantalla(0.1f, 0.1f, 0.15f);
        ArrayList<Client> clientes = server.getClients();
  //      System.out.println(clientes.size());


        batch.begin();
        fondo.dibujar();
        texto.dibujar();
        texto2.setTexto("Clientes conectados: "+ clientes.size());
        texto2.dibujar();



        batch.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (fondo != null) fondo.dispose();
        server.terminate();
    }

    @Override
    public void startGame() {

    }

    @Override
    public void setearPuntosIniciales(int puntos) {
        System.out.println("🎯 Seteando puntos iniciales: " + puntos);

        // ✅ Crear la pantalla del juego
        PantallaDosJugadores pantallaJuego = new PantallaDosJugadores(puntos, server);

        // ✅✅✅ CRÍTICO: Actualizar el gameController del servidor ✅✅✅
        server.gameController = pantallaJuego;
        System.out.println("✅ GameController actualizado a PantallaDosJugadores");

        // ✅ Enviar mensaje a los clientes
        server.sendMessageToAll("Iniciar_Partida:" + puntos);

        // ✅ Cambiar la pantalla
        dispose();
        Render.app.setScreen(pantallaJuego);
    }

    @Override
    public void procesarJugada(int jugador, int idCarta) {

    }

    @Override
    public boolean cantarTruco(int jugador, String tipoCanto) {
        return false;
    }

    @Override
    public boolean cantarEnvido(int jugador, String tipoEnvido) {
        return false;
    }

    @Override
    public int responderCanto(int jugador, boolean quiero) {
        return 0;
    }

    @Override
    public void irAlMazo(int jugador) {

    }

    @Override
    public int getPuntosJ1() {
        return 0;
    }

    @Override
    public int getPuntosJ2() {
        return 0;
    }
}
