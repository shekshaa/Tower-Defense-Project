import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Random;

enum EnemyName {
    Tree, Dark, Light, Fire, None
}

public abstract class Enemy implements Comparable<Enemy>{
    private ArrayList<Position> path;
    private int indexInPath;
    private int toCastle;
    private int health;
    private int speed;
    private double cost;
    private int timeToNextMove;
    private Position position;
    private EnemyName enemyName;
    private BooleanProperty canBeRemoved;

    public Enemy(int x, int y, ArrayList<Position> path, int timeToNextMove, EnemyInfo enemyInfo) {
        this.path = path;
        this.indexInPath = 0;
        this.toCastle = path.size();
        this.timeToNextMove = timeToNextMove;
        this.position = new Position(x, y);
        this.speed = enemyInfo.speed;
        this.cost = enemyInfo.cost;
        this.health = enemyInfo.health;
        canBeRemoved = new SimpleBooleanProperty(true);
        canBeRemoved.addListener((observable, oldValue, newValue) -> {
            if (newValue && health <= 0) {
                Position deadEnemyPosition = path.get(indexInPath);
                ((RoadCell)Main.background[deadEnemyPosition.x][deadEnemyPosition.y]).removeEnemy();
                if (((RoadCell)Main.background[deadEnemyPosition.x][deadEnemyPosition.y]).getEnemyCounter() == 0) {
                    Main.background[deadEnemyPosition.x][deadEnemyPosition.y].getImageView().setImage(new Image(getClass().getResourceAsStream("resources/sand.png")));
                    Main.curMap[deadEnemyPosition.x][deadEnemyPosition.y] = '.';
                }
                Main.player.addCoin(cost);
                Main.player.addScore((int)(cost * 4));
                Main.currentWave.getEnemies().remove(Enemy.this);

//				//sound (Extremely Annoying :D)
//				Sound dieSound = new Sound("Die");
//				dieSound.play();
            }
        });
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public EnemyName getEnemyName() {
        return enemyName;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void addIndexInPath() {
        this.indexInPath++;
    }

    public int getToCastle() {
        return toCastle;
    }

    public void subtractToCastle() {
        this.toCastle--;
    }

    abstract public void getAttacked(Tower tower, int damage);

    public void harmEnemy(int damage) {
        this.health -= damage;
    }

    //make a path transition of bullet from tower to enemy
    public void makeBullet(Tower tower) {
        Position towerPosition = tower.getPosition();
        Position enemyPosition = this.position;
        Rectangle2D rectangle2D = new Rectangle2D(0, 0, 20, 20);
        ImageView bullet = new ImageView(new Image(getClass().getResourceAsStream("resources/fireBall.png")));
        bullet.relocate(-50, -50);
        bullet.setViewport(rectangle2D);
        Main.gameRoot.getChildren().add(bullet);
        Path path = new Path();
        path.getElements().addAll(new MoveTo(towerPosition.y * Main.sizeOfCell + Main.sizeOfCell / 2 + 50, towerPosition.x * Main.sizeOfCell + Main.sizeOfCell / 2 + 50),
                new LineTo(enemyPosition.y * Main.sizeOfCell + Main.sizeOfCell / 2 + 50, enemyPosition.x * Main.sizeOfCell + Main.sizeOfCell / 2 + 50));
        PathTransition pathTransition = new PathTransition(new Duration(Math.max(50, timeToNextMove) - 10), path, bullet);
        pathTransition.setInterpolator(Interpolator.LINEAR);
        pathTransition.playFromStart();
        canBeRemoved.setValue(false);
        pathTransition.setOnFinished(event -> {
            Main.gameRoot.getChildren().remove(bullet);
            canBeRemoved.setValue(true);
        });
    }

    public int getSpeed() {
        return speed;
    }

    public int getTimeToNextMove() {
        return timeToNextMove;
    }

    public void setTimeToNextMove() {
        this.timeToNextMove = speed;
    }

    public void stun(int time) { //added stun function for treeTower special attack
        timeToNextMove += time;
    }

    public void decreaseTimeToNextMove(int time) {
        this.timeToNextMove -= time;
    }

    public void move() {
        subtractToCastle();
        addIndexInPath();
        setTimeToNextMove();
        if (indexInPath != path.size())//change position if it is not the last cell of path
            setPosition(path.get(indexInPath));
    }

    public void decreaseSpeed(int time) {
        speed += time;
    }

    @Override
    public int compareTo(Enemy o) {
        if (this.health != o.health)
            return this.health - o.health;
        return this.toCastle - o.toCastle;
    }

    public void setEnemyName(EnemyName enemyName) {
        this.enemyName = enemyName;
    }

}

class NoneEnemy extends Enemy {

    public NoneEnemy(int x, int y, ArrayList<Position> path, int timeToNextMove, EnemyInfo enemyInfo) {
        super(x, y, path, timeToNextMove, enemyInfo);
        setEnemyName(EnemyName.None);
    }

    @Override
    public void getAttacked(Tower tower, int damage) {
        harmEnemy(damage);
        makeBullet(tower);
    }
}

class TreeEnemy extends Enemy{

    public TreeEnemy(int x, int y, ArrayList<Position> path, int timeToNextMove, EnemyInfo enemyInfo) {
        super(x, y, path, timeToNextMove, enemyInfo);
        setEnemyName(EnemyName.Tree);
    }

    @Override
    public void getAttacked(Tower tower, int damage) {
        Random random = new Random();
        int prob = random.nextInt(5);
        if (prob != 0) {
            makeBullet(tower);
            harmEnemy(damage);
        }
    }
}

class DarkEnemy extends Enemy{

    public DarkEnemy(int x, int y, ArrayList<Position> path, int timeToNextMove, EnemyInfo enemyInfo) {
        super(x, y, path, timeToNextMove, enemyInfo);
        setEnemyName(EnemyName.Dark);
    }

    @Override
    public void getAttacked(Tower tower, int damage) {
        Random random = new Random();
        int prob = random.nextInt(20);
        makeBullet(tower);
        harmEnemy(damage);
        if (prob == 0) {
            Main.killedTowers.add(tower);
        }
    }
}

class LightEnemy extends Enemy{

    public LightEnemy(int x, int y, ArrayList<Position> path, int timeToNextMove, EnemyInfo enemyInfo) {
        super(x, y, path, timeToNextMove, enemyInfo);
        setEnemyName(EnemyName.Light);
    }

    @Override
    public void getAttacked(Tower tower, int damage) {
        makeBullet(tower);
        harmEnemy(damage);
        Random random = new Random();
        int prob = random.nextInt(5);
        if (prob <= 1 && this.getSpeed() > 100) {
            this.setSpeed(this.getSpeed() / 2);
        }
    }
}

class FireEnemy extends Enemy{

    public FireEnemy(int x, int y, ArrayList<Position> path, int timeToNextMove, EnemyInfo enemyInfo) {
        super(x, y, path, timeToNextMove, enemyInfo);
        setEnemyName(EnemyName.Fire);
    }

    @Override
    public void getAttacked(Tower tower, int damage) {
        makeBullet(tower);
        harmEnemy(damage);
        Random random = new Random();
        int prob = random.nextInt(5);
        if (prob <= 1) {
            tower.addReloadTime(50);
        }
    }
}