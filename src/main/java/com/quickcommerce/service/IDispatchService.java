package com.quickcommerce.service;

import com.quickcommerce.matching.PartnerMatchingStrategy;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.DeliveryTask;
import java.util.List;

public interface IDispatchService {
    void registerPartner(DeliveryPartner partner);
    void setMatchingStrategy(PartnerMatchingStrategy matchingStrategy);
    PartnerMatchingStrategy getMatchingStrategy();
    List<DeliveryPartner> getAvailablePartners();
    DeliveryPartner dispatchTask(DeliveryTask task);
}
