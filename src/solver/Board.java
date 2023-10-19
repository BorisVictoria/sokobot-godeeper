package solver;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

import java.lang.reflect.Array;
import java.util.ArrayDeque;

public class Board {
    private ArrayDeque<Push> pushes;
    private int heuristic;

    public Board(ArrayDeque<Push> pushes, int heuristic) {
        this.pushes = pushes;
        this.heuristic = heuristic;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public ArrayDeque<Push> getPushes() {
        return pushes;
    }

}
