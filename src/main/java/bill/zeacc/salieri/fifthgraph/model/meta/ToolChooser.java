package bill.zeacc.salieri.fifthgraph.model.meta;

import java.util.List ;
import java.util.function.Supplier ;

@FunctionalInterface
public interface ToolChooser extends Supplier <List <InternalTool> > {

}
