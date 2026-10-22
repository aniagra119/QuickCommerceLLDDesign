package com.quickcommerce.model;

import com.quickcommerce.enums.TaskStatus;
import java.time.Instant;
import java.util.Objects;

public class DeliveryTask {
    private final String taskId;
    private final String orderId;
    private final Location pickupLocation;
    private final Location dropoffLocation;
    private volatile DeliveryPartner assignedPartner;
    private volatile TaskStatus status;
    private volatile Instant assignedAt;
    private volatile Instant pickedUpAt;
    private volatile Instant deliveredAt;

    public DeliveryTask(String taskId, String orderId, Location pickupLocation, Location dropoffLocation) {
        this.taskId = Objects.requireNonNull(taskId, "taskId cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
        this.pickupLocation = Objects.requireNonNull(pickupLocation, "pickupLocation cannot be null");
        this.dropoffLocation = Objects.requireNonNull(dropoffLocation, "dropoffLocation cannot be null");
        this.status = TaskStatus.PENDING;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public Location getDropoffLocation() {
        return dropoffLocation;
    }

    public DeliveryPartner getAssignedPartner() {
        return assignedPartner;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getPickedUpAt() {
        return pickedUpAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public synchronized void assignToPartner(DeliveryPartner partner) {
        this.assignedPartner = Objects.requireNonNull(partner, "partner cannot be null");
        this.status = TaskStatus.ASSIGNED;
        this.assignedAt = Instant.now();
        partner.markBusy();
    }

    public synchronized void markPickedUp() {
        if (this.status != TaskStatus.ASSIGNED) {
            throw new IllegalStateException("Task must be in ASSIGNED status to be picked up");
        }
        this.status = TaskStatus.PICKED_UP;
        this.pickedUpAt = Instant.now();
    }

    public synchronized void markDelivered() {
        if (this.status != TaskStatus.PICKED_UP) {
            throw new IllegalStateException("Task must be in PICKED_UP status to be delivered");
        }
        this.status = TaskStatus.DELIVERED;
        this.deliveredAt = Instant.now();
        if (assignedPartner != null) {
            assignedPartner.markAvailable();
        }
    }

    public synchronized void markFailed() {
        this.status = TaskStatus.FAILED;
        if (assignedPartner != null) {
            assignedPartner.markAvailable();
        }
    }
}
