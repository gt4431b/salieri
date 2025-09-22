package bill.zeacc.salieri.fifthgraph.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MetaConfig Tests")
public class MetaConfigTest {

    private MetaConfig metaConfig;

    @BeforeEach
    protected void setUp() {
        metaConfig = new MetaConfig();
    }

    @Test
    @DisplayName("Should create BaseCheckpointSaver bean")
    public void shouldCreateBaseCheckpointSaverBean() {
        // When
        BaseCheckpointSaver checkpointSaver = metaConfig.continuityStrategy();
        
        // Then
        assertThat(checkpointSaver).isNotNull();
        assertThat(checkpointSaver).isInstanceOf(MemorySaver.class);
    }

    @Test
    @DisplayName("Should return new instance on each call")
    public void shouldReturnNewInstanceOnEachCall() {
        // When
        BaseCheckpointSaver saver1 = metaConfig.continuityStrategy();
        BaseCheckpointSaver saver2 = metaConfig.continuityStrategy();
        
        // Then
        assertThat(saver1).isNotNull();
        assertThat(saver2).isNotNull();
        assertThat(saver1).isNotSameAs(saver2);
        assertThat(saver1).isInstanceOf(MemorySaver.class);
        assertThat(saver2).isInstanceOf(MemorySaver.class);
    }

    @Test
    @DisplayName("Should consistently return MemorySaver instances")
    public void shouldConsistentlyReturnMemorySaverInstances() {
        // When
        BaseCheckpointSaver saver1 = metaConfig.continuityStrategy();
        BaseCheckpointSaver saver2 = metaConfig.continuityStrategy();
        BaseCheckpointSaver saver3 = metaConfig.continuityStrategy();
        
        // Then
        assertThat(saver1).isInstanceOf(MemorySaver.class);
        assertThat(saver2).isInstanceOf(MemorySaver.class);
        assertThat(saver3).isInstanceOf(MemorySaver.class);
    }

    @Test
    @DisplayName("Should be a valid Spring configuration class")
    public void shouldBeAValidSpringConfigurationClass() {
        // When/Then
        assertThat(metaConfig.getClass().isAnnotationPresent(org.springframework.context.annotation.Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Should have continuityStrategy method annotated with Bean")
    public void shouldHaveContinuityStrategyMethodAnnotatedWithBean() throws NoSuchMethodException {
        // When/Then
        assertThat(metaConfig.getClass().getMethod("continuityStrategy").isAnnotationPresent(org.springframework.context.annotation.Bean.class)).isTrue();
    }
}