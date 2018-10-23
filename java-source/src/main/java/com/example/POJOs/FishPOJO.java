package com.example.POJOs;

public class FishPOJO {

    private String ruler;
    private String vessel;
    private double weight;
    private double length;
    private String catchMethod;
    private String specie;
    private String description;
    private double latitude;
    private double longitude;
    private long dateFishCaught;
    private boolean isValid;

    public FishPOJO() {
        this.ruler = "";
        this.vessel = "";
        this.weight = 0;
        this.length = 0;
        this.catchMethod = "";
        this.specie = "";
        this.description = "";
        this.latitude = 0;
        this.longitude = 0;
        this.dateFishCaught = 0;
        this.isValid = false;
    }

    public String getRuler() {
        return ruler;
    }

    public void setRuler(String ruler) {
        this.ruler = ruler;
    }

    public long getDateFishCaught() {
        return dateFishCaught;
    }

    public void setDateFishCaught(long dateFishCaught) {
        this.dateFishCaught = dateFishCaught;
    }

    public String getCatchMethod() {
        return catchMethod;
    }

    public void setCatchMethod(String catchMethod) {
            this.catchMethod = catchMethod;
    }

    public String getSpecie() {
        return specie;
    }

    public void setSpecie(String specie) {
        this.specie = specie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getVessel() {
        return vessel;
    }

    public void setVessel(String vessel) {
        this.vessel = vessel;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
