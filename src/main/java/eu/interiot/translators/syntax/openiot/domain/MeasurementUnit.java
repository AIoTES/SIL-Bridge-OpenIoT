package eu.interiot.translators.syntax.openiot.domain;


import java.io.Serializable;
import java.util.Objects;

/**
 * A MeasurementUnit.
 */

public class MeasurementUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String unit;
    private String description;

    // simlife-needle-entity-add-field - Simlife will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public MeasurementUnit unit(String unit) {
        this.unit = unit;
        return this;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public MeasurementUnit description(String description) {
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
        MeasurementUnit measurementUnit = (MeasurementUnit) o;
        if (measurementUnit.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), measurementUnit.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MeasurementUnit{" +
            "id=" + getId() +
            ", unit='" + getUnit() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
