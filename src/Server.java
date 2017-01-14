import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable{

    static ArrayList<ClientManager> clients = new ArrayList<>();
    static IntegerProperty aliveClients = new SimpleIntegerProperty();
    ServerSocket server;
    int maxSize;
    IntegerProperty numOfClientsConnected;
	//this is true when all players have joined and start button has been pressed in server, creates and sends map to all clients
    BooleanProperty timeToStart = new SimpleBooleanProperty(false);

    public static ClientManager findClient(String name) { //searches between all clients, for sending private message
        for (ClientManager client : clients) {
            if (client.name.equals(name)) {
                return client;
            }
        }
        return null;
    }

    public Server(int port, int size) {
        maxSize = size;

        //when all of the players have won or lost, sends a message to all to create a back button
        aliveClients.addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(0)) {
                try {
                    for (ClientManager client : clients) {
                        client.outputStream.writeObject("show back button");
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        numOfClientsConnected = new SimpleIntegerProperty(0);
        try {
            server = new ServerSocket(port, size);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            numOfClientsConnected = new SimpleIntegerProperty(0);
            while (clients.size() < maxSize) {
                Socket client = server.accept();
                ClientManager clientManager = new ClientManager(client);
                Platform.runLater(() -> numOfClientsConnected.setValue(numOfClientsConnected.getValue() + 1));
                clients.add(clientManager); //adding the new client to clients' list and assigning a thread to it
                new Thread(clientManager).start();
            }
            aliveClients.setValue(maxSize);
            timeToStart.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    try {
                        GameMap map = new GameMap(Main.mapSize, Main.mapSize);
                        map.createMap();
						//sending map and other players' info with specified message
                        for (ClientManager client : clients) {
                            client.outputStream.writeObject("map:");
                            client.outputStream.writeObject(map);
                            client.outputStream.writeObject("other players info");
                            client.outputStream.writeObject(maxSize);
                        }
						//sending name of all players to each one of them
                        for (int i = 0; i < clients.size(); i++) {
                            for (ClientManager client : clients) {
                                clients.get(i).outputStream.writeObject(client.name);
                            }
                            clients.get(i).outputStream.writeObject("start");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
