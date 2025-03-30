package customer.incident_management.handler;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for credit card validation
 */
public class CreditCardValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(CreditCardValidator.class);
    
    /**
     * Validates a credit card number
     * 
     * @param card_no The credit card number to validate
     * @return true if the card is valid, false otherwise
     * @throws ValueError if the input is invalid
     */
    public static boolean checkifValidCreditCard(String card_no) {
        logger.debug("Validating credit card: {}", maskCardNumber(card_no));
        
        try {
            if (card_no == null || card_no.isEmpty()) {
                logger.error("Credit card validation failed: Card number is null or empty");
                throw new ValueError("Credit card number cannot be null or empty");
            }
            
            String regex = "^1\\d{15}$";
            
            boolean isValidFormat = isValid(card_no, regex);
            
            if (!isValidFormat) {
                logger.error("Credit card validation failed: Invalid format");
                throw new ValueError("Invalid credit card format. Card must start with digit 1 and be 16 digits in length");
            }
            
            logger.info("Credit card validation successful");
            return true;
        } catch (Exception e) {
            if (e instanceof ValueError) {
                throw e;
            }
            logger.error("Unexpected error during credit card validation", e);
            throw new ValueError("Credit card validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a credit card number matches the given regex pattern
     * 
     * @param card_no The credit card number to check
     * @param regex The regex pattern to match against
     * @return true if the card number matches the pattern, false otherwise
     */
    public static boolean isValid(String card_no, String regex) {
        logger.debug("Checking card format against regex");
        return Pattern.matches(regex, card_no);
    }
    
    /**
     * Custom exception for validation errors
     */
    public static class ValueError extends RuntimeException {
        public ValueError(String message) {
            super(message);
        }
    }
    
    /**
     * Masks a credit card number for secure logging
     * Only shows the last 4 digits
     */
    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "[INVALID CARD]";
        }
        
        int length = cardNumber.length();
        return "XXXX-XXXX-XXXX-" + cardNumber.substring(length - 4);
    }
}