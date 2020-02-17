package eu.interiot.translators.syntax.openiot.domain;



import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * A Place.
 */

public class Place implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;


    private String address;


    private String city;


    private String country;


    private Float latitude;


    private Float longitude;


    private String province;


    private String continent;


    private String infos;


    private Date created;


    private Date updated;

    // simlife-needle-entity-add-field - Simlife will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public Place address(String address) {
        this.address = address;
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public Place city(String city) {
        this.city = city;
        return this;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public Place country(String country) {
        this.country = country;
        return this;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Place latitude(Float latitude) {
        this.latitude = latitude;
        return this;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public Place longitude(Float longitude) {
        this.longitude = longitude;
        return this;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getProvince() {
        return province;
    }

    public Place province(String province) {
        this.province = province;
        return this;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getContinent() {
        return continent;
    }

    public Place continent(String continent) {
        this.continent = continent;
        return this;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getInfos() {
        return infos;
    }

    public Place infos(String infos) {
        this.infos = infos;
        return this;
    }

    public void setInfos(String infos) {
        this.infos = infos;
    }

    public Date getCreated() {
        return created;
    }

    public Place created(Date created) {
        this.created = created;
        return this;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public Place updated(Date updated) {
        this.updated = updated;
        return this;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
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
        Place place = (Place) o;
        if (place.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), place.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Place{" +
            "id=" + getId() +
            ", address='" + getAddress() + "'" +
            ", city='" + getCity() + "'" +
            ", country='" + getCountry() + "'" +
            ", latitude=" + getLatitude() +
            ", longitude=" + getLongitude() +
            ", province='" + getProvince() + "'" +
            ", continent='" + getContinent() + "'" +
            ", infos='" + getInfos() + "'" +
            ", created='" + getCreated() + "'" +
            ", updated='" + getUpdated() + "'" +
            "}";
    }
}
