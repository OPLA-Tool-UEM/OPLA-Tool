package arquitetura.representation;


import java.io.Serializable;
import java.util.Objects;

/**
 * Representa parametro de um método.
 *
 * @author edipofederle<edipofederle@gmail.com>
 */
public class ParameterMethod implements Serializable {

    private String name;
    private String type;
    private String direction;

    public ParameterMethod() {
    }

    /**
     * @param name      - Name
     * @param type      - Tipo (ex: String)
     * @param direction - in ou out. In = entrada, out = saida
     */
    public ParameterMethod(String name, String type, String direction) {
        this.name = name;
        this.type = type;
        this.direction = direction;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ParameterMethod other = (ParameterMethod) obj;
        return Objects.equals(name, other.name) /*&&
                Objects.equals(type, other.type) &&
                Objects.equals(direction, other.direction)*/;
    }


}
