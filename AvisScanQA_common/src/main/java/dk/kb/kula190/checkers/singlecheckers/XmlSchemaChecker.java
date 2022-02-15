package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandlerWithSections;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

public class XmlSchemaChecker extends DecoratedEventHandlerWithSections {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(XmlSchemaChecker.class);
    public Validator[] validators = {null,null};
    private ParsingEvent parsingEvent;
    public XmlSchemaChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }

    @Override
    public void batchBegins(NodeParsingEvent event, String batch, String roundTrip, LocalDate startDate, LocalDate endDate) {
        try (InputStream schemaStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("MetadataSchema/MixSchema.xsd");
             InputStream schemaStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("MetadataSchema/AltoSchema.xsd")) {
            setErrorHandler("Mix",schemaStream, 0);
            setErrorHandler("Alto",schemaStream2,1);


        } catch (IOException | SAXException e) {
            getResultCollector().addFailure(event.getName(),
                    EventRunner.EXCEPTION,
                    this.getClass().getSimpleName(),
                    EventRunner.UNEXPECTED_ERROR + e,
                    Arrays.stream(e.getStackTrace())
                            .map(StackTraceElement::toString)
                            .collect(Collectors.joining("\n")));
        }

    }



    @Override
    public synchronized void mixFile(AttributeParsingEvent event, String editionName, LocalDate editionDate, String
            udgave, String sectionName, Integer pageNumber) throws IOException {
        parsingEvent = event;
        try (InputStream stream = event.getData()) {

            validators[0].validate(new StreamSource(stream));
        } catch (SAXException e) {
            //Something wrong with the xml file - dump into resultcollector
            log.error("SAX error", e);
        } catch (IOException e) {
            // handle exception while reading source
            log.error("IO error", e);
        }


    }

    @Override
    public void altoFile(AttributeParsingEvent event, String editionName, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) throws IOException {
        parsingEvent = event;
        try (InputStream stream = event.getData()) {

            validators[1].validate(new StreamSource(stream));
        } catch (SAXException e) {
            //Something wrong with the xml file - dump into resultcollector
            log.error("SAX error", e);
        } catch (IOException e) {
            // handle exception while reading source
            log.error("IO error", e);
        }
    }

    public void setErrorHandler(String name, InputStream stream, int validatorIndex) throws SAXException {

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(stream));

        validators[validatorIndex] = schema.newValidator();
        validators[validatorIndex].setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                log.warn(name+" warning", exception);
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                getResultCollector().addFailure(parsingEvent.getName(),
                        name+" Schema",
                        XmlSchemaChecker.class.getSimpleName(),
                        name+" error encountered",
                        exception.getMessage());
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                getResultCollector().addFailure(parsingEvent.getName(),
                        EventRunner.EXCEPTION,
                        this.getClass().getSimpleName(),
                        EventRunner.UNEXPECTED_ERROR + exception,
                        Arrays.stream(exception.getStackTrace())
                                .map(StackTraceElement::toString)
                                .collect(Collectors.joining("\n")));

            }
        });
    }


}
