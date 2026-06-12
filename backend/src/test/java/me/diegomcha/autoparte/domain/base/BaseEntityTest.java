package me.diegomcha.autoparte.domain.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

class BaseEntityTest {

    private static class TestEntity extends BaseEntity {
        protected TestEntity() {
            super();
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Test
    void testConstructionContract() {
        var entity = new TestEntity();
        Assertions.assertNotNull(entity.getId());
        Assertions.assertTrue(entity.isNew());
    }

    @ParameterizedTest
    @MethodSource("subclasses")
    void testSubclassesHaveProtectedNoArgsConstructor(Class<?> clazz) throws NoSuchMethodException {
        var constructor = clazz.getDeclaredConstructor();

        Assertions.assertEquals(0, constructor.getParameterCount());
        Assertions.assertTrue(Modifier.isProtected(constructor.getModifiers()));
    }

    @ParameterizedTest
    @MethodSource("subclasses")
    void testSubclassesHaveToString(Class<?> clazz) throws NoSuchMethodException {
        var toStringMethod = clazz.getMethod("toString");
        Assertions.assertNotEquals(Object.class, toStringMethod.getDeclaringClass());
    }

    static Stream<Class<?>> subclasses() {
        // Use Spring's ClassPathScanningCandidateComponentProvider to find all subclasses of BaseEntity in the specified package
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(BaseEntity.class));

        return provider
                .findCandidateComponents("me.diegomcha.autoparte.domain").stream()
                .map(BeanDefinition::getBeanClassName)
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
