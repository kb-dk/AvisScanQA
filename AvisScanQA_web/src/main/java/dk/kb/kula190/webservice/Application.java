package dk.kb.kula190.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import dk.kb.kula190.api.impl.DefaultApiServiceImpl;
import org.apache.cxf.jaxrs.provider.FormEncodingProvider;
import org.apache.cxf.jaxrs.provider.JavaTimeTypesParamConverterProvider;
import org.apache.cxf.jaxrs.provider.ServerProviderFactory;

import javax.ws.rs.ApplicationPath;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class Application extends javax.ws.rs.core.Application {
    
    public Set<Class<?>> getClasses() {
        return new HashSet<>(Arrays.asList(DefaultApiServiceImpl.class, ServiceExceptionMapper.class));
    }
    
    
    @Override
    public Set<Object> getSingletons() {
    
    
        return Set.of(getJsonProviderWithDateTimes());
    }
    
    
    public static JacksonJaxbJsonProvider getJsonProviderWithDateTimes() {
        // see https://github.com/FasterXML/jackson-modules-java8
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        
        return new JacksonJaxbJsonProvider(mapper,
                                           JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
    }
    
}

