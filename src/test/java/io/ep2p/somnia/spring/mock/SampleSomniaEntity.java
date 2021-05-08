package io.ep2p.somnia.spring.mock;

import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.EntityType;
import io.ep2p.somnia.model.SomniaEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sample_collection")
@SomniaDocument(uniqueKey = true, type = EntityType.HIT, inMemory = true)
@Getter
@Setter
public class SampleSomniaEntity extends SomniaEntity<SampleData> {
}
