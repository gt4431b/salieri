package bill.zeacc.salieri.fifthgraph.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

@DisplayName("NodeConfig Tests")
public class NodeConfigTest {

    private NodeConfig nodeConfig;

    @BeforeEach
    protected void setUp() {
        nodeConfig = new NodeConfig();
    }

    @Test
    @DisplayName("Should be instantiable")
    public void shouldBeInstantiable() {
        // When/Then
        assertThat(nodeConfig).isNotNull();
        assertThat(nodeConfig).isInstanceOf(NodeConfig.class);
    }

    @Test
    @DisplayName("Should be annotated with Configuration")
    public void shouldBeAnnotatedWithConfiguration() {
        // When/Then
        assertThat(nodeConfig.getClass().isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Should have Configuration annotation with correct values")
    public void shouldHaveConfigurationAnnotationWithCorrectValues() {
        // When
        Configuration configAnnotation = nodeConfig.getClass().getAnnotation(Configuration.class);
        
        // Then
        assertThat(configAnnotation).isNotNull();
        assertThat(configAnnotation.value()).isEmpty(); // Default value
    }

    @Test
    @DisplayName("Should support multiple instances")
    public void shouldSupportMultipleInstances() {
        // Given
        NodeConfig config1 = new NodeConfig();
        NodeConfig config2 = new NodeConfig();
        
        // When/Then
        assertThat(config1).isNotNull();
        assertThat(config2).isNotNull();
        assertThat(config1).isNotSameAs(config2);
    }

    @Test
    @DisplayName("Should be a valid placeholder configuration class")
    public void shouldBeAValidPlaceholderConfigurationClass() {
        // When/Then
        // Verify it's a proper Spring configuration class
        assertThat(nodeConfig.getClass().getPackage().getName()).isEqualTo("bill.zeacc.salieri.fifthgraph.config");
        assertThat(nodeConfig.getClass().getSimpleName()).isEqualTo("NodeConfig");
        assertThat(nodeConfig.getClass().isAnnotationPresent(Configuration.class)).isTrue();
    }
}