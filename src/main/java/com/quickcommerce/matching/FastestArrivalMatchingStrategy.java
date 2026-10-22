package com.quickcommerce.matching;

import com.quickcommerce.enums.VehicleType;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.DeliveryTask;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FastestArrivalMatchingStrategy implements PartnerMatchingStrategy {

    @Override
    public DeliveryPartner matchPartner(DeliveryTask task, List<DeliveryPartner> availablePartners) {
        Objects.requireNonNull(task, "task cannot be null");
        if (availablePartners == null || availablePartners.isEmpty()) {
            return null;
        }

        return availablePartners.stream()
                .filter(DeliveryPartner::isEligibleForDispatch)
                .min(Comparator.comparingDouble(partner -> calculateEtaMinutes(partner, task)))
                .orElse(null);
    }

    private double calculateEtaMinutes(DeliveryPartner partner, DeliveryTask task) {
        double distanceKm = partner.getCurrentLocation().distanceTo(task.getPickupLocation());
        double speedKmPerHour = getAverageSpeedKmPerHour(partner.getVehicleType());
        return (distanceKm / speedKmPerHour) * 60.0;
    }

    private double getAverageSpeedKmPerHour(VehicleType vehicleType) {
        return switch (vehicleType) {
            case BICYCLE -> 15.0;
            case SCOOTER, ELECTRIC_VEHICLE -> 25.0;
            case MOTORCYCLE -> 35.0;
        };
    }
}
