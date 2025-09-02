package bill.zeacc.salieri.fourthgraph;


public interface SpringTool {
	public String getName();
	public String getDescription();
    public String executionSpec ( ) ;
    public ToolResponse execute(String arguments);
}
