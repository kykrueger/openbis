import ch.systemsx.cisd.openbis.generic.server.ComponentNames as ComponentNames
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider as CommonServiceProvider

def process(context, parameters):
    method = parameters.get("method");
    result = None;
    
    if method == "freeze":
        result = "OK";
    
    return result;