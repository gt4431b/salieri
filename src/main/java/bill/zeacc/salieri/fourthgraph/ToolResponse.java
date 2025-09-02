package bill.zeacc.salieri.fourthgraph;

import java.io.Serializable ;

import lombok.Getter ;
import lombok.Setter ;

@Getter
@Setter
public class ToolResponse implements Serializable {
	private static final long serialVersionUID = 1L ;

	private String id ;
	private String toolName ;
	private String output ;

	public ToolResponse ( String toolExecutionId, String name, String result ) {
		this.id = toolExecutionId ;
		this.toolName = name ;
		this.output = result ;
	}
}
