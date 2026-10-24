package com.quickcommerce.service;

import com.quickcommerce.matching.PartnerMatchingStrategy;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.DeliveryTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DispatchService {
    private PartnerMatchingStrategy matchingStrategy;
    private final Map<String, DeliveryPartner> partners = new ConcurrentHashMap<>();

    public DispatchService(PartnerMatchingStrategy matchingStrategy) {
        this.matchingStrategy = Objects.requireNonNull(matchingStrategy, "matchingStrategy cannot be null");
    }

    public void registerPartner(DeliveryPartner partner) {
        partners.put(partner.getPartnerId(), Objects.requireNonNull(partner, "partner cannot be null"));
    }

    public synchronized void setMatchingStrategy(PartnerMatchingStrategy matchingStrategy) {
        this.matchingStrategy = Objects.requireNonNull(matchingStrategy, "matchingStrategy cannot be null");
    }

    public List<DeliveryPartner> getAvailablePartners() {
        return partners.values().stream()
                .filter(DeliveryPartner::isEligibleForDispatch)
                .toList();
    }

    public synchronized DeliveryPartner dispatchTask(DeliveryTask task) {
        Objects.requireNonNull(task, "task cannot be null");
        List<DeliveryPartner> eligiblePartners = new ArrayList<>(getAvailablePartners());

        DeliveryPartner matchedPartner = matchingStrategy.matchPartner(task, eligiblePartners);
        if (matchedPartner != null) {
            task.assignToPartner(matchedPartner);
        }
        return matchedPartner;
    }
}
