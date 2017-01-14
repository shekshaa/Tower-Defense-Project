import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TestServer extends Application{

    int width = 800;
    int height = 500;
    Group menuRoot = new Group();
    Scene menuScene = new Scene(menuRoot, width, height);
    Group waitingRoot = new Group();
    Scene waitingScene = new Scene(waitingRoot, width, height);

    String buttonStyle = "-fx-background-color:\n" +
            "            linear-gradient(#ffd65b, #e68400),\n" +
            "            linear-gradient(#ffef84, #f2ba44),\n" +
            "            linear-gradient(#ffea6a, #efaa22),\n" +
            "            linear-gradient(#ffe657 0%, #f8c202 50%, #eea10b 100%);\n" +
            "    -fx-background-radius: 30;\n" +
            "    -fx-background-insets: 0,1,2,3,0;\n" +
            "    -fx-text-fill: #654b00;\n" +
            "    -fx-font-weight: bold;\n" +
            "    -fx-font-size: 20px;\n" +
            "    -fx-padding: 10 30 10 30;";

    public static void main(String[] args) {
        launch(args);
    }

    //after pressing multiplayer this method is called
    public void makeMenu(Scene scene, Group root) {
        scene.setFill(Color.web("#0a2d2e"));
        Label menuName = new Label("Making Server");
        menuName.setFont(new Font(35));
        menuName.setEffect(new Glow(1));
        menuName.setTextFill(Color.YELLOWGREEN);
        menuName.relocate(width / 2 - 140, height / 5);

        Label port = new Label("Port number");
        port.setFont(new Font(20));
        port.setTextFill(Color.ANTIQUEWHITE);
        port.relocate(width / 2 - 170, height / 2.5);
        TextField portNum = new TextField("12345");
        portNum.relocate(width / 2 - 20, height / 2.5);

        Label clients = new Label("Number of Clients");
        clients.setFont(new Font(20));
        clients.setTextFill(Color.ANTIQUEWHITE);
        clients.relocate(width / 2 - 200, height / 2);
        TextField numOfClients = new TextField("1");
        numOfClients.relocate(width / 2 - 20, height / 2);

        Button create = new Button("Create");
        create.setStyle(buttonStyle);
        Main.makeZoomInZoomOut(create);
        create.relocate(width / 2 - 80, height / 1.5);
        create.setOnAction(event -> {
            if (!portNum.getText().equals("") && !numOfClients.getText().equals("")) {
                Integer portNumber = Integer.parseInt(portNum.getText());
                Integer numberOfClients = Integer.parseInt(numOfClients.getText());
                if (portNumber <= 65535 && portNumber >= 1024 && numberOfClients >= 1) { //checking port info and number of players
                    playButtonSound();
                    Server myServer = new Server(portNumber, numberOfClients);
                    new Thread(myServer).start();
                    makeWaitingScene(waitingScene, waitingRoot, myServer);
                    ((Stage)scene.getWindow()).setScene(waitingScene);
                }
            }
        });

        root.getChildren().addAll(menuName, port, portNum, clients, numOfClients, create);
    }

    public void makeWaitingScene(Scene scene, Group root, Server myServer) {
        scene.setFill(Color.web("#0a2d2e"));
        Label menuName = new Label("Waiting for clients");
        menuName.setFont(new Font(35));
        menuName.setEffect(new Glow(1));
        menuName.setTextFill(Color.YELLOWGREEN);
        menuName.relocate(width / 2 - 120, height / 5);

        Label clientsConnected = new Label("Clients Connected : ");
        clientsConnected.setFont(new Font(20));
        clientsConnected.setTextFill(Color.ANTIQUEWHITE);
        clientsConnected.relocate(width / 2 - 120, height / 2);

        Label numOfClientsConnected = new Label();
        numOfClientsConnected.setFont(new Font(20));
        numOfClientsConnected.setTextFill(Color.ANTIQUEWHITE);
        numOfClientsConnected.relocate(width / 2 + 85, height / 2);
        numOfClientsConnected.textProperty().bind(myServer.numOfClientsConnected.asString());

        myServer.numOfClientsConnected.addListener((observable, oldValue, newValue) -> { //when all players have joined
            if (newValue.equals(myServer.maxSize)) {
                Label ready = new Label("Ready for start...");
                ready.setFont(new Font(20));
                ready.setTextFill(Color.ANTIQUEWHITE);
                ready.relocate(width / 2 - 120, height / 2 + 70);
                Button start = new Button("start");
                start.setStyle(buttonStyle);
                start.relocate(width / 2 - 100, height / 2 + 140);
                Main.makeZoomInZoomOut(start);
                start.setOnAction(event -> {
                    playButtonSound();
                    myServer.timeToStart.setValue(true); //after the server has pressed the start button
                    menuName.setText("Started");
                    menuName.relocate(width / 2 - 70, height / 5);
                    clientsConnected.setText("All of clients connected!");
                    root.getChildren().removeAll(numOfClientsConnected, ready, start);
                });
                root.getChildren().addAll(ready, start);
            }
        });
        root.getChildren().addAll(menuName, numOfClientsConnected, clientsConnected);
    }

    public void playButtonSound() {
        Sound buttonSound = new Sound("ButtonSound");
        buttonSound.play();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        makeMenu(menuScene, menuRoot);
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Making Server");
        primaryStage.show();
    }
}
