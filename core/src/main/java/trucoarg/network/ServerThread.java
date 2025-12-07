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
        } catch (SocketException e) {
            System.err.println("❌ Error creando socket del servidor: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        do {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.receive(packet);
                processMessage(packet);
            } catch (IOException e) {
                if (!end) {
                    System.err.println("❌ Error recibiendo paquete: " + e.getMessage());
                }
            }
        } while(!end);
    }

    private void processMessage(DatagramPacket packet) {
        String message = (new String(packet.getData())).trim();
        String[] parts = message.split(":");
        int index = findClientIndex(packet);
        System.out.println("📨 Mensaje recibido: " + message);

        if(parts[0].equals("Connect")){
            if(index != -1) {
                System.out.println("⚠️ Cliente ya conectado");
                this.sendMessage("AlreadyConnected", packet.getAddress(), packet.getPort());
                return;
            }

            if(connectedClients < MAX_CLIENTS) {
                connectedClients++;
                Client newClient = new Client(connectedClients, packet.getAddress(), packet.getPort());
                clients.add(newClient);
                sendMessage("Connected:"+connectedClients, packet.getAddress(), packet.getPort());
                System.out.println("✅ Cliente " + connectedClients + " conectado");

                if(connectedClients == MAX_CLIENTS) {
                    System.out.println("🎮 Ambos clientes conectados - Enviando señal Start");
                    for(Client client : clients) {
                        sendMessage("Start", client.getIp(), client.getPort());
                    }
                    gameController.startGame();
                }

            } else {
                System.out.println("❌ Servidor lleno");
                sendMessage("Full", packet.getAddress(), packet.getPort());
            }
        } else if(index == -1){
            System.out.println("❌ Cliente no conectado");
            this.sendMessage("NotConnected", packet.getAddress(), packet.getPort());
            return;
        } else {
            Client client = clients.get(index);
            switch(parts[0]){
                case "Setearpuntos":
                    int puntos = Integer.parseInt(parts[1]);
                    System.out.println("🎯 Cliente solicita iniciar partida a " + puntos + " puntos");
                    Gdx.app.postRunnable(() -> gameController.setearPuntosIniciales(puntos));
                    break;

                case "JugarCarta":
                    int jugador = Integer.parseInt(parts[1]);
                    int idCarta = Integer.parseInt(parts[2]);

                    System.out.println("🎴 Servidor recibe: J" + jugador + " juega carta " + idCarta);

                    Gdx.app.postRunnable(() -> {
                        System.out.println("🔄 Ejecutando procesarJugada");
                        gameController.procesarJugada(jugador, idCarta);
                    });
                    break;

                default:
                    System.out.println("⚠️ Mensaje desconocido: " + parts[0]);
                    break;
            }
        }
    }

    private int findClientIndex(DatagramPacket packet) {
        int i = 0;
        int clientIndex = -1;
        while(i < clients.size() && clientIndex == -1) {
            Client client = clients.get(i);
            String id = packet.getAddress().toString()+":"+packet.getPort();
            if(id.equals(client.getId())){
                clientIndex = i;
            }
            i++;
        }
        return clientIndex;
    }

    public void sendMessage(String message, InetAddress clientIp, int clientPort) {
        byte[] byteMessage = message.getBytes();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, clientIp, clientPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("❌ Error enviando mensaje: " + e.getMessage());
        }
    }

    public void terminate(){
        this.end = true;
        socket.close();
        this.interrupt();
    }

    public void sendMessageToAll(String message) {
        for (Client client : clients) {
            sendMessage(message, client.getIp(), client.getPort());
        }
    }

    public void disconnectClients() {
        for (Client client : clients) {
            sendMessage("Disconnect", client.getIp(), client.getPort());
        }
        this.clients.clear();
        this.connectedClients = 0;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }
}
