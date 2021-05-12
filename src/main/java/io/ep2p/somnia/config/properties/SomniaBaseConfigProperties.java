package io.ep2p.somnia.config.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "somnia.config")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SomniaBaseConfigProperties {
    private String basePackage = null;
    @Builder.Default
    private String mongoKeyIndexName = "s_key_index";
}
