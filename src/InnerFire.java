public class InnerFire {

    private Tower sourceTower;
    private Enemy targetEnemy;
    private final int damage;
    private int numOfBullet;//usually 2

    public InnerFire(Tower sourceTower, Enemy enemy, int numOfBullet, int damage) {
        this.sourceTower = sourceTower;
        this.targetEnemy = enemy;
        this.numOfBullet = numOfBullet;
        this.damage = damage;
    }

    public void decreaseNumOfBullet() {
        this.numOfBullet--;
    }

    //attack the enemy with special ability of Fire Tower(Inner fire)
    public boolean attack() {
        targetEnemy.makeBullet(sourceTower);
        targetEnemy.harmEnemy(damage);
        decreaseNumOfBullet();
        return (numOfBullet == 0);
    }
}
