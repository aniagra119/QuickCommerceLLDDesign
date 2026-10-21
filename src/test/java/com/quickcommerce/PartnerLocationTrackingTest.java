package com.quickcommerce;

import com.quickcommerce.enums.VehicleType;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.Location;
import com.quickcommerce.model.LocationPing;
import com.quickcommerce.tracking.PartnerLocationTracker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Partner Location Tracking Tests")
class PartnerLocationTrackingTest {

    @Test
    @DisplayName("Should record frequent periodic location pings and maintain audit history")
    void shouldTrackFrequentLocationPings() {
        PartnerLocationTracker tracker = new PartnerLocationTracker();
        DeliveryPartner partner = new DeliveryPartner("DP-1", "Ramesh Kumar", "+919876500001", VehicleType.MOTORCYCLE, new Location(12.9352, 77.6245));
        partner.goOnline();
        tracker.registerPartner(partner);

        Instant t1 = Instant.parse("2026-09-11T10:00:00Z");
        Instant t2 = Instant.parse("2026-09-11T10:00:05Z");
        Instant t3 = Instant.parse("2026-09-11T10:00:10Z");

        tracker.recordLocationPing("DP-1", new Location(12.9355, 77.6248), t1);
        tracker.recordLocationPing("DP-1", new Location(12.9360, 77.6255), t2);
        tracker.recordLocationPing("DP-1", new Location(12.9372, 77.6268), t3);

        assertEquals(new Location(12.9372, 77.6268), tracker.getCurrentLocation("DP-1"));
        assertEquals(new Location(12.9372, 77.6268), partner.getCurrentLocation());

        List<LocationPing> history = tracker.getLocationHistory("DP-1");
        assertEquals(3, history.size());
        assertEquals(t1, history.get(0).getTimestamp());
        assertEquals(t3, history.get(2).getTimestamp());
    }
}
