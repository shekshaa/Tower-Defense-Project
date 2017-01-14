import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.ObjectInputStream;


//in each client's program, this class is responsible for interacting with server's socket and getting information
//they may be orders before sending other players' information, the game map or those data themselves
//it may be a chat message (sender:receiver:message) or an order for sending a change in another player's coin, score, etc
//(player/coin,.../new value)

public class ServerManager implements Runnable{

    ObjectInputStream inputStream;
    TextArea textArea;
    Client client;

    public ServerManager(Client client, ObjectInputStream inputStream, TextArea textArea) {
        this.inputStream = inputStream;
        this.textArea = textArea;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            label:
            while (true) {
                final String message = (String) inputStream.readObject();
                switch (message) {
                    case "disconnect": //when the game is finished this message is sent from server
                        client.closeConnection();
                        break label;
                    case "show back button":
                        Platform.runLater(() -> client.timeToEnd.setValue(true));
                        break;
                    case "start":
                        Platform.runLater(() -> client.timeToStart.setValue(true));
                        break;
                    case "other players info": //first gets the number of players, then gets all of their names
                        int maxSize = (Integer) inputStream.readObject();
                        for (int i = 0; i < maxSize; i++) {
                            PlayerInfo info = new PlayerInfo();
                            info.setName((String) inputStream.readObject());
                            client.playerSelector.getItems().add(info.getName());
                            Main.playersInfo.add(info);
                        }
                        break;
                    case "map:":
                        client.setMyMap((GameMap) inputStream.readObject());
                        break;
                    default:
                        if (message.contains(":")) { //it's a normal chat message
                            int indexOfFirstColon = message.indexOf(":");
                            int indexOfSecondColon = message.lastIndexOf(":");
                            //writing the new message in chatBox
                            Platform.runLater(() -> textArea.appendText(message.substring(0, indexOfFirstColon) + ": " + message.substring(indexOfSecondColon + 1) + '\n'));
                        }
                        //message = "player's name/" + "changed item/" + "new value
                        else if (message.contains("/")) { //it's a new order about change in another player's information
                            int indexOfSlash = message.indexOf("/");
                            int lastIndexOfSlash = message.lastIndexOf("/");

                            //the changed item is in second part of message
                            //based on its value, the change should be applied to all players info object's variable (they are then shown in the right menu)
                            for (PlayerInfo info : Main.playersInfo) {
                                if (info.getName().equals(message.substring(indexOfSlash + 1, lastIndexOfSlash))) {
                                    Platform.runLater(() -> {
                                        switch (message.substring(0, indexOfSlash)) {
                                            case "coin":
                                                info.coinProperty().setValue(Double.parseDouble(message.substring(lastIndexOfSlash + 1)));
                                                break;
                                            case "score":
                                                info.scoreProperty().setValue(Integer.parseInt(message.substring(lastIndexOfSlash + 1)));
                                                break;
                                            case "health":
                                                info.healthOfCastleProperty().setValue(Integer.parseInt(message.substring(lastIndexOfSlash + 1)));
                                                break;
                                            case "wave":
                                                info.waveLevelProperty().setValue(Integer.parseInt(message.substring(lastIndexOfSlash + 1)));
                                                break;
                                        }
                                    });
                                    break;
                                }
                            }
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
