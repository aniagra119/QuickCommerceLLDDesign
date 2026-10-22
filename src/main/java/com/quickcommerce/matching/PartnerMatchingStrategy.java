package com.quickcommerce.matching;

import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.DeliveryTask;
import java.util.List;

public interface PartnerMatchingStrategy {
    DeliveryPartner matchPartner(DeliveryTask task, List<DeliveryPartner> availablePartners);
}
