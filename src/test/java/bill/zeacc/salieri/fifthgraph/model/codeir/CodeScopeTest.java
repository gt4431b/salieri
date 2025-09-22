package bill.zeacc.salieri.fifthgraph.model.codeir;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CodeScope Tests")
public class CodeScopeTest {

    private CodeScope codeScope;

    @BeforeEach
    protected void setUp() {
        codeScope = new CodeScope();
    }

    @Test
    @DisplayName("Should be instantiable")
    public void shouldBeInstantiable() {
        // When/Then
        assertThat(codeScope).isNotNull();
        assertThat(codeScope).isInstanceOf(CodeScope.class);
    }

    @Test
    @DisplayName("Should support multiple instances")
    public void shouldSupportMultipleInstances() {
        // Given
        CodeScope scope1 = new CodeScope();
        CodeScope scope2 = new CodeScope();
        
        // When/Then
        assertThat(scope1).isNotNull();
        assertThat(scope2).isNotNull();
        assertThat(scope1).isNotSameAs(scope2);
    }

    @Test
    @DisplayName("Should have correct package")
    public void shouldHaveCorrectPackage() {
        // When/Then
        assertThat(codeScope.getClass().getPackage().getName())
            .isEqualTo("bill.zeacc.salieri.fifthgraph.model.codeir");
    }

    @Test
    @DisplayName("Should have correct class name")
    public void shouldHaveCorrectClassName() {
        // When/Then
        assertThat(codeScope.getClass().getSimpleName()).isEqualTo("CodeScope");
    }

    @Test
    @DisplayName("Should support object equality")
    public void shouldSupportObjectEquality() {
        // Given
        CodeScope scope1 = new CodeScope();
        CodeScope scope2 = new CodeScope();
        
        // When/Then
        // Since it's an empty class with no overridden equals, 
        // each instance should be unique
        assertThat(scope1).isNotEqualTo(scope2);
        assertThat(scope1).isEqualTo(scope1); // Self-equality
    }

    @Test
    @DisplayName("Should support toString")
    public void shouldSupportToString() {
        // When
        String toString = codeScope.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("CodeScope");
    }

    @Test
    @DisplayName("Should support hashCode")
    public void shouldSupportHashCode() {
        // When
        int hashCode1 = codeScope.hashCode();
        int hashCode2 = new CodeScope().hashCode();
        
        // Then
        assertThat(hashCode1).isNotNegative();
        // Since it's an empty class with default Object behavior,
        // different instances should have different hash codes
        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }

    @Test
    @DisplayName("Should be a valid placeholder class")
    public void shouldBeAValidPlaceholderClass() {
        // When/Then
        // Verify it's a proper class in the correct package
        assertThat(codeScope.getClass().isInterface()).isFalse();
        assertThat(codeScope.getClass().isEnum()).isFalse();
        assertThat(codeScope.getClass().isAnnotation()).isFalse();
        assertThat(codeScope.getClass().isPrimitive()).isFalse();
    }

    @Test
    @DisplayName("Should extend Object")
    public void shouldExtendObject() {
        // When/Then
        assertThat(codeScope.getClass().getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    @DisplayName("Should have no interfaces")
    public void shouldHaveNoInterfaces() {
        // When/Then
        assertThat(codeScope.getClass().getInterfaces()).isEmpty();
    }

    @Test
    @DisplayName("Should have public no-arg constructor")
    public void shouldHavePublicNoArgConstructor() throws NoSuchMethodException {
        // When/Then
        assertThat(codeScope.getClass().getConstructor()).isNotNull();
        assertThat(codeScope.getClass().getConstructor().getParameterCount()).isEqualTo(0);
    }
}