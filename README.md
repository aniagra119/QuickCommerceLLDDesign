# Quick Commerce Delivery Platform — Low-Level Design (LLD)

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=apachemaven&logoColor=white)](pom.xml)
[![Tests](https://img.shields.io/badge/Tests-7%20Passed-success?style=for-the-badge&logo=junit5&logoColor=white)](src/test/java/com/quickcommerce)
[![Design Patterns](https://img.shields.io/badge/Design_Patterns-Strategy_%7C_Observer_%7C_Facade-blueviolet?style=for-the-badge)](QuickCommerceSchema.md)

A production-grade, object-oriented Low-Level Design (LLD) for a **Quick Commerce Delivery Platform** (Zepto / Blinkit / Instamart style 10-minute grocery delivery) in Java 21 LTS adhering to clean architecture, SOLID principles, and Gang of Four (GoF) design patterns.

---

## 1. System Requirements & Domain Feature Matrix

| # | Requirement | Implementation Details | Design Pattern / Component |
| :---: | :--- | :--- | :--- |
| **1** | **Multi-Store Dark Store Network** | Network of localized dark stores with inventory catalogs and geolocations. | `Store`, `Location`, `Address` |
| **2** | **Atomic Inventory Reservation** | All-or-nothing stock reservation prior to order confirmation; guarantees 0 partial order failures. | `StoreInventory`, `StoreInventoryItem` |
| **3** | **Online / Offline Partner Filtering** | Strict status filtering: only `ONLINE` available delivery partners considered for task dispatch. | `PartnerStatus`, `DeliveryPartner` |
| **4** | **Proximity Dispatch Strategy** | Distance-based partner matching assigning the closest online partner to the pickup store. | `PartnerMatchingStrategy`, `ProximityToStoreMatchingStrategy` (Strategy Pattern) |
| **5** | **Real-Time GPS Location Tracking** | Ingestion of periodic location pings every few seconds from partner mobile apps with trajectory history. | `PartnerLocationTracker`, `LocationPing` |
| **6** | **Observer Event Notification Bus** | Decoupled event bus broadcasting live order status to customer SMS alerts and store packing screens. | `OrderEventPublisher`, `OrderEventListener` (Observer Pattern) |
| **7** | **Platform Facade** | Unified entry point coordinating Dark Stores, Delivery Partners, Orders, and GPS Tracking. | `QuickCommercePlatform` (Facade Pattern) |

---

## 2. Architecture Diagram

```mermaid
graph TD
    Client([User / Mobile App]) -->|Places Order| QCP[QuickCommercePlatform Facade]
    
    subgraph Dark Store & Inventory
        QCP --> Store[Store]
        Store --> SI[StoreInventory]
        SI --> SII[StoreInventoryItem - Atomic Stock]
    end
    
    subgraph Order & Dispatch
        QCP --> OS[OrderService]
        OS --> DS[DispatchService]
        DS --> PMS[PartnerMatchingStrategy - Strategy Pattern]
        PMS --> PSMS[ProximityToStoreMatchingStrategy]
        PMS --> FAMS[FastestArrivalMatchingStrategy]
        DS --> DP[DeliveryPartner - ONLINE only]
    end
    
    subgraph Real-Time GPS Tracking
        PartnerApp([Partner Mobile App]) -->|Periodic Pings| QCP
        QCP --> PLT[PartnerLocationTracker - Lat/Lon History]
    end
    
    subgraph Observer Event Layer
        OS --> OEP[OrderEventPublisher]
        OEP --> CSNL[CustomerSmsNotificationListener]
        OEP --> SPKL[StorePackingKdsListener]
    end
```

---

## 3. Test Verification
```bash
mvn clean test
```
- **7 unit & integration tests passing with 100% success**.
- Atomic inventory reservation tests verifying all-or-nothing rollback when stock is insufficient.
- Strategy pattern tests verifying closest online partner selection and vehicle-speed weighted fastest arrival.

