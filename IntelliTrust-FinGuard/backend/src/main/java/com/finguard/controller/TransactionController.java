package com.finguard.controller;

import com.finguard.dto.transaction.*;
import com.finguard.entity.TransactionCategory;
import com.finguard.entity.TransactionType;
import com.finguard.security.AppUserPrincipal;
import com.finguard.service.CsvImportService;
import com.finguard.service.TransactionService;
import com.finguard.service.TransactionSimulatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CsvImportService csvImportService;
    private final TransactionSimulatorService simulatorService;

    @GetMapping
    public List<TransactionResponse> list(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) TransactionCategory category,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Boolean fraudOnly,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String search
    ) {
        return transactionService.list(principal.getUser(), category, type, fraudOnly, from, to, search);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@AuthenticationPrincipal AppUserPrincipal principal, @Valid @RequestBody TransactionRequest request) {
        return transactionService.create(principal.getUser(), request);
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return transactionService.getById(principal.getUser(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        transactionService.delete(principal.getUser(), id);
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ImportResultResponse importCsv(@AuthenticationPrincipal AppUserPrincipal principal, @RequestParam("file") MultipartFile file) {
        return csvImportService.importCsv(principal.getUser(), file);
    }

    @PostMapping("/simulate")
    public List<TransactionResponse> simulate(@AuthenticationPrincipal AppUserPrincipal principal, @RequestBody(required = false) SimulateRequest request) {
        return simulatorService.simulate(principal.getUser(), request == null ? new SimulateRequest(1, false) : request);
    }
}
