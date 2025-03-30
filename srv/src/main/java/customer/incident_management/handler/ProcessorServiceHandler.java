package customer.incident_management.handler;

import cds.gen.processorservice.Incidents;
import cds.gen.processorservice.ProcessorService_;
import cds.gen.sap.capire.incidents.*;
import com.sap.cds.ql.Select;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Locale;

@Component
@ServiceName(ProcessorService_.CDS_NAME)
public class ProcessorServiceHandler implements EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorServiceHandler.class);

    private final PersistenceService db;

    public ProcessorServiceHandler(PersistenceService db) {
        this.db = db;
    }

    /*
     * Change the urgency of an incident to "high" if the title contains the word
     * "urgent"
     */
    @Before(event = CqnService.EVENT_CREATE)
    public void ensureHighUrgencyForIncidentsWithUrgentInTitle(List<Incidents> incidents) {
        for (Incidents incident : incidents) {
            if (incident.getTitle().toLowerCase(Locale.ENGLISH).contains("urgent") &&
                    incident.getUrgencyCode() == null || !incident.getUrgencyCode().equals("H")) {
                incident.setUrgencyCode("H");
                logger.info("Adjusted Urgency for incident '{}' to 'HIGH'.", incident.getTitle());
            }

        }
    }

    /*
     * Handler to avoid updating a "closed" incident
     */
    @Before(event = CqnService.EVENT_UPDATE)
    public void ensureNoUpdateOnClosedIncidents(Incidents incident) {
        Incidents in = db.run(Select.from(Incidents_.class).where(i -> i.ID().eq(incident.getId())))
                .single(Incidents.class);
        // Customers cust = db.run(Select.from(Customers_.class).where(i ->
        // i.ID().eq(incident.getId())))
        // .single(Incidents.class);
        // Customers customer = db.run(
        // Select.from(Customers_.class)
        // .join(Incidents_.class)
        // .on(i -> i.CustomerID().eq(Customer.ID())) // Join condition
        // .where(i -> i.ID().eq(incident.getId())) // Filter by incident ID
        // ).single(Customers.class);

        Customers customer = db.run(Select.from(Customers_.class).where(c -> c.ID().eq(incident.getCustomerId())))
                .single(Customers.class);

        validateCreditCardBeforeCreate(customer);
        // System.out.println(customer + " customer123");

        // System.out.println(in + " incident");
        // Customers customers = db.run(Select.from(Customers_.class).where(cust ->
        // cust.ID().eq(incident.getId())))
        // .single(Incidents.class);

        if (in.getStatusCode().equals("C")) {
            throw new ServiceException(ErrorStatuses.CONFLICT, "Can't modify a closed incident");
        }

    }

    public void validateCreditCardBeforeCreate(Customers customers) {
        // for (Customers customer : customers) {
        Customers customer = customers;
        logger.info("Validating credit card for new customer: {}", customer.getName());

        String creditCardNo = customer.getCreditCardNo();

        try {
            // Using the existing CreditCardValidator to validate the credit card
            if (creditCardNo != null && !creditCardNo.isEmpty()) {
                boolean isValid = CreditCardValidator.checkifValidCreditCard(creditCardNo);

                if (!isValid) {
                    // This shouldn't normally happen as the validator throws exceptions for invalid
                    // cards
                    // but adding as a safeguard
                    throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                            "Credit card validation failed for customer " + customer.getName());
                }

                logger.info("Credit card validation successful for customer: {}", customer.getName());
            } else {
                logger.warn("No credit card provided for customer: {}", customer.getName());
                // Depending on business requirements, you might want to:
                // - Allow customers without credit cards (do nothing)
                // - Require credit cards (throw an exception)
                // - Flag the customer record in some way

                // For this implementation, we'll require a credit card
                throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                        "Credit card is required for new customers");
            }

        } catch (CreditCardValidator.ValueError e) {
            // Convert the validator's custom exception to a service exception
            logger.error("Credit card validation failed: {}", e.getMessage());
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                    "Invalid credit card: " + e.getMessage());
        }
    }
}
