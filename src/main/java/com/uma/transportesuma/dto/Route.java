package com.uma.transportesuma.dto;

import com.uma.transportesuma.vo.LatLng;
import com.uma.transportesuma.vo.RouteSummary;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
@ToString
public class Route {
    private RouteSummary routeSummary;
    private List<LatLng> points;
}
