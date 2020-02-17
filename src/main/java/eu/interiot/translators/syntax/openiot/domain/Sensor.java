package eu.interiot.translators.syntax.openiot.domain;


import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * A Sensor.
 */

public class Sensor implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String sensorId;
    private String unit;
    private String sensorType;
    private String source;
    private Float latitude;
    private Float longitude;
    private Date created;
    private Date updated;
    private Place place;
    private Platform platform;
    private Category category;
    private String location;




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

    public Sensor name(String name) {
        this.name = name;
        return this;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSensorId() {
        return sensorId;
    }

    public Sensor sensorId(String sensorId) {
        this.sensorId = sensorId;
        return this;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }



    public String getUnit() {
        return unit;
    }

    public Sensor unit(String unit) {
        this.unit = unit;
        return this;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSensorType() {
        return sensorType;
    }

    public Sensor sensorType(String sensorType) {
        this.sensorType = sensorType;
        return this;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getSource() {
        return source;
    }

    public Sensor source(String source) {
        this.source = source;
        return this;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Sensor latitude(Float latitude) {
        this.latitude = latitude;
        return this;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public Sensor longitude(Float longitude) {
        this.longitude = longitude;
        return this;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Date getCreated() {
        return created;
    }

    public Sensor created(Date created) {
        this.created = created;
        return this;
    }



    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public Sensor updated(Date updated) {
        this.updated = updated;
        return this;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Place getPlace() {
        return place;
    }

    public Sensor place(Place place) {
        this.place = place;
        return this;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Sensor platform(Platform platform) {
        this.platform = platform;
        return this;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
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
        Sensor sensor = (Sensor) o;
        if (sensor.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), sensor.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Sensor{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", sensorId='" + sensorId + '\'' +
            ", unit='" + unit + '\'' +
            ", sensorType='" + sensorType + '\'' +
            ", source='" + source + '\'' +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", created=" + created +
            ", updated=" + updated +
            ", place=" + place +
            ", platform=" + platform +
            ", category=" + category +
            '}';
    }
}
