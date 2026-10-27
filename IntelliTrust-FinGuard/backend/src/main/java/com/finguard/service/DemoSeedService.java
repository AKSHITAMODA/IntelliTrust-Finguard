package com.finguard.service;

import com.finguard.entity.*;
import com.finguard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Builds the six-month (Mar-Aug, relative to "today") realistic demo dataset:
 * ~Rs.84,500 net balance, ~Rs.18,500 last-month spending, trust score ~100 and
 * zero alerts, by construction (fixed, hand-tuned amounts, no randomness).
 */
@Service
@RequiredArgsConstructor
public class DemoSeedService {

    public static final String DEMO_EMAIL = "demo@finguard.com";
    public static final String DEMO_PASSWORD = "Demo@1234";
    private static final double HOME_LAT = 12.9716;
    private static final double HOME_LON = 77.5946;
    private static final BigDecimal MONTHLY_SALARY = BigDecimal.valueOf(30000);

    private record Seed(int monthOffset, int day, int hour, String merchant, TransactionCategory category, double amount) {}

    // monthOffset 0 = 5 months ago ... 5 = last completed month (highest spend, shown as "Monthly Spending").
    private static final List<Seed> DEBIT_SEEDS = List.of(
            new Seed(0, 3, 13, "Swiggy", TransactionCategory.FOOD, 480),
            new Seed(0, 7, 20, "Zomato", TransactionCategory.FOOD, 620),
            new Seed(0, 10, 11, "Amazon", TransactionCategory.SHOPPING, 3200),
            new Seed(0, 14, 16, "Flipkart", TransactionCategory.SHOPPING, 2800),
            new Seed(0, 16, 9, "Uber", TransactionCategory.TRAVEL, 450),
            new Seed(0, 18, 18, "Indian Oil Petrol Pump", TransactionCategory.TRAVEL, 1600),
            new Seed(0, 22, 10, "Airtel Recharge", TransactionCategory.BILLS, 799),
            new Seed(0, 25, 9, "Electricity Board", TransactionCategory.BILLS, 2450),
            new Seed(0, 28, 19, "BESCOM Water", TransactionCategory.BILLS, 2401),

            new Seed(1, 2, 14, "Swiggy", TransactionCategory.FOOD, 520),
            new Seed(1, 5, 21, "Zomato", TransactionCategory.FOOD, 580),
            new Seed(1, 9, 12, "Amazon", TransactionCategory.SHOPPING, 3600),
            new Seed(1, 12, 17, "Myntra", TransactionCategory.SHOPPING, 2200),
            new Seed(1, 15, 8, "Uber", TransactionCategory.TRAVEL, 380),
            new Seed(1, 19, 19, "Ola", TransactionCategory.TRAVEL, 720),
            new Seed(1, 21, 10, "Airtel Recharge", TransactionCategory.BILLS, 799),
            new Seed(1, 24, 9, "Electricity Board", TransactionCategory.BILLS, 2600),
            new Seed(1, 27, 20, "Rent Utility", TransactionCategory.BILLS, 4201),

            new Seed(2, 3, 13, "Swiggy", TransactionCategory.FOOD, 460),
            new Seed(2, 6, 20, "Zomato", TransactionCategory.FOOD, 540),
            new Seed(2, 9, 11, "Amazon", TransactionCategory.SHOPPING, 2900),
            new Seed(2, 13, 16, "Flipkart", TransactionCategory.SHOPPING, 2500),
            new Seed(2, 16, 9, "Uber", TransactionCategory.TRAVEL, 410),
            new Seed(2, 19, 18, "Indian Oil Petrol Pump", TransactionCategory.TRAVEL, 1500),
            new Seed(2, 22, 10, "Airtel Recharge", TransactionCategory.BILLS, 799),
            new Seed(2, 25, 9, "Electricity Board", TransactionCategory.BILLS, 2300),
            new Seed(2, 28, 19, "Broadband Bill", TransactionCategory.BILLS, 2791),

            new Seed(3, 2, 14, "Swiggy", TransactionCategory.FOOD, 500),
            new Seed(3, 5, 21, "Zomato", TransactionCategory.FOOD, 600),
            new Seed(3, 8, 12, "Amazon", TransactionCategory.SHOPPING, 3900),
            new Seed(3, 12, 17, "Flipkart", TransactionCategory.SHOPPING, 3100),
            new Seed(3, 15, 8, "Uber", TransactionCategory.TRAVEL, 400),
            new Seed(3, 18, 19, "Ola", TransactionCategory.TRAVEL, 800),
            new Seed(3, 21, 10, "Airtel Recharge", TransactionCategory.BILLS, 799),
            new Seed(3, 24, 9, "Electricity Board", TransactionCategory.BILLS, 2700),
            new Seed(3, 27, 20, "Insurance Premium", TransactionCategory.BILLS, 4001),

            new Seed(4, 3, 13, "Swiggy", TransactionCategory.FOOD, 500),
            new Seed(4, 6, 20, "Zomato", TransactionCategory.FOOD, 600),
            new Seed(4, 9, 11, "Amazon", TransactionCategory.SHOPPING, 3200),
            new Seed(4, 13, 16, "Flipkart", TransactionCategory.SHOPPING, 2600),
            new Seed(4, 16, 9, "Uber", TransactionCategory.TRAVEL, 400),
            new Seed(4, 19, 18, "Indian Oil Petrol Pump", TransactionCategory.TRAVEL, 1500),
            new Seed(4, 22, 10, "Airtel Recharge", TransactionCategory.BILLS, 799),
            new Seed(4, 25, 9, "Electricity Board", TransactionCategory.BILLS, 2400),
            new Seed(4, 28, 19, "Rent Utility", TransactionCategory.BILLS, 3601),

            new Seed(5, 2, 14, "Swiggy", TransactionCategory.FOOD, 560),
            new Seed(5, 5, 21, "Zomato", TransactionCategory.FOOD, 640),
            new Seed(5, 8, 12, "Amazon", TransactionCategory.SHOPPING, 4200),
            new Seed(5, 12, 17, "Flipkart", TransactionCategory.SHOPPING, 3400),
            new Seed(5, 15, 8, "Uber", TransactionCategory.TRAVEL, 450),
            new Seed(5, 18, 19, "Ola", TransactionCategory.TRAVEL, 850),
            new Seed(5, 21, 10, "Airtel Recharge", TransactionCategory.BILLS, 799),
            new Seed(5, 24, 9, "Electricity Board", TransactionCategory.BILLS, 2900),
            new Seed(5, 27, 20, "Insurance Premium", TransactionCategory.BILLS, 4701)
    );

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final BehaviorEventRepository behaviorEventRepository;
    private final UserLocationRepository userLocationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void seedIfAbsent() {
        if (userRepository.existsByEmailIgnoreCase(DEMO_EMAIL)) return;

        User user = new User();
        user.setName("Demo User");
        user.setEmail(DEMO_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        user.setTrustScore(100.0);
        user = userRepository.save(user);

        populate(user);
    }

    @Transactional
    public void resetUser(User user) {
        transactionRepository.deleteAll(transactionRepository.findByUserOrderByTxnDateDesc(user));
        alertRepository.deleteAll(alertRepository.findByUserOrderByCreatedAtDesc(user));
        behaviorEventRepository.deleteAll(behaviorEventRepository.findByUserOrderByCapturedAtDesc(user));
        userLocationRepository.findFirstByUserOrderByCapturedAtDesc(user).ifPresent(userLocationRepository::delete);

        user.setTrustScore(100.0);
        user.setLastTrustUpdate(java.time.Instant.now());
        userRepository.save(user);

        populate(user);
    }

    private void populate(User user) {
        YearMonth currentMonth = YearMonth.now();

        for (int i = 0; i < 6; i++) {
            YearMonth month = currentMonth.minusMonths(6 - i);
            Transaction salary = new Transaction();
            salary.setUser(user);
            salary.setTxnDate(month.atDay(1).atTime(9, 0));
            salary.setDescription("Monthly Salary Credit");
            salary.setMerchant("Employer Payroll");
            salary.setCategory(TransactionCategory.INCOME);
            salary.setAmount(MONTHLY_SALARY);
            salary.setTxnType(TransactionType.CREDIT);
            salary.setSource(TransactionSource.SIMULATED);
            transactionRepository.save(salary);
        }

        for (Seed seed : DEBIT_SEEDS) {
            YearMonth month = currentMonth.minusMonths(6 - seed.monthOffset());
            Transaction t = new Transaction();
            t.setUser(user);
            t.setTxnDate(month.atDay(Math.min(seed.day(), month.lengthOfMonth())).atTime(seed.hour(), 0));
            t.setDescription(seed.merchant() + " purchase");
            t.setMerchant(seed.merchant());
            t.setCategory(seed.category());
            t.setAmount(BigDecimal.valueOf(seed.amount()));
            t.setTxnType(TransactionType.DEBIT);
            t.setSource(TransactionSource.SIMULATED);
            t.setLatitude(HOME_LAT + 0.01);
            t.setLongitude(HOME_LON + 0.01);
            t.setCity("Bengaluru");
            t.setCountry("India");
            t.setRiskScore(0);
            t.setFraud(false);
            transactionRepository.save(t);
        }

        var location = new com.finguard.entity.UserLocation();
        location.setUser(user);
        location.setLatitude(HOME_LAT);
        location.setLongitude(HOME_LON);
        location.setAccuracy(20.0);
        userLocationRepository.save(location);

        LocalDateTime now = LocalDateTime.now();
        double[][] baseline = {
                {58, 190, 320, 9}, {56, 195, 310, 8}, {60, 185, 330, 10}, {57, 192, 315, 9}, {59, 188, 325, 9}
        };
        for (int i = 0; i < baseline.length; i++) {
            BehaviorEvent event = new BehaviorEvent();
            event.setUser(user);
            event.setTypingSpeedWpm(baseline[i][0]);
            event.setAvgKeyIntervalMs(baseline[i][1]);
            event.setMouseSpeedPxS(baseline[i][2]);
            event.setClickRatePerMin(baseline[i][3]);
            event.setSessionDurationS(300);
            event.setRiskScore(0);
            event.setAnomaly(false);
            event.setReason("Baseline sample consistent with established pattern.");
            event.setCapturedAt(now.minusDays(baseline.length - i).atZone(java.time.ZoneId.systemDefault()).toInstant());
            behaviorEventRepository.save(event);
        }
    }
}
