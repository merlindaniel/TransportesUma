package com.uma.transportesuma.dto;

import com.google.gson.annotations.SerializedName;
import com.uma.transportesuma.vo.LatLng;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class FuelStation {
    @SerializedName("C.P.")
    private String zipCode;

    @SerializedName("Dirección")
    private String address;

    @SerializedName("Horario")
    private String openingHours;

    @SerializedName("Municipio")
    private String town;

    @SerializedName("Precio Biodiesel")
    private String priceBioDiesel;

    @SerializedName("Precio Bioetanol")
    private String priceBioTanol;

    //---------------------------------------GESOIL PRICES
    @SerializedName("Precio Gasoleo A")
    private String priceGasoilA;

    @SerializedName("Precio Gasoleo B")
    private String priceGasoilB;

    @SerializedName("Precio Gasoleo Premium")
    private String priceGasoilPremium;

    //---------------------------------------GOSILINE PRICES
    @SerializedName("Precio Gasolina 95 E10")
    private String priceFuel95E10;

    @SerializedName("Precio Gasolina 95 E5")
    private String priceFuel95E5;

    @SerializedName("Precio Gasolina 95 E5 Premium")
    private String priceFuel95E5Premium;


    @SerializedName("Precio Gasolina 98 E10")
    private String priceFuel98E10;

    @SerializedName("Precio Gasolina 98 E5")
    private String priceFuel98E5;


    private LatLng latLng;
}
