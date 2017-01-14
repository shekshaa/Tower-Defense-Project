import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.File;
import java.util.*;
import java.util.Map;


//in all methods responsible for creating root's graphical objects, a boolean should be sent to indicate wether the game is multiplayer or not
//because this can affect some graphical nodes and some features like creating the map

public class Main extends Application{

    TableView<Rank> table = new TableView<>();
    final ObservableList<Rank> data = FXCollections.observableArrayList();

    public static class Rank {
        final private StringProperty name;
        final private IntegerProperty score;

        public Rank(String name, Integer score) {
            this.name = new SimpleStringProperty(name);
            this.score = new SimpleIntegerProperty(score);
        }

        public String getName() {
            return name.get();
        }

        public int getScore() {
            return score.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setScore(int score) {
            this.score.set(score);
        }
    }

    static ArrayList<PlayerInfo> playersInfo = new ArrayList<>();
    static int playerInfoIndex;
    static BooleanProperty isSoundOn = new SimpleBooleanProperty(true);
    static boolean isRemoving = false;
    static boolean isUpgrading = false;
    static int scaleTime = 50;
    static int sizeOfCell = 30;
    static int mapSize = 26;
    static int width = 1340;
    static int height = 840;
    static int lineWidth = 5;
    static Wave currentWave;
    static Cell[][] background = new Cell[mapSize + 2][mapSize + 2];
    static Player player;
    static ArrayList<Tower> killedTowers = new ArrayList<>();
    static char[][] curMap = new char[mapSize + 2][mapSize + 2];
    static HashMap<TowerName, TowerInfo> towerInfo = new HashMap<>();
    static HashMap<EnemyName, EnemyInfo> enemyInfo = new HashMap<>();
    Image sandImage = new Image(getClass().getResourceAsStream("resources/sand.png"));
    Image grassImage = new Image(getClass().getResourceAsStream("resources/grass2.png"));
    Image castleImage = new Image(getClass().getResourceAsStream("resources/Castle.png"));
    Image backButtonImage = new Image(getClass().getResourceAsStream("resources/back.png"));
    Image pauseImage = new Image(getClass().getResourceAsStream("resources/pause.png"));
    Image soundOffImage = new Image(getClass().getResourceAsStream("resources/soundOff.png"));
    Image soundOnImage = new Image(getClass().getResourceAsStream("resources/soundOn.png"));
    String cssFilePath = getClass().getResource("styling.css").toExternalForm();
    static Group gameRoot = new Group();
    static Group menuRoot = new Group();
    static Group newGameRoot = new Group();
    static Group loadGameRoot = new Group();
    static Group pauseRoot = new Group();
    static Group endOfGameRoot = new Group();
    static Group multiplayerMenuRoot = new Group();
    static Scene multiplayerMenuScene = new Scene(multiplayerMenuRoot, width, height);
    static Scene endOfGameScene = new Scene(endOfGameRoot, width, height);
    static Scene newGameScene = new Scene(newGameRoot, width, height);
    static Scene loadGameScene = new Scene(loadGameRoot, width, height);
    static Scene pauseScene = new Scene(pauseRoot, width, height);
    static Scene gameScene = new Scene(gameRoot, width, height);
    static Scene menuScene = new Scene(menuRoot, width, height);
    static Timeline timeline = new Timeline();


    public void makeDirection(ArrayList<Position>[] paths) {
        for (int i = 0; i < lineWidth; i++) {
            for (int j = 0; j < paths[i].size() - 1; j++) {
                if (paths[i].get(j + 1).x == paths[i].get(j).x - 1) {
                    ((RoadCell)Main.background[paths[i].get(j).x][paths[i].get(j).y]).setDirection(Direction.Up);
                }
                else if (paths[i].get(j + 1).y == paths[i].get(j).y + 1) {
                    ((RoadCell)Main.background[paths[i].get(j).x][paths[i].get(j).y]).setDirection(Direction.Right);
                }
                else if (paths[i].get(j + 1).x == paths[i].get(j).x + 1) {
                    ((RoadCell)Main.background[paths[i].get(j).x][paths[i].get(j).y]).setDirection(Direction.Down);
                }
                else if (paths[i].get(j + 1).y == paths[i].get(j).y - 1) {
                    ((RoadCell)Main.background[paths[i].get(j).x][paths[i].get(j).y]).setDirection(Direction.Left);
                }
            }
        }
    }

    public void addStyle() {
        endOfGameScene.getStylesheets().add(cssFilePath);
        newGameScene.getStylesheets().add(cssFilePath);
        loadGameScene.getStylesheets().add(cssFilePath);
        pauseScene.getStylesheets().add(cssFilePath);
        gameScene.getStylesheets().add(cssFilePath);
        menuScene.getStylesheets().add(cssFilePath);
        multiplayerMenuScene.getStylesheets().add(cssFilePath);
    }

    public void makePauseScene(Scene scene, Group root) {
        root.getChildren().clear();
        makeSoundButton(root);
        scene.setFill(Color.web("#0a2d2e"));
        Label menuName = new Label("Pause");
        menuName.setEffect(new Glow(1));
        menuName.setFont(new Font(60));
        menuName.relocate(width / 2 - 70, height / 5);
        menuName.setTextFill(Color.YELLOWGREEN);

        Button resume = new Button("Resume");
        resume.relocate(width / 2 - 60, height / 2.5 + 100);
        makeZoomInZoomOut(resume);
        resume.setOnAction(event -> {
            playButtonSound();
            ((Stage)scene.getWindow()).setScene(gameScene);
            timeline.play();
        });
        Button backToMenu = new Button("Back to menu");
        backToMenu.relocate(width / 2 - 100, height / 2 + 140);
        makeZoomInZoomOut(backToMenu);
        backToMenu.setOnAction(event -> goToMainMenu(scene));
        root.getChildren().addAll(menuName, resume, backToMenu);
    }

    public void goToMainMenu(Scene scene) {
        playButtonSound();
        makeMenuScene(menuScene, menuRoot);
        ((Stage)scene.getWindow()).setScene(menuScene);
    }

    public void makeGameSceneRightMenu(Group root, boolean isMultiplayer) {
        makeTowerMenu(root);
        ImageView pauseImageView = new ImageView(new Image(getClass().getResourceAsStream("resources/RightMenu/Pause.png")));
        pauseImageView.relocate(840, 0);
        root.getChildren().add(pauseImageView);

        ImageView infoImageView = new ImageView(new Image(getClass().getResourceAsStream("resources/RightMenu/Info.png")));
        infoImageView.relocate(840, 72);

        Label nameLabel = new Label();
        nameLabel.textProperty().bind(player.getInfo().nameProperty());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.relocate(890, 105);

        Label coinLabel = new Label();
        coinLabel.textProperty().bind(player.getCoin().asString());
        coinLabel.setFont(new Font(18));
        coinLabel.relocate(925, 143);

        Label scoreLabel = new Label();
        scoreLabel.textProperty().bind(player.getScore().asString());
        scoreLabel.setFont(new Font(18));
        scoreLabel.relocate(925, 190);

        Label lifeLabel = new Label();
        lifeLabel.textProperty().bind(player.healthOfCastleProperty().asString());
        lifeLabel.setFont(new Font(18));
        lifeLabel.relocate(925, 235);

        Label waveNumLabel = new Label();
        waveNumLabel.textProperty().bind(player.waveLevelProperty().asString());
        waveNumLabel.setFont(new Font(18));
        waveNumLabel.relocate(925, 270);

        if (!isMultiplayer) { //only single player game has pause button
            ImageView pauseButton = new ImageView(pauseImage);
            pauseButton.relocate(900, 16);
            makeZoomInZoomOut(pauseButton);
            pauseButton.setOnMouseClicked(event -> {
                Main.timeline.stop();
                Sound buttonSound = new Sound("ButtonSound");
                buttonSound.play();
                makePauseScene(pauseScene, pauseRoot);
                ((Stage) root.getScene().getWindow()).setScene(pauseScene);
            });
            root.getChildren().add(pauseButton);
        }
        else {
            for (int i = 0; i < playersInfo.size(); i++) {
                if (player.getInfo().getName().equals(playersInfo.get(i).getName())) {
                    playerInfoIndex = i;
                    break;
                }
            }
            ImageView prevUser = new ImageView(new Image(getClass().getResourceAsStream("resources/prev.png")));
            prevUser.relocate(875, 16);
            makeZoomInZoomOut(prevUser);
            ImageView nextUser = new ImageView(new Image(getClass().getResourceAsStream("resources/next.png")));
            nextUser.relocate(925, 16);
            makeZoomInZoomOut(nextUser);
            root.getChildren().addAll(prevUser, nextUser);
            prevUser.setOnMouseClicked(event -> {
                Sound buttonSound = new Sound("ButtonSound");
                buttonSound.play();
                playerInfoIndex++;
                if (playerInfoIndex == playersInfo.size())
                    playerInfoIndex = 0;
                nameLabel.textProperty().bind(playersInfo.get(playerInfoIndex).nameProperty());
                coinLabel.textProperty().bind(playersInfo.get(playerInfoIndex).coinProperty().asString());
                scoreLabel.textProperty().bind(playersInfo.get(playerInfoIndex).scoreProperty().asString());
                lifeLabel.textProperty().bind(playersInfo.get(playerInfoIndex).healthOfCastleProperty().asString());
                waveNumLabel.textProperty().bind(playersInfo.get(playerInfoIndex).waveLevelProperty().asString());
            });
            nextUser.setOnMouseClicked(event -> {
                Sound buttonSound = new Sound("ButtonSound");
                buttonSound.play();
                playerInfoIndex--;
                if (playerInfoIndex == -1) {
                    playerInfoIndex = playersInfo.size() - 1;
                }
                nameLabel.textProperty().bind(playersInfo.get(playerInfoIndex).nameProperty());
                coinLabel.textProperty().bind(playersInfo.get(playerInfoIndex).coinProperty().asString());
                scoreLabel.textProperty().bind(playersInfo.get(playerInfoIndex).scoreProperty().asString());
                lifeLabel.textProperty().bind(playersInfo.get(playerInfoIndex).healthOfCastleProperty().asString());
                waveNumLabel.textProperty().bind(playersInfo.get(playerInfoIndex).waveLevelProperty().asString());
            });
        }

        root.getChildren().addAll(infoImageView, coinLabel, scoreLabel, lifeLabel, waveNumLabel, nameLabel);
    }

    public void makeEndOfGameScene(Scene scene, Group root, String message, boolean isMultiplayer) {
        root.getChildren().clear();
        makeSoundButton(root);
        scene.setFill(Color.web("#0a2d2e"));
        Label messageLabel = new Label(message);
        messageLabel.setEffect(new Glow(1));
        messageLabel.setFont(new Font(65));
        messageLabel.relocate(width / 2 - 130, height / 5);
        messageLabel.setTextFill(Color.YELLOWGREEN);
        root.getChildren().add(messageLabel);
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(500), messageLabel);
        scaleTransition.setInterpolator(Interpolator.LINEAR);
        scaleTransition.setByX(0.2);
        scaleTransition.setByY(0.2);
        scaleTransition.setCycleCount(Transition.INDEFINITE);
        scaleTransition.setAutoReverse(true);
        scaleTransition.playFromStart();

        if (!isMultiplayer) {
            Label usernameScore = new Label("Score : " + player.getScore().getValue().toString());
            usernameScore.relocate(width / 2 - 40, height / 2 - 50);
            usernameScore.setEffect(new Glow(0.85));
            usernameScore.setTextFill(Color.YELLOWGREEN);
            usernameScore.setFont(new Font(35));
            ScaleTransition scaleTransition2 = new ScaleTransition(new Duration(1000), usernameScore);
            scaleTransition2.setByX(0.2);
            scaleTransition2.setByY(0.2);
            scaleTransition2.setInterpolator(Interpolator.LINEAR);
            scaleTransition2.setAutoReverse(true);
            scaleTransition2.setCycleCount(Animation.INDEFINITE);
            scaleTransition2.playFromStart();

            HashMap<String, Integer> records = readFromFile();
            records.put(player.getInfo().getName(), player.getScore().getValue());
            writeInFile(records);

            Button startAgain = new Button("Start Again");
            makeZoomInZoomOut(startAgain);
            startAgain.relocate(width / 2 - 65, height / 2 + 80);
            startAgain.setOnAction(event -> {
                playButtonSound();
                player.setCoin(Math.max(33, player.getScore().getValue() / 4));
                player.getScore().setValue(0);
                player.healthOfCastleProperty().setValue(5);
                makeGameScene(gameRoot, false);
                ((Stage) scene.getWindow()).setScene(gameScene);
            });
            root.getChildren().addAll(usernameScore, startAgain);
        }
        Button backToMenu = new Button("Back to menu");
        makeZoomInZoomOut(backToMenu);
        backToMenu.relocate(width / 2 - 80, height / 2 + 190);
        if (isMultiplayer) { //only multiplayer game has scoreboard
            root.getChildren().add(table);
            table.relocate(width / 2 - 130, height / 2 - 110);
            backToMenu.relocate(width / 2 - 90, height / 2 + 310);
            ((Client)player).timeToEnd.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    root.getChildren().add(backToMenu);
                }
            });
            backToMenu.setOnAction(event -> {
                ((Client)player).closeConnectionCommand();
                playersInfo.clear();
                table.getColumns().clear();
                data.clear();
                goToMainMenu(scene);
            });
        }
        else {
            backToMenu.setOnAction(event -> goToMainMenu(scene));
            root.getChildren().add(backToMenu);
        }
    }

    public void writeInFile(HashMap<String, Integer> records) {
        File loadGame = new File("/Users/shekshaa/Documents/Courses/Term_2/Advanced Programming Java/Project/Phase2/src/resources/loadGame.txt");
        Set<Map.Entry<String, Integer>> entries = records.entrySet();
        try {
            Formatter formatter = new Formatter(loadGame);
            for (Map.Entry<String, Integer> entry : entries) {
                formatter.format("%s %d\n", entry.getKey(), entry.getValue());
            }
            formatter.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //loads last score of players to use load game feature
    public HashMap<String, Integer> readFromFile() {
        HashMap<String, Integer> records = new HashMap<>();
        File loadGame = new File("/Users/shekshaa/Documents/Courses/Term_2/Advanced Programming Java/Project/Phase2/src/resources/loadGame.txt");
        try {
            Scanner scanner = new Scanner(loadGame);
            while (scanner.hasNext()) {
                String name = scanner.next();
                String score = scanner.next();
                records.put(name, Integer.parseInt(score));
            }
            scanner.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return records;
    }

    //is called after clicking on startButton
    public void makeGameScene(Group root, boolean isMultiPlayer) {
        root.getChildren().clear();
        player.waveLevelProperty().setValue(1);
        for (Tower tower : player.getTowers()) {
            root.getChildren().remove(background[tower.getPosition().x][tower.getPosition().y].getImageView());
        }
        if (!isMultiPlayer) { //if the game is single player map should be created in the program, else it should be received from the server
            player.setMyMap(new GameMap(mapSize, mapSize));
            player.getMyMap().createMap();
        }
        else {
            TableColumn firstColumn = new TableColumn("Name");
            TableColumn secondColumn = new TableColumn("Score");
            table.getColumns().addAll(firstColumn, secondColumn);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefWidth(280);
            firstColumn.setCellValueFactory(new PropertyValueFactory<Rank, String>("name"));
            secondColumn.setCellValueFactory(new PropertyValueFactory<Rank, String>("score"));
            firstColumn.setStyle("-fx-alignment: CENTER;");
            secondColumn.setStyle("-fx-alignment: CENTER;");
            for (PlayerInfo aPlayersInfo : playersInfo) {
                data.add(new Rank(aPlayersInfo.getName(), 0));
                aPlayersInfo.scoreProperty().addListener((observable, oldValue, newValue) -> {
                    data.clear();
                    ArrayList<PlayerInfo> sortedInfo = new ArrayList<>();
                    for (PlayerInfo aPlayerInfo2 : playersInfo) {
                        sortedInfo.add(aPlayerInfo2);
                    }
                    Collections.sort(sortedInfo);
                    for (PlayerInfo aPlayersInfo2 : sortedInfo) {
                        data.add(new Rank(aPlayersInfo2.getName(), aPlayersInfo2.scoreProperty().getValue()));
                    }
                    table.setItems(data);
                });
            }
            table.setItems(data);

            ImageView tableBG = new ImageView(new Image(getClass().getResourceAsStream("resources/RightMenu/tableBG.png")));
            tableBG.relocate(1000, 0);
            ImageView chatBG = new ImageView(new Image(getClass().getResourceAsStream("resources/RightMenu/ChatBG.png")));
            chatBG.relocate(1000, 400);

            ((Client)player).textArea.relocate(1027, 460);
            ((Client)player).textField.relocate(1003, 410);
            ((Client)player).playerSelector.relocate(1175, 405);
            ((Client)player).playerSelector.setPrefWidth(165);
            ((Client)player).textField.setPrefWidth(165);
            table.relocate(1030, 0);
            root.getChildren().addAll(tableBG, chatBG, ((Client)player).textArea, ((Client)player).textField, ((Client)player).playerSelector, table);
        }
        ArrayList<Position> startPoints = player.getMyMap().getStartingPoints();
        ArrayList<Position> castlePoints = player.getMyMap().getCastlePoints();
        ArrayList<Position>[] path = player.getMyMap().reFillMapCells();

        for (int i = 0; i < curMap.length; i++) {
            for (int j = 0; j < curMap[i].length; j++) {
                if (player.getMyMap().getMap()[i][j] == 0)
                    curMap[i][j] = ' ';
                else
                    curMap[i][j] = '.';
            }
        }
        makeBackground(root, castlePoints);
        makeDirection(path);
        TowerInfo.readTowerInfo();
        EnemyInfo.readEnemyInfo();
        makeGameSceneRightMenu(root, isMultiPlayer);
        player.getTowers().clear(); //after creating a new game

        //detecting player's lost
        player.healthOfCastleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(0)) {
                //sound
				Sound losingSound = new Sound("Losing");
                if (isSoundOn.getValue())
				    losingSound.play();
                Main.timeline.stop();
                makeEndOfGameScene(Main.endOfGameScene, Main.endOfGameRoot, "Game Over", isMultiPlayer);
                if (isMultiPlayer) {
                    root.getChildren().remove(table);
                    ((Client)player).sentData("end");
                }
                ((Stage)Main.gameScene.getWindow()).setScene(Main.endOfGameScene);
            }
        });
        currentWave = new Wave(EnemyName.None, startPoints, path);
        timeline.getKeyFrames().clear();
        timeline.setCycleCount(Timeline.INDEFINITE);
        //every 50 milliseconds conditions for tower attack and enemy movement are checked
        KeyFrame keyFrame = new KeyFrame(Duration.millis(scaleTime), event -> {
            Iterator<Enemy> iterator = currentWave.getEnemies().iterator();
            while(iterator.hasNext()){
                Enemy enemy = iterator.next();
                if (enemy.getTimeToNextMove() <= 0) {
                    ((RoadCell)background[enemy.getPosition().x][enemy.getPosition().y]).removeEnemy();
                    if (((RoadCell)background[enemy.getPosition().x][enemy.getPosition().y]).getEnemyCounter() == 0) {
                        background[enemy.getPosition().x][enemy.getPosition().y].getImageView().setImage(sandImage);
                        curMap[enemy.getPosition().x][enemy.getPosition().y] = '.';
                    }
                    enemy.move();
                    ((RoadCell)background[enemy.getPosition().x][enemy.getPosition().y]).addEnemy();
                    if (enemy.getToCastle() == 1) {
                        iterator.remove();
                        player.decreaseHealthOfCastle();
                    }
                    else if (((RoadCell)background[enemy.getPosition().x][enemy.getPosition().y]).getEnemyCounter() == 1) {
                        background[enemy.getPosition().x][enemy.getPosition().y].getImageView().setImage(new Image(getClass().getResourceAsStream("resources/Tanks/"
                                + enemy.getEnemyName() + ((RoadCell)background[enemy.getPosition().x][enemy.getPosition().y]).getDirection() + ".png")));
                        curMap[enemy.getPosition().x][enemy.getPosition().y] = '*';
                    }
                } else
                    enemy.decreaseTimeToNextMove(scaleTime);
            }
            Collections.sort(currentWave.getEnemies());
            Iterator<InnerFire> iterator2 = player.getInnerFires().iterator();
            while (iterator2.hasNext()) {
                InnerFire innerFire = iterator2.next();
                boolean isInActive = innerFire.attack();
                if (isInActive)
                    iterator2.remove();
            }

            for (int i = 0; i < player.getTowers().size(); i++) {
                if (player.getTowers().get(i).getTimeToNextNormalAttack() == 0) {
					player.getTowers().get(i).normalAttack(currentWave.getEnemies());
                }
                else
                    player.getTowers().get(i).decreaseTimeToNextNormalAttack(50);
            }

            Iterator<Tower> iterator1 = killedTowers.iterator();
            while (iterator1.hasNext()) {
                Tower thisTower = iterator1.next();
                curMap[thisTower.getPosition().x][thisTower.getPosition().y] = ' ';
                background[thisTower.getPosition().x][thisTower.getPosition().y].getImageView().setRotate(-thisTower.getTheta());
                background[thisTower.getPosition().x][thisTower.getPosition().y].getImageView().setImage(grassImage);
                ((landCell)background[thisTower.getPosition().x][thisTower.getPosition().y]).setFilled(false);
                player.getTowers().remove(thisTower);
                iterator1.remove();
            }
            //checking winning condition
            if (currentWave.getEnemies().size() == 0) {
                if (player.waveLevelProperty().getValue() == 30) {
					//sound
					Sound winningSound = new Sound("Winning");
					if (isSoundOn.getValue())
                        winningSound.play();
                    timeline.stop();
                    if (isMultiPlayer) {
                        root.getChildren().remove(table);
                        ((Client)player).sentData("end");
                    }
                    makeEndOfGameScene(endOfGameScene, endOfGameRoot, "You Won!!", isMultiPlayer);
                    ((Stage)root.getScene().getWindow()).setScene(endOfGameScene);
                }
                player.waveLevelProperty().setValue(player.waveLevelProperty().getValue() + 1);
                try {
                    currentWave.upgradeEnemy(); //makes the enemies stronger, slower and more expensive
                    if (player.waveLevelProperty().getValue() <= 5)
                        currentWave = new Wave(EnemyName.None, startPoints, path);
                    else
                        switch ((player.waveLevelProperty().getValue() - 5) % 5) { //checks that enemies come in order of None, Fire ...
                            case 0 :
                                currentWave = new Wave(EnemyName.Light, startPoints, path);
                                break;
                            case 1 :
                                currentWave = new Wave(EnemyName.None, startPoints, path);
                                break;
                            case 2 :
                                currentWave = new Wave(EnemyName.Fire, startPoints, path);
                                break;
                            case 3 :
                                currentWave = new Wave(EnemyName.Tree, startPoints, path);
                                break;
                            case 4 :
                                currentWave = new Wave(EnemyName.Dark, startPoints, path);
                                break;
                        }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.playFromStart();
    }

    public void makeBackground(Group root, ArrayList<Position> castlePoints) {
        for (int i = 0; i < curMap.length; i++) {
            for (int j = 0; j < curMap[i].length; j++) {
                if (curMap[i][j] == ' ') {
                    Rectangle2D auxRectangle2D = new Rectangle2D(0, 0, sizeOfCell, sizeOfCell);
                    ImageView auxImageView = new ImageView(grassImage);
                    auxImageView.setViewport(auxRectangle2D);
                    auxImageView.relocate(j * sizeOfCell, i * sizeOfCell);
                    root.getChildren().add(auxImageView);
                    background[i][j] = new landCell(new Position(i, j), sizeOfCell, grassImage, root);
                }
            }
        }
        for (int i = 0; i < curMap.length; i++) {
            for (int j = 0; j < curMap[i].length; j++) {
                if (curMap[i][j] != ' ') {
                    background[i][j] = new RoadCell(new Position(i, j), sizeOfCell, sandImage, root);
                }
            }
        }
        for (Position castlePoint : castlePoints) {
            ((RoadCell)background[castlePoint.x][castlePoint.y]).setCastle(true);
            background[castlePoint.x][castlePoint.y].getImageView().setImage(castleImage);
        }
    }

    public void makeSoundButton(Group root) {
        ImageView soundButton = new ImageView();
        if (isSoundOn.getValue())
            soundButton.setImage(soundOnImage);
        else
            soundButton.setImage(soundOffImage);
        soundButton.relocate(width - 100, 40);
        soundButton.setFitWidth(70);
        soundButton.setFitHeight(70);
        soundButton.setOnMouseClicked(event -> isSoundOn.setValue(!isSoundOn.getValue()));
        makeZoomInZoomOut(soundButton);
        isSoundOn.addListener((observable, oldValue, newValue) -> {
            if (newValue)
                soundButton.setImage(soundOnImage);
            else
                soundButton.setImage(soundOffImage);
        });
        root.getChildren().add(soundButton);
    }

    public void makeTowerMenu(Group root) {
        ImageView towerMenu = new ImageView(new Image(getClass().getResourceAsStream("resources/RightMenu/Towers.png")));
        towerMenu.relocate(840, 324);
        root.getChildren().add(towerMenu);
        try {
            for (int i = 1; i <= 5; i++) {
                Rectangle2D rectangle2D = new Rectangle2D(0, 0, sizeOfCell, sizeOfCell);
                Image image = new Image(getClass().getResourceAsStream("resources/RightMenu/" +Tower.indexToName(i) + "OnWood" + ".png"));
                ImageView imageView = new ImageView(image);
                imageView.setViewport(rectangle2D);
                imageView.setFitWidth(sizeOfCell);
                imageView.setFitHeight(sizeOfCell);
                imageView.relocate(870, 50 * i + 300);
                root.getChildren().add(imageView);
                final int index = i;
                imageView.setOnDragDetected(event -> {
                    isUpgrading = false;
                    isRemoving = false;
                    Dragboard db = imageView.startDragAndDrop(TransferMode.ANY);
                    db.setDragView(image);
                    ClipboardContent clipboardContent = new ClipboardContent();
                    try {
                        clipboardContent.putString(Tower.indexToName(index));
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                    db.setContent(clipboardContent);
                    event.consume();
                });
                Label towerLabel = new Label(Tower.indexToName(i) + "-" + Main.towerInfo.get(TowerName.values()[i - 1]).costOfBuying);
                towerLabel.relocate(910, 50 * i + 305);
                towerLabel.setFont(new Font(15));
                root.getChildren().add(towerLabel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ImageView towerAdjustingImageView = new ImageView(new Image(getClass().getResourceAsStream("resources/RightMenu/TowerAdjusting.png")));
        towerAdjustingImageView.relocate(840, 612);

        Button upgrade = new Button("Upgrade");
        upgrade.relocate(873, 665);
        upgrade.setOnAction(event -> {
            Sound buttonSound = new Sound("ButtonSound");
            buttonSound.play();
            isUpgrading = true;
            isRemoving = false;
        });
        upgrade.setStyle("-fx-background-color:\n" +
                "            linear-gradient(#ffd65b, #e68400),\n" +
                "            linear-gradient(#ffef84, #f2ba44),\n" +
                "            linear-gradient(#ffea6a, #efaa22),\n" +
                "            linear-gradient(#ffe657 0%, #f8c202 50%, #eea10b 100%);\n" +
                "    -fx-background-radius: 30;\n" +
                "    -fx-background-insets: 0,1,2,3,0;\n" +
                "    -fx-text-fill: #654b00;\n" +
                "    -fx-font-weight: bold;\n" +
                "    -fx-font-size: 18px;\n" +
                "    -fx-padding: 10 10 10 10;");
        upgrade.setOnMouseEntered(event -> makeZoomInZoomOut(upgrade));
        upgrade.setOnMouseExited(event -> makeZoomInZoomOut(upgrade));
        Button remove = new Button("Remove");
        remove.relocate(873, 745);
        remove.setOnAction(event -> {
            Sound buttonSound = new Sound("ButtonSound");
            buttonSound.play();
            isUpgrading = false;
            isRemoving = true;
        });
        remove.setStyle("-fx-background-color:\n" +
                "            linear-gradient(#ffd65b, #e68400),\n" +
                "            linear-gradient(#ffef84, #f2ba44),\n" +
                "            linear-gradient(#ffea6a, #efaa22),\n" +
                "            linear-gradient(#ffe657 0%, #f8c202 50%, #eea10b 100%);\n" +
                "    -fx-background-radius: 30;\n" +
                "    -fx-background-insets: 0,1,2,3,0;\n" +
                "    -fx-text-fill: #654b00;\n" +
                "    -fx-font-weight: bold;\n" +
                "    -fx-font-size: 18px;\n" +
                "    -fx-padding: 10 10 10 10;");
        remove.setOnMouseEntered(event -> makeScaleTransition(remove, 0.05));
        remove.setOnMouseExited(event -> makeScaleTransition(remove, -0.05));
        root.getChildren().addAll(towerAdjustingImageView, upgrade, remove);
    }

    public static void makeScaleTransition(Node node, double scaleBy) {
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(2), node);
        scaleTransition.setByY(scaleBy);
        scaleTransition.setByX(scaleBy);
        scaleTransition.setInterpolator(Interpolator.LINEAR);
        scaleTransition.playFromStart();
    }

    public void makeMenuScene(Scene scene, Group root) {
        makeSoundButton(root);
        scene.setFill(Color.web("#0a2d2e"));
        Label gameName = new Label("Tower Defense");
        gameName.setEffect(new Glow(1));
        gameName.setFont(new Font(60));
        gameName.relocate(width / 2 - 200, height / 5);
        gameName.setTextFill(Color.YELLOWGREEN);

        Button newGame = new Button("New Game");
        makeZoomInZoomOut(newGame);
        newGame.setFont(new Font(30));
        newGame.relocate(width / 2 - 80, height / 2);
        newGame.setOnAction(event -> {
            playButtonSound();
            makeNewGameScene(newGameScene, newGameRoot);
            ((Stage)menuScene.getWindow()).setScene(newGameScene);
        });

        Button loadGame = new Button("Load Game");
        makeZoomInZoomOut(loadGame);
        loadGame.setFont(new Font(30));
        loadGame.relocate(width / 2 - 85, height / 2 + 100);
        loadGame.setOnAction(event -> {
            playButtonSound();
            makeLoadGameScene(loadGameScene, loadGameRoot);
            ((Stage)scene.getWindow()).setScene(loadGameScene);
        });

        Button multiplayer = new Button("Multiplayer");
        makeZoomInZoomOut(multiplayer);
        multiplayer.setFont(new Font(30));
        multiplayer.relocate(width / 2 - 90, height / 2 + 210);
        multiplayer.setOnAction(event -> {
            playButtonSound();
            makeMultiplayerMenuScene(multiplayerMenuScene, multiplayerMenuRoot);
            ((Stage)scene.getWindow()).setScene(multiplayerMenuScene);
        });
        root.getChildren().addAll(gameName, newGame, loadGame, multiplayer);
    }

    public void playButtonSound() {
        Sound buttonSound = new Sound("ButtonSound");
        buttonSound.play();
    }
    public void makeMultiplayerMenuScene(Scene scene, Group root) {
        root.getChildren().clear();
        scene.setFill(Color.web("#0a2d2e"));

        makeBackButton(scene, root);
        Label menuName = new Label("Multiplayer");
        menuName.setFont(new Font(60));
        menuName.setEffect(new Glow(1));
        menuName.setTextFill(Color.YELLOWGREEN);
        menuName.relocate(width / 2 - 160, height / 5);

        Label playerNameLabel = new Label("Your name");
        playerNameLabel.setFont(new Font(25));
        playerNameLabel.relocate(width / 2 - 140, height / 2.5);
        playerNameLabel.setTextFill(Color.ANTIQUEWHITE);
        TextField name = new TextField("shayan");
        name.relocate(width / 2 - 5, height / 2.5);

        Label ip = new Label("IP");
        ip.setFont(new Font(25));
        ip.relocate(width / 2 - 35, height / 2);
        ip.setTextFill(Color.ANTIQUEWHITE);
        TextField ipNum = new TextField("127.0.0.1");
        ipNum.relocate(width / 2 - 5, height / 2);

        Label port = new Label("Port number");
        port.setFont(new Font(25));
        port.relocate(width / 2 - 160, height / 2 + 100);
        port.setTextFill(Color.ANTIQUEWHITE);
        TextField portNum = new TextField("12345");
        portNum.relocate(width / 2 - 5, height / 2 + 100);

        Button connect = new Button("Connect");
        connect.relocate(width / 2 - 50, height / 2 + 200);
        makeZoomInZoomOut(connect);
        connect.setOnAction(event -> {
            if (!ipNum.getText().equals("") && !portNum.getText().equals("") && !name.getText().equals("")) {
                player = new Client(Integer.parseInt(portNum.getText()), ipNum.getText(), name.getText());
                try {
                    ((Client)player).runClient();
                    Sound buttonSound = new Sound("ButtonSound");
                    buttonSound.play();
                    Stage waitStage = new Stage();
                    Group waitRoot = new Group();
                    Scene waitScene = new Scene(waitRoot, 400, 100);
                    waitStage.setScene(waitScene);
                    waitScene.setFill(Color.web("#0a2d2e"));
                    waitStage.initStyle(StageStyle.UNDECORATED);
                    waitStage.initModality(Modality.WINDOW_MODAL);
                    waitStage.initOwner(scene.getWindow());
                    waitStage.show();
                    Label wait = new Label("Wait...");
                    wait.setFont(new Font(40));
                    wait.setEffect(new Glow(1));
                    wait.setTextFill(Color.YELLOWGREEN);
                    wait.relocate(150, 20);
                    waitRoot.getChildren().add(wait);
                    ((Client)player).timeToStart.addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            waitStage.close();
                            makeGameScene(gameRoot, true);
                            ((Stage) scene.getWindow()).setScene(gameScene);
                        }
                    });
                } catch (Exception ex) {
                    Stage errorStage = new Stage();
                    Group errorRoot = new Group();
                    Scene errorScene = new Scene(errorRoot, 400, 200);
                    errorStage.setScene(errorScene);
                    errorScene.setFill(Color.web("#0a2d2e"));
                    errorScene.getStylesheets().add(cssFilePath);
                    errorStage.initModality(Modality.WINDOW_MODAL);
                    errorStage.initOwner(scene.getWindow());
                    errorStage.show();
                    Label error = new Label(ex.getMessage());
                    error.setFont(new Font(35));
                    error.setEffect(new Glow(1));
                    error.setTextFill(Color.YELLOWGREEN);
                    error.relocate(80, 30);
                    Button okButton = new Button("Ok!");
                    okButton.relocate(150, 120);
                    makeZoomInZoomOut(okButton);
                    errorRoot.getChildren().addAll(error, okButton);
                    okButton.setOnAction(event1 -> errorStage.close());
                }
            }
        });
        root.getChildren().addAll(menuName, playerNameLabel, name, ip, ipNum, port, portNum, connect);
    }

    public void makeLoadGameScene(Scene scene, Group root) {
        root.getChildren().clear();
        makeSoundButton(root);
        scene.setFill(Color.web("#0a2d2e"));
        Label menuName = new Label("Load Game");
        menuName.setEffect(new Glow(1));
        menuName.setFont(new Font(60));
        menuName.relocate(width / 2 - 140, height / 5);
        menuName.setTextFill(Color.YELLOWGREEN);

        HashMap<String, Integer> records = readFromFile();
        ComboBox<String> loadComboBox = new ComboBox<>();
        loadComboBox.setValue("Choose user");
        loadComboBox.getItems().addAll(records.keySet());
        loadComboBox.relocate(width / 2 - 170, height / 2);
        makeZoomInZoomOut(loadComboBox);

        Label scoreLabel = new Label("Score : ");
        scoreLabel.relocate(width / 2 + 80, height / 2 + 10);
        scoreLabel.setFont(new Font(25));
        scoreLabel.setTextFill(Color.YELLOWGREEN);

        Label usernameScore = new Label("0");
        usernameScore.relocate(width / 2 + 180, height / 2 + 10);
        usernameScore.setTextFill(Color.YELLOWGREEN);
        usernameScore.setFont(new Font(25));
        loadComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            usernameScore.setText(records.get(loadComboBox.getValue()).toString());
        });

        Button start = new Button("Start");
        start.relocate(width / 2 - 50, height / 2 + 100);
        makeZoomInZoomOut(start);
        start.setOnAction(event -> {
            String playerName = loadComboBox.getValue();
            if (!playerName.equals("Choose user")) {
                playButtonSound();
                player = new Player(playerName, Math.max(Integer.parseInt(usernameScore.getText()) / 4.0, 33.0));
                makeGameScene(gameRoot, false);
                ((Stage) scene.getWindow()).setScene(gameScene);
            }
        });

        makeBackButton(scene, root);
        root.getChildren().addAll(menuName, loadComboBox, usernameScore, scoreLabel, start);
    }

    public static void makeZoomInZoomOut(Node node) {
        node.setOnMouseEntered(event -> makeScaleTransition(node, 0.05));
        node.setOnMouseExited(event -> makeScaleTransition(node, -0.05));
    }

    public void makeBackButton(Scene scene, Group root) {
        Rectangle2D rectangle2D = new Rectangle2D(0, 0, 70, 70);
        ImageView backButton = new ImageView(backButtonImage);
        backButton.setViewport(rectangle2D);
        backButton.relocate(30, height - 100);
        makeZoomInZoomOut(backButton);
        backButton.setOnMouseClicked(event -> {
            playButtonSound();
            ((Stage)scene.getWindow()).setScene(menuScene);
        });
        root.getChildren().add(backButton);
    }

    public void makeNewGameScene(Scene scene, Group root) {
        root.getChildren().clear();
        makeSoundButton(root);
        scene.setFill(Color.web("#0a2d2e"));
        Label menuName = new Label("New Game");
        menuName.setEffect(new Glow(1));
        menuName.setFont(new Font(60));
        menuName.relocate(width / 2 - 140, height / 5);
        menuName.setTextFill(Color.YELLOWGREEN);

        Label playerNameLabel = new Label("Your name");
        playerNameLabel.setFont(new Font(25));
        playerNameLabel.relocate(width / 2 - 140, height / 2);
        playerNameLabel.setTextFill(Color.ANTIQUEWHITE);

        TextField name = new TextField();
        name.relocate(width / 2 - 5, height / 2);

        Button start = new Button("Start");
        start.relocate(width / 2 - 50, height / 2 + 100);
        makeZoomInZoomOut(start);
        start.setOnAction(event -> {
            String playerName = name.getText();
            if (playerName != null && !playerName.equals("")) {
                playButtonSound();
                player = new Player(playerName);
                makeGameScene(gameRoot, false);
                ((Stage) scene.getWindow()).setScene(gameScene);
                timeline.playFromStart();
            }
        });

        makeBackButton(scene, root);
        root.getChildren().addAll(menuName, playerNameLabel, name, start);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Sound backgroundSound = new Sound("BackgroundSound");
        backgroundSound.play();
        backgroundSound.getMediaPlayer().setCycleCount(MediaPlayer.INDEFINITE);
        isSoundOn.addListener((observable, oldValue, newValue) -> {
            if (newValue)
                backgroundSound.play();
            else
                backgroundSound.pause();
        });
        addStyle();
        makeMenuScene(menuScene, menuRoot);
        primaryStage.setTitle("Tower Defense");
        primaryStage.setScene(menuScene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            if (player != null && player.getClass().equals(Client.class)) {
                ((Client)player).closeConnectionCommand();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
