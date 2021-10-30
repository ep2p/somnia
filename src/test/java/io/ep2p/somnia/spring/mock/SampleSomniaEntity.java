package io.ep2p.somnia.spring.mock;

import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.EntityType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sample_collection")
@SomniaDocument(uniqueKey = true, type = EntityType.HIT)
@Getter
@Setter
@NoArgsConstructor
public class SampleSomniaEntity extends MongoSomniaEntity<SampleData> {
}
