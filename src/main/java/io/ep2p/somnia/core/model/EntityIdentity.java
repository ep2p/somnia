package io.ep2p.somnia.core.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
public class EntityIdentity {
    private String name;
    private StorageMethod method;
    private List<FieldIdentity> fields;

    @Data
    @Builder
    @NoArgsConstructor
    public static class FieldIdentity {
        private String name;
        private boolean index;
        private String type;
    }
}
