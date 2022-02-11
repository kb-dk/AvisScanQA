package dk.kb.kula190.checkers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
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

public class MixXmlSchemaChecker extends DecoratedEventHandler {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(MixXmlSchemaChecker.class);
    private Validator validator;
    private ParsingEvent parsingEvent;
    public MixXmlSchemaChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }

    @Override
    public void batchBegins(ParsingEvent event, String batch, String roundTrip, LocalDate startDate, LocalDate endDate) {
        try (InputStream schemaStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("MixSchema.xsd")) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(schemaStream));
            validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    log.warn("SAX warning", exception);
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    getResultCollector().addFailure(parsingEvent.getName(),
                            "Mix Schema",
                            MixXmlSchemaChecker.class.getSimpleName(),
                            "SAX error encountered",
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
    public void batchEnds(ParsingEvent event, String batch, String roundTrip, LocalDate startDate, LocalDate
            endDate) {
        super.batchEnds(event, batch, roundTrip, startDate, endDate);
    }

    @Override
    public synchronized void mixFile(AttributeParsingEvent event, String editionName, LocalDate editionDate, String
            udgave, String sectionName, Integer pageNumber) throws IOException {
        parsingEvent = event;
        try (InputStream stream = event.getData()) {

            validator.validate(new StreamSource(stream));
        } catch (SAXException e) {
            //Something wrong with the xml file - dump into resultcollector
            log.error("SAX error", e);
        } catch (IOException e) {
            // handle exception while reading source
            log.error("IO error", e);
        }


    }


}
