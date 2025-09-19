package bill.zeacc.salieri.fifthgraph.model.codeir;

import java.util.HashMap ;
import java.util.Map ;

import lombok.Data ;

@Data
public class Codebase {

	private String name ;
	private String rootPath ;
	private String mainLanguage ;
	private String buildTech ;
	private Map <String, String> coreTechnologyVersions = new HashMap <> ( ) ;
	private String sandboxRootPath ;
}
