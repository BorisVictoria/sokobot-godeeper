package solver;
import java.util.*;

public class State
{
    private Pos pos;
    private char[][] itemsData;
    private int heuristic;
    private long hashCode;
    private ArrayList<Box> boxes;
    private ArrayDeque<Push> pushes;

    private Pos startPos;


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

    public void move(Push push) {

        int boxid = 0;
        for(int i = 0; i < boxes.size(); i++) {
            if(boxes.get(i).boxPos() == push.pos()) {
                boxid = i;
                break;
            }
        }

        boxes.remove(boxid);

        //clear position of box to be moved
        itemsData[push.pos().y()][push.pos().x()] = ' ';
        //clear current position of player
        itemsData[pos.y()][pos.x()] = ' ';

        char dir = push.dir();

        //move box to push + dir
        if(dir == 'u') {
            itemsData[push.pos().y() - 1][push.pos().x()] = '$';
            boxes.add(boxid, new Box(boxid, new Pos(push.pos().x(), push.pos().y() - 1)));
        }
        else if(dir == 'd') {
            itemsData[push.pos().y() + 1][push.pos().x()] = '$';
            boxes.add(boxid, new Box(boxid, new Pos(push.pos().x(), push.pos().y() + 1)));
        }
        else if(dir == 'l') {
            itemsData[push.pos().y()][push.pos().x() - 1] = '$';
            boxes.add(boxid, new Box(boxid, new Pos(push.pos().x() - 1, push.pos().y())));
        }
        else if(dir == 'r') {
            itemsData[push.pos().y()][push.pos().x() + 1] = '$';
            boxes.add(boxid, new Box(boxid, new Pos(push.pos().x() + 1, push.pos().y())));
        }

        //player must be where box was
        itemsData[push.pos().y()][push.pos().x()] = '@';
        pos = new Pos(push.pos().x(), push.pos().y());

        pushes.add(push);
    }

    public void unmove() {
        Push lastPush = pushes.pollLast();

        Pos lastPushPos = lastPush.pos();
        itemsData[lastPushPos.y()][lastPushPos.x()] = '$';

        // clearing box
        if(lastPush.dir() == 'u') {
            itemsData[lastPushPos.y() - 1][lastPushPos.x()] = ' ';
        }
        else if(lastPush.dir() == 'd') {
            itemsData[lastPushPos.y() + 1][lastPushPos.x()] = ' ';
        }
        else if(lastPush.dir() == 'l') {
            itemsData[lastPushPos.y()][lastPushPos.x() - 1] = ' ';

        }
        else if(lastPush.dir() == 'r') {
            itemsData[lastPushPos.y()][lastPushPos.x() + 1] = ' ';

        }

        if(!pushes.isEmpty()) {
            Push lastMinusOne = pushes.pollLast();
            Pos lastMinusOnePos = lastMinusOne.pos();

            if(lastMinusOne.dir() == 'u') {
                itemsData[lastMinusOnePos.y() + 1][lastMinusOnePos.x()] = '@';
                pos = new Pos(lastMinusOnePos.x(), lastMinusOnePos.y() + 1);
            }
            else if(lastMinusOne.dir() == 'd') {
                itemsData[lastMinusOnePos.y() - 1][lastMinusOnePos.x()] = '@';
                pos = new Pos(lastMinusOnePos.x(), lastMinusOnePos.y() - 1);
            }
            else if(lastMinusOne.dir() == 'l') {
                itemsData[lastMinusOnePos.y()][lastMinusOnePos.x() + 1] = '@';
                pos = new Pos(lastMinusOnePos.x() + 1, lastMinusOnePos.x());
            }
            else if(lastMinusOne.dir() == 'r') {
                itemsData[lastMinusOnePos.y()][lastMinusOnePos.x() - 1] = '@';
                pos = new Pos(lastMinusOnePos.x() - 1, lastMinusOnePos.y());
            }

            pushes.offerLast(lastMinusOne);
        }
        else {
            itemsData[startPos.y()][startPos.x()] = '@';
            pos = new Pos(startPos.x(), startPos.y());
        }
    }

    public ArrayDeque<Push> getPushes() {
        return this.pushes;
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
