import java.io.File;
import java.util.Scanner;

public class EnemyInfo {
    double cost;
    int health;
    int speed;
    int number;

    public EnemyInfo(double cost, int health, int speed, int number) {
        this.cost = cost;
        this.health = health;
        this.speed = speed;
        this.number = number;
    }
    public static void readEnemyInfo() {
//        File file = new File(String.valueOf(getClass().getResourceAsStream("resources/EnemyInfo.txt")));
        File file = new File("/Users/shekshaa/Documents/Courses/Term_2/Advanced Programming Java/Project/Phase2/src/resources/EnemyInfo.txt");
        try {
            Scanner scanner = new Scanner(file);
            for (int i = 0; i < 5; i++) {
                double cost = scanner.nextDouble();
                int health = scanner.nextInt();
                int speed = scanner.nextInt();
                int number = scanner.nextInt();
                Main.enemyInfo.put(EnemyName.values()[i], new EnemyInfo(cost, health, speed, number));
            }
            scanner.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
