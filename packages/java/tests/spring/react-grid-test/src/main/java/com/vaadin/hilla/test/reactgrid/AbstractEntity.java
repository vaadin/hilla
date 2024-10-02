package com.vaadin.hilla.test.reactgrid;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import com.vaadin.hilla.Nullable;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@MappedSuperclass
public class AbstractEntity {
    @Id
    @Nullable
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Nullable
    private Long version;


    private OffsetDateTime offsetDateTime;
    private ZonedDateTime zonedDateTime;
    private java.util.Date utilDate;
    private java.sql.Date sqlDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    public Date getUtilDate() {
        return utilDate;
    }

    public void setUtilDate(Date utilDate) {
        this.utilDate = utilDate;
    }

    public java.sql.Date getSqlDate() {
        return sqlDate;
    }

    public void setSqlDate(java.sql.Date sqlDate) {
        this.sqlDate = sqlDate;
    }
}
