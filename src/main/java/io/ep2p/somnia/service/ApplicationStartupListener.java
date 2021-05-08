package io.ep2p.somnia.service;

import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.config.properties.SomniaBaseConfigProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStartupListener {
    private final MongoTemplate mongoTemplate;
    private final SomniaBaseConfigProperties somniaBaseConfigProperties;

    @Autowired
    public ApplicationStartupListener(MongoTemplate mongoTemplate, SomniaBaseConfigProperties somniaBaseConfigProperties) {
        this.mongoTemplate = mongoTemplate;
        this.somniaBaseConfigProperties = somniaBaseConfigProperties;
    }

    @SneakyThrows
    @EventListener
    public void handleApplicationStartup(ApplicationStartedEvent applicationStartedEvent){
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);

        scanner.addIncludeFilter(new AnnotationTypeFilter(SomniaDocument.class));

        for (BeanDefinition bd : scanner.findCandidateComponents(somniaBaseConfigProperties.getBasePackage())){
            Class<?> aClass = Class.forName(bd.getBeanClassName());
            SomniaDocument somniaDocument = Class.forName(bd.getBeanClassName()).getAnnotation(SomniaDocument.class);
            if (somniaDocument != null){
                process(somniaDocument, aClass);
            }
        }
    }

    private void process(SomniaDocument somniaDocument, Class<?> aClass){
        if (somniaDocument.inMemory())
            return;
        log.info("Processing key indexing for " + aClass);
        Index index = new Index("key", Sort.Direction.ASC).named(somniaBaseConfigProperties.getMongoKeyIndexName());
        if (somniaDocument.uniqueKey()) {
            this.mongoTemplate.indexOps(aClass).ensureIndex(index.unique());
        }else {
            this.mongoTemplate.indexOps(aClass).ensureIndex(index);
        }

    }
}
