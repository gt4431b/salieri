package bill.zeacc.salieri.fifthgraph.agents.hello.config;

import static java.lang.annotation.ElementType.TYPE ;
import static java.lang.annotation.RetentionPolicy.RUNTIME ;

import java.lang.annotation.Documented ;
import java.lang.annotation.Retention ;
import java.lang.annotation.Target ;

import bill.zeacc.salieri.fifthgraph.config.NodeMarker ;


@Retention ( RUNTIME )
@Target ( TYPE )
@NodeMarker
@Documented
public @interface HelloNode {

}
