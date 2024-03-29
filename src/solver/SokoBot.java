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
  private boolean areBoxesOnGoalTiles;
  private final int numGoals;
  private Reach reachTiles;
  private boolean[][] deadTiles;
  private LongOpenHashSet visitedStates;
  private int maxDepth;
  private String solution;
  PriorityQueue<Board> frontiers;
  private final boolean[] toExpand;
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
    this.toExpand = new boolean[goals.size()];
    Arrays.fill(toExpand, true);

    Pos player = new Pos(0,0);
    for(int i = 0; i < height; i++) {
      for(int j = 0; j < width; j++) {
        if(itemsData[i][j] == '@') {
          player = new Pos(j, i);
          break;
        }
      }
    }



    state = new State(player, itemsData);

    Pos initialPlayer = new Pos(player.x(), player.y());
    char[][] initialItemsData = Arrays.stream(itemsData).map(char[]::clone).toArray(char[][]::new);
    initialState = new State(initialPlayer, initialItemsData);

    reachTiles = new Reach(height, width);
    clear();
    deadTiles = getDeadTiles();

    maxDepth = 153;

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

      blockedY = isBlocked(new Pos(box.x(),box.y()-1));
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

    if (reachTiles.getStamp() >= Integer.MAX_VALUE - 2)
      clear();

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
      if(box.toExpand())
      if(reachTiles.getTiles()[box.boxPos().y()][box.boxPos().x()] == reachTiles.getStamp() + 1)
      {
        if(reachTiles.getTiles()[box.boxPos().y() + 1][box.boxPos().x()] == reachTiles.getStamp() && mapData[box.boxPos().y() - 1][box.boxPos().x()] != '#' && state.getItemsData()[box.boxPos().y()-1][box.boxPos().x()] != '$' && !deadTiles[box.boxPos().y()-1][box.boxPos().x()]) {
          state.getItemsData()[playerPos.y()][playerPos.x()] = ' ';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '@';
          state.getItemsData()[box.boxPos().y() - 1][box.boxPos().x()] = '$';

          if (isSolvable(new Pos(box.boxPos().x(), box.boxPos().y() - 1))) {
            validPushes.add(new Push(box.id(), 'u'));
          } //else System.out.println("freeze deadlock!");
          state.getItemsData()[playerPos.y()][playerPos.x()] = '@';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '$';
          state.getItemsData()[box.boxPos().y() - 1][box.boxPos().x()] = ' ';

        }

        if(reachTiles.getTiles()[box.boxPos().y() - 1][box.boxPos().x()] == reachTiles.getStamp() && mapData[box.boxPos().y() + 1][box.boxPos().x()] != '#' && state.getItemsData()[box.boxPos().y() + 1][box.boxPos().x()] != '$' && !deadTiles[box.boxPos().y() + 1][box.boxPos().x()])
        {
          state.getItemsData()[playerPos.y()][playerPos.x()] = ' ';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '@';
          state.getItemsData()[box.boxPos().y() + 1][box.boxPos().x()] = '$';

          if (isSolvable(new Pos(box.boxPos().x(), box.boxPos().y() + 1))) {
            validPushes.add(new Push(box.id(), 'd'));
          }

          state.getItemsData()[playerPos.y()][playerPos.x()] = '@';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '$';
          state.getItemsData()[box.boxPos().y() + 1][box.boxPos().x()] = ' ';
        }

        if(reachTiles.getTiles()[box.boxPos().y()][box.boxPos().x() + 1] == reachTiles.getStamp() && mapData[box.boxPos().y()][box.boxPos().x() - 1] != '#' && state.getItemsData()[box.boxPos().y()][box.boxPos().x() - 1] != '$' && !deadTiles[box.boxPos().y()][box.boxPos().x() - 1])
        {
          state.getItemsData()[playerPos.y()][playerPos.x()] = ' ';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '@';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x() - 1] = '$';

          if (isSolvable(new Pos(box.boxPos().x() - 1, box.boxPos().y()))) {
            validPushes.add(new Push(box.id(), 'l'));
          }
          state.getItemsData()[playerPos.y()][playerPos.x()] = '@';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '$';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x() - 1] = ' ';
        }

        if(reachTiles.getTiles()[box.boxPos().y()][box.boxPos().x() - 1] == reachTiles.getStamp() && mapData[box.boxPos().y()][box.boxPos().x() + 1] != '#' && state.getItemsData()[box.boxPos().y()][box.boxPos().x() + 1] != '$' && !deadTiles[box.boxPos().y()][box.boxPos().x() + 1])
        {
          state.getItemsData()[playerPos.y()][playerPos.x()] = ' ';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '@';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x() + 1] = '$';

          if (isSolvable(new Pos(box.boxPos().x() + 1, box.boxPos().y()))) {
            validPushes.add(new Push(box.id(), 'r'));
          }
          state.getItemsData()[playerPos.y()][playerPos.x()] = '@';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x()] = '$';
          state.getItemsData()[box.boxPos().y()][box.boxPos().x() + 1] = ' ';
        }
      }

    }

    return validPushes;
  }

  public int setupBoard(Board board) {
    ArrayDeque<Push> boardPushes = board.getPushes();
    ArrayDeque<Push> statePushes = state.getPushes();
    ArrayDeque<Push> newStatePushes = new ArrayDeque<>(200);

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
    state.setNormal(calculateReach(state.getPos(), state.getItemsData()));
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
    ArrayList<Push> validPushes = piCorralPruning();
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
        frontiers.offer(toOffer);
        state.unmove();
      }
    }

    return false;
  }

  public String solveSokobanPuzzle() {

    calculateReach(state.getPos(), state.getItemsData());
    frontiers.offer(new Board(new ArrayDeque<>(state.getPushes()), calculateHeuristic()));
    int nodes = 0;
    while(!frontiers.isEmpty()) {

      Board curBoard = frontiers.poll();
      nodes++;

      int depth = setupBoard(curBoard);

      if (depth <= maxDepth) {

        if (expand()) {
          ArrayDeque<Push> pushes = state.getPushes();

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

    System.out.println("We are not done!");
    return "udlrudlrudlrudlrudlr";
  }

  public ArrayList<Push> piCorralPruning()
  {
    ArrayList<Push> validPushes = getValidPushes();
    Arrays.fill(toExpand, false);
    ArrayList<Box> boxes = state.getBoxPositions();
    int[][] tiles = Arrays.stream(reachTiles.getTiles()).map(int[]::clone).toArray(int[][]::new);
    int stamp = reachTiles.getStamp();
    boolean atLeastOnePiCorral = false;

    for (Push push: validPushes)
    {
      if (push.dir() == 'u')
      {
        if (tiles[boxes.get(push.id()).boxPos().y()+1][boxes.get(push.id()).boxPos().x()] < stamp) {
          if (isPiCorral(new Pos(boxes.get(push.id()).boxPos().x(), boxes.get(push.id()).boxPos().y()-1), validPushes))
          {
            atLeastOnePiCorral = true;
          }
        }

      }
      else if (push.dir() == 'd')
      {
        if (tiles[boxes.get(push.id()).boxPos().y()-1][boxes.get(push.id()).boxPos().x()] < stamp) {
          if (isPiCorral(new Pos(boxes.get(push.id()).boxPos().x(), boxes.get(push.id()).boxPos().y()+1), validPushes))
          {
            atLeastOnePiCorral = true;
          }
        }

      }

      else if (push.dir() == 'l') {
        if (tiles[boxes.get(push.id()).boxPos().y()][boxes.get(push.id()).boxPos().x() - 1] < stamp) {
          if (isPiCorral(new Pos(boxes.get(push.id()).boxPos().x() - 1, boxes.get(push.id()).boxPos().y()), validPushes))
          {
            atLeastOnePiCorral = true;
          }
        }
      }

      else if (push.dir() == 'r')
      {
        if (tiles[boxes.get(push.id()).boxPos().y()+1][boxes.get(push.id()).boxPos().x()+1] < stamp) {
          if (isPiCorral(new Pos(boxes.get(push.id()).boxPos().x()+1, boxes.get(push.id()).boxPos().y()), validPushes))
          {
            atLeastOnePiCorral = true;
          }
        }
      }
    }

    if (atLeastOnePiCorral)
    {
      for (int i = 0, boxPositionsSize = boxes.size(); i < boxPositionsSize; i++)
      {
        if (toExpand[i])
          boxes.set(i, new Box(boxes.get(i).id(), boxes.get(i).boxPos(), true));
        else
          boxes.set(i, new Box(boxes.get(i).id(), boxes.get(i).boxPos(), false));
      }
      calculateReach(state.getPos(), state.getItemsData());
      ArrayList<Push> toReturn = getValidPushes();
      bringBACKTHEBOXES();
      return toReturn;
    }
    else
    {
      return validPushes;
    }


  }

  public boolean isPiCorral(Pos floor, ArrayList<Push> validPushes) {

    calculateReach(floor, state.getItemsData());

    int[][] tiles = reachTiles.getTiles();
    int stamp = reachTiles.getStamp();
    char[][] itemsData = state.getItemsData();
    boolean areAllBoxesOnGoal = true;
    ArrayList<Box> boxPositions = state.getBoxPositions();
    for (int i = 0, boxPositionsSize = boxPositions.size(); i < boxPositionsSize; i++)
    {
      Box box = boxPositions.get(i);
      if (!(tiles[box.boxPos().y()][box.boxPos().x()] == stamp + 1))
      {
        state.getBoxPositions().set(box.id(), new Box(box.id(), box.boxPos(), false));
        itemsData[box.boxPos().y()][box.boxPos().x()] = ' ';
      }
      else
      {
        if (mapData[box.boxPos().y()][box.boxPos().x()] != '.')
          areAllBoxesOnGoal = false;
      }
    }

    if (areAllBoxesOnGoal)
    {
      bringBACKTHEBOXES();
      return false;
    }

    calculateReach(state.getPos(), state.getItemsData());

    ArrayList<Push> corralPushes = getValidPushes();

    for (Push push : corralPushes)
    {
      Box corralBox = state.getBoxPositions().get(push.id());
      if (push.dir() == 'u')
      {
        if (!(tiles[corralBox.boxPos().y()-1][corralBox.boxPos().x()] == stamp))
        {
          bringBACKTHEBOXES();
          return false;
        }

      }
      else if (push.dir() == 'd')
      {
        if (!(tiles[corralBox.boxPos().y()+1][corralBox.boxPos().x()] == stamp))
        {
          bringBACKTHEBOXES();
          return false;
        }

      }
      else if (push.dir() == 'l')
      {
        if (!(tiles[corralBox.boxPos().y()][corralBox.boxPos().x()-1] == stamp))
        {
          bringBACKTHEBOXES();
          return false;
        }

      }
      else if (push.dir() == 'r')
      {
        if (!(tiles[corralBox.boxPos().y()][corralBox.boxPos().x()+1] == stamp))
        {
          bringBACKTHEBOXES();
          return false;
        }

      }
    }

    int ctr = 0;

    for (Push corralPush: corralPushes)
    {
      for (Push push: validPushes)
      {
        if (corralPush.equals(push))
          ctr++;
      }
    }

    if (ctr != corralPushes.size())
    {
      bringBACKTHEBOXES();
      return false;
    }

    for (Box box: state.getBoxPositions())
    {
      if (box.toExpand())
        toExpand[box.id()] = true;
    }
    bringBACKTHEBOXES();
    return true;

  }
  public void bringBACKTHEBOXES()
  {

    char[][] itemsData = state.getItemsData();

    ArrayList<Box> boxPositions = state.getBoxPositions();
    for (int i = 0, boxPositionsSize = boxPositions.size(); i < boxPositionsSize; i++) {
      Box box = boxPositions.get(i);
      if (!box.toExpand())
      {
        state.getBoxPositions().set(box.id(), new Box(box.id(), box.boxPos(), true));
        itemsData[box.boxPos().y()][box.boxPos().x()] = '$';
      }

    }

  }

}
