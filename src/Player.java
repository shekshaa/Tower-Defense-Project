import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import java.util.ArrayList;

public class Player{

    private GameMap myMap;
    private PlayerInfo info;
    private ArrayList<Tower> towers = new ArrayList<>();
    private ArrayList<InnerFire> innerFires = new ArrayList<>();

    public Player(String name) {
        info = new PlayerInfo();
        info.setName(name);
        info.coinProperty().setValue(33.0);
    }

    public Player(String name, double coin) {
        this(name);
        this.info.coinProperty().setValue(coin);
    }

    public GameMap getMyMap() {
        return myMap;
    }

    public void setMyMap(GameMap myMap) {
        this.myMap = myMap;
    }

    public PlayerInfo getInfo() {
        return info;
    }

    public IntegerProperty waveLevelProperty() {
        return info.waveLevelProperty();
    }

    public ArrayList<Tower> getTowers() {
        return towers;
    }

    public DoubleProperty getCoin() {
        return info.coinProperty();
    }

    public ArrayList<InnerFire> getInnerFires() {
        return innerFires;
    }

    public void decreaseHealthOfCastle() {
        this.healthOfCastleProperty().setValue(this.healthOfCastleProperty().getValue() - 1);
    }

    public void addCoin(double addedCoin) {
        this.info.coinProperty().setValue(info.coinProperty().getValue() + addedCoin);
    }

    public void decreaseCoin(double decreasedCoin) {
        this.info.coinProperty().setValue(info.coinProperty().getValue() - decreasedCoin);
    }

    public void addScore(int addedScore) {
        info.scoreProperty().setValue(info.scoreProperty().getValue() + addedScore);
    }

    public IntegerProperty getScore() {
        return info.scoreProperty();
    }

    public IntegerProperty healthOfCastleProperty() {
        return info.healthOfCastleProperty();
    }

    public void setCoin(double coin) {
        this.info.coinProperty().setValue(coin);
    }
}
