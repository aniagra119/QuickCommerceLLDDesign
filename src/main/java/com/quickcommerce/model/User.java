package com.quickcommerce.model;

import java.util.Objects;

public class User {
    private final String userId;
    private final String name;
    private final String phoneNumber;
    private final String email;
    private final Address address;

    public User(String userId, String name, String phoneNumber, String email, Address address) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.phoneNumber = Objects.requireNonNull(phoneNumber, "phoneNumber cannot be null");
        this.email = Objects.requireNonNull(email, "email cannot be null");
        this.address = Objects.requireNonNull(address, "address cannot be null");
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }
}
