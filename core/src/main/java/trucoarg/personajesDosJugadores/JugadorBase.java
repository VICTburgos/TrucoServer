package trucoarg.personajesDosJugadores;

import com.badlogic.gdx.graphics.g2d.Sprite;
import trucoarg.network.ServerThread;
import trucoarg.personajesSolitario.MazoSolitario;
import trucoarg.personajesSolitario.CartaSolitario;

import java.util.ArrayList;
import java.util.List;

public class JugadorBase extends Sprite{
    public ServerThread server;
    private CartaSolitario carta;
    private int id;
    private List<CartaSolitario> mano;
    private boolean esMano;

    public JugadorBase(int id, MazoSolitario mazo, boolean esMano, ServerThread server) {
        this.id = id;
        this.esMano = esMano;
        this.mano = new ArrayList<>();
        this.server= server;

        for(int i = 0; i < 3; i++){
            carta = mazo.sacarCartita();
            if(carta != null){
                mano.add(carta);
                server.sendMessageToAll("Repartir:"+id+":"+carta.getId());
            }
        }
    }

    public boolean esMano() {
        return esMano;
    }

    public void setEsMano(boolean esMano) {
        this.esMano = esMano;
    }

    public List<CartaSolitario> getMano() {
        return mano;
    }
}

