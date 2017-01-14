import java.io.*;
import java.net.Socket;


//this class is created in server and is responsible for interacting with all clients and controlling their chat or new
//information messages, and also the messages sent when the game has ended

public class ClientManager implements Runnable{

    Socket client;
    ObjectInputStream inputStream;//input from client
    ObjectOutputStream outputStream;//output to client

    String name;

    public ClientManager(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            outputStream = new ObjectOutputStream(client.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(client.getInputStream());
            String message;
            while (true) {
                message = (String)inputStream.readObject();
                if (message.equals("close connection")) { //at the end of the game when all players have either lost or finished the game
                    outputStream.writeObject("disconnect"); //tells the server to close its connection to avoid exception in server
					//avoiding exception in clients
                    inputStream.close();
                    outputStream.close();
                    client.close();
                    Server.clients.remove(this);
                    break;
                }
                if (message.equals("end")) {
                    Server.aliveClients.setValue(Server.aliveClients.getValue() - 1);
                }
                else if (message.contains(":")) { //sending a normal chat message
					//first part sender, second part receiver(may be ALL), third part the message
                    String[] parts = message.split(":");
                    if (message.contains("ALL")) { //send to all
                        for (ClientManager destinationClient : Server.clients) {
                            if (!destinationClient.name.equals(parts[0])) {
                                destinationClient.outputStream.writeObject(message);
                            }
                        }
                    } else if (!parts[0].equals(parts[1])) { //if sender and receiver are not the same
                        ClientManager destinationClient = Server.findClient(parts[1]);
                        if (destinationClient != null) { //if player was not found destinationClient is null
                            destinationClient.outputStream.writeObject(message);
                        }
                    }
                } else if (message.contains("/")) { //for sending changes in playerInfo (coin, score ...)
                    for (ClientManager destinationClient : Server.clients) {
                        destinationClient.outputStream.writeObject(message);
                    }
                } else { //when a new client is connected to the server, sends its name to server
                    this.name = message;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
