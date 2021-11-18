package com.uma.transportesuma.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Place {
    private String title;
    private Double lat;
    private Double lng;
    private Integer distance;
    private String address;
}