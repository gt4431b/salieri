package bill.zeacc.salieri.thirdgraph;

import java.io.Serializable ;
import java.util.List ;
import java.util.Map ;
import java.util.stream.Collectors ;

import dev.langchain4j.agent.tool.ToolExecutionRequest ;
import dev.langchain4j.agent.tool.ToolSpecification ;
import lombok.Getter ;
import lombok.Setter ;

@Getter
@Setter
public class ToolCall implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String arguments;
    private String id;
    private String toolDescription ;
	private String originalQuery ;
    
    public ToolCall() {} // Default constructor for serialization
    
    public ToolCall(String name, String arguments, String id, String originalQuery, String toolDescription) {
        this.name = name;
        this.arguments = arguments;
        this.id = id;
        this.originalQuery = originalQuery;
        this.toolDescription = toolDescription ;
    }
    
    public static ToolCall from(ToolExecutionRequest request, String originalQuery, List <ToolSpecification> tools) {
    	Map <String, ToolSpecification> specs = tools.stream ( ).collect ( Collectors.toMap ( ToolSpecification::name, t -> t ) ) ;
    	ToolSpecification spec = specs.get ( request.name ( ) ) ;
    	StringBuilder q = new StringBuilder ( ) ;
    	if ( spec != null ) {
    		q.append ( spec.description ( ) ) ;
    	}
    	q.append ( "  User original query, for context: " ).append ( originalQuery ) ;
        return new ToolCall(
            request.name(),
            request.arguments(),
            request.id(),
            q.toString ( ),
            spec != null ? spec.description() : "No description available"
        );
    }
    public ToolExecutionRequest toExecutionRequest() {
        return ToolExecutionRequest.builder()
            .id(this.id)
            .name(this.name)
            .arguments(this.arguments)
            .build();
    }

    @Override
    public String toString() {
        return "ToolCall{name='" + name + "', arguments='" + arguments + "', id='" + id + "'}";
    }
}
