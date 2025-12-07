package trucoarg.network;

public interface GameController {

    public void startGame();
    public void setearPuntosIniciales(int puntos);
    void procesarJugada(int jugador, int idCarta);
}
