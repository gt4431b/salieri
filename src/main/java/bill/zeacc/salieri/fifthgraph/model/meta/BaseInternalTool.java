package bill.zeacc.salieri.fifthgraph.model.meta;

import java.io.IOException ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.UUID ;

import org.springframework.beans.factory.InitializingBean ;

import com.fasterxml.jackson.databind.ObjectMapper ;

import lombok.Getter ;
import lombok.Setter ;

public abstract class BaseInternalTool implements InternalTool, InitializingBean {

	private ObjectMapper om = new ObjectMapper ( ) ;
	private ToolArgumentSpec [] arguments ;

	@Override
	public void afterPropertiesSet ( ) throws Exception {
		init ( ) ;
	}

	@Override
	public String getName() {
		return "baseTool";
	}

	@Override
	public String getDescription() {
		return "Base tool implementation";
	}

	@Override
	public final ToolResponse execute(String arguments) {
		String toolExecutionId = "unknown" ;
		try {
			@SuppressWarnings ( "unchecked" )
			Map <String, Object> argsMap = om.readValue ( arguments, Map.class ) ;
			argsMap = validateArguments ( argsMap ) ;
			toolExecutionId = getToolExecutionId ( argsMap ) ;
			String result = doExecute ( toolExecutionId, argsMap ) ;
			return response ( toolExecutionId, result ) ;
		} catch ( IOException e ) {
			return response ( toolExecutionId, "Error: " + e.getMessage ( ) ) ;
		}
	}

	protected ToolResponse response ( String toolExecutionId, String result ) {
		return new ToolResponse ( toolExecutionId, getName ( ), result ) ;
	}

	public abstract String doExecute ( String toolExecutionId, Map <String, Object> argsMap ) throws IOException ;

	protected String getToolExecutionId (Map <String, Object> argsMap ) {
		return UUID.randomUUID ( ).toString ( ) ;
	}

	protected Map <String, Object> validateArguments ( Map <String, Object> argsMap ) {
		Map <String, Object> convertedObjects = new HashMap <> ( ) ;
		for ( ToolArgumentSpec arg : arguments ) {
			if ( arg.isRequired ( ) && ! argsMap.containsKey ( arg.getArgName ( ) ) ) {
				throw new IllegalArgumentException ( "Missing required argument: " + arg.getArgName ( ) ) ;
			}
			Object value = argsMap.get ( arg.getArgName ( ) ) ;
			String expectedType = arg.getType ( ) ;
			value = convert ( value, expectedType ) ;
			if ( value == null ) {
				throw new IllegalArgumentException ( "Invalid type for argument: " + arg.getArgName ( ) + ". Cannot convert to " + expectedType ) ;
			} else {
				convertedObjects.put ( arg.getArgName ( ), value ) ;
			}
		}
		return convertedObjects ;
	}

	protected Object convert ( Object value, String expectedType ) {
		if ( "string".equals ( expectedType ) ) {
			return value == null ? null : value.toString ( ) ;
		}
		// Add more type conversions as needed
		return null ;
	}

	@Getter
	@Setter
	public static class ToolInvocationSpec {
		private String invocation ;
		private ToolArgumentSpec [] args ;
	}

	@Getter
	@Setter
	public static class ToolArgumentSpec {
		private String argName ;
		private String type ;
		private String description ;
		private boolean required ;

		public Class <?> getTypeClass ( ) throws ClassNotFoundException {
			return Class.forName ( type ) ;
		}

		public boolean isRequired ( ) {
			return required ;
		}
	}

	protected void init ( ) throws Exception {
		String spec = executionSpec ( ) ;
		ToolInvocationSpec inv = om.readValue ( spec, ToolInvocationSpec.class ) ;
		arguments = inv.getArgs ( ) ;
		if ( arguments == null ) {
			arguments = new ToolArgumentSpec [ 0 ] ;
		}
	}
}
