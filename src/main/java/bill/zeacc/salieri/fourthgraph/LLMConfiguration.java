package bill.zeacc.salieri.fourthgraph;

import java.io.IOException ;
import java.util.List ;

import org.springframework.ai.chat.model.Generation ;
import org.springframework.ai.model.tool.ToolCallingManager ;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate ;
import org.springframework.ai.ollama.OllamaChatModel ;
import org.springframework.ai.ollama.api.OllamaApi ;
import org.springframework.ai.ollama.api.OllamaOptions ;
import org.springframework.ai.ollama.management.ModelManagementOptions ;
import org.springframework.ai.ollama.management.PullModelStrategy ;
import org.springframework.ai.tool.resolution.SpringBeanToolCallbackResolver ;
import org.springframework.ai.tool.resolution.ToolCallbackResolver ;
import org.springframework.ai.util.json.schema.SchemaType ;
import org.springframework.beans.factory.annotation.Value ;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty ;
import org.springframework.boot.context.properties.EnableConfigurationProperties ;
import org.springframework.context.ApplicationContext ;
import org.springframework.context.annotation.Bean ;
import org.springframework.context.annotation.Configuration ;
import org.springframework.context.annotation.Primary ;
import org.springframework.context.support.GenericApplicationContext ;
import org.springframework.http.HttpStatusCode ;
import org.springframework.http.client.ClientHttpResponse ;
import org.springframework.retry.backoff.ExponentialBackOffPolicy ;
import org.springframework.retry.policy.SimpleRetryPolicy ;
import org.springframework.retry.support.RetryTemplate ;
import org.springframework.web.client.ResponseErrorHandler ;
import org.springframework.web.client.RestClient ;
import org.springframework.web.reactive.function.client.WebClient ;

import com.fasterxml.jackson.databind.ObjectMapper ;

import io.micrometer.observation.ObservationRegistry ;

@Configuration
@EnableConfigurationProperties(LLMProperties.class)
public class LLMConfiguration {
    
	@Bean
    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "ollama")
	@Primary
    public OllamaChatModel ollamaChatModel(
            RestClient.Builder restBuilder,
            WebClient.Builder webClientBuilder,
            ToolCallingManager toolCallingManager,
            RetryTemplate retryTemplate,
            ObservationRegistry observationRegistry,
            @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${spring.ai.ollama.chat.options.model:llama3-groq-tool-use:latest}") String model,
            @Value("${app.llm.temperature:0.3}") Double temperature,
            ObjectMapper om) {
        
        // OllamaApi constructor needs RestTemplate
        OllamaApi ollamaApi = OllamaApi.builder ( )
        		.baseUrl ( baseUrl )
        		.restClientBuilder ( restBuilder )
        		.webClientBuilder ( webClientBuilder )
        		.responseErrorHandler ( new ResponseErrorHandler ( ) {

        			@Override
        			public boolean hasError ( ClientHttpResponse response ) throws IOException {
        				HttpStatusCode statusCode = response.getStatusCode ( ) ;
        				if ( statusCode.is4xxClientError ( ) || statusCode.is5xxServerError ( ) ) {
            				System.out.println ( "Ollama response status: " + response.getStatusCode ( ) ) ;
							return true ;
						} else {
							return false ;
						}
        			} } )
        		.build ( ) ;

        ModelManagementOptions modelManagementOptions = ModelManagementOptions.builder()
                .pullModelStrategy ( PullModelStrategy.WHEN_MISSING )
                .build();

        ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate = 
                (promptOptions, chatResponse) -> {
                	List <Generation> results = chatResponse.getResults ( ) ;
                	for ( Generation x : results ) {
                		if ( ! x.getOutput ( ).getToolCalls ( ).isEmpty ( ) ) {
                			return true ;
                		}
                	}
                	return false ;
                } ;

        OllamaOptions defaultOptions = OllamaOptions.builder()
        		.model(model)
        		.temperature(temperature)
        		.build ( );
        
        // OllamaChatModel constructor needs api and options
        return new OllamaChatModel(ollamaApi, defaultOptions, toolCallingManager, observationRegistry, modelManagementOptions, toolExecutionEligibilityPredicate, retryTemplate);
    }

	@Bean
	public ToolCallingManager toolCallingManager ( ToolCallbackResolver resolver, ObservationRegistry observationRegistry ) {
		return ToolCallingManager.builder ( )
				.toolCallbackResolver ( resolver )
				.observationRegistry ( observationRegistry )
				.build ( ) ;
	}

	@Bean
	public ObservationRegistry observationRegistry ( ) {
		return ObservationRegistry.create ( ) ;
	}

	@Bean
	public ToolCallbackResolver toolCallbackResolver ( ApplicationContext ctx ) {
		return new SpringBeanToolCallbackResolver ( ( GenericApplicationContext ) ctx, SchemaType.JSON_SCHEMA ) ;
	}

	@Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
        retryTemplate.setBackOffPolicy(new ExponentialBackOffPolicy());
        return retryTemplate;
    }
/** /
//	@Bean
    @ConditionalOnProperty(name = "app.llm.provider", havingValue = "openai")
	@Primary
    public ChatModel openAiChatModel(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.chat.options.model:gpt-4}") String model,
            @Value("${app.llm.temperature:0.3}") Double temperature,
            ToolCallingManager toolCallingManager,
            RetryTemplate retryTemplate,
            ObservationRegistry observationRegistry) {
        
        OpenAiApi openAiApi = OpenAiApi.builder ( )
				.apiKey ( apiKey )
				.build ( ) ;
        
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .model(model)
            .temperature(temperature)
            .build();
            
        return new OpenAiChatModel(openAiApi, options, toolCallingManager, retryTemplate, observationRegistry);
    }
/**/
}
