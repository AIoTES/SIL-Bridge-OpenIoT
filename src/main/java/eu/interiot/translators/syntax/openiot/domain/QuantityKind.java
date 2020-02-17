package eu.interiot.translators.syntax.openiot.domain;



import java.io.Serializable;
import java.util.Objects;

/**
 * A QuantityKind.
 */

public class QuantityKind implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;


    private String name;

    private String description;

    // simlife-needle-entity-add-field - Simlife will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public QuantityKind name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public QuantityKind description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    // simlife-needle-entity-add-getters-setters - Simlife will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuantityKind quantityKind = (QuantityKind) o;
        if (quantityKind.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), quantityKind.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "QuantityKind{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
