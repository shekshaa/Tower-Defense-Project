import javafx.beans.property.*;

import java.io.Serializable;

public class PlayerInfo implements Serializable, Comparable<PlayerInfo>{
    private IntegerProperty waveLevel = new SimpleIntegerProperty(1);
    private IntegerProperty healthOfCastle = new SimpleIntegerProperty(5);
    private DoubleProperty coin = new SimpleDoubleProperty(33.0);
    private IntegerProperty score = new SimpleIntegerProperty(0);
    private StringProperty name = new SimpleStringProperty();

    public IntegerProperty waveLevelProperty() {
        return waveLevel;
    }

    public IntegerProperty healthOfCastleProperty() {
        return healthOfCastle;
    }

    public DoubleProperty coinProperty() {
        return coin;
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public String getName() {
        return name.getValue();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public int compareTo(PlayerInfo o) {
        return o.score.getValue() - score.getValue();
    }
}
