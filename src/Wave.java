import javafx.scene.image.Image;

import java.util.ArrayList;

public class Wave {

    private ArrayList<Enemy> enemies = new ArrayList<>();
    private EnemyName enemyName;

    public Wave(EnemyName enemyName, ArrayList<Position> startPoints, ArrayList<Position>[] path){
        this.enemyName = enemyName;
        EnemyInfo enemyInfo = Main.enemyInfo.get(enemyName); //uses EnemyInfo class data for creating a new enemy, this class data are changed after each wave
        switch (enemyName) {
            case None:
                for (int i = 0; i < enemyInfo.number / startPoints.size(); i++) {
                    for (int j = 0; j < startPoints.size(); j++) {
                        Enemy enemy = new NoneEnemy(startPoints.get(j).x, startPoints.get(j).y, path[j], 1000 + 2 * i * enemyInfo.speed, enemyInfo);
                        getEnemies().add(enemy);
                        Direction direction = ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).getDirection();
                        ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).addEnemy();
                        Main.background[enemy.getPosition().x][enemy.getPosition().y].getImageView().setImage(new Image(getClass().getResourceAsStream("resources/Tanks/" + enemyName + direction + ".png")));
                    }
                }
                break;
            case Tree:
                for (int i = 0; i < enemyInfo.number / startPoints.size(); i++) {
                    for (int j = 0; j < startPoints.size(); j++) {
                        Enemy enemy = new TreeEnemy(startPoints.get(j).x, startPoints.get(j).y, path[j], 1000 + 2 * i * enemyInfo.speed, enemyInfo);
                        getEnemies().add(enemy);
                        Direction direction = ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).getDirection();
                        ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).addEnemy();
                        Main.background[enemy.getPosition().x][enemy.getPosition().y].getImageView().setImage(new Image(getClass().getResourceAsStream("resources/Tanks/" + enemyName + direction + ".png")));
                    }
                }
                break;
            case Light:
                for (int i = 0; i < enemyInfo.number / startPoints.size(); i++) {
                    for (int j = 0; j < startPoints.size(); j++) {
                        Enemy enemy = new LightEnemy(startPoints.get(j).x, startPoints.get(j).y, path[j], 1000 + 2 * i * enemyInfo.speed, enemyInfo);
                        getEnemies().add(enemy);
                        Direction direction = ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).getDirection();
                        ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).addEnemy();
                        Main.background[enemy.getPosition().x][enemy.getPosition().y].getImageView().setImage(new Image(getClass().getResourceAsStream("resources/Tanks/" + enemyName + direction + ".png")));
                    }
                }
                break;
            case Dark:
                for (int i = 0; i < enemyInfo.number / startPoints.size(); i++) {
                    for (int j = 0; j < startPoints.size(); j++) {
                        Enemy enemy = new DarkEnemy(startPoints.get(j).x, startPoints.get(j).y, path[j], 1000 + 2 * i * enemyInfo.speed, enemyInfo);
                        getEnemies().add(enemy);
                        Direction direction = ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).getDirection();
                        ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).addEnemy();
                        Main.background[enemy.getPosition().x][enemy.getPosition().y].getImageView().setImage(new Image(getClass().getResourceAsStream("resources/Tanks/" + enemyName + direction + ".png")));
                    }
                }
                break;
            case Fire:
                for (int i = 0; i < enemyInfo.number / startPoints.size(); i++) {
                    for (int j = 0; j < startPoints.size(); j++) {
                        Enemy enemy = new FireEnemy(startPoints.get(j).x, startPoints.get(j).y, path[j], 1000 + 2 * i * enemyInfo.speed, enemyInfo);
                        getEnemies().add(enemy);
                        Direction direction = ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).getDirection();
                        ((RoadCell)Main.background[enemy.getPosition().x][enemy.getPosition().y]).addEnemy();
                        Main.background[enemy.getPosition().x][enemy.getPosition().y].getImageView().setImage(new Image(getClass().getResourceAsStream("resources/Tanks/" + enemyName + direction + ".png")));
                    }
                }
                break;
        }

    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    //changes information stored in EnemyInfo class, because new wave information is based on this class data
    public void upgradeEnemy() throws Exception{
        EnemyInfo enemyInfo = Main.enemyInfo.get(enemyName);
        enemyInfo.speed += 50;
        enemyInfo.cost += 0.25;
        enemyInfo.health += 200;
    }
}







