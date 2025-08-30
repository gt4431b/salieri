package bill.zeacc.salieri.fourthgraph ;

//SystemInfoTool.java
import org.springframework.stereotype.Component ;
import lombok.extern.slf4j.Slf4j ;
import java.net.InetAddress ;
import java.util.HashMap ;
import java.util.Map ;

@Component
@Slf4j
public class SystemInfoTool implements SpringTool {

	@Override
	public String getName ( ) {
		return "getSystemInfo" ;
	}

	@Override
	public String getDescription ( ) {
		return "Gets system information like hostname, OS, and Java version" ;
	}

	@Override
	public String execute ( String arguments ) {
		try {
			Map <String, String> info = new HashMap <> ( ) ;
			info.put ( "hostname", InetAddress.getLocalHost ( ).getHostName ( ) ) ;
			info.put ( "os", System.getProperty ( "os.name" ) ) ;
			info.put ( "java", System.getProperty ( "java.version" ) ) ;
			info.put ( "user", System.getProperty ( "user.name" ) ) ;
			return info.toString ( ) ;
		} catch ( Exception e ) {
			log.error ( "Error getting system info", e ) ;
			return "Error: " + e.getMessage ( ) ;
		}
	}
}
