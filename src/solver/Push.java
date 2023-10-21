package solver;

public record Push(int id, char dir) {

    @Override
    public boolean equals(Object o) {
        Push push = (Push) o;

        if (id != push.id) return false;
        return dir == push.dir;
    }

}
