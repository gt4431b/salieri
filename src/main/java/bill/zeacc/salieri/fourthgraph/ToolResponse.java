package bill.zeacc.salieri.fourthgraph;

import java.io.Serializable ;

public record ToolResponse ( String toolName, String output ) implements Serializable {
	private static final long serialVersionUID = 1L ;
}
