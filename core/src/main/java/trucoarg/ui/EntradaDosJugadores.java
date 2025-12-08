package trucoarg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import trucoarg.pantallas.PantallaDosJugadores;
import trucoarg.personajesSolitario.CartaSolitario;
import java.util.List;

public class EntradaDosJugadores implements InputProcessor {

    private final List<CartaSolitario> cartasJugador1;
    private final List<CartaSolitario> cartasJugador2;
    private final PantallaDosJugadores pantalla;

    // 🆕 Variable para detectar ESC
    private boolean escape = false;

    public EntradaDosJugadores(List<CartaSolitario> cartasJugador1,
                               List<CartaSolitario> cartasJugador2,
                               PantallaDosJugadores pantalla) {
        this.cartasJugador1 = cartasJugador1;
        this.cartasJugador2 = cartasJugador2;
        this.pantalla = pantalla;
    }

    // 🆕 Método público para verificar si se presionó ESC
    public boolean escape() {
        boolean fuePresionado = escape;
        escape = false;
        return fuePresionado;
    }


    // 🆕 Detectar cuando se presiona ESC
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            escape = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            escape = false;
            return true;
        }
        return false;
    }

    // Métodos requeridos por InputProcessor
    @Override public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
