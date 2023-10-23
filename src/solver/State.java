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
    private int moveCtr;

    public State(Pos pos, char[][] itemsData)
    {
        this.pos = pos;;
        this.startPos = new Pos(pos.x(), pos.y());
        this.normal = new Pos(pos.x(), pos.y());
        this.itemsData = itemsData;
        this.pushes = new ArrayDeque<>(200);
        this.boxes = new ArrayList<>();
        this.moveCtr = 0;
        int ctr = 0;
        for(int i = 0; i < itemsData.length; i++)
        {
            for(int j = 0; j < itemsData[0].length; j++)
            {
                if (itemsData[i][j] == '$')
                {
                    boxes.add(new Box(ctr,new Pos(j,i),true));
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
                    boxes.add(new Box(ctr,new Pos(j,i),true));
                    ctr++;
                }

            }
        }
    }

    public void move(Push push) {

        pushes.add(push);

        Pos boxPos = boxes.get(push.id()).boxPos();
        char dir = push.dir();

        itemsData[boxPos.y()][boxPos.x()] = '@';
        itemsData[pos.y()][pos.x()] = ' ';
        this.pos = new Pos(boxPos.x(), boxPos.y());

        if (dir == 'u')
        {
           itemsData[boxPos.y()-1][boxPos.x()] = '$';
           boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()-1), true));

        }
        else if (dir == 'd')
        {
            itemsData[boxPos.y()+1][boxPos.x()] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()+1), true));
        }
        else if (dir == 'l')
        {
            itemsData[boxPos.y()][boxPos.x()-1] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()-1, boxPos.y()), true));
        }
        else if (dir == 'r')
        {
            itemsData[boxPos.y()][boxPos.x()+1] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()+1, boxPos.y()), true));
        }
        moveCtr++;

    }

    public void unmove() {


       if (pushes.size() > 1)
       {
           Push push = pushes.pollLast();
           Pos boxPos = boxes.get(push.id()).boxPos();
           char dir = push.dir();

           itemsData[pos.y()][pos.x()] = ' ';

           itemsData[boxPos.y()][boxPos.x()] = ' ';
           if (dir == 'u') {
               itemsData[boxPos.y() + 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()+1), true));
               itemsData[boxPos.y() + 2][boxPos.x()] = '@';
               this.pos = new Pos(boxPos.x(), boxPos.y()+2);
           } else if (dir == 'd') {
               itemsData[boxPos.y() - 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()-1), true));
               itemsData[boxPos.y() - 2][boxPos.x()] = '@';
               this.pos = new Pos(boxPos.x(), boxPos.y()-2);
           } else if (dir == 'l') {
               itemsData[boxPos.y()][boxPos.x() + 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()+1, boxPos.y()), true));
               itemsData[boxPos.y()][boxPos.x() + 2] = '@';
               this.pos = new Pos(boxPos.x() + 2, boxPos.y());
           } else if (dir == 'r') {
               itemsData[boxPos.y()][boxPos.x() - 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()-1, boxPos.y()), true));
               itemsData[boxPos.y()][boxPos.x() - 2] = '@';
               this.pos = new Pos(boxPos.x()-2, boxPos.y());
           }
            moveCtr--;
       }
       else if (pushes.size() == 1 && moveCtr == 1)
       {
           Push push = pushes.pollLast();
           Pos boxPos = boxes.get(push.id()).boxPos();
           char dir = push.dir();

           itemsData[pos.y()][pos.x()] = ' ';
           itemsData[boxPos.y()][boxPos.x()] = ' ';
           this.pos = new Pos(startPos.x(), startPos.y());
           itemsData[pos.y()][pos.x()] = '@';

           if (dir == 'u')
           {
               itemsData[boxPos.y() + 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()+1), true));
           } else if (dir == 'd')
           {
               itemsData[boxPos.y() - 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()-1), true));
           } else if (dir == 'l')
           {
               itemsData[boxPos.y()][boxPos.x() + 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()+1, boxPos.y()), true));
           } else if (dir == 'r')
           {
               itemsData[boxPos.y()][boxPos.x() - 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()-1, boxPos.y()), true));
           }
           moveCtr--;
       }
       else
       {
           Push push = pushes.pollLast();
           Pos boxPos = boxes.get(push.id()).boxPos();
           char dir = push.dir();

           itemsData[pos.y()][pos.x()] = ' ';
           itemsData[boxPos.y()][boxPos.x()] = ' ';
           if (dir == 'u') {
               itemsData[boxPos.y() + 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()+1), true));
               itemsData[boxPos.y() + 2][boxPos.x()] = '@';
               this.pos = new Pos(boxPos.x(), boxPos.y()+2);
           } else if (dir == 'd') {
               itemsData[boxPos.y() - 1][boxPos.x()] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()-1), true));
               itemsData[boxPos.y() - 2][boxPos.x()] = '@';
               this.pos = new Pos(boxPos.x(), boxPos.y()-2);
           } else if (dir == 'l') {
               itemsData[boxPos.y()][boxPos.x() + 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()+1, boxPos.y()), true));
               itemsData[boxPos.y()][boxPos.x() + 2] = '@';
               this.pos = new Pos(boxPos.x() + 2, boxPos.y());
           } else if (dir == 'r') {
               itemsData[boxPos.y()][boxPos.x() - 1] = '$';
               boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()-1, boxPos.y()), true));
               itemsData[boxPos.y()][boxPos.x() - 2] = '@';
               this.pos = new Pos(boxPos.x()-2, boxPos.y());
           }
           moveCtr--;
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

        Pos boxPos = boxes.get(push.id()).boxPos();
        char dir = push.dir();

        itemsData[boxPos.y()][boxPos.x()] = '@';
        itemsData[pos.y()][pos.x()] = ' ';
        this.pos = new Pos(boxPos.x(), boxPos.y());

        if (dir == 'u')
        {
            itemsData[boxPos.y()-1][boxPos.x()] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()-1), true));

        }
        else if (dir == 'd')
        {
            itemsData[boxPos.y()+1][boxPos.x()] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x(), boxPos.y()+1), true));
        }
        else if (dir == 'l')
        {
            itemsData[boxPos.y()][boxPos.x()-1] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()-1, boxPos.y()), true));
        }
        else if (dir == 'r')
        {
            itemsData[boxPos.y()][boxPos.x()+1] = '$';
            boxes.set(push.id(), new Box(push.id(), new Pos(boxPos.x()+1, boxPos.y()), true));
        }

    }
    public void setMoveCtr(int moveCtr) {
        this.moveCtr = moveCtr;
    }
}
