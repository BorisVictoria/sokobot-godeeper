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

  public String solveSokobanPuzzle()
  {


    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }

}
