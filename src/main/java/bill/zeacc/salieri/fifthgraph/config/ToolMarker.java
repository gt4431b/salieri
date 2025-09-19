package bill.zeacc.salieri.fifthgraph.config;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE ;
import static java.lang.annotation.RetentionPolicy.RUNTIME ;

import java.lang.annotation.Documented ;
import java.lang.annotation.Retention ;
import java.lang.annotation.Target ;


@Documented
@Retention ( RUNTIME )
@Target ( ANNOTATION_TYPE )
public @interface ToolMarker {

}
