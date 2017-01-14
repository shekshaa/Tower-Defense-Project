import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.*;
import java.net.Socket;

//Client class is basically a player, but it supports network and it can receive messages from server with ServerManager class

public class Client extends Player{

    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;

    Socket connection;
    TextField textField; //for entering the chat message
    TextArea textArea; //for writing all of the chats
    ComboBox<String> playerSelector; //for chat feature, choosing which player the message should be sent to

    int port;
    String ip;
    BooleanProperty timeToStart = new SimpleBooleanProperty(false); //this BooleanProperty object is set true after start button is pressed from server
    BooleanProperty timeToEnd = new SimpleBooleanProperty(false);

    public Client(int port, String ip, String name) {
        super(name);
        this.port = port;
        this.ip = ip;
        //creating the comboBox next to chat text text box
        playerSelector = new ComboBox<>();
        playerSelector.setValue("To All");
        playerSelector.getItems().add("To All");
        textArea = new TextArea();
        textArea.setPrefColumnCount(19);
        textArea.setPrefRowCount(23);
        textArea.setEditable(false);
        textField = new TextField();
        textField.setEditable(false);
        textField.setPrefColumnCount(12);
        textField.setOnAction(event -> {
            String message = textField.getText();
            //based on option selected in combo box(public, or a player's name), the second part of message is changed
            //message = "Sender:" + "Receiver:" + "Message"
            if (playerSelector.getValue().equals("To All")) {
                sentData(name + ":" + "ALL:" + message);
            }
            else
                sentData(name + ":" + playerSelector.getValue() + ":" + message);
            textField.setText("");
            textArea.appendText("Me: " + message + '\n'); //the sender's name in local chat box should be "Me", not the player's name
        });
    }

    //this method is created for adding a listener, so that whenever a specified information is changed
    //(coin, score, etc) a message be sent to the server(and from there sent to all players)
    public ChangeListener<Number> makeListener(String info) { //info is coin, score, etc
        return (observable, oldValue, newValue) -> sentData(info + "/" + getInfo().getName() + "/" + newValue);
    }

    public void runClient() throws Exception{
        //adding listener to a player's information with above method
        getCoin().addListener(makeListener("coin"));
        getScore().addListener(makeListener("score"));
        healthOfCastleProperty().addListener(makeListener("health"));
        waveLevelProperty().addListener(makeListener("wave"));

        connectToServer();
        getStreams(); //getting input and output stream from socket

        sentData(getInfo().getName()); //sending the player's name to the server
        textField.setEditable(true);

        new Thread(new ServerManager(this, inputStream, textArea)).start(); //for managing received messages from server
    }

    public void sentData(String message) {
        try {
            outputStream.writeObject(message);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void connectToServer() throws Exception{
        try {
            connection = new Socket(ip, port);
        } catch (Exception ex) {
            throw new Exception("No such server!");
        }
    }


    public void getStreams() throws Exception{
        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(connection.getInputStream());
        } catch (Exception ex) {
            throw new Exception("Error opening streams");
        }
    }

    public void closeConnectionCommand() {
        sentData("close connection");
    }

    public void closeConnection() { //after all players have either lost or finished the game
        try {
            inputStream.close();
            outputStream.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
