package bill.zeacc.salieri.fifthgraph.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ToolMarker Tests")
public class ToolMarkerTest {

    @Test
    @DisplayName("Should be an annotation interface")
    public void shouldBeAnAnnotationInterface() {
        // When/Then
        assertThat(ToolMarker.class.isAnnotation()).isTrue();
        assertThat(ToolMarker.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Should be annotated with Documented")
    public void shouldBeAnnotatedWithDocumented() {
        // When/Then
        assertThat(ToolMarker.class.isAnnotationPresent(Documented.class)).isTrue();
    }

    @Test
    @DisplayName("Should have RUNTIME retention policy")
    public void shouldHaveRuntimeRetentionPolicy() {
        // When
        Retention retention = ToolMarker.class.getAnnotation(Retention.class);
        
        // Then
        assertThat(retention).isNotNull();
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    @DisplayName("Should target ANNOTATION_TYPE")
    public void shouldTargetAnnotationType() {
        // When
        Target target = ToolMarker.class.getAnnotation(Target.class);
        
        // Then
        assertThat(target).isNotNull();
        assertThat(target.value()).containsExactly(ElementType.ANNOTATION_TYPE);
    }

    @Test
    @DisplayName("Should have no declared methods")
    public void shouldHaveNoDeclaredMethods() {
        // When/Then
        assertThat(ToolMarker.class.getDeclaredMethods()).isEmpty();
    }

    @Test
    @DisplayName("Should have correct package")
    public void shouldHaveCorrectPackage() {
        // When/Then
        assertThat(ToolMarker.class.getPackage().getName()).isEqualTo("bill.zeacc.salieri.fifthgraph.config");
    }

    @Test
    @DisplayName("Should be usable as meta-annotation")
    public void shouldBeUsableAsMetaAnnotation() {
        // When/Then - Verify ToolMarker can be applied to annotation types
        assertThat(ToolMarker.class.getAnnotation(Target.class).value()).containsExactly(ElementType.ANNOTATION_TYPE);
        assertThat(ToolMarker.class.isAnnotation()).isTrue();
    }

    @Test
    @DisplayName("Should have all expected annotation metadata")
    public void shouldHaveAllExpectedAnnotationMetadata() {
        // When
        Annotation[] annotations = ToolMarker.class.getAnnotations();
        
        // Then
        assertThat(annotations).hasSize(3); // Documented, Retention, Target
        
        boolean hasDocumented = false;
        boolean hasRetention = false;
        boolean hasTarget = false;
        
        for (Annotation annotation : annotations) {
            if (annotation instanceof Documented) hasDocumented = true;
            if (annotation instanceof Retention) hasRetention = true;
            if (annotation instanceof Target) hasTarget = true;
        }
        
        assertThat(hasDocumented).isTrue();
        assertThat(hasRetention).isTrue();
        assertThat(hasTarget).isTrue();
    }

    @Test
    @DisplayName("Should have correct simple name")
    public void shouldHaveCorrectSimpleName() {
        // When/Then
        assertThat(ToolMarker.class.getSimpleName()).isEqualTo("ToolMarker");
    }

    @Test
    @DisplayName("Should extend Annotation interface")
    public void shouldExtendAnnotationInterface() {
        // When/Then
        assertThat(Annotation.class.isAssignableFrom(ToolMarker.class)).isTrue();
        assertThat(ToolMarker.class.getInterfaces()).contains(Annotation.class);
    }
}