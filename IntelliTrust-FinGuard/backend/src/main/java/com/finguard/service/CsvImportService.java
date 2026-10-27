package com.finguard.service;

import com.finguard.dto.transaction.ImportResultResponse;
import com.finguard.entity.*;
import com.finguard.exception.BadRequestException;
import com.finguard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CsvImportService {

    private static final List<String> DATE_COLUMNS = List.of("date", "txn_date", "transaction date", "value date");
    private static final List<String> DESCRIPTION_COLUMNS = List.of("description", "narration", "details", "particulars");
    private static final List<String> MERCHANT_COLUMNS = List.of("merchant", "payee");
    private static final List<String> AMOUNT_COLUMNS = List.of("amount", "value");
    private static final List<String> TYPE_COLUMNS = List.of("type", "txn_type", "transaction type");
    private static final List<String> DEBIT_COLUMNS = List.of("debit", "withdrawal");
    private static final List<String> CREDIT_COLUMNS = List.of("credit", "deposit");
    private static final List<String> CATEGORY_COLUMNS = List.of("category");

    private static final Map<TransactionCategory, List<String>> CATEGORY_KEYWORDS = Map.of(
            TransactionCategory.FOOD, List.of("swiggy", "zomato", "restaurant", "cafe", "food", "dine", "eat"),
            TransactionCategory.SHOPPING, List.of("amazon", "flipkart", "myntra", "mall", "store", "shop", "electronics"),
            TransactionCategory.TRAVEL, List.of("uber", "ola", "irctc", "flight", "airlines", "travel", "fuel", "petrol", "cab"),
            TransactionCategory.BILLS, List.of("electricity", "water bill", "recharge", "broadband", "insurance", "emi", "rent", "airtel", "jio"),
            TransactionCategory.INCOME, List.of("salary", "credited", "refund", "interest", "bonus")
    );

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    private final TransactionRepository transactionRepository;
    private final FraudService fraudService;

    @Transactional
    public ImportResultResponse importCsv(User user, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required");
        }

        List<String> errors = new ArrayList<>();
        int total = 0, imported = 0, duplicates = 0, invalid = 0;

        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            Map<String, String> headerMap = buildLowercaseHeaderMap(parser.getHeaderNames());

            for (CSVRecord record : parser) {
                total++;
                try {
                    Transaction transaction = parseRecord(user, record, headerMap);
                    String fingerprint = fingerprint(user.getId(), transaction);
                    if (transactionRepository.findByUserAndFingerprint(user, fingerprint).isPresent()) {
                        duplicates++;
                        continue;
                    }
                    transaction.setFingerprint(fingerprint);
                    transaction.setSource(TransactionSource.CSV_IMPORT);
                    transaction = transactionRepository.save(transaction);
                    fraudService.evaluate(user, transaction);
                    imported++;
                } catch (Exception rowEx) {
                    invalid++;
                    errors.add("Row " + (total + 1) + ": " + rowEx.getMessage());
                }
            }
        } catch (IOException e) {
            throw new BadRequestException("Unable to read CSV file: " + e.getMessage());
        }

        return new ImportResultResponse(total, imported, duplicates, invalid, errors);
    }

    private Transaction parseRecord(User user, CSVRecord record, Map<String, String> headerMap) {
        String dateStr = firstValue(record, headerMap, DATE_COLUMNS);
        if (dateStr == null || dateStr.isBlank()) {
            throw new IllegalArgumentException("Missing date column");
        }
        LocalDate date = parseDate(dateStr);

        String description = firstValue(record, headerMap, DESCRIPTION_COLUMNS);
        String merchant = firstValue(record, headerMap, MERCHANT_COLUMNS);
        if (merchant == null || merchant.isBlank()) merchant = description;

        BigDecimal amount;
        TransactionType type;

        String debitStr = firstValue(record, headerMap, DEBIT_COLUMNS);
        String creditStr = firstValue(record, headerMap, CREDIT_COLUMNS);
        if ((debitStr != null && !debitStr.isBlank()) || (creditStr != null && !creditStr.isBlank())) {
            if (debitStr != null && !debitStr.isBlank()) {
                amount = new BigDecimal(sanitizeNumber(debitStr));
                type = TransactionType.DEBIT;
            } else {
                amount = new BigDecimal(sanitizeNumber(creditStr));
                type = TransactionType.CREDIT;
            }
        } else {
            String amountStr = firstValue(record, headerMap, AMOUNT_COLUMNS);
            if (amountStr == null || amountStr.isBlank()) {
                throw new IllegalArgumentException("Missing amount column");
            }
            amount = new BigDecimal(sanitizeNumber(amountStr));

            String typeStr = firstValue(record, headerMap, TYPE_COLUMNS);
            if (typeStr != null && typeStr.trim().equalsIgnoreCase("credit")) {
                type = TransactionType.CREDIT;
            } else if (typeStr != null && typeStr.trim().equalsIgnoreCase("debit")) {
                type = TransactionType.DEBIT;
            } else {
                type = amount.signum() < 0 ? TransactionType.DEBIT : TransactionType.CREDIT;
            }
        }
        amount = amount.abs();

        String categoryStr = firstValue(record, headerMap, CATEGORY_COLUMNS);
        TransactionCategory category = categoryStr != null && !categoryStr.isBlank()
                ? parseCategoryOrNull(categoryStr)
                : null;
        if (category == null) {
            category = type == TransactionType.CREDIT ? TransactionCategory.INCOME : classify(description, merchant);
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTxnDate(date.atStartOfDay());
        transaction.setDescription(description);
        transaction.setMerchant(merchant);
        transaction.setAmount(amount);
        transaction.setTxnType(type);
        transaction.setCategory(category);
        return transaction;
    }

    private TransactionCategory classify(String description, String merchant) {
        String text = ((description == null ? "" : description) + " " + (merchant == null ? "" : merchant)).toLowerCase();
        for (var entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword)) return entry.getKey();
            }
        }
        return TransactionCategory.OTHER;
    }

    private TransactionCategory parseCategoryOrNull(String value) {
        try {
            return TransactionCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(value.trim(), fmt);
            } catch (Exception ignored) {
                // try next format
            }
        }
        throw new IllegalArgumentException("Unrecognized date format: " + value);
    }

    private String sanitizeNumber(String raw) {
        return raw.replace(",", "").replace("₹", "").trim();
    }

    private Map<String, String> buildLowercaseHeaderMap(List<String> headers) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String h : headers) {
            map.put(h.trim().toLowerCase(), h);
        }
        return map;
    }

    private String firstValue(CSVRecord record, Map<String, String> headerMap, List<String> candidates) {
        for (String candidate : candidates) {
            String actualHeader = headerMap.get(candidate);
            if (actualHeader != null && record.isMapped(actualHeader)) {
                String value = record.get(actualHeader);
                if (value != null && !value.isBlank()) return value.trim();
            }
        }
        return null;
    }

    private String fingerprint(Long userId, Transaction transaction) {
        try {
            String raw = userId + "|" + transaction.getTxnDate() + "|" + transaction.getDescription()
                    + "|" + transaction.getAmount() + "|" + transaction.getTxnType();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
