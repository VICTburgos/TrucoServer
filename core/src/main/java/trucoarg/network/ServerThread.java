package trucoarg.network;

import com.badlogic.gdx.Gdx;
import trucoarg.pantallas.PantallaDosJugadores;
import trucoarg.pantallas.PantallaSeleccionPuntos;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ServerThread extends Thread {

    private DatagramSocket socket;
    private int serverPort = 5555;
    private boolean end = false;
    private final int MAX_CLIENTS = 2;
    private int connectedClients = 0;
    private ArrayList<Client> clients = new ArrayList<Client>();
    public GameController gameController;

    public ServerThread(GameController gameController) {
        this.gameController = gameController;
        try {
            socket = new DatagramSocket(serverPort);
            System.out.println(" Servidor iniciado en puerto " + serverPort);
        } catch (SocketException e) {
            System.err.println(" Error creando socket del servidor: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println(" ServerThread ejecutándose - Esperando clientes...");
        do {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.receive(packet);
                processMessage(packet);
            } catch (IOException e) {
                if (!end) {
                    System.err.println(" Error recibiendo paquete: " + e.getMessage());
                }
            }
        } while(!end);
    }

    private void processMessage(DatagramPacket packet) {
        String message = (new String(packet.getData())).trim();
        String[] parts = message.split(":");
        int index = findClientIndex(packet);
        System.out.println(" Mensaje recibido: " + message);

        if(parts[0].equals("Connect")){
            if(index != -1) {
                System.out.println("⚠ Cliente ya conectado");
                this.sendMessage("AlreadyConnected", packet.getAddress(), packet.getPort());
                return;
            }

            if(connectedClients < MAX_CLIENTS) {
                connectedClients++;
                Client newClient = new Client(connectedClients, packet.getAddress(), packet.getPort());
                clients.add(newClient);
                sendMessage("Connected:" + connectedClients, packet.getAddress(), packet.getPort());
                System.out.println("Cliente " + connectedClients + " conectado desde " +
                    packet.getAddress() + ":" + packet.getPort());

                if(connectedClients == MAX_CLIENTS) {
                    System.out.println(" Ambos clientes conectados - Enviando señal Start");
                    sendMessageToAll("Start");
                    Gdx.app.postRunnable(() -> gameController.startGame());
                }

            } else {
                System.out.println(" Servidor lleno");
                sendMessage("Full", packet.getAddress(), packet.getPort());
            }
        }

        else if(index == -1){
            System.out.println("Cliente no conectado intentando enviar: " + parts[0]);
            this.sendMessage("NotConnected", packet.getAddress(), packet.getPort());
            return;
        }

        else {
            Client client = clients.get(index);
            System.out.println(" Procesando mensaje de Cliente #" + client.getNum() + ": " + parts[0]);

            switch(parts[0]){
                case "Setearpuntos":
                    int puntos = Integer.parseInt(parts[1]);
                    System.out.println("Cliente #" + client.getNum() + " solicita iniciar partida a " + puntos + " puntos");
                    Gdx.app.postRunnable(() -> gameController.setearPuntosIniciales(puntos));
                    break;

                case "JugarCarta":
                    int jugador = Integer.parseInt(parts[1]);
                    int idCarta = Integer.parseInt(parts[2]);
                    System.out.println(" Servidor recibe: J" + jugador + " juega carta " + idCarta);
                    Gdx.app.postRunnable(() -> {
                        System.out.println(" Ejecutando procesarJugada");
                        gameController.procesarJugada(jugador, idCarta);
                    });
                    break;

                case "CantarTruco":
                    int jugadorTruco = Integer.parseInt(parts[1]);
                    String tipoCanto = parts[2];
                    System.out.println( "J" + jugadorTruco + " canta " + tipoCanto.toUpperCase());
                    Gdx.app.postRunnable(() -> {
                        boolean exito = gameController.cantarTruco(jugadorTruco, tipoCanto);

                        if (exito) {
                            String mensaje = "CantoRealizado:truco:" + jugadorTruco + ":" + tipoCanto;
                            ((PantallaDosJugadores) gameController).server.sendMessageToAll(mensaje);


                            ((PantallaDosJugadores) gameController).enviarEstadoBotonesATodos();
                        }
                    });
                    break;

                case "CantarEnvido":
                    int jugadorEnvido = Integer.parseInt(parts[1]);
                    String tipoEnvido = parts[2];
                    System.out.println(" J" + jugadorEnvido + " canta " + tipoEnvido.toUpperCase());
                    Gdx.app.postRunnable(() -> {
                        boolean exito = gameController.cantarEnvido(jugadorEnvido, tipoEnvido);

                        if (exito) {
                            String mensaje = "CantoRealizado:envido:" + jugadorEnvido + ":" + tipoEnvido;
                            ((PantallaDosJugadores) gameController).server.sendMessageToAll(mensaje);

                            ((PantallaDosJugadores) gameController).enviarEstadoBotonesATodos();
                        }
                    });
                    break;

                case "ResponderCanto":
                    int jugadorRespuesta = Integer.parseInt(parts[1]);
                    String respuesta = parts[2];
                    boolean quiero = respuesta.equalsIgnoreCase("quiero");
                    System.out.println(" J" + jugadorRespuesta + " responde: " + (quiero ? "QUIERO" : "NO QUIERO"));
                    Gdx.app.postRunnable(() -> procesarRespuestaCanto(jugadorRespuesta, quiero));
                    break;

                case "IrAlMazo":
                    int jugadorMazo = Integer.parseInt(parts[1]);
                    System.out.println(" J" + jugadorMazo + " se va al mazo");
                    Gdx.app.postRunnable(() -> procesarIrAlMazo(jugadorMazo));
                    break;

                case "SolicitarBotones":
                    int jugadorSolicitante = Integer.parseInt(parts[1]);
                    System.out.println("J" + jugadorSolicitante + " solicita estado de botones");
                    Gdx.app.postRunnable(() -> {
                        PantallaDosJugadores pantalla = (PantallaDosJugadores) gameController;
                        pantalla.enviarEstadoBotones(jugadorSolicitante);
                    });
                    break;

                default:
                    System.out.println(" Mensaje desconocido: " + parts[0]);
                    break;
            }
        }
    }


    private void procesarCantoTruco(int jugador, String tipoCanto) {
        System.out.println(" Procesando canto de truco: J" + jugador + " - " + tipoCanto);

        boolean exito = gameController.cantarTruco(jugador, tipoCanto);

        if (exito) {
            String mensaje = "CantoRealizado:truco:" + jugador + ":" + tipoCanto;
            sendMessageToAll(mensaje);
            System.out.println(" Enviado a todos: " + mensaje);
        } else {
            System.out.println(" Canto de truco rechazado por lógica del juego");
        }
    }

    private void procesarCantoEnvido(int jugador, String tipoEnvido) {
        System.out.println(" Procesando canto de envido: J" + jugador + " - " + tipoEnvido);

        boolean exito = gameController.cantarEnvido(jugador, tipoEnvido);

        if (exito) {
            String mensaje = "CantoRealizado:envido:" + jugador + ":" + tipoEnvido;
            sendMessageToAll(mensaje);
            System.out.println(" Enviado a todos: " + mensaje);
        } else {
            System.out.println(" Canto de envido rechazado por lógica del juego");
        }
    }


    private void procesarRespuestaCanto(int jugador, boolean quiero) {
        System.out.println(" Procesando respuesta: J" + jugador + " - " + (quiero ? "QUIERO" : "NO QUIERO"));

        int resultado = gameController.responderCanto(jugador, quiero);

        String respuesta = quiero ? "quiero" : "noquiero";
        String mensaje = "RespuestaCanto:" + jugador + ":" + respuesta + ":" + resultado;
        sendMessageToAll(mensaje);
        System.out.println(" Enviado a todos: " + mensaje);

        String mensajePuntos = "Puntos:" + gameController.getPuntosJ1() + ":" + gameController.getPuntosJ2();
        sendMessageToAll(mensajePuntos);
        System.out.println(" Puntos actualizados: " + mensajePuntos);
    }

    private void procesarIrAlMazo(int jugador) {
        System.out.println(" Procesando ir al mazo: J" + jugador);

        gameController.irAlMazo(jugador);

        sendMessageToAll("JugadorAlMazo:" + jugador);
        System.out.println("📤 Enviado: JugadorAlMazo:" + jugador);

        String mensajePuntos = "Puntos:" + gameController.getPuntosJ1() + ":" + gameController.getPuntosJ2();
        sendMessageToAll(mensajePuntos);
        System.out.println("📤 Puntos actualizados: " + mensajePuntos);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private int findClientIndex(DatagramPacket packet) {
        String id = packet.getAddress().toString() + ":" + packet.getPort();

        for (int i = 0; i < clients.size(); i++) {
            if (id.equals(clients.get(i).getId())) {
                return i;
            }
        }

        return -1;
    }

    public void sendMessage(String message, InetAddress clientIp, int clientPort) {
        byte[] byteMessage = message.getBytes();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, clientIp, clientPort);
        try {
            socket.send(packet);
            System.out.println(" Mensaje enviado a " + clientIp + ":" + clientPort + " -> " + message);
        } catch (IOException e) {
            System.err.println(" Error enviando mensaje: " + e.getMessage());
        }
    }

    public void sendMessageToAll(String message) {
        System.out.println("Enviando a todos los clientes (" + clients.size() + "): " + message);
        for (Client client : clients) {
            sendMessage(message, client.getIp(), client.getPort());
        }
    }

    public void terminate(){
        System.out.println("Terminando ServerThread...");
        this.end = true;
        socket.close();
        this.interrupt();
    }

    public void disconnectClients() {
        System.out.println(" Desconectando todos los clientes...");
        for (Client client : clients) {
            sendMessage("Disconnect", client.getIp(), client.getPort());
        }
        this.clients.clear();
        this.connectedClients = 0;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public int getConnectedClients() {
        return connectedClients;
    }
}
