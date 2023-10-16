package solver;
import java.util.*;

public class State
{
    private Pos pos;
    private char[][] itemsData;
    private int heuristic;
    private String path;
    private long hashCode;
    private Pos[] boxPositions;

    public State(Pos pos, char[][] itemsData, String path) 
    {
        this.pos = new Pos(pos.x(), pos.y());
        this.itemsData = itemsData;
        this.path = path;

        ArrayList<Pos> boxPositions= new ArrayList<>();
        for(int i = 0; i < itemsData.length; i++) {
            for(int j = 0; j < itemsData[0].length; i++) {
                if(itemsData[i][j] == '$')
                    boxPositions.add(new Pos(j, i));
            }
        }

        // idk if this works
        this.boxPositions = boxPositions.toArray(new Pos[0]);


        //this.heuristic = heuristic;
        //this.hashCode = hashCode;
    }

    public void playerUp() {
        itemsData[pos.y()][pos.x()] = ' ';

        if(itemsData[pos.y() - 1][pos.x()] == '$') {
            itemsData[pos.y() - 2][pos.x()] = '$';
        }

        itemsData[pos.y() - 1][pos.x()] = '@';

        pos = new Pos(pos.x(), pos.y() - 1);
    }

    public void playerDown() {
        itemsData[pos.y()][pos.x()] = ' ';

        if(itemsData[pos.y() + 1][pos.x()] == '$') {
            itemsData[pos.y() + 2][pos.x()] = '$';
        }

        itemsData[pos.y() + 1][pos.x()] = '@';

        pos = new Pos(pos.x(), pos.y() + 1);
    }

    public void playerLeft() {
        itemsData[pos.y()][pos.x()] = ' ';

        if(itemsData[pos.y()][pos.x() - 1] == '$') {
            itemsData[pos.y()][pos.x() - 2] = '$';
        }

        itemsData[pos.y()][pos.x() - 1] = '@';

        pos = new Pos(pos.x() - 1, pos.y());
    }

    public void playerRight() {
        itemsData[pos.y()][pos.x()] = ' ';

        if(itemsData[pos.y()][pos.x() + 1] == '$') {
            itemsData[pos.y()][pos.x() + 2] = '$';
        }

        itemsData[pos.y()][pos.x() + 1] = '@';

        pos = new Pos(pos.x() + 1, pos.y());
    }

    public void unmove() {
        char dir = path.charAt(path.length() - 1);

        if(dir == 'u') {
            itemsData[pos.y()][pos.x()] = ' ';

            if(itemsData[pos.y() - 1][pos.x()] == '$') {
                itemsData[pos.y()][pos.x()] = '$';
            }

            itemsData[pos.y() + 1][pos.x()] = '@';

            pos = new Pos(pos.x(), pos.y() - 1);
        }
        if(dir == 'd') {
            itemsData[pos.y()][pos.x()] = ' ';

            if(itemsData[pos.y() + 1][pos.x()] == '$') {
                itemsData[pos.y()][pos.x()] = '$';
            }

            itemsData[pos.y() - 1][pos.x()] = '@';

            pos = new Pos(pos.x(), pos.y() + 1);
        }
        if(dir == 'l') {
            itemsData[pos.y()][pos.x()] = ' ';

            if(itemsData[pos.y()][pos.x() - 1] == '$') {
                itemsData[pos.y()][pos.x()] = '$';
            }

            itemsData[pos.y()][pos.x() + 1] = '@';

            pos = new Pos(pos.x() - 1, pos.y());
        }
        if(dir == 'r') {
            itemsData[pos.y()][pos.x()] = ' ';

            if(itemsData[pos.y()][pos.x() + 1] == '$') {
                itemsData[pos.y()][pos.x()] = '$';
            }

            itemsData[pos.y()][pos.x() - 1] = '@';

            pos = new Pos(pos.x() + 1, pos.y());
        }
    }


    public Pos[] getBoxPositions() {
        return boxPositions;
    }

    public Pos getPos() {
        return pos;
    }
    

    public char[][] getItemsData() {
        return itemsData;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public String getPath() {
        return path;
    }

    public long getHashCode() {
        return hashCode;
    }

}
