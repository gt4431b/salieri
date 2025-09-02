package bill.zeacc.salieri.fourthgraph ;

//ToolCall.java - Serializable wrapper for tool calls
import java.io.Serializable ;

public class ToolCall implements Serializable {

	private static final long serialVersionUID = 1L ;

	private String id ;
	private String name ;
	private String arguments ;

	public ToolCall ( ) {
	}

	public ToolCall ( String id, String name, String arguments ) {
		this.id = id ;
		this.name = name ;
		this.arguments = arguments ;
	}

	public String getId ( ) {
		return id ;
	}

	public String getName ( ) {
		return name ;
	}

	public void setId ( String id ) {
		this.id = id ;
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
		return "ToolCall{id='" + id + "', name='" + name + "', arguments='" + arguments + "'}" ;
	}
}
