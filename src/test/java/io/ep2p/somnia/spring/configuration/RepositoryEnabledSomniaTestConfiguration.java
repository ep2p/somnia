package io.ep2p.somnia.spring.configuration;

import io.ep2p.somnia.config.dynamic.EnableSomniaRepository;
import org.springframework.boot.test.context.TestConfiguration;

@EnableSomniaRepository(basePackages = "io.ep2p.somnia.spring.repository")
@TestConfiguration
public class RepositoryEnabledSomniaTestConfiguration {
}
