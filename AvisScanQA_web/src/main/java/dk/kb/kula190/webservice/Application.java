package dk.kb.kula190.webservice;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import dk.kb.kula190.api.impl.DefaultApiServiceImpl;

import javax.ws.rs.ApplicationPath;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class Application extends javax.ws.rs.core.Application {

    public Set<Class<?>> getClasses() {
        return new HashSet<>(Arrays.asList(JacksonJsonProvider.class, DefaultApiServiceImpl.class));
    }
}

