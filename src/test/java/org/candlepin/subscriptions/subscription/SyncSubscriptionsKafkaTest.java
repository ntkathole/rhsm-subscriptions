package org.candlepin.subscriptions.subscription;

import org.candlepin.subscriptions.capacity.CapacityReconciliationController;
import org.candlepin.subscriptions.db.SubscriptionRepository;
import org.candlepin.subscriptions.subscription.api.model.Subscription;
import org.candlepin.subscriptions.subscription.api.model.SubscriptionProduct;
import org.candlepin.subscriptions.util.ApplicationClock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
@ActiveProfiles({"worker", "test", "kafka-test", "kafka-queue"})
public class SyncSubscriptionsKafkaTest {

    private static final OffsetDateTime NOW = OffsetDateTime.now();

    @Autowired
    SubscriptionSyncController subscriptionSyncController;

    @Autowired private ApplicationClock clock;


    @Autowired SubscriptionWorker subscriptionWorker;

    @MockBean
    SubscriptionRepository subscriptionRepository;

    @MockBean
    CapacityReconciliationController capacityReconciliationController;

    @MockBean SubscriptionService subscriptionService;

    @Test
    void shouldSyncSubscriptionsWithinLimitForOrgAndQueueTaskForNext() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        List<Subscription> subscriptions =
                List.of(
                        createDto(100, "456", 10),
                        createDto(100, "457", 10),
                        createDto(100, "458", 10),
                        createDto(100, "459", 10),
                        createDto(100, "500", 10));

        Mockito.when(subscriptionService.getSubscriptionsByOrgId("100", 0, 3))
                .thenReturn(List.of(subscriptions.get(0), subscriptions.get(1), subscriptions.get(2)));
        Mockito.when(subscriptionService.getSubscriptionsByOrgId("100", 2, 3))
                .thenReturn(List.of(subscriptions.get(2), subscriptions.get(3), subscriptions.get(4)));
        Mockito.when(subscriptionService.getSubscriptionsByOrgId("100", 4, 3))
                .thenReturn(List.of(subscriptions.get(4)));
        subscriptions.forEach(
                subscription -> {
                    Mockito.when(
                            subscriptionRepository.findActiveSubscription(subscription.getId().toString()))
                            .thenReturn(Optional.of(convertDto(subscription)));
                });

        subscriptionSyncController.syncSubscriptions("100", 0, 2);
        latch.await(60L, TimeUnit.SECONDS);
    }

    private org.candlepin.subscriptions.subscription.api.model.Subscription createDto(
            Integer orgId, String subId, int quantity) {
        final var dto = new org.candlepin.subscriptions.subscription.api.model.Subscription();
        dto.setQuantity(quantity);
        dto.setId(Integer.valueOf(subId));
        dto.setSubscriptionNumber("123");
        dto.setEffectiveStartDate(NOW.toEpochSecond());
        dto.setEffectiveEndDate(NOW.plusDays(30).toEpochSecond());
        dto.setWebCustomerId(orgId);

        var product = new SubscriptionProduct().parentSubscriptionProductId(null).sku("testsku");
        List<SubscriptionProduct> products = Collections.singletonList(product);
        dto.setSubscriptionProducts(products);

        return dto;
    }

    private org.candlepin.subscriptions.db.model.Subscription convertDto(
            org.candlepin.subscriptions.subscription.api.model.Subscription subscription) {

        return org.candlepin.subscriptions.db.model.Subscription.builder()
                .subscriptionId(String.valueOf(subscription.getId()))
                .sku(SubscriptionDtoUtil.extractSku(subscription))
                .ownerId(subscription.getWebCustomerId().toString())
                .accountNumber(String.valueOf(subscription.getOracleAccountNumber()))
                .quantity(subscription.getQuantity())
                .startDate(clock.dateFromMilliseconds(subscription.getEffectiveStartDate()))
                .endDate(clock.dateFromMilliseconds(subscription.getEffectiveEndDate()))
                .marketplaceSubscriptionId(SubscriptionDtoUtil.extractMarketplaceId(subscription))
                .build();
    }

}


