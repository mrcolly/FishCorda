package com.example.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

public class RuleSchema extends MappedSchema {

    public RuleSchema() {
        super(RuleSchema.class, 1, ImmutableList.of(PersistentRule.class));
    }

    @Entity
    @Table(name = "ruleStates")
    public static class PersistentRule extends PersistentState {
        @Column(name = "owner") private final String owner;
        @Column(name = "id") private final UUID id;
        @Column(name = "specie") private final String specie;
        @Column(name = "catchMethod") private final String catchMethod;
        @Column(name = "latitude") private final double latitude;
        @Column(name = "longitude") private final double longitude;
        @Column(name = "radius") private final double radius;
        @Column(name = "fromDate") private final long fromDate;
        @Column(name = "toDate") private final long toDate;
        @Column(name = "dateCreation") private final long dateCreation;

        public PersistentRule(String owner, UUID id, String specie, String catchMethod, double latitude, double longitude, double radius, long fromDate, long toDate, long dateCreation) {
            this.owner = owner;
            this.id = id;
            this.specie = specie;
            this.catchMethod = catchMethod;
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.dateCreation = dateCreation;
        }

        public PersistentRule() {
            this.owner = "";
            this.id = null;
            this.specie = "";
            this.catchMethod = "";
            this.latitude = 0;
            this.longitude = 0;
            this.radius = 0;
            this.fromDate = 0;
            this.toDate = 0;
            this.dateCreation = 0;
        }

        public String getOwner() {
            return owner;
        }

        public UUID getId() {
            return id;
        }

        public String getSpecie() {
            return specie;
        }

        public String getcatchMethod() {
            return catchMethod;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getRadius() {
            return radius;
        }

        public long getFromDate() {
            return fromDate;
        }

        public long getToDate() {
            return toDate;
        }

        public long getDateCreation() {
            return dateCreation;
        }
    }



}
