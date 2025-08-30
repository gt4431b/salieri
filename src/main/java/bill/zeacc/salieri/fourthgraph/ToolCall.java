package bill.zeacc.salieri.fourthgraph ;

//ToolCall.java - Serializable wrapper for tool calls
import java.io.Serializable ;

public class ToolCall implements Serializable {

	private static final long serialVersionUID = 1L ;

	private String name ;
	private String arguments ;

	public ToolCall ( ) {
	}

	public ToolCall ( String name, String arguments ) {
		this.name = name ;
		this.arguments = arguments ;
	}

	public String getName ( ) {
		return name ;
	}

	public void setName ( String name ) {
		this.name = name ;
	}

	public String getArguments ( ) {
		return arguments ;
	}

	public void setArguments ( String arguments ) {
		this.arguments = arguments ;
	}

	@Override
	public String toString ( ) {
		return "ToolCall{name='" + name + "', arguments='" + arguments + "'}" ;
	}
}
