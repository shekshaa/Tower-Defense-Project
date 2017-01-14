import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;

public class Cell {
    private Position position;
    private ImageView imageView;

    public Cell(Position position, int size, Image image, Group root) {
        this.position = position;
        Rectangle2D rectangle2D = new Rectangle2D(0, 0, size, size);
        imageView = new ImageView();
        imageView.setViewport(rectangle2D);
        imageView.setImage(image);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        imageView.relocate(position.y * size, position.x * size);
        root.getChildren().add(imageView);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public Position getPosition() {
        return position;
    }

}

//cell that contains grass and towers
class landCell extends Cell {
    private Tower tower;
    private boolean isFilled;
    private TowerName towerType;

    private Image lastImage;
    private boolean droppedTower; //

    public landCell(Position position, int size, Image image, Group root) {
        super(position, size, image, root);
        isFilled = false;
        ImageView imageView = getImageView();
        imageView.setOnMouseClicked(event1 -> {
            if (Main.isUpgrading) {
                if (isFilled && towerType != TowerName.Combined) {
                    boolean isUpgraded = tower.upgrade();
                    if (isUpgraded) {
						System.out.println("Upgraded!");

						//sound
						Sound upgradeSound = new Sound("Upgrade");
                        if (Main.isSoundOn.getValue())
						    upgradeSound.play();
					}
                    Main.isUpgrading = false;
                }
            }
            else if (Main.isRemoving) {
                if (isFilled) {
                    Main.player.addCoin(tower.getCurrentCost() / 2.0);
                    Main.background[position.x][position.y].getImageView().setRotate(-tower.getTheta());
                    Main.background[position.x][position.y].getImageView().setImage(new Image(getClass().getResourceAsStream("resources/grass2.png")));
                    Main.player.getTowers().remove(tower);
                    isFilled = false;
                    towerType = null;
                    tower = null;

					//sound
					Sound removeSound = new Sound("Remove");
                    if (Main.isSoundOn.getValue())
					    removeSound.play();
                }
            }
        });

        //when player drags a tower on a cell
        imageView.setOnDragOver(event -> {
            try {
                //checks if player has enough money
                if (Main.towerInfo.get(TowerName.values()[Tower.nameToIndex(event.getDragboard().getString())]).costOfBuying <= Main.player.getCoin().getValue()) {
                    if (!isFilled) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    //if cell is fillet, method must check that none of the towers are NoneTower and two towers are not the same
                    else if (towerType != TowerName.Combined && !towerType.toString().equals(event.getDragboard().getString()) && !towerType.toString().equals("None") && !event.getDragboard().getString().equals("None")) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            event.consume();
        });
        imageView.setOnDragDropped(event -> {
            String nameOfTower = event.getDragboard().getString();
            TowerInfo info = Main.towerInfo.get(TowerName.valueOf(nameOfTower));
            getImageView().setImage(new Image(getClass().getResourceAsStream("resources/" + nameOfTower + "Tower" + ".png")));
            Main.player.decreaseCoin(info.costOfBuying);
            try {
                if (!isFilled) { //creating a new tower
                    //puts the firs character of tower name in the curMap
                    Main.curMap[getPosition().x][getPosition().y] = nameOfTower.charAt(0);
                    switch (nameOfTower) {
                        case "None":
                            tower = new NoneTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range);
                            break;
                        case "Fire":
                            tower = new FireTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range);
                            break;
                        case "Tree":
                            tower = new TreeTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range);
                            break;
                        case "Light":
                            tower = new LightTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range);
                            break;
                        case "Dark":
                            tower = new DarkTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range);
                            break;
                    }
                    Main.player.getTowers().add(tower);
                    this.towerType = TowerName.values()[Tower.nameToIndex(nameOfTower)]; //saves type of new tower in towerType of this cell
                    //for detecting creation of CombinedTower
                    isFilled = true;
                } else {
                    Main.curMap[getPosition().x][getPosition().y] = 'C';
                    Main.player.getTowers().remove(tower); //creates a new Tower for CombinedTower, so the old tower should
                    //be removed from player's towers
                    switch (nameOfTower) {
                        case "None":
                            tower = new CombinedTower(tower,
                                    new NoneTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range));
                            break;
                        case "Fire":
                            tower = new CombinedTower(tower,
                                    new FireTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range));
                            break;
                        case "Tree":
                            tower = new CombinedTower(tower,
                                    new TreeTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range));
                            break;
                        case "Light":
                            tower = new CombinedTower(tower,
                                    new LightTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range));
                            break;
                        case "Dark":
                            tower = new CombinedTower(tower,
                                    new DarkTower(getPosition(), info.attackPower, info.reloadTime, info.costOfBuying, info.range));
                            break;
                    }
                    getImageView().setImage(new Image(getClass().getResourceAsStream("resources/CombinedTower.png")));
                    Main.player.getTowers().add(tower);
                    towerType = TowerName.Combined; //saves Combined name in towerType, so that next time creating CombinedTower won't be possible
                }
                droppedTower = true; //this boolean is for dragExited method, because Dropped event is before Exited,
                //if player has dropped a new tower, after realising mouse click cell's image should not be changed(or else the tower will be invisible)

                //sound
				Sound newTowerSound = new Sound("NewTower");
				if (Main.isSoundOn.getValue())
                    newTowerSound.play();

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            event.consume();
        });
        imageView.setOnDragEntered(event -> {
            lastImage = getImageView().getImage(); //saves current image for using in dragExited event

            //conditions are like those in DragOver
            if (Main.player.getCoin().getValue() < Main.towerInfo.get(TowerName.values()[Tower.nameToIndex(event.getDragboard().getString())]).costOfBuying) {
                getImageView().setImage(new Image(getClass().getResourceAsStream("resources/NotOK.png")));
            } else if (!isFilled) {
                    getImageView().setImage(new Image(getClass().getResourceAsStream("resources/OK.png")));
                } else if (!towerType.toString().equals("None") && !event.getDragboard().getString().equals("None") && towerType != TowerName.Combined && !towerType.toString().equals(event.getDragboard().getString())) {
                    getImageView().setImage(new Image(getClass().getResourceAsStream("resources/OK.png")));
                }
                else {
                    getImageView().setImage(new Image(getClass().getResourceAsStream("resources/NotOK.png")));
                }
        });
        imageView.setOnDragExited(event -> {
            if (!droppedTower) { //for creating a new tower the cell's image shouldn't change and must stay the same as before(the tower image)
                getImageView().setImage(lastImage);
            }
            droppedTower = false; //so that the image of cell changes next time
        });
    }

    public void setFilled(boolean filled) {
        isFilled = filled;
    }
}

//cell that contains path and enemies
class RoadCell extends Cell {

    private int enemyCounter;
    private Direction direction;
    private boolean isCastle;
    private Image lastImage;

    public RoadCell(Position position, int size, Image image, Group root) {
        super(position, size, image, root);
        enemyCounter = 0;

        ImageView imageView = getImageView();
        imageView.setBlendMode(BlendMode.SRC_ATOP);

        //dropping is always impossible in roadCells
        imageView.setOnDragEntered(event -> {
            if (event.getGestureSource() != imageView) {
                lastImage = getImageView().getImage();
                getImageView().setImage(new Image(getClass().getResourceAsStream("resources/NotOK.png")));
            }
        });
        imageView.setOnDragExited(event -> {
            if (enemyCounter == 0) { //if an enemy has moved out of the cell, last image is enemyImage and it shouldn't be replaced as cellImage
                getImageView().setImage(new Image(getClass().getResourceAsStream("resources/" + (!isCastle ? "sand" : "castle") + ".png")));
            }
            else {
                getImageView().setImage(lastImage);
            }
        });
    }

    public void setCastle(boolean castle) {
        isCastle = castle;
    }

    public void removeEnemy() {
        enemyCounter--;
    }

    public void addEnemy() {
        enemyCounter++;
    }

    public int getEnemyCounter() {
        return enemyCounter;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
