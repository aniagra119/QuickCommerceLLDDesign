package com.quickcommerce.matching;

import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.DeliveryTask;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ProximityToStoreMatchingStrategy implements PartnerMatchingStrategy {

    @Override
    public DeliveryPartner matchPartner(DeliveryTask task, List<DeliveryPartner> availablePartners) {
        Objects.requireNonNull(task, "task cannot be null");
        if (availablePartners == null || availablePartners.isEmpty()) {
            return null;
        }

        return availablePartners.stream()
                .filter(DeliveryPartner::isEligibleForDispatch)
                .min(Comparator.comparingDouble(partner ->
                        partner.getCurrentLocation().distanceTo(task.getPickupLocation())
                ))
                .orElse(null);
    }
}
