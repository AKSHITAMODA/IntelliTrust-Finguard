package com.finguard.config;

import com.finguard.service.DemoSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DemoSeedService demoSeedService;

    @Value("${app.demo.seed-on-startup}")
    private boolean seedOnStartup;

    @Override
    public void run(String... args) {
        if (seedOnStartup) {
            demoSeedService.seedIfAbsent();
        }
    }
}
