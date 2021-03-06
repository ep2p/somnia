package io.ep2p.somnia.service;

import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.config.properties.SomniaBaseConfigProperties;
import io.ep2p.somnia.decentralized.SomniaEntityManager;
import io.ep2p.somnia.model.SomniaEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class EntityManagerRegisterer {
    private final SomniaEntityManager somniaEntityManager;
    private final SomniaBaseConfigProperties somniaBaseConfigProperties;
    private final ApplicationContext applicationContext;

    public EntityManagerRegisterer(SomniaEntityManager somniaEntityManager, SomniaBaseConfigProperties somniaBaseConfigProperties, ApplicationContext applicationContext) {
        this.somniaEntityManager = somniaEntityManager;
        this.somniaBaseConfigProperties = somniaBaseConfigProperties;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init(){
        Set<String> packages = new HashSet<>();

        if("".equals(somniaBaseConfigProperties.getBasePackage()) || somniaBaseConfigProperties.getBasePackage() != null){
            packages.add(somniaBaseConfigProperties.getBasePackage());
        }

        Map<String, Object> candidates = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        Class<?> aClass = candidates.isEmpty() ? null : candidates.values().toArray()[0].getClass();
        if (aClass != null){
            String className = aClass.getName();
            if (className.contains("$$") && className.contains(".")){
                className = extractClass(className);
            }
            packages.add(className);
            log.info("Scanning " + className + " for somnia entities");
        }

        if (packages.size() == 0){
            throw new RuntimeException("For an environment without any @SpringBootApplication you need to pass base packages in somnia.config.basePackage property");
        }

        scanPackages(packages);
    }

    private String extractClass(String c){
        String[] items = c.split("\\$$")[0].split("\\.");
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < items.length - 1; i++){
            stringBuilder.append(items[i]);
            if (i != items.length - 2){
                stringBuilder.append(".");
            }
        }
        return stringBuilder.toString();
    }

    @SneakyThrows
    private void scanPackages(Set<String> packages){
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new AnnotationTypeFilter(SomniaDocument.class));

        for (String aPackage : packages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(aPackage)){
                Class<?> aClass = Class.forName(bd.getBeanClassName());
                SomniaDocument somniaDocument = aClass.getAnnotation(SomniaDocument.class);
                if (somniaDocument != null){
                    somniaEntityManager.register((Class<SomniaEntity<?,?>>) aClass);
                }
            }
        }
    }

}
