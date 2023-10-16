package solver;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SokoBot {

  private final int width;
  private final int height;
  private final char[][] mapData;
  private final long[][][] zobrist;
  private final State state;
  private final ObjectArrayList<Pos> goals;

  private final int numGoals;
  private Reach reachTiles;
  private final boolean[][] deadTiles;

  public SokoBot(int width, int height, char[][] mapData, char[][] itemsData) {

    this.width = width;
    this.height = height;
    this.mapData = mapData;
    this.zobrist = new long[2][height][width];
    this.goals = new ObjectArrayList<>();

    Random rand = new Random();

    for (int i = 0; i < 2; i++)
    {
      for (int j = 0; j < height; j++)
      {
        for(int k = 0; k < width; k++)
        {
          zobrist[i][j][k] = rand.nextLong();
        }
      }
    }

    int ctr = 0;
    for(int i = 0; i < height; i++)
    {
      for(int j = 0; j < width; j++)
      {
        if(mapData[i][j] == '.')
        {
          goals.add(new Pos(j, i));
          ctr++;
        }
      }
    }
    this.numGoals = ctr;

    Pos player = new Pos(0,0);
    for(int i = 0; i < height; i++) {
      for(int j = 0; j < width; j++) {
        if(itemsData[i][j] == '@') {
          player = new Pos(j, i);
          break;
        }
      }
    }

    state = new State(player, itemsData, "");
    reachTiles = new Reach(height, width);
    deadTiles = getDeadTiles();
  }

  public boolean isPullValid(Pos pos, char dir) {

    if(dir == 'u') {
      if(mapData[pos.y() - 1][pos.x()] == '#')
        return false;
      else if(mapData[pos.y() - 2][pos.x()] == '#')
        return false;
    }
    if(dir == 'd') {
      if(mapData[pos.y() + 1][pos.x()] == '#')
        return false;
      else if(mapData[pos.y() + 2][pos.x()] == '#')
        return false;
    }
    if(dir == 'l') {
      if(mapData[pos.y()][pos.x() - 1] == '#')
        return false;
      else if(mapData[pos.y()][pos.x() - 2] == '#')
        return false;
    }
    if(dir == 'r') {
      if (mapData[pos.y()][pos.x() + 1] == '#')
        return false;
      else if (mapData[pos.y()][pos.x() + 2] == '#')
        return false;
    }

    return true;
  }

  public boolean[][] getDeadTiles() {
    boolean[][] deadTiles = new boolean[height][width];
    Arrays.stream(deadTiles).forEach(row->Arrays.fill(row,true));

    for(int i = 0; i < numGoals; i++) {
      ObjectArrayFIFOQueue<Pos> toCheck = new ObjectArrayFIFOQueue<>();
      HashSet<Pos> visited = new HashSet<>();
      Pos curPos = goals.get(i);

      toCheck.enqueue(curPos);

      do {

        curPos = toCheck.dequeue();

        if(!visited.contains(curPos)) {
          visited.add(curPos);

          Pos newPos;

          newPos = new Pos(curPos.x(), curPos.y() - 1);
          if(isPullValid(curPos, 'u')) {
            toCheck.enqueue(newPos);
            deadTiles[newPos.y()][newPos.x()] = false;
          }

          newPos = new Pos(curPos.x(), curPos.y() + 1);
          if(isPullValid(curPos, 'd')) {
            toCheck.enqueue(newPos);
            deadTiles[newPos.y()][newPos.x()] = false;
          }

          newPos = new Pos(curPos.x() - 1, curPos.y());
          if(isPullValid(curPos, 'l')) {
            toCheck.enqueue(newPos);
            deadTiles[newPos.y()][newPos.x()] = false;
          }

          newPos = new Pos(curPos.x() + 1, curPos.y());
          if(isPullValid(curPos, 'r')) {
            toCheck.enqueue(newPos);
            deadTiles[newPos.y()][newPos.x()] = false;
          }
        }

      } while (!toCheck.isEmpty());
    }

    for (int i = 0; i < numGoals; i++) {
      deadTiles[goals.get(i).y()][goals.get(i).x()] = false;
    }

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if(deadTiles[i][j])
          System.out.print(".");
        else System.out.print("O");
      }
      System.out.println();
    }

    return deadTiles;
  }

  public boolean isBlocked(char[][] nextItemsData, Pos box)
  {

    boolean blockedX = false;
    boolean blockedY = false;

    // the hack kekw
    nextItemsData[box.y()][box.x()] = 'W';

    // medyo hacky pero this treats the new crate as a wall
    if (mapData[box.y()-1][box.x()] == '#' || mapData[box.y()+1][box.x()] == '#' || nextItemsData[box.y()-1][box.x()] == 'W' || nextItemsData[box.y()+1][box.x()] == 'W')
    {
      blockedY = true;
    }
    else if (nextItemsData[box.y()-1][box.x()] == '$')
    {

      blockedY = isBlocked(nextItemsData, new Pos(box.x(),box.y()-1)); // TRY IT AGAIN BITCH
    }

    else if (nextItemsData[box.y() + 1][box.x()] == '$')
    {
      blockedY = isBlocked(nextItemsData, new Pos(box.x(),box.y()+1));
    }

    if (mapData[box.y()][box.x()-1] == '#' || mapData[box.y()][box.x()+1] == '#' || nextItemsData[box.y()][box.x()-1] == 'W' || nextItemsData[box.y()][box.x()+1] == 'W')
    {
      blockedX = true;
    }

    else if (nextItemsData[box.y()][box.x()-1] == '$')
    {
      blockedX = isBlocked(nextItemsData, new Pos(box.x()-1, box.y()));
    }

    // i'm actually talking to my ex sa dc, goddamn ikr // that's why nandito pa rin ako rn like damn
    // BROOOOOO
    //
    // oh shit ur still here pala HAHAHAHA, i understand the algorithm na pala from the website, im trying to trace ur code rn
    else if (nextItemsData[box.y()][box.x() + 1] == '$')
    {
      blockedX = isBlocked(nextItemsData, new Pos(box.x()+1, box.y()));
    }

//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`---'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\---/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /--.--\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=---='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//               Tiam-Lee bless you         永无BUG cum on my face
    return blockedX && blockedY;

  }

  public boolean isSolvable(char[][] nextItemsData, Pos movedBox)
  {

    if (isBlocked(nextItemsData, movedBox))
    {
//          System.out.println("is Deadlock?: " + !(mapData[movedBox[1]][movedBox[0]] == '.'));
      return mapData[movedBox.y()][movedBox.x()] == '.';
    }

    return true;
  }


  public void clear()
  {
    reachTiles.setMin(new Pos(0,0));
    reachTiles.setStamp(0);
    int[][] tiles = reachTiles.getTiles();

    for (int i = 0; i < height; i++)
    {
      for (int j = 0; j < width; j++)
      {
        if (mapData[i][j] == '#')
          tiles[i][j] = Integer.MAX_VALUE;
        else
          tiles[i][j] = 0;
      }
    }
  }

  public void calculateReach(Pos start, char[][] itemsData)
  {
    //Reset before overflow
    if (reachTiles.getStamp() >= Integer.MAX_VALUE - 10)
      clear();

    //Initialization
    reachTiles.setStamp(reachTiles.getStamp()+2);
    reachTiles.setMin(start);
    int[][] tiles = reachTiles.getTiles();
    int stamp = reachTiles.getStamp();
    tiles[start.y()][start.x()] = stamp;

    Queue<Pos> queue = new ArrayDeque<Pos>();
    queue.add(start);

    while (!queue.isEmpty())
    {

      Pos toCheck = queue.poll();

      if (tiles[toCheck.y()-1][toCheck.x()] < stamp)
      {
        if (itemsData[toCheck.y()-1][toCheck.x()] == '$')
        {
          tiles[toCheck.y()-1][toCheck.x()] = stamp + 1;
        }
        else
        {
          Pos up = new Pos(toCheck.x(), toCheck.y()-1);
          queue.add(up);
          tiles[toCheck.y()-1][toCheck.x()] = stamp;
          if ((reachTiles.getMin().x() * width + reachTiles.getMin().y()) - (up.x() * width + up.y()) > 0)
            reachTiles.setMin(up);
        }
      }

      if (tiles[toCheck.y()+1][toCheck.x()] < stamp)
      {
        if (itemsData[toCheck.y()+1][toCheck.x()] == '$')
        {
          tiles[toCheck.y()+1][toCheck.x()] = stamp + 1;
        }
        else
        {
          Pos down = new Pos(toCheck.x(), toCheck.y()+1);
          queue.add(down);
          tiles[toCheck.y()+1][toCheck.x()] = stamp;
          if ((reachTiles.getMin().x() * width + reachTiles.getMin().y()) - (down.x() * width + down.y()) > 0)
            reachTiles.setMin(down);
        }
      }

      if (tiles[toCheck.y()][toCheck.x()+1] < stamp)
      {
        if (itemsData[toCheck.y()][toCheck.x()+1] == '$')
        {
          tiles[toCheck.y()][toCheck.x()+1] = stamp + 1;
        }
        else
        {
          Pos right = new Pos(toCheck.x()+1, toCheck.y());
          queue.add(right);
          tiles[toCheck.y()][toCheck.x()+1] = stamp;
          if ((reachTiles.getMin().x() * width + reachTiles.getMin().y()) - (right.x() * width + right.y()) > 0)
            reachTiles.setMin(right);
        }
      }

      if (tiles[toCheck.y()][toCheck.x()-1] < stamp)
      {
        if (itemsData[toCheck.y()][toCheck.x()-1] == '$')
        {
          tiles[toCheck.y()][toCheck.x()-1] = stamp + 1;

        }
        else
        {
          Pos left = new Pos(toCheck.x()-1, toCheck.y());
          queue.add(left);
          tiles[toCheck.y()][toCheck.x()-1] = stamp;
          if ((reachTiles.getMin().x() * width + reachTiles.getMin().y()) - (left.x() * width + left.y()) > 0)
            reachTiles.setMin(left);
        }
      }

    }

  }

  public ArrayList<Push> getValidPushes() {
    Pos[] boxPositions = state.getBoxPositions();
    Pos playerPos = state.getPos();
    ArrayList<Push> validPushes = new ArrayList<>();

    for(Pos boxPos : boxPositions) {
      if(reachTiles.getTiles()[boxPos.y()][boxPos.x()] == reachTiles.getStamp() + 1) {
        // check up
        if(reachTiles.getTiles()[boxPos.y() + 1][boxPos.x()] == reachTiles.getStamp() && mapData[boxPos.y() - 1][boxPos.x()] != '#' && state.getItemsData()[boxPos.y()-1][boxPos.x()] != '$' && !deadTiles[boxPos.y()-1][boxPos.x()]) {
          char[][] newItemsData = Arrays.stream(state.getItemsData()).map(char[]::clone).toArray(char[][]::new); //copy current items data
          newItemsData[playerPos.y()][playerPos.x()] = ' '; //clear player
          newItemsData[boxPos.y()][boxPos.x()] = '@'; //replace with player
          newItemsData[boxPos.y() - 1][boxPos.x()] = '$'; //move box

          char[][] check = Arrays.stream(newItemsData).map(char[]::clone).toArray(char[][]::new);
          if (isSolvable(check, new Pos(boxPos.x(), boxPos.y() - 1))) {
            validPushes.add(new Push(boxPos, 'u'));
          }
        }
        // check down
        if(reachTiles.getTiles()[boxPos.y() - 1][boxPos.x()] == reachTiles.getStamp() && mapData[boxPos.y() + 1][boxPos.x()] != '#' && state.getItemsData()[boxPos.y() + 1][boxPos.x()] != '$' && !deadTiles[boxPos.y() + 1][boxPos.x()]) {
          char[][] newItemsData = Arrays.stream(state.getItemsData()).map(char[]::clone).toArray(char[][]::new); //copy current items data
          newItemsData[playerPos.y()][playerPos.x()] = ' '; //clear player
          newItemsData[boxPos.y()][boxPos.x()] = '@'; //replace with player
          newItemsData[boxPos.y() + 1][boxPos.x()] = '$'; //move box

          char[][] check = Arrays.stream(newItemsData).map(char[]::clone).toArray(char[][]::new);
          if (isSolvable(check, new Pos(boxPos.x(), boxPos.y() + 1))) {
            validPushes.add(new Push(boxPos, 'd'));
          }
        }
        // check left
        if(reachTiles.getTiles()[boxPos.y()][boxPos.x() + 1] == reachTiles.getStamp() && mapData[boxPos.y()][boxPos.x() - 1] != '#' && state.getItemsData()[boxPos.y()][boxPos.x() - 1] != '$' && !deadTiles[boxPos.y()][boxPos.x() - 1]) {
          char[][] newItemsData = Arrays.stream(state.getItemsData()).map(char[]::clone).toArray(char[][]::new); //copy current items data
          newItemsData[playerPos.y()][playerPos.x()] = ' '; //clear player
          newItemsData[boxPos.y()][boxPos.x()] = '@'; //replace with player
          newItemsData[boxPos.y()][boxPos.x() - 1] = '$'; //move box

          char[][] check = Arrays.stream(newItemsData).map(char[]::clone).toArray(char[][]::new);
          if (isSolvable(check, new Pos(boxPos.x() - 1, boxPos.y()))) {
            validPushes.add(new Push(boxPos, 'l'));
          }
        }
        // check right
        if(reachTiles.getTiles()[boxPos.y()][boxPos.x() - 1] == reachTiles.getStamp() && mapData[boxPos.y()][boxPos.x() + 1] != '#' && state.getItemsData()[boxPos.y()][boxPos.x() + 1] != '$' && !deadTiles[boxPos.y()][boxPos.x() + 1]) {
          char[][] newItemsData = Arrays.stream(state.getItemsData()).map(char[]::clone).toArray(char[][]::new); //copy current items data
          newItemsData[playerPos.y()][playerPos.x()] = ' '; //clear player
          newItemsData[boxPos.y()][boxPos.x()] = '@'; //replace with player
          newItemsData[boxPos.y()][boxPos.x() + 1] = '$'; //move box

          char[][] check = Arrays.stream(newItemsData).map(char[]::clone).toArray(char[][]::new);
          if (isSolvable(check, new Pos(boxPos.x() + 1, boxPos.y()))) {
            validPushes.add(new Push(boxPos, 'r'));
          }
        }
      }
    }

    return validPushes;
  }


  public String solveSokobanPuzzle()
  {




    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }

}


/*

startTile;
getValidMoves(startTile);
pushAll(move); ul
d = 0;
d++;


if (d > 2)
{
getValidMoves(move.pop)
doMove(move.pop)
unmove.push(move.pop)
if (!visited)

d++
}


while(moveStack !empty) {
  if(isSolved):
    return unmoveStack
  if(d > max):
    doMove(unmove.pop)
    d--
  else:
    validMoves = getValidMoves
    for each valid move:
      doMove(validMove)
      if(isVisited)
        doMove(unmove.pop)
      else:
        d++


}



 */
