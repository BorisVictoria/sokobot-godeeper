package solver;

public class Reach
{
    Pos min;
    int stamp;
    int[][] tiles;
    public Reach(int height, int width)
    {
        this.tiles = new int[height][width];
    }
    public Pos getMin() {
        return min;
    }
    public void setMin(Pos min) {
        this.min = min;
    }

    public int getStamp() {
        return stamp;
    }

    public void setStamp(int stamp) {
        this.stamp = stamp;
    }

    public int[][] getTiles() {
        return tiles;
    }

    public void setTiles(int[][] tiles) {
        this.tiles = tiles;
    }
}
