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

    // The paintComponent method is called every time
    // that the panel needs to be displayed or refreshed.
    // Anything you want drawn on the panel should be drawn
    // in this method.
    public void paintComponent(Graphics page)
    {
        super.paintComponent(page);

        setBackground(Color.white); // set preferredSize for JScrollPane

        this.setPreferredSize(maze.windowSize()); // draw the maze and the solution
        // path in red points

        maze.draw(page);
    }
}