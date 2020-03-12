package sim;

import java.util.Objects;

// Purpose of this class is to create a mutable int for the lines
public class Vertex {
    private int id;

    public Vertex(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return id == vertex.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
