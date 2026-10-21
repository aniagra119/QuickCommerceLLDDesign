package com.quickcommerce.model;

import com.quickcommerce.enums.PartnerStatus;
import com.quickcommerce.enums.VehicleType;
import java.time.Instant;
import java.util.Objects;

public class DeliveryPartner {
    private final String partnerId;
    private final String name;
    private final String phoneNumber;
    private final VehicleType vehicleType;
    private volatile PartnerStatus status;
    private volatile Location currentLocation;
    private volatile Instant lastLocationUpdate;

    public DeliveryPartner(String partnerId, String name, String phoneNumber, VehicleType vehicleType, Location initialLocation) {
        this.partnerId = Objects.requireNonNull(partnerId, "partnerId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.phoneNumber = Objects.requireNonNull(phoneNumber, "phoneNumber cannot be null");
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType cannot be null");
        this.currentLocation = Objects.requireNonNull(initialLocation, "initialLocation cannot be null");
        this.status = PartnerStatus.OFFLINE;
        this.lastLocationUpdate = Instant.now();
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public PartnerStatus getStatus() {
        return status;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public Instant getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public boolean isEligibleForDispatch() {
        return this.status == PartnerStatus.ONLINE;
    }

    public synchronized void goOnline() {
        this.status = PartnerStatus.ONLINE;
    }

    public synchronized void goOffline() {
        this.status = PartnerStatus.OFFLINE;
    }

    public synchronized void markBusy() {
        this.status = PartnerStatus.BUSY;
    }

    public synchronized void markAvailable() {
        this.status = PartnerStatus.ONLINE;
    }

    public synchronized void updateLocation(Location newLocation, Instant timestamp) {
        this.currentLocation = Objects.requireNonNull(newLocation, "newLocation cannot be null");
        this.lastLocationUpdate = Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }
}
