package io.ep2p.somnia.config.dynamic;

import io.ep2p.somnia.storage.SomniaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

@Slf4j
@Configuration
public class SomniaRepositoryBeanRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {
    private ClassPathScanner classpathScanner;
    private ClassLoader classLoader;

    public SomniaRepositoryBeanRegistrar() {
        classpathScanner = new ClassPathScanner(false);
        classpathScanner.addIncludeFilter(new AnnotationTypeFilter(DynamicRepository.class));
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        String[] basePackages = getBasePackages(importingClassMetadata);
        if (ArrayUtils.isNotEmpty(basePackages)) {
            for (String basePackage : basePackages) {
                createSomniaRepositoryProxies(basePackage, beanDefinitionRegistry);
            }
        }
    }

    private void createSomniaRepositoryProxies(String basePackage, BeanDefinitionRegistry beanDefinitionRegistry) {
        try {
            for (BeanDefinition beanDefinition : classpathScanner.findCandidateComponents(basePackage)) {

                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());

                if (!clazz.isAssignableFrom(SomniaRepository.class)) {
                    throw new IllegalAccessException("A @DynamicRepository should also implement SomniaRepository interface, but this one doesnt: " + clazz.getName());
                }

                DynamicRepository somniaRepository = clazz.getAnnotation(DynamicRepository.class);

                String beanName = StringUtils.isNotEmpty(somniaRepository.bean())
                        ? somniaRepository.bean() : ClassUtils.getQualifiedName(clazz);

                GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
                proxyBeanDefinition.setBeanClass(clazz);

                ConstructorArgumentValues args = new ConstructorArgumentValues();

                args.addGenericArgumentValue(classLoader);
                args.addGenericArgumentValue(clazz);
                proxyBeanDefinition.setConstructorArgumentValues(args);

                proxyBeanDefinition.setFactoryBeanName("somniaRepositoryProxyBeanFactory");
                proxyBeanDefinition.setFactoryMethodName("createSomniaRepositoryProxyBean");

                beanDefinitionRegistry.registerBeanDefinition(beanName, proxyBeanDefinition);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private String[] getBasePackages(AnnotationMetadata importingClassMetadata) {
        String[] basePackages = null;

        MultiValueMap<String, Object> allAnnotationAttributes =
                importingClassMetadata.getAllAnnotationAttributes(EnableSomniaRepository.class.getName());

        if (MapUtils.isNotEmpty(allAnnotationAttributes)) {
            basePackages = (String[]) allAnnotationAttributes.getFirst("basePackages");
        }

        return basePackages;
    }
}
