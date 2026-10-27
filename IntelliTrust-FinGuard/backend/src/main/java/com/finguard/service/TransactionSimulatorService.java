package com.finguard.service;

import com.finguard.dto.transaction.SimulateRequest;
import com.finguard.dto.transaction.TransactionResponse;
import com.finguard.entity.*;
import com.finguard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Generates realistic demo transactions and runs the complete fraud pipeline on
 * each one, so the simulator exercises the same code path as CSV import or a
 * manual entry -- roughly one in four are deliberately suspicious.
 */
@Service
@RequiredArgsConstructor
public class TransactionSimulatorService {

    private record MerchantProfile(String merchant, TransactionCategory category, double minAmount, double maxAmount) {}

    private static final List<MerchantProfile> NORMAL_MERCHANTS = List.of(
            new MerchantProfile("Swiggy", TransactionCategory.FOOD, 150, 900),
            new MerchantProfile("Zomato", TransactionCategory.FOOD, 200, 800),
            new MerchantProfile("Amazon", TransactionCategory.SHOPPING, 300, 4500),
            new MerchantProfile("Flipkart", TransactionCategory.SHOPPING, 250, 3800),
            new MerchantProfile("Uber", TransactionCategory.TRAVEL, 100, 650),
            new MerchantProfile("Indian Oil Petrol Pump", TransactionCategory.TRAVEL, 500, 2000),
            new MerchantProfile("Airtel Recharge", TransactionCategory.BILLS, 299, 999),
            new MerchantProfile("Electricity Board", TransactionCategory.BILLS, 800, 3500)
    );

    // Bengaluru, India reference point -- matches the demo user's usual city.
    private static final double HOME_LAT = 12.9716;
    private static final double HOME_LON = 77.5946;

    private final TransactionRepository transactionRepository;
    private final FraudService fraudService;
    private final Random random = new Random();

    @Transactional
    public List<TransactionResponse> simulate(User user, SimulateRequest request) {
        int count = request.countOrDefault();
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> simulateOne(user, request.forceSuspiciousOrDefault() || random.nextDouble() < 0.25))
                .toList();
    }

    private TransactionResponse simulateOne(User user, boolean suspicious) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTxnDate(LocalDateTime.now());
        transaction.setTxnType(TransactionType.DEBIT);
        transaction.setSource(TransactionSource.SIMULATED);

        if (suspicious) {
            transaction.setMerchant("Unknown Electronics Wholesaler");
            transaction.setDescription("High-value purchase at unfamiliar merchant");
            transaction.setCategory(TransactionCategory.SHOPPING);
            transaction.setAmount(BigDecimal.valueOf(60000 + random.nextInt(90000)));
            // Far from home: randomised distant coordinates (several thousand km away).
            transaction.setLatitude(HOME_LAT + (random.nextBoolean() ? 1 : -1) * (20 + random.nextDouble() * 20));
            transaction.setLongitude(HOME_LON + (random.nextBoolean() ? 1 : -1) * (40 + random.nextDouble() * 40));
            transaction.setCity("Unknown");
            transaction.setCountry("Unknown");
        } else {
            MerchantProfile profile = NORMAL_MERCHANTS.get(random.nextInt(NORMAL_MERCHANTS.size()));
            transaction.setMerchant(profile.merchant());
            transaction.setDescription(profile.merchant() + " purchase");
            transaction.setCategory(profile.category());
            double amount = profile.minAmount() + random.nextDouble() * (profile.maxAmount() - profile.minAmount());
            transaction.setAmount(BigDecimal.valueOf(Math.round(amount * 100.0) / 100.0));
            transaction.setLatitude(HOME_LAT + (random.nextDouble() - 0.5) * 0.2);
            transaction.setLongitude(HOME_LON + (random.nextDouble() - 0.5) * 0.2);
            transaction.setCity("Bengaluru");
            transaction.setCountry("India");
        }

        transaction = transactionRepository.save(transaction);
        fraudService.evaluate(user, transaction);
        return TransactionResponse.from(transaction);
    }
}
