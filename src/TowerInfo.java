import java.io.File;
import java.util.Scanner;

public class TowerInfo {
    int costOfBuying;
    int range;
    int attackPower;
    int reloadTime;

    public TowerInfo(int costOfBuying, int range, int attackPower, int reloadTime) {
        this.costOfBuying = costOfBuying;
        this.range = range;
        this.attackPower = attackPower;
        this.reloadTime = reloadTime;
    }

    public static void readTowerInfo() {
//        File file = new File(String.valueOf(getClass().getResourceAsStream("resources/TowerInfo.txt")));
        File file = new File("/Users/shekshaa/Documents/Courses/Term_2/Advanced Programming Java/Project/Phase2/src/resources/TowerInfo.txt");
        try {
            Scanner scanner = new Scanner(file);
            for (int i = 0; i < 5; i++) {
                int cost = scanner.nextInt();
                int range = scanner.nextInt();
                int attackPower = scanner.nextInt();
                int reloadTime = scanner.nextInt();
                Main.towerInfo.put(TowerName.values()[i], new TowerInfo(cost, range, attackPower, reloadTime));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
