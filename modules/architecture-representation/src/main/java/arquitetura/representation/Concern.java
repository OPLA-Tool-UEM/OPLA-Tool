package arquitetura.representation;


import java.io.Serializable;

/**
 * @author edipofederle<edipofederle@gmail.com>
 */
public class Concern implements Serializable {

    private String name;

    public Concern(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void updateConcernsList(String newName) {
        this.name += "," + newName;
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
        Concern other = (Concern) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public boolean namesMatch(Concern otherConcern) {
        return this.getName().equalsIgnoreCase(otherConcern.getName());
    }

    public boolean isContainedByElement(Element element) {
        return element.containsConcern(this);
    }

    public boolean isOnlyConcernOfElement(Element element) {
        return element.hasOnlyOneConcern(this);
    }
}