package com.uma.transportesuma.dto;

import com.uma.transportesuma.vo.LatLng;
import com.uma.transportesuma.vo.RouteSummary;

import java.util.List;

public class Route {
    private RouteSummary routeSummary;
    private List<LatLng> points;

    public Route(RouteSummary routeSummary, List<LatLng> points) {
        this.routeSummary = routeSummary;
        this.points = points;
    }

    public RouteSummary getRouteSummary() {
        return routeSummary;
    }

    public void setRouteSummary(RouteSummary routeSummary) {
        this.routeSummary = routeSummary;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }
}
