package Maze;

import javax.swing.*;
import java.awt.*;

public class MazePanel extends JPanel
{
    private Maze maze; // the maze object

    public MazePanel(Maze theMaze)
    {
        maze = theMaze;
    }


    public void paintComponent(Graphics page)
    {
        super.paintComponent(page);

        setBackground(Color.white);
        this.setPreferredSize(maze.windowSize());

        maze.draw(page);
    }
}