package Maze;

import java.awt.*;
import java.io.Serializable;

public class Door implements Serializable {
    private Button button;
    private Color color;
    private int number;
    private boolean open = false;

    public Door(Color color, int number) {
        this.color = color;
        this.number = number;
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

    public void openDoor() {
        this.open = true;
    }

    public boolean isOpen() {
        return open;
    }
}
