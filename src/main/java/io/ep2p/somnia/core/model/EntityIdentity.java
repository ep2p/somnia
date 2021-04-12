package io.ep2p.somnia.core.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class EntityIdentity {
    private String name;
    private StorageMethod method;
    private List<FieldIdentity> fields;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class FieldIdentity {
        private String name;
        private boolean index;
        private String type;
    }
}
