package trucoarg.personajesDosJugadores;

public class Truco extends Canto {

    private static final int PUNTOS_TRUCO = 2;
    private static final int PUNTOS_RETRUCO = 3;
    private static final int PUNTOS_VALE_CUATRO = 4;
    private static final int PUNTOS_SIN_CANTO = 1;

    @Override
    public boolean cantar(int jugador, String tipoCanto) {
        String cantoLower = tipoCanto.toLowerCase().trim();

        System.out.println("DEBUG Truco.cantar() - J" + jugador + " intenta cantar: " + cantoLower);
        System.out.println("  Estado actual: cantoActual=" + cantoActual +
            ", esperandoRespuesta=" + esperandoRespuesta +
            ", cantoAceptado=" + cantoAceptado);

        // ✅ PERMITIR SUBIR EL CANTO mientras hay respuesta pendiente
        // El jugador que debe responder puede "subir" en lugar de aceptar/rechazar
        if (esperandoRespuesta) {
            int jugadorQueDebeResponder = getJugadorQueDebeResponder();

            // Solo puede subir el jugador que debe responder
            if (jugador != jugadorQueDebeResponder) {
                System.out.println("  RECHAZADO: No es tu turno para responder/subir");
                return false;
            }

            // Verificar que sea una subida válida
            if (!esSubidaValida(cantoLower)) {
                System.out.println("  RECHAZADO: No puedes subir con ese canto");
                return false;
            }

            // ✅ SUBIR EL CANTO (cambiar roles)
            System.out.println("  🔄 J" + jugador + " SUBE el canto de " + cantoActual + " a " + cantoLower);
            cantoActual = cantoLower;
            jugadorQueCanto = jugador;  // Ahora este jugador es quien cantó
            esperandoRespuesta = true;  // Sigue esperando respuesta (del otro)
            cantoAceptado = false;      // Se resetea porque es un NUEVO canto

            System.out.println("  ✅ Canto subido. Ahora J" + getJugadorQueDebeResponder() + " debe responder");
            return true;
        }

        // ✅ Canto inicial (sin respuesta pendiente)
        if (!validarCanto(cantoLower, jugador)) {
            return false;
        }

        // Registrar el canto
        cantoActual = cantoLower;
        jugadorQueCanto = jugador;
        esperandoRespuesta = true;
        cantoAceptado = false;

        System.out.println("  ÉXITO: Canto registrado. Esperando respuesta del J" + getJugadorQueDebeResponder());
        return true;
    }

    /**
     * ✅ NUEVO: Verifica si el canto es una subida válida
     */
    private boolean esSubidaValida(String cantoNuevo) {
        if (cantoActual == null) return false;

        // Desde TRUCO solo se puede subir a RETRUCO
        if (cantoActual.equals("truco") && cantoNuevo.equals("retruco")) {
            return true;
        }

        // Desde RETRUCO solo se puede subir a VALE CUATRO
        if (cantoActual.equals("retruco") && (cantoNuevo.equals("vale cuatro") || cantoNuevo.equals("vale 4"))) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean validarCanto(String tipoCanto, int jugador) {
        switch (tipoCanto) {
            case "truco":
                // Truco solo si no hay ningún canto previo
                if (cantoActual != null) {
                    System.out.println("  RECHAZADO: Ya hay canto activo (" + cantoActual + ")");
                    return false;
                }
                return true;

            case "retruco":
                // Retruco solo si hay truco aceptado y lo canta el otro jugador
                if (cantoActual == null || !cantoActual.equals("truco") || !cantoAceptado) {
                    System.out.println("  RECHAZADO: No hay truco aceptado para retruco");
                    return false;
                }
                if (jugador == jugadorQueCanto) {
                    System.out.println("  RECHAZADO: El mismo jugador no puede retrucarse a sí mismo");
                    return false;
                }
                return true;

            case "vale cuatro":
            case "vale 4":
                // Vale cuatro solo si hay retruco aceptado y lo canta el otro jugador
                if (cantoActual == null || !cantoActual.equals("retruco") || !cantoAceptado) {
                    System.out.println("  RECHAZADO: No hay retruco aceptado para vale cuatro");
                    return false;
                }
                if (jugador == jugadorQueCanto) {
                    System.out.println("  RECHAZADO: El mismo jugador no puede hacer vale cuatro después de su retruco");
                    return false;
                }
                return true;

            default:
                System.out.println("  RECHAZADO: Canto desconocido");
                return false;
        }
    }

    @Override
    public int getPuntos() {
        if (cantoActual == null) {
            return PUNTOS_SIN_CANTO;
        }

        switch (cantoActual) {
            case "truco":
                return PUNTOS_TRUCO;
            case "retruco":
                return PUNTOS_RETRUCO;
            case "vale cuatro":
            case "vale 4":
                return PUNTOS_VALE_CUATRO;
            default:
                return PUNTOS_SIN_CANTO;
        }
    }

    public int puntosDeLaMano() {
        return getPuntos();
    }
}
