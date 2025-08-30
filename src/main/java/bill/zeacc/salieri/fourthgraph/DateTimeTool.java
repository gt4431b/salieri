package bill.zeacc.salieri.fourthgraph ;

//DateTimeTool.java
import org.springframework.stereotype.Component ;
import lombok.extern.slf4j.Slf4j ;
import java.time.ZonedDateTime ;
import java.time.ZoneId ;
import java.time.format.DateTimeFormatter ;

@Component
@Slf4j
public class DateTimeTool implements SpringTool {

	@Override
	public String getName ( ) {
		return "getDateTime" ;
	}

	@Override
	public String getDescription ( ) {
		return "Gets current date and time" ;
	}

	@Override
	public String execute ( String arguments ) {
		try {
			ZonedDateTime now = ZonedDateTime.now ( ZoneId.systemDefault ( ) ) ;
			return now.format ( DateTimeFormatter.RFC_1123_DATE_TIME ) ;
		} catch ( Exception e ) {
			log.error ( "Error getting date/time", e ) ;
			return "Error: " + e.getMessage ( ) ;
		}
	}
}
