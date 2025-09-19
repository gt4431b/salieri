package bill.zeacc.salieri.fifthgraph.model.meta ;

public interface InternalTool {

	public String getName ( ) ;

	public String getDescription ( ) ;

	public String executionSpec ( ) ;

	public ToolResponse execute ( String arguments ) ;
}
