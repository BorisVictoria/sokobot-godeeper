package solver;

import java.util.*;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SokoBot {

  private final int width;
  private final int height;
  private final char[][] mapData;
  private final long[][][] zobristTable;
  private final State state;
  private final State initialState;
  private final ObjectArrayList<Pos> goals;

  private final int numGoals;
  private Reach reachTiles;
  private final boolean[][] deadTiles;
  private final LongOpenHashSet visitedStates;

  private int maxDepth;
  private String solution;

  public SokoBot(int width, int height, char[][] mapData, char[][] itemsData) {

    this.width = width;
    this.height = height;
    this.mapData = mapData;
    this.zobristTable = new long[2][height][width];
    this.goals = new ObjectArrayList<>();
    visitedStates = new LongOpenHashSet();

    Random rand = new Random();

    for (int i = 0; i < 2; i++)
    {
      for (int j = 0; j < height; j++)
      {
        for(int k = 0; k < width; k++)
        {
          zobristTable[i][j][k] = rand.nextLong();
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
    initialState = new State(player, itemsData, "");

    reachTiles = new Reach(height, width);
    clear();
    deadTiles = getDeadTiles();

    maxDepth = 1000;

    solution = "";

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

  public Pos calculateReach(Pos start, char[][] itemsData)
  {
    //Reset before overflow
    if (reachTiles.getStamp() >= Integer.MAX_VALUE - 2)
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

    return reachTiles.getMin();

  }

  //Calculates the hash of the current state;
  public long calculateHash()
  {
    long key = 0;
    char[][] itemsData = state.getItemsData();

    for (int i = 0; i < height; i++)
    {
      for (int j = 0; j < width; j++)
      {
        if (itemsData[i][j] == '$')
        {
          key ^= zobristTable[0][i][j];
        }
        else if (itemsData[i][j] == '@')
        {
          key ^= zobristTable[1][i][j];
        }
      }
    }
    return key;
  }

  public boolean isSolved()
  {
    boolean s = true;

    for (int i = 0; i < goals.size(); i++)
    {
      if (state.getItemsData()[goals.get(i).y()][goals.get(i).x()] != '$')
      {
        s = false;
        break;
      }
    }

    return s;
  }

  /*
  boolean DFS(curState, d, solution) {
    if(isSolved)
      return true

    if (d > max)
      return false

    if (isVisited())
      return false

    for(Push validMove : curState.getValidMoves)
      dMove(Move)
      visited.add(curState)
      if (DFS(d+1))
        solution.append(push.getDir)
        return true
      else
        unmove(push.getDir)

    return false
  }
   */

  public String calculatePath(Pos player, Pos dest)
  {

    Comparator<Path> posComparator = Comparator.comparing(Path::heuristic);
    PriorityQueue<Path> queue = new PriorityQueue<>(posComparator);

    int heuristic = Math.abs(player.x() - dest.x()) + Math.abs(player.y() - dest.y());
    Path start = new Path(player, heuristic, "");
    HashSet<Pos> visited = new HashSet<>();
    int[][] tiles = reachTiles.getTiles();
    int stamp = reachTiles.getStamp();

    queue.offer(start);

    while (!queue.isEmpty())
    {
      Path current = queue.poll();
      //System.out.println(current.path());

      if (current.pos().x() == dest.x() && current.pos().y() == dest.y())
      {
        return current.path();
      }

      Pos curPos = current.pos();

      if (tiles[curPos.y()-1][curPos.x()] == stamp)
      {
        Pos posup = new Pos(curPos.x(),curPos.y()-1);
        int h = Math.abs(posup.x() - dest.x()) + Math.abs(posup.y() - dest.y());
        Path up = new Path(posup, h, current.path() + "u");
        if (!visited.contains(posup))
          queue.add(up);
      }

      if (tiles[curPos.y()+1][curPos.x()] == stamp)
      {
        Pos posdown = new Pos(curPos.x(),curPos.y()+1);
        int h = Math.abs(posdown.x() - dest.x()) + Math.abs(posdown.y() - dest.y());
        Path down = new Path(posdown, h, current.path() + "d");
        if (!visited.contains(posdown))
          queue.add(down);
      }

      if (tiles[curPos.y()][curPos.x()+1] == stamp)
      {
        Pos posright = new Pos(curPos.x()+1,curPos.y());
        int h = Math.abs(posright.x() - dest.x()) + Math.abs(posright.y() - dest.y());
        Path right = new Path(posright, h, current.path() + "r");
        if (!visited.contains(posright))
          queue.add(right);
      }

      if (tiles[curPos.y()][curPos.x()-1] == stamp)
      {
        Pos posleft = new Pos(curPos.x()-1,curPos.y());
        int h = Math.abs(posleft.x() - dest.x()) + Math.abs(posleft.y() - dest.y());
        Path left = new Path(posleft, h, current.path() + "l");
        if (!visited.contains(posleft))
          queue.add(left);
      }

      visited.add(curPos);
    }

    throw new RuntimeException("Wasn't able to find the path!");

  }


  public boolean goDeeper(int depth) {
    System.out.println("depth: " + depth);
    for (int i = 0; i < height; i++)
        {
          for (int j = 0; j < width; j++)
          {
            if (state.getItemsData()[i][j] == ' ')
              System.out.print(mapData[i][j]);
            else System.out.print(state.getItemsData()[i][j]);
          }
          System.out.println();
        }

    if(isSolved()) {
      System.out.println("solution found!");
      return true;
    }

    if(depth > maxDepth) {
      System.out.println("max depth reached");
      return false;
    }

    if(visitedStates.contains(calculateHash())) {
      System.out.println("already visited");
      return false;
    }

    calculateReach(state.getPos(), state.getItemsData());
    ArrayList<Push> validPushes = getValidPushes();

    for(int i = 0; i < validPushes.size(); i++)
    {
      char dir = validPushes.get(i).dir();
      int id = validPushes.get(i).id();

      calculateReach(state.getPos(), state.getItemsData());

//      for (int i = 0; i < height; i++)
//        {
//          for (int j = 0; j < width; j++)
//          {
//            if (reachTiles.getTiles()[i][j] == Integer.MAX_VALUE)
//              System.out.print("+");
//            else
//              System.out.print(reachTiles.getTiles()[i][j]);
//          }
//          System.out.println();
//        }

      System.out.println("Box: " + id + " " + state.getBoxPositions().get(id).boxPos().x() + " " + state.getBoxPositions().get(id).boxPos().y() + " " + dir);

      visitedStates.add(calculateHash());
      System.out.println("moving");
      state.move(validPushes.get(i));

      System.out.println("going deeper!");
      if(goDeeper(depth + 1)) {
        //state.getSolution().add(validPushes.get(i));
        return true;
      }
      else {
        state.unmove();
        System.out.println("unmoved cur state");
        for (int j = 0; j < height; j++)
        {
          for (int k = 0; k < width; k++)
          {
            if (state.getItemsData()[j][k] == ' ')
              System.out.print(mapData[j][k]);
            else System.out.print(state.getItemsData()[j][k]);
          }
          System.out.println();
        }
      }

    }

    System.out.println("i give up");
    return false;
  }

  public ArrayList<Push> getValidPushes() {
    ArrayList<Box> boxPositions = state.getBoxPositions();
    Pos playerPos = state.getPos();
    ArrayList<Push> validPushes = new ArrayList<>();

    for(Box box : boxPositions) {

      if(reachTiles.getTiles()[box.boxPos().y()][box.boxPos().x()] == reachTiles.getStamp() + 1)
      {
        System.out.println("Box " + box.id() + " " + box.boxPos().x() + " " + box.boxPos().y());
        // check up
        System.out.println("checking taas!");
        if(reachTiles.getTiles()[box.boxPos().y() + 1][box.boxPos().x()] == reachTiles.getStamp() && mapData[box.boxPos().y() - 1][box.boxPos().x()] != '#' && state.getItemsData()[box.boxPos().y()-1][box.boxPos().x()] != '$' && !deadTiles[box.boxPos().y()-1][box.boxPos().x()]) {
          char[][] newItemsData = Arrays.stream(state.getItemsData()).map(char[]::clone).toArray(char[][]::new); //copy current items data
          newItemsData[playerPos.y()][playerPos.x()] = ' '; //clear player
          newItemsData[box.boxPos().y()][box.boxPos().x()] = '@'; //replace with player
          newItemsData[box.boxPos().y() - 1][box.boxPos().x()] = '$'; //move box

          if (isSolvable(newItemsData, new Pos(box.boxPos().x(), box.boxPos().y() - 1))) {
            validPushes.add(new Push(box.id(), 'u'));
          } else System.out.println("freeze deadlock!");
        } else System.out.println("wall, crate, or deadtile encountered");
        // check down
        System.out.println("checking baba!");
        if(reachTiles.getTiles()[box.boxPos().y() - 1][box.boxPos().x()] == reachTiles.getStamp() && mapData[box.boxPos().y() + 1][box.boxPos().x()] != '#' && state.getItemsData()[box.boxPos().y() + 1][box.boxPos().x()] != '$' && !deadTiles[box.boxPos().y() + 1][box.boxPos().x()])
        {
          char[][] newItemsData = Arrays.stream(state.getItemsData()).map(char[]::clone).toArray(char[][]::new); //copy current items data
          newItemsData[playerPos.y()][playerPos.x()] = ' '; //clear player
          newItemsData[box.boxPos().y()][box.boxPos().x()] = '@'; //replace with player
          newItemsData[box.boxPos().y() + 1][box.boxPos().x()] = '$'; //move box

          if (isSolvable(newItemsData, new Pos(box.boxPos().x(), box.boxPos().y() + 1))) {
            validPushes.add(new Push(box.id(), 'd'));
          } else System.out.println("freeze deadlock!");
        } else System.out.println("wall, crate, or deadtile encountered");
        // check left
        System.out.println("checking kaliwa!");
        if(reachTiles.getTiles()[box.boxPos().y()][box.boxPos().x() + 1] == reachTiles.getStamp() && mapData[box.boxPos().y()][box.boxPos().x() - 1] != '#' && state.getItemsData()[box.boxPos().y()][box.boxPos().x() - 1] != '$' && !deadTiles[box.boxPos().y()][box.boxPos().x() - 1])
        {
          char[][] newItemsData = Arrays.stream(state.getItemsData()).map(char[]::clone).toArray(char[][]::new); //copy current items data
          newItemsData[playerPos.y()][playerPos.x()] = ' '; //clear player
          newItemsData[box.boxPos().y()][box.boxPos().x()] = '@'; //replace with player
          newItemsData[box.boxPos().y()][box.boxPos().x() - 1] = '$'; //move box

          if (isSolvable(newItemsData, new Pos(box.boxPos().x() - 1, box.boxPos().y()))) {
            validPushes.add(new Push(box.id(), 'l'));
          } else System.out.println("freeze deadlock!");
        } else System.out.println("wall, crate, or deadtile encountered");
        // check right
        System.out.println("checking kanan!");
        if(reachTiles.getTiles()[box.boxPos().y()][box.boxPos().x() - 1] == reachTiles.getStamp() && mapData[box.boxPos().y()][box.boxPos().x() + 1] != '#' && state.getItemsData()[box.boxPos().y()][box.boxPos().x() + 1] != '$' && !deadTiles[box.boxPos().y()][box.boxPos().x() + 1])
        {
          char[][] newItemsData = Arrays.stream(state.getItemsData()).map(char[]::clone).toArray(char[][]::new); //copy current items data
          newItemsData[playerPos.y()][playerPos.x()] = ' '; //clear player
          newItemsData[box.boxPos().y()][box.boxPos().x()] = '@'; //replace with player
          newItemsData[box.boxPos().y()][box.boxPos().x() + 1] = '$'; //move box

          if (isSolvable(newItemsData, new Pos(box.boxPos().x() + 1, box.boxPos().y()))) {
            validPushes.add(new Push(box.id(), 'r'));
          } else System.out.println("freeze deadlock!");
        } else System.out.println("wall, crate, or deadtile encountered");
      } else System.out.println("box is unreachable");
      System.out.println();
    }

    return validPushes;
  }


  public String solveSokobanPuzzle()
  {
    if (goDeeper(0))
    {
      //Stack<Push> pushes = state.getSolution();
      ArrayDeque<Push> pushes = state.getPushes();
      System.out.println(pushes.size());

//      while(!pushes.isEmpty())
//      {
//        Push push = pushes.poll();
//        System.out.println("Box " + push.id() + " " + push.dir());
//      }
      while (!pushes.isEmpty())
      {
        System.out.println("calculating path of push");
        Push push = pushes.poll();
        calculateReach(initialState.getPos(), initialState.getItemsData());



        if (dir == 'u') {

          System.out.println("Cur sol: " + solution);
        } else if (dir == 'd') {

          System.out.println("Cur sol: " + solution);
        } else if (dir == 'l') {


        } else if (dir == 'r') {


        }
      }

      System.out.println("Bread first search, we are done!");
      return solution;
    }


    System.out.println("We are not done!");
    return "lrlrlrlrlr";
  }

}


/*

  visited.add(curState)

  do {
    ctr = 0
    validMoves = validMoves(curState)
    for (Push validMove: getValidMoves) {
      if(!isVisited(checkMove(validMove))) {
        moves.push(validMove)
        ctr++
      }
    }
    if(d < max && ctr > 0) {
      move = moves.pop
      dMove(move)
      path.push(move)

      if(isCurSolved) {
        return getPath(path)
      }

      visited.add(curState)
      d++
    }
    else {
      doUnmove(path.pop)
      d--
    }
  } while(!moves.isEmpty())










 */
