package com.example.state;

import com.example.schema.RuleSchema;
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

public class RuleState implements LinearState,QueryableState {

    private final Party owner;
    private final UniqueIdentifier id;
    private final String specie;
    private final String catchMethod;
    private final double latitude;
    private final double longitude;
    private final double radius;
    private final long fromDate;
    private final long toDate;
    private final long dateCreation;

    public RuleState(Party owner, UniqueIdentifier id, String specie, String catchMethod, double latitude, double longitude, double radius, long fromDate, long toDate) {
        this.owner = owner;
        this.id = id;
        this.specie = specie;
        this.catchMethod = catchMethod;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.dateCreation = System.currentTimeMillis();
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return id;
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new RuleSchema());
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof RuleSchema) {
            return new RuleSchema.PersistentRule(
                    this.owner.getName().toString(),
                    this.id.getId(),
                    this.specie,
                    this.catchMethod,
                    this.latitude,
                    this.longitude,
                    this.radius,
                    this.fromDate,
                    this.toDate,
                    this.dateCreation);
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner);
    }

    public Party getOwner() {
        return owner;
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
