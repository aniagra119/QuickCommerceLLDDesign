package com.quickcommerce.model;

import java.util.Objects;

public class Address {
    private final String street;
    private final String area;
    private final String city;
    private final String pincode;
    private final Location location;

    public Address(String street, String area, String city, String pincode, Location location) {
        this.street = Objects.requireNonNull(street, "street cannot be null");
        this.area = Objects.requireNonNull(area, "area cannot be null");
        this.city = Objects.requireNonNull(city, "city cannot be null");
        this.pincode = Objects.requireNonNull(pincode, "pincode cannot be null");
        this.location = Objects.requireNonNull(location, "location cannot be null");
    }

    public String getStreet() {
        return street;
    }

    public String getArea() {
        return area;
    }

    public String getCity() {
        return city;
    }

    public String getPincode() {
        return pincode;
    }

    public Location getLocation() {
        return location;
    }

    public String getFormattedAddress() {
        return street + ", " + area + ", " + city + " - " + pincode;
    }
}
