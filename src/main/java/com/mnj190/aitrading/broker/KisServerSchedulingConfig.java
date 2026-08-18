package com.mnj190.aitrading.broker;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's {@code @Scheduled} machinery only for the long-running
 * {@code kis-server} profile. Scoping this to the profile keeps the
 * scheduler thread pool out of tests and one-shot runner profiles.
 */
@Configuration
@Profile("kis-server")
@EnableScheduling
class KisServerSchedulingConfig {
}
