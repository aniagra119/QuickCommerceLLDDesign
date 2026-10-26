// ============================================================================
// RELATIONSHIPS & STRUCTURAL CONTRACTS
// ============================================================================
// - QuickCommercePlatform has-a List<Store> (Composition)
// - QuickCommercePlatform has-a List<User> (Composition)
// - QuickCommercePlatform has-a DispatchService (Composition)
// - QuickCommercePlatform has-a OrderService (Composition)
// - QuickCommercePlatform has-a PartnerLocationTracker (Composition)
// - Store has-a Location (Composition)
// - Store has-a Address (Composition)
// - Store has-a StoreInventory (Composition, 1..1)
// - StoreInventory has-a Map<String, StoreInventoryItem> (Composition)
// - StoreInventoryItem has-a Item (Aggregation)
// - Item has-a ItemCategory (Composition, Enum)
// - User has-a Address (Composition)
// - Address has-a Location (Composition)
// - DeliveryPartner has-a VehicleType (Composition, Enum: BICYCLE, MOTORCYCLE, SCOOTER, EV)
// - DeliveryPartner has-a PartnerStatus (Composition, Enum: ONLINE, OFFLINE, BUSY)
// - DeliveryPartner has-a Location (Composition, current live coordinates)
// - LocationPing has-a Location (Composition, breadcrumb coordinate timestamp)
// - PartnerLocationTracker has-a Map<String, List<LocationPing>> (Composition, high-frequency audit trail)
// - Order has-a User (Aggregation, customer placing the order)
// - Order has-a Store (Aggregation, fulfillment dark store)
// - Order has-a List<OrderItem> (Composition, 1..*)
// - Order has-a Location (Composition, customer doorstep dropoff location)
// - Order has-a OrderStatus (Composition, Enum: PLACED, CONFIRMED, PACKING, ASSIGNED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED)
// - Order has-a DeliveryTask (Composition, delivery execution unit)
// - OrderItem has-a Item (Aggregation)
// - DeliveryTask has-a DeliveryPartner (Aggregation, assigned driver, null if pending)
// - DeliveryTask has-a TaskStatus (Composition, Enum: PENDING, ASSIGNED, PICKED_UP, DELIVERED, FAILED)
// - DeliveryTask has-a Location (Composition: pickupLocation at store, dropoffLocation at customer)
// - DispatchService has-a PartnerMatchingStrategy (Aggregation via Strategy Pattern)
// - PartnerMatchingStrategy is interface implemented by ProximityToStoreMatchingStrategy and FastestArrivalMatchingStrategy (Strategy Pattern)
// - OrderService has-a DispatchService (Aggregation)
// - OrderService has-a OrderEventPublisher (Composition via Observer Pattern)
// - OrderEventPublisher has-a List<OrderEventListener> (Aggregation via Observer Pattern)
// - OrderEventListener is interface implemented by CustomerSmsNotificationListener and StorePackingKdsListener (Observer Pattern)

// ============================================================================
// ENUMS
// ============================================================================

enum PartnerStatus {
    ONLINE,
    OFFLINE,
    BUSY
}

enum OrderStatus {
    PLACED,
    CONFIRMED,
    PACKING,
    ASSIGNED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

enum TaskStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    DELIVERED,
    FAILED
}

enum VehicleType {
    BICYCLE,
    MOTORCYCLE,
    SCOOTER,
    ELECTRIC_VEHICLE
}

enum ItemCategory {
    FRUITS_AND_VEGETABLES,
    DAIRY_AND_BREAD,
    SNACKS_AND_BEVERAGES,
    INSTANT_FOOD,
    PERSONAL_CARE,
    HOUSEHOLD
}

// ============================================================================
// GEOLOCATION & VALUE OBJECTS
// ============================================================================

class Location {
    - double latitude
    - double longitude

    + Location(double latitude, double longitude)
    + double getLatitude()
    + double getLongitude()
    + double distanceTo(Location other)
}

class Address {
    - String street
    - String area
    - String city
    - String pincode
    - Location location

    + Address(String street, String area, String city, String pincode, Location location)
    + String getStreet()
    + String getArea()
    + String getCity()
    + String getPincode()
    + Location getLocation()
    + String getFormattedAddress()
}

class LocationPing {
    - String partnerId
    - Location location
    - Instant timestamp

    + LocationPing(String partnerId, Location location, Instant timestamp)
    + String getPartnerId()
    + Location getLocation()
    + Instant getTimestamp()
}

// ============================================================================
// ACTORS & USERS
// ============================================================================

class User {
    - String userId
    - String name
    - String phoneNumber
    - String email
    - Address address

    + User(String userId, String name, String phoneNumber, String email, Address address)
    + String getUserId()
    + String getName()
    + String getPhoneNumber()
    + String getEmail()
    + Address getAddress()
}

class DeliveryPartner {
    - String partnerId
    - String name
    - String phoneNumber
    - VehicleType vehicleType
    - PartnerStatus status
    - Location currentLocation
    - Instant lastLocationUpdate

    + DeliveryPartner(String partnerId, String name, String phoneNumber, VehicleType vehicleType, Location initialLocation)
    + String getPartnerId()
    + String getName()
    + String getPhoneNumber()
    + VehicleType getVehicleType()
    + PartnerStatus getStatus()
    + Location getCurrentLocation()
    + Instant getLastLocationUpdate()
    + boolean isEligibleForDispatch()
    + void goOnline()
    + void goOffline()
    + void markBusy()
    + void markAvailable()
    + void updateLocation(Location newLocation, Instant timestamp)
}

// ============================================================================
// STORE, ITEMS & ATOMIC INVENTORY
// ============================================================================

class Item {
    - String itemId
    - String name
    - String description
    - double price
    - ItemCategory category
    - double weightKg

    + Item(String itemId, String name, String description, double price, ItemCategory category, double weightKg)
    + String getItemId()
    + String getName()
    + String getDescription()
    + double getPrice()
    + ItemCategory getCategory()
    + double getWeightKg()
}

class StoreInventoryItem {
    - Item item
    - int availableQuantity
    - int reservedQuantity

    + StoreInventoryItem(Item item, int initialQuantity)
    + Item getItem()
    + int getAvailableQuantity()
    + int getReservedQuantity()
    + int getTotalQuantity()
    + void addStock(int quantity)
    + boolean hasSufficientStock(int quantity)
    + boolean reserve(int quantity)
    + void commitReservation(int quantity)
    + void releaseReservation(int quantity)
}

class StoreInventory {
    - String storeId
    - Map<String, StoreInventoryItem> inventoryMap

    + StoreInventory(String storeId)
    + String getStoreId()
    + void addItem(Item item, int quantity)
    + StoreInventoryItem getInventoryItem(String itemId)
    + Map<String, StoreInventoryItem> getAllItems()
    + boolean hasAvailableStock(List<OrderItem> items)
    + boolean reserveStock(List<OrderItem> items)
    + void commitStock(List<OrderItem> items)
    + void releaseStock(List<OrderItem> items)
}

class Store {
    - String storeId
    - String name
    - Location location
    - Address address
    - StoreInventory inventory

    + Store(String storeId, String name, Location location, Address address)
    + String getStoreId()
    + String getName()
    + Location getLocation()
    + Address getAddress()
    + StoreInventory getInventory()
}

// ============================================================================
// ORDERS & DELIVERY TASKS
// ============================================================================

class OrderItem {
    - String orderItemId
    - Item item
    - int quantity
    - double unitPrice

    + OrderItem(String orderItemId, Item item, int quantity)
    + String getOrderItemId()
    + Item getItem()
    + int getQuantity()
    + double getUnitPrice()
    + double getSubtotal()
}

class DeliveryTask {
    - String taskId
    - String orderId
    - Location pickupLocation
    - Location dropoffLocation
    - DeliveryPartner assignedPartner
    - TaskStatus status
    - Instant assignedAt
    - Instant pickedUpAt
    - Instant deliveredAt

    + DeliveryTask(String taskId, String orderId, Location pickupLocation, Location dropoffLocation)
    + String getTaskId()
    + String getOrderId()
    + Location getPickupLocation()
    + Location getDropoffLocation()
    + DeliveryPartner getAssignedPartner()
    + TaskStatus getStatus()
    + Instant getAssignedAt()
    + Instant getPickedUpAt()
    + Instant getDeliveredAt()
    + void assignToPartner(DeliveryPartner partner)
    + void markPickedUp()
    + void markDelivered()
    + void markFailed()
}

class Order {
    - String orderId
    - User customer
    - Store store
    - List<OrderItem> items
    - double totalAmount
    - Location deliveryLocation
    - OrderStatus status
    - DeliveryTask deliveryTask
    - Instant createdAt

    + Order(String orderId, User customer, Store store, List<OrderItem> items, Location deliveryLocation)
    + String getOrderId()
    + User getCustomer()
    + Store getStore()
    + List<OrderItem> getItems()
    + double getTotalAmount()
    + Location getDeliveryLocation()
    + OrderStatus getStatus()
    + DeliveryTask getDeliveryTask()
    + Instant getCreatedAt()
    + void setStatus(OrderStatus status)
    + void attachDeliveryTask(DeliveryTask deliveryTask)
}

// ============================================================================
// PARTNER-TASK MATCHING (Strategy Pattern)
// ============================================================================

interface PartnerMatchingStrategy {
    + DeliveryPartner matchPartner(DeliveryTask task, List<DeliveryPartner> availablePartners)
}

class ProximityToStoreMatchingStrategy implements PartnerMatchingStrategy {
    + ProximityToStoreMatchingStrategy()
    + DeliveryPartner matchPartner(DeliveryTask task, List<DeliveryPartner> availablePartners)
}

class FastestArrivalMatchingStrategy implements PartnerMatchingStrategy {
    + FastestArrivalMatchingStrategy()
    + DeliveryPartner matchPartner(DeliveryTask task, List<DeliveryPartner> availablePartners)
}

// ============================================================================
// REAL-TIME PARTNER LOCATION TRACKING
// ============================================================================

class PartnerLocationTracker {
    - Map<String, List<LocationPing>> locationHistory
    - Map<String, DeliveryPartner> registeredPartners

    + PartnerLocationTracker()
    + void registerPartner(DeliveryPartner partner)
    + void recordLocationPing(String partnerId, Location location, Instant timestamp)
    + Location getCurrentLocation(String partnerId)
    + List<LocationPing> getLocationHistory(String partnerId)
}

// ============================================================================
// EVENT-DRIVEN OBSERVER BUS (Observer Pattern)
// ============================================================================

abstract class OrderEvent {
    # String eventId
    # Order order
    # Instant timestamp

    + OrderEvent(String eventId, Order order, Instant timestamp)
    + String getEventId()
    + Order getOrder()
    + Instant getTimestamp()
}

class OrderPlacedEvent extends OrderEvent {
    + OrderPlacedEvent(String eventId, Order order, Instant timestamp)
}

class OrderDispatchedEvent extends OrderEvent {
    + OrderDispatchedEvent(String eventId, Order order, Instant timestamp)
}

class OrderDeliveredEvent extends OrderEvent {
    + OrderDeliveredEvent(String eventId, Order order, Instant timestamp)
}

interface OrderEventListener {
    + void onOrderPlaced(OrderPlacedEvent event)
    + void onOrderDispatched(OrderDispatchedEvent event)
    + void onOrderDelivered(OrderDeliveredEvent event)
}

class CustomerSmsNotificationListener implements OrderEventListener {
    + CustomerSmsNotificationListener()
    + void onOrderPlaced(OrderPlacedEvent event)
    + void onOrderDispatched(OrderDispatchedEvent event)
    + void onOrderDelivered(OrderDeliveredEvent event)
}

class StorePackingKdsListener implements OrderEventListener {
    + StorePackingKdsListener()
    + void onOrderPlaced(OrderPlacedEvent event)
    + void onOrderDispatched(OrderDispatchedEvent event)
    + void onOrderDelivered(OrderDeliveredEvent event)
}

class OrderEventPublisher {
    - List<OrderEventListener> listeners

    + OrderEventPublisher()
    + void registerListener(OrderEventListener listener)
    + void removeListener(OrderEventListener listener)
    + void publishOrderPlaced(OrderPlacedEvent event)
    + void publishOrderDispatched(OrderDispatchedEvent event)
    + void publishOrderDelivered(OrderDeliveredEvent event)
}

// ============================================================================
// SERVICES & PLATFORM FACADE
// ============================================================================

class DispatchService {
    - PartnerMatchingStrategy matchingStrategy
    - Map<String, DeliveryPartner> partners

    + DispatchService(PartnerMatchingStrategy matchingStrategy)
    + void registerPartner(DeliveryPartner partner)
    + void setMatchingStrategy(PartnerMatchingStrategy matchingStrategy)
    + List<DeliveryPartner> getAvailablePartners()
    + DeliveryPartner dispatchTask(DeliveryTask task)
}

class OrderService {
    - DispatchService dispatchService
    - OrderEventPublisher eventPublisher
    - Map<String, Order> orders

    + OrderService(DispatchService dispatchService, OrderEventPublisher eventPublisher)
    + Order placeOrder(User customer, Store store, List<OrderItem> items, Location deliveryLocation)
    + void pickUpOrder(String orderId)
    + void deliverOrder(String orderId)
    + void cancelOrder(String orderId)
    + Order getOrder(String orderId)
}

class QuickCommercePlatform {
    - Map<String, Store> stores
    - Map<String, User> users
    - DispatchService dispatchService
    - OrderService orderService
    - PartnerLocationTracker locationTracker

    + QuickCommercePlatform(DispatchService dispatchService, OrderService orderService, PartnerLocationTracker locationTracker)
    + void registerStore(Store store)
    + void registerUser(User user)
    + void registerPartner(DeliveryPartner partner)
    + void updatePartnerLocation(String partnerId, Location location, Instant timestamp)
    + void setPartnerMatchingStrategy(PartnerMatchingStrategy strategy)
    + Order placeOrder(String userId, String storeId, List<OrderItem> items, Location deliveryLocation)
    + void pickUpOrder(String orderId)
    + void deliverOrder(String orderId)
    + void cancelOrder(String orderId)
    + List<Store> getStores()
    + Location getPartnerLocation(String partnerId)
}

// ============================================================================
// DESIGN PATTERN JUSTIFICATION & ARCHITECTURAL SUMMARY
// ============================================================================
// 1. Strategy Pattern (Partner-Task Matching):
//    - PartnerMatchingStrategy decouples the dispatch optimization algorithm from DispatchService and Order.
//    - ProximityToStoreMatchingStrategy calculates Haversine/Euclidean distance from the partner's current
//      coordinates to the store pickup location, strictly filtering for ONLINE and available partners.
//    - FastestArrivalMatchingStrategy incorporates vehicle travel speeds (BICYCLE vs SCOOTER vs MOTORCYCLE).
//    - Allows future algorithms (e.g. BatchOrderMatchingStrategy, DriverRatingWeightedStrategy) to plug in seamlessly
//      without modifying core order workflows (complying with OCP).
//
// 2. High-Frequency Real-Time Location Tracking:
//    - PartnerLocationTracker ingests periodic GPS pings from mobile apps (LocationPing: lat, lon, timestamp),
//      updating partner's live coordinates while appending to an immutable historical breadcrumb trajectory.
//
// 3. Atomic All-or-Nothing Inventory Reservation:
//    - StoreInventory and StoreInventoryItem enforce strict inventory invariants: before order confirmation,
//      all items are verified and atomically reserved. If any single item is short in quantity, all reservations
//      roll back, preventing partial order fulfillment traps.
//
// 4. Observer Pattern (Order Lifecycle Bus):
//    - OrderEventPublisher broadcasts OrderPlacedEvent, OrderDispatchedEvent, and OrderDeliveredEvent to
//      CustomerSmsNotificationListener (doorstep delivery alerts) and StorePackingKdsListener (dark store KDS).
//
// 5. Facade Pattern (QuickCommercePlatform):
//    - Simplifies external client interactions across complex subsystems (Store inventory, Driver dispatching,
//      Location tracking, and Order settlement).

