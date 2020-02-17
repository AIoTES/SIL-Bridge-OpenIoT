package eu.interiot.translators.syntax.openiot.domain;


import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * A Platform.
 */

public class Platform implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;

    private String logo;

    private byte[] logoImage;

    private String logoImageContentType;
    private String about;
    private String address;

    private String url;

    private Float latitude;

    private Float longitude;

    private String platformId;

    private Date created;

    private Date updated;

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

    public Platform name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public Platform logo(String logo) {
        this.logo = logo;
        return this;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public byte[] getLogoImage() {
        return logoImage;
    }

    public Platform logoImage(byte[] logoImage) {
        this.logoImage = logoImage;
        return this;
    }

    public void setLogoImage(byte[] logoImage) {
        this.logoImage = logoImage;
    }

    public String getLogoImageContentType() {
        return logoImageContentType;
    }

    public Platform logoImageContentType(String logoImageContentType) {
        this.logoImageContentType = logoImageContentType;
        return this;
    }

    public void setLogoImageContentType(String logoImageContentType) {
        this.logoImageContentType = logoImageContentType;
    }

    public String getAbout() {
        return about;
    }

    public Platform about(String about) {
        this.about = about;
        return this;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getAddress() {
        return address;
    }

    public Platform address(String address) {
        this.address = address;
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUrl() {
        return url;
    }

    public Platform url(String url) {
        this.url = url;
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Platform latitude(Float latitude) {
        this.latitude = latitude;
        return this;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public Platform longitude(Float longitude) {
        this.longitude = longitude;
        return this;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getPlatformId() {
        return platformId;
    }

    public Platform platformId(String platformId) {
        this.platformId = platformId;
        return this;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public Date getCreated() {
        return created;
    }

    public Platform created(Date created) {
        this.created = created;
        return this;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public Platform updated(Date updated) {
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
        Platform platform = (Platform) o;
        if (platform.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), platform.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Platform{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", logo='" + getLogo() + "'" +
            ", logoImage='" + getLogoImage() + "'" +
            ", logoImageContentType='" + getLogoImageContentType() + "'" +
            ", about='" + getAbout() + "'" +
            ", address='" + getAddress() + "'" +
            ", url='" + getUrl() + "'" +
            ", latitude=" + getLatitude() +
            ", longitude=" + getLongitude() +
            ", platformId='" + getPlatformId() + "'" +
            ", created='" + getCreated() + "'" +
            ", updated='" + getUpdated() + "'" +
            "}";
    }
}
