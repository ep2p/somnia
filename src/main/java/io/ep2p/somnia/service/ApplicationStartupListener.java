package io.ep2p.somnia.service;

import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.config.properties.SomniaBaseConfigProperties;
import io.ep2p.somnia.decentralized.SomniaEntityManager;
import io.ep2p.somnia.model.SomniaEntity;
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

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class ApplicationStartupListener {
    private final MongoTemplate mongoTemplate;
    private final SomniaEntityManager somniaEntityManager;
    private final SomniaBaseConfigProperties somniaBaseConfigProperties;

    @Autowired
    public ApplicationStartupListener(MongoTemplate mongoTemplate, SomniaEntityManager somniaEntityManager, SomniaBaseConfigProperties somniaBaseConfigProperties) {
        this.mongoTemplate = mongoTemplate;
        this.somniaEntityManager = somniaEntityManager;
        this.somniaBaseConfigProperties = somniaBaseConfigProperties;
    }

    @SneakyThrows
    @EventListener
    public void handleApplicationStartup(ApplicationStartedEvent applicationStartedEvent){
        Set<String> packages = new HashSet<>();

        if("".equals(somniaBaseConfigProperties.getBasePackage()) || somniaBaseConfigProperties.getBasePackage() != null){
            packages.add(somniaBaseConfigProperties.getBasePackage());
        }else {
            applicationStartedEvent.getSpringApplication().getAllSources().forEach(o -> {
                if (o instanceof Class){
                    packages.add(((Class<?>) o).getPackage().getName());
                }
            });
        }

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new AnnotationTypeFilter(SomniaDocument.class));

        for (String aPackage : packages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(aPackage)){
                Class<?> aClass = Class.forName(bd.getBeanClassName());
                SomniaDocument somniaDocument = aClass.getAnnotation(SomniaDocument.class);
                if (somniaDocument != null){
                    processMongo(somniaDocument, aClass);
                    processEntityManager(aClass);
                }
            }
        }
    }

    private void processEntityManager(Class<?> aClass) {
        somniaEntityManager.register((Class<SomniaEntity<?>>) aClass);
    }

    private void processMongo(SomniaDocument somniaDocument, Class<?> aClass){
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
