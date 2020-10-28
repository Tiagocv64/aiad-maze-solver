package Maze;

public class MazeRunner {
    Maze maze;
    int initialY, initialX;

    public MazeRunner(Maze maze){
        this.maze = maze;
        initialY = maze.N-1;
        initialX = maze.N-1;
    }

    public boolean[] getPossibleMovesFromPosition(int y, int x){
        boolean[] possible = new boolean[4];
        for (int i = 0; i < 4; i++)
            possible[i] = getCellFromPosition(y, x).walls[i] == maze.N * maze.N;
        return possible;
    }

    private Maze.Cell getCellFromPosition(int y, int x){
        return maze.cells[y*maze.N+x];
    }

    public int getInitialY() {
        return initialY;
    }

    public int getInitialX() {
        return initialX;
    }
}
