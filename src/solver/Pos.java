package solver;

public record Pos(int x, int y) {
    @Override
    public boolean equals(Object obj) {
        Pos pos2 = (Pos) obj;
        return x == pos2.x() && y == pos2.y();
    }
}
