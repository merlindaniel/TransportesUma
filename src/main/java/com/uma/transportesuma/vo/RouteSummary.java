package com.uma.transportesuma.vo;

public class RouteSummary {
    private int lengthInMeters;
    private int travelTimeInSeconds;
    private int trafficDelayInSeconds;
    private int trafficLengthInMeters;
    private String departureTime;
    private String arrivalTime;

    public int getLengthInMeters() {
        return lengthInMeters;
    }

    public void setLengthInMeters(int lengthInMeters) {
        this.lengthInMeters = lengthInMeters;
    }

    public int getTravelTimeInSeconds() {
        return travelTimeInSeconds;
    }

    public void setTravelTimeInSeconds(int travelTimeInSeconds) {
        this.travelTimeInSeconds = travelTimeInSeconds;
    }

    public int getTrafficDelayInSeconds() {
        return trafficDelayInSeconds;
    }

    public void setTrafficDelayInSeconds(int trafficDelayInSeconds) {
        this.trafficDelayInSeconds = trafficDelayInSeconds;
    }

    public int getTrafficLengthInMeters() {
        return trafficLengthInMeters;
    }

    public void setTrafficLengthInMeters(int trafficLengthInMeters) {
        this.trafficLengthInMeters = trafficLengthInMeters;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}
