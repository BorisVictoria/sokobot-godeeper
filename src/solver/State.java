package solver;
import java.util.*;

public class State
{
    private Pos pos;
    private char[][] itemsData;
    private int heuristic;
    private long hashCode;
    private Pos[] boxPositions;

    public State(Pos pos, char[][] itemsData, String path) 
    {
        this.pos = pos;
        this.startPos = pos;
        this.itemsData = itemsData;
        this.pushes = new ArrayDeque<>();

        this.boxes = new ArrayList<>();
        int ctr = 0;
        for(int i = 0; i < itemsData.length; i++)
        {
            for(int j = 0; j < itemsData[0].length; j++)
            {
                if (itemsData[i][j] == '$')
                {
                    boxes.add(new Box(ctr,new Pos(j,i)));
                    ctr++;
                }

            }
        }


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

    public Pos[] getBoxPositions()
    {
        Pos[] boxPositions = new Pos[boxes.size()];
        for (int i = 0; i < boxes.size(); i++)
        {
            boxPositions[i] = boxes.get(i).boxPos();
        }

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

    public long getHashCode() {
        return hashCode;
    }

}
