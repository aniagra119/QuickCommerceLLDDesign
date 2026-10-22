package com.quickcommerce;

import com.quickcommerce.enums.VehicleType;
import com.quickcommerce.matching.FastestArrivalMatchingStrategy;
import com.quickcommerce.matching.ProximityToStoreMatchingStrategy;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.DeliveryTask;
import com.quickcommerce.model.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Partner Matching Strategy Tests")
class PartnerMatchingStrategyTest {

    @Test
    @DisplayName("Should select partner closest to the store, completely ignoring offline partners")
    void shouldSelectClosestOnlinePartner() {
        ProximityToStoreMatchingStrategy strategy = new ProximityToStoreMatchingStrategy();

        Location storeLoc = new Location(12.9350, 77.6240); // Koramangala Store
        Location customerLoc = new Location(12.9450, 77.6340);
        DeliveryTask task = new DeliveryTask("T-1", "O-1", storeLoc, customerLoc);

        // Partner 1: 0.1 km away from store, but OFFLINE
        DeliveryPartner p1 = new DeliveryPartner("P-1", "Offline Partner", "+9190001", VehicleType.MOTORCYCLE, new Location(12.9351, 77.6241));
        // p1 remains OFFLINE

        // Partner 2: 0.5 km away from store, ONLINE
        DeliveryPartner p2 = new DeliveryPartner("P-2", "Online Near Partner", "+9190002", VehicleType.SCOOTER, new Location(12.9380, 77.6260));
        p2.goOnline();

        // Partner 3: 2.0 km away from store, ONLINE
        DeliveryPartner p3 = new DeliveryPartner("P-3", "Online Far Partner", "+9190003", VehicleType.MOTORCYCLE, new Location(12.9500, 77.6400));
        p3.goOnline();

        DeliveryPartner matched = strategy.matchPartner(task, List.of(p1, p2, p3));
        assertNotNull(matched);
        assertEquals("P-2", matched.getPartnerId());
    }

    @Test
    @DisplayName("Should use fastest arrival strategy taking vehicle speed into account")
    void shouldConsiderSpeedInFastestArrival() {
        FastestArrivalMatchingStrategy strategy = new FastestArrivalMatchingStrategy();

        Location storeLoc = new Location(12.9350, 77.6240);
        DeliveryTask task = new DeliveryTask("T-1", "O-1", storeLoc, new Location(12.9450, 77.6340));

        // Partner 1 on Bicycle: 0.8 km away @ 15 km/h -> ~3.2 minutes
        DeliveryPartner p1 = new DeliveryPartner("P-Bicycle", "Bicycle Partner", "+9190001", VehicleType.BICYCLE, new Location(12.9400, 77.6290));
        p1.goOnline();

        // Partner 2 on Motorcycle: 1.0 km away @ 35 km/h -> ~1.7 minutes (Arrives faster!)
        DeliveryPartner p2 = new DeliveryPartner("P-Moto", "Moto Partner", "+9190002", VehicleType.MOTORCYCLE, new Location(12.9420, 77.6310));
        p2.goOnline();

        DeliveryPartner matched = strategy.matchPartner(task, List.of(p1, p2));
        assertNotNull(matched);
        assertEquals("P-Moto", matched.getPartnerId());
    }
}
