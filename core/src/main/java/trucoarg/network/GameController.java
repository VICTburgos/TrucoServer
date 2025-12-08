package trucoarg.network;

public interface GameController {
    void startGame();
    void setearPuntosIniciales(int puntos);
    void procesarJugada(int jugador, int idCarta);

    // 🆕 MÉTODOS PARA CANTOS
    boolean cantarTruco(int jugador, String tipoCanto);
    boolean cantarEnvido(int jugador, String tipoEnvido);
    int responderCanto(int jugador, boolean quiero);
    void irAlMazo(int jugador);

    // 🆕 GETTERS
    int getPuntosJ1();
    int getPuntosJ2();
}
