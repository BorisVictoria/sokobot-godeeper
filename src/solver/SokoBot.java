package solver;

import java.util.*;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SokoBot {

  private final int width;
  private final int height;
  private final char[][] mapData;
  private long[][][] zobristTable;
  private final State state;
  private final State initialState;

  private final State corralState;
  private final ObjectArrayList<Pos> goals;
  private boolean areBoxesOnGoalTiles;
  private final int numGoals;
  private Reach reachTiles;
  private boolean[][] deadTiles;
  private LongOpenHashSet visitedStates;

  private int maxDepth;
  private String solution;
  PriorityQueue<Board> frontiers;

  private boolean debugMode = false;

  public SokoBot(int width, int height, char[][] mapData, char[][] itemsData) {

    this.width = width;
    this.height = height;
    this.mapData = mapData;
    this.zobristTable = new long[2][height][width];
    this.goals = new ObjectArrayList<>();
    visitedStates = new LongOpenHashSet();
    Comparator<Board> comp = Comparator.comparing(Board::getHeuristic);
    frontiers = new PriorityQueue<>(comp);

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

    Pos initialPlayer = new Pos(player.x(), player.y());
    char[][] initialItemsData = Arrays.stream(itemsData).map(char[]::clone).toArray(char[][]::new);
    initialState = new State(initialPlayer, initialItemsData, "");

    Pos corralPlayer = new Pos(player.x(), player.y());
    char[][] corralItemsData = Arrays.stream(itemsData).map(char[]::clone).toArray(char[][]::new);
    corralState = new State(corralPlayer, corralItemsData, "");

    reachTiles = new Reach(height, width);
    clear();
    deadTiles = getDeadTiles();

    maxDepth = 143;

    solution = "";

  }

  public boolean isPullValid(Pos pos, char dir) {

    if(dir == 'u') {
      if(mapData[pos.y() - 1][pos.x()] == '#')
        return false;
      else if(mapData[pos.y() - 2][pos.x()] == '#')
        return false;
    }
    else if(dir == 'd') {
      if(mapData[pos.y() + 1][pos.x()] == '#')
        return false;
      else if(mapData[pos.y() + 2][pos.x()] == '#')
        return false;
    }
    else if(dir == 'l') {
      if(mapData[pos.y()][pos.x() - 1] == '#')
        return false;
      else if(mapData[pos.y()][pos.x() - 2] == '#')
        return false;
    }
    else if(dir == 'r') {
      if (mapData[pos.y()][pos.x() + 1] == '#')
        return false;
      else if (mapData[pos.y()][pos.x() + 2] == '#')
        return false;
    }

    return true;
  }

  public boolean[][] getDeadTiles()
  {
    boolean[][] deadTiles = new boolean[height][width];
    Arrays.stream(deadTiles).forEach(row->Arrays.fill(row,true));

    for(int i = 0; i < numGoals; i++) {
      ObjectArrayFIFOQueue<Pos> toCheck = new ObjectArrayFIFOQueue<>();
      HashSet<Pos> visited = new HashSet<>();
      Pos curPos = goals.get(i);

      toCheck.enqueue(curPos);

      do
      {

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

    for (int i = 0; i < numGoals; i++)
    {
      deadTiles[goals.get(i).y()][goals.get(i).x()] = false;
    }

    return deadTiles;
  }

  public boolean isBlocked(Pos box)
  {

    this.areBoxesOnGoalTiles = false;
    boolean blockedX = false;
    boolean blockedY = false;

    if (mapData[box.y()][box.x()] == '.')
      this.areBoxesOnGoalTiles = true;

    // the hack kekw
    state.getItemsData()[box.y()][box.x()] = 'W';

    // medyo hacky pero this treats the new crate as a wall
    if (mapData[box.y()-1][box.x()] == '#' || mapData[box.y()+1][box.x()] == '#' || state.getItemsData()[box.y()-1][box.x()] == 'W' || state.getItemsData()[box.y()+1][box.x()] == 'W' || deadTiles[box.y()-1][box.x()] && deadTiles[box.y()+1][box.x()])
    {
      blockedY = true;
    }
    else if (state.getItemsData()[box.y()-1][box.x()] == '$')
    {

      blockedY = isBlocked(new Pos(box.x(),box.y()-1)); // TRY IT AGAIN BITCH
    }

    else if (state.getItemsData()[box.y() + 1][box.x()] == '$')
    {
      blockedY = isBlocked(new Pos(box.x(),box.y()+1));
    }

    if (mapData[box.y()][box.x()-1] == '#' || mapData[box.y()][box.x()+1] == '#' || state.getItemsData()[box.y()][box.x()-1] == 'W' || state.getItemsData()[box.y()][box.x()+1] == 'W' || deadTiles[box.y()][box.x()-1] && deadTiles[box.y()][box.x()+1] )
    {
      blockedX = true;
    }

    else if (state.getItemsData()[box.y()][box.x()-1] == '$')
    {
      blockedX = isBlocked(new Pos(box.x()-1, box.y()));
    }

    else if (state.getItemsData()[box.y()][box.x() + 1] == '$')
    {
      blockedX = isBlocked(new Pos(box.x()+1, box.y()));
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
//               Tiam-Lee bless you         永无BUG
    return blockedX && blockedY;

  }

  public boolean isSolvable(Pos movedBox)
  {

    if (isBlocked(movedBox))
    {
      for (int i = 0; i < height; i++)
      {
        for (int j = 0; j < width; j++)
        {
          if (state.getItemsData()[i][j] == 'W')
            state.getItemsData()[i][j] = '$';
        }
      }
      return areBoxesOnGoalTiles;
    }
    for (int i = 0; i < height; i++)
    {
      for (int j = 0; j < width; j++)
      {
        if (state.getItemsData()[i][j] == 'W')
          state.getItemsData()[i][j] = '$';
      }
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
          if((reachTiles.getMin().x() * width + reachTiles.getMin().y()) - (right.x() * width + right.y()) > 0)
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

    return new Pos(reachTiles.getMin().x(), reachTiles.getMin().y());

  }

  //Calculates the hash of the current state;
  public long calculateHash()
  {
    long key = 0;
    char[][] itemsData = state.getItemsData();
    Pos normal = state.getNormal();
    key ^= zobristTable[1][normal.y()][normal.x()];

    for (int i = 0; i < height; i++)
    {
      for (int j = 0; j < width; j++)
      {
        if (itemsData[i][j] == '$')
        {
          key ^= zobristTable[0][i][j];
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

  public ArrayList<Push> getValidPushes() {
    ArrayList<Box> boxPositions = state.getBoxPositions();
    Pos playerPos = state.getPos();
    ArrayList<Push> validPushes = new ArrayList<>();

    for(Box box : boxPositions) {
      //System.out.println("Box " + box.id() + " " + box.boxPos().x() + " " + box.boxPos().y());
      if(reachTiles.getTiles()[box.boxPos().y()][box.boxPos().x()] == reachTiles.getStamp() + 1)
      {

        // check up
        //System.out.println("checking taas!");
        if(reachTiles.getTiles()[box.boxPos().y() + 1][box.boxPos().x()] == reachTiles.getStamp() && mapData[box.boxPos().y() - 1][box.boxPos().x()] != '#' && state.getItemsData()[box.boxPos().y()-1][box.boxPos().x()] != '$' && !deadTiles[box.boxPos().y()-1][box.boxPos().x()]) {
          state.getItemsData()[playerPos.y()][playerPos.x()] = ' '; //clear player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '@'; //replace with player
          state.getItemsData()[box.boxPos().y() - 1][box.boxPos().x()] = '$'; //move box

          if (isSolvable(new Pos(box.boxPos().x(), box.boxPos().y() - 1))) {
            validPushes.add(new Push(box.id(), 'u'));
          } //else System.out.println("freeze deadlock!");
          state.getItemsData()[playerPos.y()][playerPos.x()] = '@'; //add player back
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '$'; //add box back
          state.getItemsData()[box.boxPos().y() - 1][box.boxPos().x()] = ' '; //remove box

        } // else System.out.println("wall, crate, or deadtile encountered");
        // check down
        // System.out.println("checking baba!");
        if(reachTiles.getTiles()[box.boxPos().y() - 1][box.boxPos().x()] == reachTiles.getStamp() && mapData[box.boxPos().y() + 1][box.boxPos().x()] != '#' && state.getItemsData()[box.boxPos().y() + 1][box.boxPos().x()] != '$' && !deadTiles[box.boxPos().y() + 1][box.boxPos().x()])
        {
          state.getItemsData()[playerPos.y()][playerPos.x()] = ' '; //clear player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '@'; //replace with player
          state.getItemsData()[box.boxPos().y() + 1][box.boxPos().x()] = '$'; //move box

          if (isSolvable(new Pos(box.boxPos().x(), box.boxPos().y() + 1))) {
            validPushes.add(new Push(box.id(), 'd'));
          } // else System.out.println("freeze deadlock!");

          state.getItemsData()[playerPos.y()][playerPos.x()] = '@'; //clear player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '$'; //replace with player
          state.getItemsData()[box.boxPos().y() + 1][box.boxPos().x()] = ' '; //move box
        } // else System.out.println("wall, crate, or deadtile encountered");
        // check left
        // System.out.println("checking kaliwa!");
        if(reachTiles.getTiles()[box.boxPos().y()][box.boxPos().x() + 1] == reachTiles.getStamp() && mapData[box.boxPos().y()][box.boxPos().x() - 1] != '#' && state.getItemsData()[box.boxPos().y()][box.boxPos().x() - 1] != '$' && !deadTiles[box.boxPos().y()][box.boxPos().x() - 1])
        {
          state.getItemsData()[playerPos.y()][playerPos.x()] = ' '; //clear player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '@'; //replace with player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x() - 1] = '$'; //move box

          if (isSolvable(new Pos(box.boxPos().x() - 1, box.boxPos().y()))) {
            validPushes.add(new Push(box.id(), 'l'));
          } // else System.out.println("freeze deadlock!");
          state.getItemsData()[playerPos.y()][playerPos.x()] = '@'; //clear player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '$'; //replace with player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x() - 1] = ' '; //move box
        } // else System.out.println("wall, crate, or deadtile encountered");
        // check right
        // System.out.println("checking kanan!");
        if(reachTiles.getTiles()[box.boxPos().y()][box.boxPos().x() - 1] == reachTiles.getStamp() && mapData[box.boxPos().y()][box.boxPos().x() + 1] != '#' && state.getItemsData()[box.boxPos().y()][box.boxPos().x() + 1] != '$' && !deadTiles[box.boxPos().y()][box.boxPos().x() + 1])
        {
          state.getItemsData()[playerPos.y()][playerPos.x()] = ' '; //clear player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '@'; //replace with player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x() + 1] = '$'; //move box

          if (isSolvable(new Pos(box.boxPos().x() + 1, box.boxPos().y()))) {
            validPushes.add(new Push(box.id(), 'r'));
          } // else System.out.println("freeze deadlock!");
          state.getItemsData()[playerPos.y()][playerPos.x()] = '@'; //clear player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '$'; //replace with player
          state.getItemsData()[box.boxPos().y()][box.boxPos().x() + 1] = ' '; //move box
        } // else System.out.println("wall, crate, or deadtile encountered");
      } // else System.out.println("box is unreachable");
      // System.out.println();
    }

    return validPushes;
  }

  public int setupBoard(Board board) {
    ArrayDeque<Push> boardPushes = board.getPushes();
    ArrayDeque<Push> statePushes = state.getPushes();
    ArrayDeque<Push> newStatePushes = new ArrayDeque<>(1000);

//    state.setState(new Pos(initialState.getPos().x(), initialState.getPos().y()), Arrays.stream(initialState.getItemsData()).map(char[]::clone).toArray(char[][]::new), "");
//    int depth = 0;
//    while(!boardPushes.isEmpty()) {
//      state.move(boardPushes.poll());
//      depth++;
//    }
//
//    return depth;

    while (!boardPushes.isEmpty() && !statePushes.isEmpty())
    {
      Push boardPush = boardPushes.poll();
      Push statePush = statePushes.poll();

      if (!boardPush.equals(statePush))
      {
        boardPushes.offerFirst(boardPush);
        statePushes.offerFirst(statePush);
        break;
      }
      else
        newStatePushes.offer(boardPush);
    }

    while (!statePushes.isEmpty())
    {
      state.unmove();
    }

    state.setPushes(newStatePushes);

    while (!boardPushes.isEmpty())
    {
      state.move(boardPushes.poll());
    }

    return state.getPushes().size();

  }

  public int calculateHeuristic() {
    int heuristic = 0;

    ArrayList<Box> boxes = state.getBoxPositions();
    for(int i = 0; i < boxes.size(); i++) {
      int min = 999;
      for(int j = 0; j < goals.size(); j++) {
        int dist = Math.abs(boxes.get(i).boxPos().x() - goals.get(j).x()) + Math.abs(boxes.get(i).boxPos().y() - goals.get(j).y());
        if(dist < min)
          min = dist;
      }
      heuristic += min;
    }

    return heuristic;
  }
  public boolean expand() {
    state.setNormal(calculateReach(state.getPos(), state.getItemsData()));
    ArrayList<Push> validPushes = isPiCorralled();
    for(int i = 0; i < validPushes.size(); i++) {
      state.move(validPushes.get(i));
      if(isSolved()) {
        return true;
      }
      state.setNormal(calculateReach(state.getPos(), state.getItemsData()));
      if(visitedStates.contains(calculateHash())) {
        state.unmove();
      }
      else {
        visitedStates.add(calculateHash());
        Board toOffer = new Board(new ArrayDeque<>(state.getPushes()), calculateHeuristic());
        // System.out.println("offering: " + state.getPushes());
        frontiers.offer(toOffer);
        state.unmove();
      }
    }

    return false;
  }

  public ArrayList<Push> isPiCorralled()
  {
    // Check if there is a corral
    int[][] reachTiles = this.reachTiles.getTiles();
    boolean[][] corralTilesArr = new boolean[height][width];
    char[][] itemsData = state.getItemsData();
    ArrayList<Push> validPushes = getValidPushes();
    ArrayList<Push> corralPushes = new ArrayList<>();

    boolean isCorral = false;
    for(int i = 0; i < height; i++) {
      for(int j = 0; j < width; j ++) {
        if(reachTiles[i][j] < this.reachTiles.getStamp() && reachTiles[i][j] != 0 && itemsData[i][j] != '$') {
          isCorral = true;
          corralTilesArr[i][j] = true;
        }
        else {
          corralTilesArr[i][j] = false;
        }
      }
    }

    if(!isCorral) {
      //System.out.println("no unreachable tile detected");
      return validPushes;
    }

    // Check if all possible pushes of the boxes on the corral barrier are pushes into the corral

    // find pushes on boxes that are adjacent to a corral tile

    boolean isPICorral = true;
    boolean isAllOnGoal = true;
    for(Push validPush : validPushes) {
      Pos boxPos = state.getBoxPositions().get(validPush.id()).boxPos();
      char dir = validPush.dir();
      boolean isAdj = false;

      if(corralTilesArr[boxPos.y() - 1][boxPos.x()])
        isAdj = true;
      if(corralTilesArr[boxPos.y() + 1][boxPos.x()])
        isAdj = true;
      if(corralTilesArr[boxPos.y()][boxPos.x() - 1])
        isAdj = true;
      if(corralTilesArr[boxPos.y()][boxPos.x() + 1])
        isAdj = true;

      if(isAdj) {
        corralPushes.add(validPush);
        if(mapData[boxPos.y()][boxPos.x()] != '.') {
          isAllOnGoal = false;
        }
        if (dir == 'u' && !corralTilesArr[boxPos.y() - 1][boxPos.x()]) {
          isPICorral = false;
          break;
        }
        if (dir == 'd' && !corralTilesArr[boxPos.y() + 1][boxPos.x()]) {
          isPICorral = false;
          break;
        }
        if (dir == 'l' && !corralTilesArr[boxPos.y()][boxPos.x() - 1]) {
          isPICorral = false;
          break;
        }
        if (dir == 'r' && !corralTilesArr[boxPos.y()][boxPos.x() + 1]) {
          isPICorral = false;
          break;
        }
      }
    }

    if(!isPICorral) {
      //System.out.println("not all pushes are towards corral");
      return validPushes;
    }

    // Check if at least one of the boxes in the corral has to be pushed to solve the level
    if(isAllOnGoal) {
      //System.out.println("all boxes are on goal");
      return validPushes;
    }
    //System.out.println("replacing pushes with corral pushes");

    return corralPushes;
  }

  public String testPICorral()
  {
    char[][] emptyItemsData = new char[height][width];
    for(int i = 0; i < height; i ++) {
      for(int j = 0; j < width; j++) {
        emptyItemsData[i][j] = ' ';
      }
    }
    calculateReach(state.getPos(), emptyItemsData);
    calculateReach(state.getPos(), state.getItemsData());
    for (int i = 0; i < height; i++)
    {
      for (int j = 0; j < width; j++)
      {
        if (reachTiles.getTiles()[i][j] == Integer.MAX_VALUE)
          System.out.print("+");
        else
          System.out.print(reachTiles.getTiles()[i][j]);
      }
      System.out.println();
    }
    System.out.println(isPiCorralled());
    return "lrrlrlrlr";
  }


  public String solveSokobanPuzzle() {

//    for (int i = 0; i < height; i++)
//    {
//      for (int j = 0; j < width; j++)
//      {
//        if (mapData[i][j] == '#')
//          System.out.print("~");
//        else if (deadTiles[i][j])
//          System.out.print("x");
//        else
//          System.out.print("O");
//      }
//      System.out.println();
//    }
    char[][] emptyItemsData = new char[height][width];
    for(int i = 0; i < height; i ++) {
      for(int j = 0; j < width; j++) {
        emptyItemsData[i][j] = ' ';
      }
    }

    while(maxDepth < 500) {
      // RESET EVERYTHING
      state.setState(new Pos(initialState.getPos().x(), initialState.getPos().y()), Arrays.stream(initialState.getItemsData()).map(char[]::clone).toArray(char[][]::new), "");
      visitedStates.clear();
      frontiers.clear();
      clear();
      calculateReach(state.getPos(), emptyItemsData);
      calculateReach(state.getPos(), state.getItemsData());
      maxDepth += 10;
      solution = "";
      System.out.println("Max Depth: " + maxDepth);

      frontiers.offer(new Board(new ArrayDeque<>(state.getPushes()), calculateHeuristic()));
      int nodes = 0;

      while(!frontiers.isEmpty()) {

        Board curBoard = frontiers.poll();
        nodes++;
        // System.out.println("Expanding:" + curBoard.getPushes() + " , heuristic: " + curBoard.getHeuristic());
        int depth = setupBoard(curBoard);

        if (depth <= maxDepth) {
          // DEBUG PRINTS
//          for (int j = 0; j < height; j++)
//          {
//            for (int k = 0; k < width; k++)
//            {
//              if (state.getItemsData()[j][k] == ' ')
//                System.out.print(mapData[j][k]);
//              else System.out.print(state.getItemsData()[j][k]);
//            }
//            System.out.println();
//          }

          if (expand()) {
            ArrayDeque<Push> pushes = state.getPushes();
            // System.out.println(pushes.size());
            //System.out.println(pushes.size());


//      while(!pushes.isEmpty())
//      {
//        Push push = pushes.poll();
//        System.out.println("Box " + push.id() + " " + push.dir());
//      }
            while (!pushes.isEmpty()) {
              calculateReach(initialState.getPos(), initialState.getItemsData());
              Push push = pushes.poll();
              Pos boxPos = initialState.getBoxPositions().get(push.id()).boxPos();
              Pos startPos = initialState.getPos();

              if (push.dir() == 'u') {
                solution += calculatePath(startPos, new Pos(boxPos.x(), boxPos.y() + 1)) + "u";
              } else if (push.dir() == 'd') {
                solution += calculatePath(startPos, new Pos(boxPos.x(), boxPos.y() - 1)) + "d";
              } else if (push.dir() == 'l') {
                solution += calculatePath(startPos, new Pos(boxPos.x() + 1, boxPos.y())) + "l";
              } else if (push.dir() == 'r') {
                solution += calculatePath(startPos, new Pos(boxPos.x() - 1, boxPos.y())) + "r";
              }

              initialState.moveInitial(push);
            }
            System.out.println("Bread first search, we are done!");
            System.out.println("Nodes expanded: " + nodes);
            return solution;
          }
        }
      }

    }
    System.out.println("We are not done!");
    return "uuuuuuu";
  }


}


/*

 */