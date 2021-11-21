package com.uma.transportesuma.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteSummary {
    private int lengthInMeters;
    private int travelTimeInSeconds;
    private int trafficDelayInSeconds;
    private int trafficLengthInMeters;
    private String departureTime;
    private String arrivalTime;

}
