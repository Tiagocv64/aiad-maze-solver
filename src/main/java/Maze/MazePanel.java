package Maze;

import javax.swing.*;
import java.awt.*;

public class MazePanel extends JPanel implements Runnable
{
    private Maze maze; // the maze object
    private Thread animator;

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

        setBackground(Color.white);
        this.setPreferredSize(maze.windowSize());

        maze.draw(page);
    }

    private void cycle() {


    }

    @Override
    public void addNotify() {
        super.addNotify();

        animator = new Thread(this);
        animator.start();
    }

    @Override
    public void run() {
        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while (true) {

            cycle();
            repaint();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = 100 - timeDiff;

            if (sleep < 0) {
                sleep = 2;
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {

                String msg = String.format("Thread interrupted: %s", e.getMessage());

                JOptionPane.showMessageDialog(this, msg, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            beforeTime = System.currentTimeMillis();
        }
    }

}