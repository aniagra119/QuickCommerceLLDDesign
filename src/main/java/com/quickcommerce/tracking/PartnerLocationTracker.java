package com.quickcommerce.tracking;

import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.Location;
import com.quickcommerce.model.LocationPing;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PartnerLocationTracker {
    private final Map<String, List<LocationPing>> locationHistory = new ConcurrentHashMap<>();
    private final Map<String, DeliveryPartner> registeredPartners = new ConcurrentHashMap<>();

    public void registerPartner(DeliveryPartner partner) {
        registeredPartners.put(partner.getPartnerId(), Objects.requireNonNull(partner, "partner cannot be null"));
        locationHistory.put(partner.getPartnerId(), new CopyOnWriteArrayList<>());
    }

    /**
     * Receives periodic high-frequency location pings from the delivery partner mobile app.
     */
    public void recordLocationPing(String partnerId, Location location, Instant timestamp) {
        DeliveryPartner partner = registeredPartners.get(partnerId);
        if (partner == null) {
            throw new IllegalArgumentException("Unknown partner ID: " + partnerId);
        }

        partner.updateLocation(location, timestamp);
        LocationPing ping = new LocationPing(partnerId, location, timestamp);
        locationHistory.computeIfAbsent(partnerId, k -> new CopyOnWriteArrayList<>()).add(ping);
    }

    public Location getCurrentLocation(String partnerId) {
        DeliveryPartner partner = registeredPartners.get(partnerId);
        return partner != null ? partner.getCurrentLocation() : null;
    }

    public List<LocationPing> getLocationHistory(String partnerId) {
        List<LocationPing> history = locationHistory.get(partnerId);
        return history != null ? Collections.unmodifiableList(new ArrayList<>(history)) : Collections.emptyList();
    }
}
