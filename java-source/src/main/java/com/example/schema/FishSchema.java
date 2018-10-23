package com.example.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

public class FishSchema extends MappedSchema {

    public FishSchema() {
        super(FishSchema.class, 1, ImmutableList.of(PersistentFish.class));
    }


    @Entity
    @Table(name = "fishStates")
    public static class PersistentFish extends PersistentState {

        @Column(name = "owner") private final String owner;
        @Column(name = "ruler") private final String ruler;
        @Column(name = "id") private final UUID id;
        @Column(name = "vessel") private final String vessel;
        @Column(name = "weight") private final double weight;
        @Column(name = "length") private final double length;
        @Column(name = "catchMethod") private String catchMethod;
        @Column(name = "specie") private String specie;
        @Column(name = "description") private String description;
        @Column(name = "latitude") private double latitude;
        @Column(name = "longitude") private double longitude;
        @Column(name = "dateFishCaught") private long dateFishCaught;
        @Column(name = "isValid") private boolean isValid;


        public PersistentFish(String owner,String ruler, UUID id, String vessel, double weight, double length, String catchMethod, String specie, String description, double latitude, double longitude, long dateFishCaught, boolean isValid) {
            this.owner = owner;
            this.ruler = ruler;
            this.id = id;
            this.vessel = vessel;
            this.weight = weight;
            this.length = length;
            this.catchMethod = catchMethod;
            this.specie = specie;
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
            this.dateFishCaught = dateFishCaught;
            this.isValid = isValid;
        }

        public PersistentFish() {
            this.owner = "";
            this.ruler = "";
            this.id = null;
            this.vessel = "";
            this.weight = 0;
            this.length = 0;
            this.catchMethod = "";
            this.specie = "";
            this.description = "";
            this.latitude = 0;
            this.longitude = 0;
            this.dateFishCaught = 0;
            this.isValid = true;
        }

        public String getOwner() {
            return owner;
        }

        public String getRuler() {
            return ruler;
        }

        public UUID getId() {
            return id;
        }

        public String getVessel() {
            return vessel;
        }

        public double getWeight() {
            return weight;
        }

        public double getLength() {
            return length;
        }

        public String getCatchMethod() {
            return catchMethod;
        }

        public String getSpecie() {
            return specie;
        }

        public String getDescription() {
            return description;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public long getDateFishCaught() {
            return dateFishCaught;
        }

        public boolean isValid() {
            return isValid;
        }
    }
}
