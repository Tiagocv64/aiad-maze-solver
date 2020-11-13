package Maze;

import java.awt.*;
import java.io.Serializable;

public class Door implements Serializable {
    private Button button;
    private Color color;
    private int number;
    private boolean open;

    public Door(Color color, int number) {
        this.color = color;
        this.number = number;
        this.open = false;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public Button getButton() {
        return this.button;
    }

    public Color getColor() {
        return color;
    }

    public int getNumber() {
        return number;
    }

    public boolean isOpen() {
        return open;
    }

    public void openDoor() {
        this.open = true;
    }

    public void closeDoor() {
        this.open = false;
    }
}
