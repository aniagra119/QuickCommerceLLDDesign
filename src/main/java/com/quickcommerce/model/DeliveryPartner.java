package com.quickcommerce.model;

import com.quickcommerce.enums.PartnerStatus;
import com.quickcommerce.enums.VehicleType;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DeliveryPartner {
    private final String partnerId;
    private final String name;
    private final String phoneNumber;
    private final VehicleType vehicleType;
    private volatile PartnerStatus status;
    private volatile Location currentLocation;
    private volatile Instant lastLocationUpdate;
    private volatile int activeDeliveryCount;
    private volatile int totalDeliveriesCompleted;
    private volatile double averageRating;
    private final int maxConcurrentOrders;
    private final Set<String> activeTaskIds = Collections.synchronizedSet(new HashSet<>());

    public DeliveryPartner(String partnerId, String name, String phoneNumber, VehicleType vehicleType, Location initialLocation) {
        this.partnerId = Objects.requireNonNull(partnerId, "partnerId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.phoneNumber = Objects.requireNonNull(phoneNumber, "phoneNumber cannot be null");
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType cannot be null");
        this.currentLocation = Objects.requireNonNull(initialLocation, "initialLocation cannot be null");
        this.status = PartnerStatus.OFFLINE;
        this.lastLocationUpdate = Instant.now();
        this.activeDeliveryCount = 0;
        this.totalDeliveriesCompleted = 0;
        this.averageRating = 5.0;
        this.maxConcurrentOrders = (vehicleType == VehicleType.BICYCLE) ? 2 : 3;
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

    public int getActiveDeliveryCount() {
        return activeDeliveryCount;
    }

    public int getTotalDeliveriesCompleted() {
        return totalDeliveriesCompleted;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getMaxConcurrentOrders() {
        return maxConcurrentOrders;
    }

    public Set<String> getActiveTaskIds() {
        synchronized (activeTaskIds) {
            return new HashSet<>(activeTaskIds);
        }
    }

    public boolean isEligibleForDispatch() {
        return this.status == PartnerStatus.ONLINE && this.activeDeliveryCount < this.maxConcurrentOrders;
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
        if (this.status != PartnerStatus.OFFLINE) {
            this.status = PartnerStatus.ONLINE;
        }
    }

    public synchronized void updateLocation(Location newLocation) {
        updateLocation(newLocation, Instant.now());
    }

    public synchronized void updateLocation(Location newLocation, Instant timestamp) {
        this.currentLocation = Objects.requireNonNull(newLocation, "newLocation cannot be null");
        this.lastLocationUpdate = Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }

    public synchronized void assignTask(String taskId) {
        this.activeTaskIds.add(taskId);
        this.activeDeliveryCount = this.activeTaskIds.size();
        if (this.activeDeliveryCount >= this.maxConcurrentOrders) {
            markBusy();
        }
    }

    public synchronized void completeTask(String taskId, double rating) {
        this.activeTaskIds.remove(taskId);
        this.activeDeliveryCount = this.activeTaskIds.size();
        this.totalDeliveriesCompleted++;
        this.averageRating = ((this.averageRating * (this.totalDeliveriesCompleted - 1)) + rating) / this.totalDeliveriesCompleted;
        if (this.activeDeliveryCount < this.maxConcurrentOrders && this.status == PartnerStatus.BUSY) {
            markAvailable();
        }
    }
}
