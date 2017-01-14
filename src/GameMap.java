import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

enum Direction {
	Up(0), Right(1), Down(2), Left(3);

	int number;

	Direction(int number) {
		this.number = number;
	}
}

class GameMap implements Serializable{
	private int rows;
	private int columns;
	private int[][] map;
	/*private File mapFile;*/
	private ArrayList<Position> startingPoints = new ArrayList<>();
	private ArrayList<Position> castlePoints = new ArrayList<>();
	private int lineWidth = 0;

	public GameMap(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		map = new int[rows + 2][columns + 2];
	}

	public int[][] getMap(){
		return map;
	}



	public ArrayList<Position> getStartingPoints() {
		if (startingPoints.size() != 0)
			return startingPoints;
		else {
			for (int i = 1; i < rows + 1; i++) {
				for (int j = 1; j < columns + 1; j++) {
					if (map[i][j] == -2) {
						//age masir e voroodi ofoghi bashe
						if (i == 1 || i == rows) {
							while (map[i][j] == -2) {
								startingPoints.add(new Position(i, j));
								j++; //raftan be khooneye baadi(tooye sotoon e baadi)
								lineWidth++;
							}
						}
						//age masir e voroodi amoodi bashe
						else {
							while (map[i][j] == -2) {
								startingPoints.add(new Position(i, j));
								i++; //raftan be khooneye baadi(tooye radif e baadi)
								lineWidth++;
							}
						}
						break;
					}
				}
			}
		}
		return startingPoints;
	}

	public ArrayList<Position> getCastlePoints() {
		if (castlePoints.size() != 0)
			return castlePoints;
		else {
			for (int i = 1; i < rows + 1; i++) {
				for (int j = 0; j < columns + 1; j++) {
					if (map[i][j] == -3)
						castlePoints.add(new Position(i, j));
				}
			}
		}

		return castlePoints;
	}

	public ArrayList<Position>[] reFillMapCells() {
		Position currentCell;
		Position nextCell;

		ArrayList<Position>[] path = new ArrayList[lineWidth];
		for (int i = 0; i < lineWidth; i++) {
			path[i] = new ArrayList<>();
		}

		for (int i = 0; i < lineWidth; i++) {
			currentCell = startingPoints.get(i); //az in khoone shoroo mikonim va edameye masir ro peida mikonim
			path[i].add(currentCell);
			while (true) {
				//khooneye baadi ba vizhegi haye gofte shode ro peida mikone va dakhele nextCell mirize
				nextCell = nextCellFinder(currentCell, -1, i);
				map[currentCell.x][currentCell.y] = i + 1; //shomare line ro mizare too khooneE ke alan hast
				if (nextCell.x == 0) { //age barname be akhare masir reside bood
					nextCell = nextCellFinder(currentCell, -3, i);
					path[i].add(nextCell);
					map[nextCell.x][nextCell.y] = i + 1;
					break; //vared e line baadi e masir mishe
				}
				currentCell = nextCell; //mirim be khooneye jadidi ke peida shode
				path[i].add(currentCell);
			}
		}
		return path;
	}

	public Position nextCellFinder(Position curPosition, int firstKindSearch, int secondKindSearch) {
		Position result = new Position(0 ,0);
		//khoonehayi check mishan ke dar rastaye curPosition bashan, chon emkan nadare ke khooneye
		//baadiE masir goosheye khooneye fe'li bashe
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				//check kardan e in ke khoone dar rastaye curPosition bashe va khode curPosition nabashe
				if ((i * j == 0) && //i ya j yekishoon 0 bashan
						(i + j != 0)) { // i va j har do hamzaman 0 nabashan
					if (map[curPosition.x + i][curPosition.y + j] == firstKindSearch) {
						if (secondPositionChecker(curPosition.x + i, curPosition.y + j, secondKindSearch)) {
							result.setXY(curPosition.x + i, curPosition.y + j);
							return result;
						}
					}
				}
			}
		}
		return result;
	}

	public boolean secondPositionChecker(int curI, int curJ, int secondKindSearch) {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if ((i != 0) || (j != 0)) {
					if (map[curI + i][curJ + j] == secondKindSearch) {
						return true;
					}
				}
			}
		}
		return false;
	}



	public void createMap() {
		lineWidth = 5;
		Random randomGenerator = new Random();
		int startP, startQ, startSide;
        //p and q show the upper left cell of a block (block's dimension is lineWidth * length, which length is randomly created each time)
		int p, q, lastP, lastQ, length, lastLength;
		startSide = randomGenerator.nextInt(4);
		Direction direction, lastDirection; //direction is created randomly each time

		ArrayList<Direction> uncheckedDirections = new ArrayList<>();
		uncheckedDirections.add(Direction.Up);
		uncheckedDirections.add(Direction.Right);
		uncheckedDirections.add(Direction.Down);
		uncheckedDirections.add(Direction.Left);
		int numOfLeftDirections;


		boolean isVertical;
		length = randomGenerator.nextInt(5) + lineWidth + 1;
		switch (startSide) { // firs and last line of map are not allowed
			case 0 : //top side
				startP = 1;
				startQ = randomGenerator.nextInt(columns + 1 - lineWidth) + 1; //q -> [1, columns + 1 - lineWidth]
				p = startP;
				q = startQ;
				direction = Direction.Down;
				isVertical = true;
				break;
			case 1 : //right side
				startP = randomGenerator.nextInt(rows + 1 - lineWidth) + 1; //p -> [1, rows + 1 - lineWidth]
				startQ = columns;
				p = startP;
				q = startQ - length + 1;
				direction = Direction.Left;
				isVertical = false;
				break;
			case 2 : //bottom side
				startP = rows;
				startQ = randomGenerator.nextInt(columns + 1 - lineWidth) + 1;
				p = startP - length + 1;
				q = startQ;
				direction = Direction.Up;
				isVertical = true;
				break;
			default : //left side
				startP = randomGenerator.nextInt(rows + 1 - lineWidth) + 1;
				startQ = 1;
				p = startP;
				q = startQ;
				direction = Direction.Right;
				isVertical = false;
				break;
		}
		for (int i = 0; i < lineWidth; i++) {
			if (startSide == 0 || startSide == 2) //top and bottom
				startingPoints.add(new Position(startP, startQ + i));
			else
				startingPoints.add(new Position(startP + i, startQ));
		}
		putBlock(p, q, length, isVertical);
		lastP = p;
		lastQ = q;
		lastDirection = direction;
		lastLength = length;
		do {
			numOfLeftDirections = 4;
			length = randomGenerator.nextInt(5) + lineWidth;
			while (numOfLeftDirections != 0) { //while there are still possible directions to go
				direction = uncheckedDirections.get(randomGenerator.nextInt(numOfLeftDirections));
				isVertical = (direction == Direction.Up || direction == Direction.Down);

				//finding next p and q based on last p, q, direction and length
				int[] pq = getNextPQ(lastP, lastQ, direction, lastDirection, length, lastLength);
				p = pq[0];
				q = pq[1];


				if (checkBlock(p, q, length, direction, lastDirection, isVertical)) { //if its possible to move in that direction
					putBlock(p, q, length, isVertical);
					lastP = p;
					lastQ = q;
					lastDirection = direction;
					lastLength = length;
					break;
				}
				else { //removing impossible directions
					for (int i = 0; i < numOfLeftDirections; i++) {
						if (uncheckedDirections.get(i).number == direction.number) {
							uncheckedDirections.remove(i);
							break;
						}

					}
					numOfLeftDirections--;
				}
			}
			if (numOfLeftDirections == 0) { //if there is no direction to go, map is finished
				break;
			}
			else {
				uncheckedDirections.clear();
				uncheckedDirections.add(Direction.Up);
				uncheckedDirections.add(Direction.Right);
				uncheckedDirections.add(Direction.Down);
				uncheckedDirections.add(Direction.Left);
			}
		} while (true);

		castlePoints = getCastlePoints(lastP, lastQ, lastLength, lastDirection);

		for (Position startingPoint : startingPoints) {
			map[startingPoint.x][startingPoint.y] = -2;
		}
		for (Position castlePoint : castlePoints) {
			map[castlePoint.x][castlePoint.y] = -3;
		}
	}

	private int[] getNextPQ(int lastP, int lastQ,
							Direction nextDirection, Direction lastDirection, int length, int lastLength) {
		int[] result = new int[2]; //p and q
		int p = 1, q = 1;
		switch (nextDirection) {
			case Up :
				switch (lastDirection) {
					case Up :
						p = lastP - length;
						q = lastQ;
						break;
					case Right:
						p = lastP - length;
						q = lastQ + lastLength - lineWidth;
						break;
					case Down:
						p = lastP - length + lastLength;
						q = lastQ;
						break;
					case Left:
						p = lastP - length;
						q = lastQ;
						break;
				}
				break;
			case Right:
				switch (lastDirection) {
					case Up :
						p = lastP;
						q = lastQ + lineWidth;
						break;
					case Right:
						p = lastP;
						q = lastQ + lastLength;
						break;
					case Down:
						p = lastP + lastLength - lineWidth;
						q = lastQ + lineWidth;
						break;
					case Left:
						p = lastP;
						q = lastQ + length - lastLength;
						break;
				}
				break;
			case Down:
				switch (lastDirection) {
					case Up :
						p = lastP + length - lastLength;
						q = lastQ;
						break;
					case Right:
						p = lastP + lineWidth;
						q = lastQ + lastLength - lineWidth;
						break;
					case Down:
						p = lastP + lastLength;
						q = lastQ;
						break;
					case Left:
						p = lastP + lineWidth;
						q = lastQ;
						break;
				}
				break;
			case Left:
				switch (lastDirection) {
					case Up :
						p = lastP;
						q = lastQ - length;
						break;
					case Right:
						p = lastP;
						q = lastQ - length + lastLength;
						break;
					case Down:
						p = lastP + lastLength - lineWidth;
						q = lastQ - length;
						break;
					case Left:
						p = lastP;
						q = lastQ - length;
						break;
				}
				break;
		}

		result[0] = p;
		result[1] = q;
		return result;
	}

	//based on last direction and current block information, this method checks if the cells of block and cells around it
    //(except for those which are in last block) are empty or not.
    //(cells around block are checked so that the two different parts of road don't be near each other
	private boolean checkBlock(int p, int q, int length, Direction direction, Direction lastDirection, boolean isVertical) {
		//return false if block is outside of the map
		if (p > rows || q > columns)
			return false;
		if (p < 1 || q < 1)
			return false;
		if ((isVertical && (p + length > rows + 1)) || (!isVertical && (q + length > columns + 1)))
			return false;

		//saving filled cells, so later we can check weather they are inside the last block(true) or not(false)
		ArrayList<Position> filledCells = new ArrayList<>();
		if (isVertical) {
			if (q < 1 || q > columns + 1 - lineWidth) //total with of line can't be placed in map
				return false;
			for (int i = p - 1; i < p + length + 1; i++) {
				for (int j = q - 1; j < q + lineWidth + 1; j++) {
					if (map[i][j] == -1)
						filledCells.add(new Position(i, j));
				}
			}
		}
		else {
			if (p < 1 || p > rows + 1 - lineWidth) //total with of line can't be placed in map
				return false;
			for (int i = p - 1; i < p + lineWidth + 1; i++) {
				for (int j = q - 1; j < q + length + 1; j++) {
					if (map[i][j] == -1)
						filledCells.add(new Position(i, j));
				}
			}
		}

		//if direction is the opposite of lastDirection, result is always false
		//if there is a filled cell which is not inside the last block, function must return false
		//if all of the filled cells are inside last block, then function must return true
		switch (direction) {
			case Up:
				if (lastDirection == Direction.Down)
					return false;
				else {
					for (Position filledCell : filledCells) {//only cells with this condition are inside last block
						if (filledCell.x != p + length)
							return false;
					}
				}
				break;
			case Right:
				if (lastDirection == Direction.Left)
					return false;
				else {
					for (Position filledCell : filledCells) {
						if (filledCell.y != q - 1)
							return false;
					}
				}
				break;
			case Down:
				if (lastDirection == Direction.Up)
					return false;
				else {
					for (Position filledCell : filledCells) {
						if (filledCell.x != p - 1)
							return false;
					}
				}
				break;
			case Left:
				if (lastDirection == Direction.Right)
					return false;
				else {
					for (Position filledCell : filledCells) {
						if (filledCell.y != q + length)
							return false;
					}
				}
				break;
		}
		return true;
	}

	private void putBlock(int p, int q, int length, boolean isVertical) {
		if (isVertical) {
			for (int i = Math.max(1, p); i < p + length && i <= rows; i++) {
				for (int j = Math.max(1, q); j < q + lineWidth && j <= columns; j++) {
					map[i][j] = -1;
				}
			}
		}
		else {
			for (int i = Math.max(1, p); i < p + lineWidth && i <= rows; i++) {
				for (int j = Math.max(1, q); j < q + length && j <= columns; j++) {
					map[i][j] = -1;
				}
			}
		}
	}

	private ArrayList<Position> getCastlePoints(int p, int q, int length, Direction direction) {
		ArrayList<Position> result = new ArrayList<>();
		switch (direction) {
			case Up:
				for (int i = 0; i < lineWidth; i++) {
					result.add(new Position(p, q + i));
				}
				break;
			case Right:
				for (int i = 0; i < lineWidth; i++) {
					result.add(new Position(p + i, Math.min(q + length - 1, columns)));
				}
				break;
			case Down:
				for (int i = 0; i < lineWidth; i++) {
					result.add(new Position(Math.min(p + length - 1, rows), q + i));
				}
				break;
			case Left:
				for (int i = 0; i < lineWidth; i++) {
					result.add(new Position(p + i, q));
				}
				break;
		}
		return result;
	}
}
