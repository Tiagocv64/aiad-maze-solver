package Agents;

import java.awt.*;
import java.io.Serializable;

public class AgentInfo implements Serializable {
    public Integer effort;
    public Color color;

    public AgentInfo(Color color) {
        this.color = color;
        this.effort = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final AgentInfo other = (AgentInfo) obj;

        // change to ID
        return this.color.equals(other.color);
    }

    @Override
    public int hashCode() {
        int result = effort;
        result = 31 * result + color.getRGB();
        return result;
    }

}
