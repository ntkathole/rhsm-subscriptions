package org.candlepin.subscriptions.tally;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountResetServiceTest {

  @InjectMocks
  AccountResetService accountResetService;

  @Test
  void sanityCheck() {
    assertTrue(true);
    assertFalse(false);
  }

  void setUp() {

    var firstAccount = "account123";
    var secondAccount = "bananas";

    //initialize some things to be deleted - hosts, events, tallies (and buckets?), subscription capacity

//    private final EventRecordRepository eventRecordRepo;
//    private final HostRepository hostRepo;
//    private final TallySnapshotRepository tallySnapshotRepository;
//    private final AccountServiceInventoryRepository accountServiceInventoryRepository;
//    private final SubscriptionCapacityRepository subscriptionCapacityRepository;
//    private final SubscriptionRepository subscriptionRepository;
  }

  @Test
  void dummyMethodForTesting() {
    var color = "purple";

    var expected = color;
    var actual = accountResetService.dummyMethodForTesting(color);

    assertEquals(expected, actual);

  }

  @Test
  void testDeleteDataForAccount() {

    var accountNumber = "account123";
    accountResetService.deleteDataForAccount(accountNumber);

    //check that all tables are clear of account123-related data (one test per)
    //check that all tables still contain banana-related data
    //try to get a unit test that reproduces the FK constraint bug

  }
  // do we do unit tests for the JmxBeans? would like to //check that if env var is set to FALSE, appropriate exception occurs

}