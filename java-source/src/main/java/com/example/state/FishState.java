package com.example.state;

import com.example.schema.FishSchema;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FishState implements LinearState,QueryableState {

    private final Party owner;
    private final Party ruler;
    private final UniqueIdentifier id;
    private final String vessel;
    private final double weight;
    private final double length;
    private String catchMethod;
    private String specie;
    private String description;
    private double latitude;
    private double longitude;
    private long dateFishCaught;
    private boolean isValid;

    public FishState(Party owner,Party ruler, UniqueIdentifier id, String vessel, double weight, double length, String catchMethod, String specie, String description, double latitude, double longitude, long dateFishCaught, boolean isValid) {
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

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new FishSchema());
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof FishSchema) {
            return new FishSchema.PersistentFish(
                    this.owner.getName().toString(),
                    this.ruler.getName().toString(),
                    this.id.getId(),
                    this.vessel,
                    this.weight,
                    this.length,
                    this.catchMethod,
                    this.specie,
                    this.description,
                    this.latitude,
                    this.longitude,
                    this.dateFishCaught,
                    this.isValid);
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner, ruler);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return id;
    }

    public Party getOwner() {
        return owner;
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

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public Party getRuler() {
        return ruler;
    }
}
