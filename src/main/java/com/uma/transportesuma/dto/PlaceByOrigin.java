package com.uma.transportesuma.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class PlaceByOrigin {
    private String title;
    private Double lat;
    private Double lng;
    private Integer distance;
    private String address;
}
