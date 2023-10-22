package solver;
import java.util.*;

public class State
{
    private Pos pos;
    private Pos startPos;
    private char[][] itemsData;
    private ArrayList<Box> boxes;
    private ArrayDeque<Push> pushes;
    private Pos normal;

    public State(Pos pos, char[][] itemsData)
    {
        this.pos = pos;;
        this.startPos = new Pos(pos.x(), pos.y());
        this.normal = new Pos(pos.x(), pos.y());
        this.itemsData = itemsData;
        this.pushes = new ArrayDeque<>(200);
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

    }

    public void setState(Pos pos, char[][] itemsData) {
        this.pos = new Pos(pos.x(), pos.y());
        this.normal = new Pos(pos.x(), pos.y());
        this.itemsData = Arrays.stream(itemsData).map(char[]::clone).toArray(char[][]::new);
        this.pushes = new ArrayDeque<>(200);
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
    }

    public void move(Push push) {

        //add to current queue of pushes
        pushes.add(push);

        //get box referenced by id
        Pos boxPos = boxes.get(push.id()).boxPos();
        char dir = push.dir();

        //clear position of current box and replace with player
        itemsData[boxPos.y()][boxPos.x()] = '@';
        itemsData[pos.y()][pos.x()] = ' ';
        this.pos = new Pos(boxPos.x(), boxPos.y());

        //push box according to direction
        if (dir == 'u')
        {
           itemsData[boxPos.y()-1][boxPos.x()] = '$';
           boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()-1)));

        }
        else if (dir == 'd')
        {
            itemsData[boxPos.y()+1][boxPos.x()] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()+1)));
        }
        else if (dir == 'l')
        {
            itemsData[boxPos.y()][boxPos.x()-1] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()-1, boxPos.y())));
        }
        else if (dir == 'r')
        {
            itemsData[boxPos.y()][boxPos.x()+1] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()+1, boxPos.y())));
        }

    }

    public void unmove() {


       if (pushes.size() > 1)
       {
           Push push = pushes.pollLast();
           Pos boxPos = boxes.get(push.id()).boxPos();
           char dir = push.dir();

           //clear player
           itemsData[pos.y()][pos.x()] = ' ';
           //clear box
           itemsData[boxPos.y()][boxPos.x()] = ' ';
           if (dir == 'u') {
               itemsData[boxPos.y() + 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()+1)));
               itemsData[boxPos.y() + 2][boxPos.x()] = '@';
               this.pos = new Pos(boxPos.x(), boxPos.y()+2);
           } else if (dir == 'd') {
               itemsData[boxPos.y() - 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()-1)));
               itemsData[boxPos.y() - 2][boxPos.x()] = '@';
               this.pos = new Pos(boxPos.x(), boxPos.y()-2);
           } else if (dir == 'l') {
               itemsData[boxPos.y()][boxPos.x() + 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()+1, boxPos.y())));
               itemsData[boxPos.y()][boxPos.x() + 2] = '@';
               this.pos = new Pos(boxPos.x() + 2, boxPos.y());
           } else if (dir == 'r') {
               itemsData[boxPos.y()][boxPos.x() - 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()-1, boxPos.y())));
               itemsData[boxPos.y()][boxPos.x() - 2] = '@';
               this.pos = new Pos(boxPos.x()-2, boxPos.y());
           }
       }
       else // player must be put in start pos
       {
           Push push = pushes.pollLast();
           Pos boxPos = boxes.get(push.id()).boxPos();
           char dir = push.dir();

           //clear player
           itemsData[pos.y()][pos.x()] = ' ';
           //clear box
           itemsData[boxPos.y()][boxPos.x()] = ' ';
           //add player to start pos
           this.pos = new Pos(startPos.x(), startPos.y());
           itemsData[pos.y()][pos.x()] = '@';

           if (dir == 'u')
           {
               itemsData[boxPos.y() + 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()+1)));
           } else if (dir == 'd')
           {
               itemsData[boxPos.y() - 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()-1)));
           } else if (dir == 'l')
           {
               itemsData[boxPos.y()][boxPos.x() + 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()+1, boxPos.y())));
           } else if (dir == 'r')
           {
               itemsData[boxPos.y()][boxPos.x() - 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()-1, boxPos.y())));
           }


       }

    }

    public ArrayDeque<Push> getPushes() {
        return this.pushes;
    }
    public void setPushes(ArrayDeque<Push> pushes) {
        this.pushes = pushes;
    }
    public ArrayList<Box> getBoxPositions()
    {
       return boxes;
    }
    public Pos getPos() {
        return pos;
    }
    public char[][] getItemsData() {
        return itemsData;
    }
    public Pos getNormal() {
        return normal;
    }
    public void setNormal(Pos normal) {
        this.normal = normal;
    }

    public void moveInitial(Push push)
    {

        //get box referenced by id
        Pos boxPos = boxes.get(push.id()).boxPos();
        char dir = push.dir();

        //clear position of current box and replace with player
        itemsData[boxPos.y()][boxPos.x()] = '@';
        itemsData[pos.y()][pos.x()] = ' ';
        this.pos = new Pos(boxPos.x(), boxPos.y());

        //push box according to direction
        if (dir == 'u')
        {
            itemsData[boxPos.y()-1][boxPos.x()] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()-1)));

        }
        else if (dir == 'd')
        {
            itemsData[boxPos.y()+1][boxPos.x()] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()+1)));
        }
        else if (dir == 'l')
        {
            itemsData[boxPos.y()][boxPos.x()-1] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()-1, boxPos.y())));
        }
        else if (dir == 'r')
        {
            itemsData[boxPos.y()][boxPos.x()+1] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()+1, boxPos.y())));
        }

    }
}
