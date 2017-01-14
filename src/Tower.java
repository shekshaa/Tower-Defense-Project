import javafx.animation.*;
import javafx.util.Duration;
import java.util.ArrayList;

enum TowerName {
    None, Fire, Tree, Light, Dark, Combined
}

public abstract class Tower {

    private Position position;
    private int attackPower;
    private int reloadTime;
    private int costOfBuying;
    private int currentCost;
    private int range;
    private int timeToNextNormalAttack;
    private Enemy enemyToAttack;
    //index 0-4 are for Tree, Dark,... . if tower should attack the enemy with double damage,
    // performance[i] is set 1(for half damage -1, and for normal attack 0)
    public int[] performance = new int[5];
    private int level;
    private double Theta;

    public static String indexToName(int index){
        switch (index) {
            case 1 :
                return "None";
            case 2 :
                return "Fire";
            case 3 :
                return "Tree";
            case 4 :
                return "Light";
            case 5 :
                return "Dark";
            default:
                return "None";
        }
    }

    public static int nameToIndex(String name){
        switch (name) {
            case "None" :
                return 0;
            case "Fire" :
                return 1;
            case "Tree" :
                return 2;
            case "Light" :
                return 3;
            case "Dark" :
                return 4;
            default:
                return 0;
        }
    }

    abstract void specialAbility();

    public Tower(Position position, int attackPower, int reloadTime, int costOfBuying, int range) {
        this.position = position;
        this.attackPower = attackPower;
        this.reloadTime = reloadTime;
        this.costOfBuying = costOfBuying;
        this.currentCost = costOfBuying;
        this.range = range;
        level = 0;
    }

    public int targetIndex(ArrayList<Enemy> enemies) {
        for (int i = 0; i < enemies.size(); i++) {
            if (distance(enemies.get(i).getPosition().x, enemies.get(i).getPosition().y) <= range * range) {
                return i;
            }
        }
        return -1;
    }

    private void addLevel() {
        level++;
    }

    public void addReloadTime(int delay) {
        this.reloadTime += delay;
    }

    public void normalAttack(ArrayList<Enemy> enemies) {
        int index = targetIndex(enemies);
        if (index != -1) { //no enemy was found
            //checks whether if tower should enemy with double or half damage
            if (performance[enemies.get(index).getEnemyName().ordinal()] >= 1)
                enemies.get(index).getAttacked(this, 2 * attackPower);
            else if (performance[enemies.get(index).getEnemyName().ordinal()] <= -1)
                enemies.get(index).getAttacked(this, attackPower / 2);
            else
                enemies.get(index).getAttacked(this, attackPower);
            enemyToAttack = enemies.get(index);
            Position enemyPosition = enemyToAttack.getPosition();
            double nextTheta = Math.atan((double)(enemyPosition.x - position.x) / (double)(enemyPosition.y - position.y));
            if (enemyPosition.y - position.y < 0) {
                nextTheta = Math.PI + nextTheta;
            }
            if (enemyPosition.y - position.y == 0) {
                if (enemyPosition.x < position.x) {
                    nextTheta = (3 * Math.PI / 2);
                } else
                    nextTheta = (Math.PI / 2);
            }
//			//sound (makes the game laggy)
//			Sound attackSound = new Sound("Attack");
//			attackSound.play();
            Timeline timeLine = new Timeline();
            KeyValue keyValue = new KeyValue(Main.background[position.x][position.y].getImageView().rotateProperty() , nextTheta * 180 / Math.PI, Interpolator.LINEAR);
            timeLine.getKeyFrames().add(new KeyFrame(new Duration(100), keyValue));
            Theta = nextTheta;
            timeLine.play();
        }
        else
            enemyToAttack = null;
        timeToNextNormalAttack = reloadTime;
        specialAbility();
	}

    public double getTheta() {
        return Theta;
    }

    public int distance(int x, int y) {
        return (position.x - x) * (position.x - x) + (position.y - y) * (position.y - y);
    }

    public int[] getPerformance() {
        return performance;
    }

    public boolean upgrade() {
        if (level < 3 && Main.player.getCoin().getValue() >= costOfBuying / 2) {
            addLevel();
            currentCost += costOfBuying / 2;
            attackPower += 100;
            reloadTime += 50;
            Main.player.decreaseCoin(costOfBuying / 2);
            //System.out.println(Main.player.getCoin().getValue());
            return true;
        }
        return false;
    }

    public int getReloadTime() {
        return reloadTime;
    }

    public Enemy getEnemyToAttack() {
        return enemyToAttack;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public Position getPosition() {
        return position;
    }

    public int getCurrentCost() {
        return currentCost;
    }

    public int getRange() {
        return range;
    }

    public int getTimeToNextNormalAttack() {
        return timeToNextNormalAttack;
    }


    public void decreaseTimeToNextNormalAttack(int time) {
        timeToNextNormalAttack -= time;
    }
}

class NoneTower extends Tower {

    public NoneTower(Position position, int attackPower, int reloadTime, int costOfBuying, int range) {
        super(position, attackPower, reloadTime, costOfBuying, range);
    }

    @Override
    void specialAbility() {
    }
}

class FireTower extends Tower {

    public FireTower(Position position, int attackPower, int reloadTime, int costOfBuying, int range) {
        super(position, attackPower, reloadTime, costOfBuying, range);
        performance[1] = performance[3] = performance[4] = 0;
        performance[0] = 1;
        performance[2] = -1;
    }

    @Override
    void specialAbility() {
        if (getEnemyToAttack() != null)
            Main.player.getInnerFires().add(new InnerFire(this, getEnemyToAttack(), 2, 50));
    }

}

class TreeTower extends Tower {

    public TreeTower(Position position, int attackPower, int reloadTime, int costOfBuying, int range) {
        super(position, attackPower, reloadTime, costOfBuying, range);
        performance[3] = -1;
        performance[1] = 1;
        performance[0] = performance[2] = performance[4] = 0;
    }

    @Override
    void specialAbility() {
        if (getEnemyToAttack() != null) {
            getEnemyToAttack().stun(100);
        }
    }


}

class LightTower extends Tower {

    public LightTower(Position position, int attackPower, int reloadTime, int costOfBuying, int range) {
        super(position, attackPower, reloadTime, costOfBuying, range);
        performance[2] = 1;
        performance[0] = -1;
        performance[1] = performance[3] = performance[4] = 0;
    }

    @Override
    void specialAbility() {
        int targetEnemyX, targetEnemyY;
        if (getEnemyToAttack() != null) {
            targetEnemyX = getEnemyToAttack().getPosition().x;
            targetEnemyY = getEnemyToAttack().getPosition().y;
            Main.currentWave.getEnemies().stream().filter(enemy -> Math.abs(enemy.getPosition().x - targetEnemyX) <= 1 && Math.abs(enemy.getPosition().y - targetEnemyY) <= 1).forEach(enemy -> {
                enemy.makeBullet(this);
                enemy.harmEnemy((int) (0.4 * this.getAttackPower()));
            });
        }
    }
}

class DarkTower extends Tower {

    public DarkTower(Position position, int attackPower, int reloadTime, int costOfBuying, int range) {
        super(position, attackPower, reloadTime, costOfBuying, range);
        performance[3] = 1;
        performance[1] = -1;
        performance[0] = performance[2] = performance[4] = 0;
    }

    @Override
    void specialAbility() {
        if (getEnemyToAttack() != null) {
            getEnemyToAttack().decreaseSpeed(50);
        }
    }
}

//gets attack power, reload time, ... from first tower and uses them for normal attack,
//for special ability calls both special abilities of main and added towers
class CombinedTower extends Tower {
    Tower mainTower, addedTower;

    public CombinedTower(Tower mainTower, Tower addedTower){
        super(mainTower.getPosition(), mainTower.getAttackPower(), mainTower.getReloadTime(),
                mainTower.getCurrentCost() + addedTower.getCurrentCost(), mainTower.getRange());
        this.mainTower = mainTower;
        this.addedTower = addedTower;
        for (int i = 0; i < 5; i++) {
            this.getPerformance()[i] = mainTower.getPerformance()[i] + addedTower.getPerformance()[i];
        }
    }

    @Override
    void specialAbility() {
        mainTower.specialAbility();
        addedTower.specialAbility();
    }
}




