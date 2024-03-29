package com.uma.transportesuma.dto;

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
    private String address;
}
