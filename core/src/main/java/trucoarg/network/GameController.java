package trucoarg.network;

public interface GameController {
    void startGame();
    void setearPuntosIniciales(int puntos);
    void procesarJugada(int jugador, int idCarta);

    boolean cantarTruco(int jugador, String tipoCanto);
    boolean cantarEnvido(int jugador, String tipoEnvido);
    int responderCanto(int jugador, boolean quiero);
    void irAlMazo(int jugador);

    int getPuntosJ1();
    int getPuntosJ2();
}
