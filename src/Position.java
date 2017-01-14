import java.io.Serializable;

public class Position implements Serializable{
    public int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + " " + y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(this.getClass())) {
            Position obj2 = (Position)obj;
            return (x == obj2.x) && (y == obj2.y);
        }
        return false;
    }
}
