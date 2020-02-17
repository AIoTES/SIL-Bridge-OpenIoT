package eu.interiot.translators.syntax.openiot.domain;


import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * A Observation.
 */

public class Observation implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;

    private String observationId;

    private String observedProperty;
    private Double value;
    private String unit;
    private Date created;
    private Sensor sensor;

    // simlife-needle-entity-add-field - Simlife will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObservationId() {
        return observationId;
    }

    public Observation observationId(String observationId) {
        this.observationId = observationId;
        return this;
    }

    public void setObservationId(String observationId) {
        this.observationId = observationId;
    }

    public String getObservedProperty() {
        return observedProperty;
    }

    public Observation observedProperty(String observedProperty) {
        this.observedProperty = observedProperty;
        return this;
    }

    public void setObservedProperty(String observedProperty) {
        this.observedProperty = observedProperty;
    }

    public Double getValue() {
        return value;
    }

    public Observation value(Double value) {
        this.value = value;
        return this;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public Observation unit(String unit) {
        this.unit = unit;
        return this;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Date getCreated() {
        return created;
    }

    public Observation created(Date created) {
        this.created = created;
        return this;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Observation sensor(Sensor sensor) {
        this.sensor = sensor;
        return this;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
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
        Observation observation = (Observation) o;
        if (observation.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), observation.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Observation{" +
            "id=" + getId() +
            ", observationId='" + getObservationId() + "'" +
            ", observedProperty='" + getObservedProperty() + "'" +
            ", value=" + getValue() +
            ", unit='" + getUnit() + "'" +
            ", created='" + getCreated() + "'" +
            "}";
    }
}
