package Maze;

import java.awt.*;
import java.io.Serializable;

public class Door implements Serializable {
    private Button button;
    private Color color;

    public Door(Color color) {
        this.color = color;
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
}
