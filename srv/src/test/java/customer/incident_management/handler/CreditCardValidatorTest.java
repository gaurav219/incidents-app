package customer.incident_management.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EmptySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for CreditCardValidator
 */
class CreditCardValidatorTest {

    @Test
    @DisplayName("Valid credit card numbers should pass validation")
    void testValidCreditCards() {
        // Valid 16-digit card starting with non-zero digit
        String validCard = "1234567890123456";
        assertTrue(CreditCardValidator.checkifValidCreditCard(validCard));
        
        // Valid 16-digit card with all different digits
        String validCard2 = "9876543210987654";
        assertTrue(CreditCardValidator.checkifValidCreditCard(validCard2));
    }
    
    @ParameterizedTest
    @DisplayName("Invalid credit card format should throw ValueError")
    @ValueSource(strings = {
        "0123456789012345", // Starts with zero
        "12345678901234",   // Too short (15 digits)
        "123456789012345678", // Too long (18 digits)
        "12345678901234x6", // Contains non-digits
        "1234-5678-9012-3456", // Contains hyphens
        "1234 5678 9012 3456"  // Contains spaces
    })
    void testInvalidCreditCardFormat(String invalidCard) {
        CreditCardValidator.ValueError exception = assertThrows(
            CreditCardValidator.ValueError.class,
            () -> CreditCardValidator.checkifValidCreditCard(invalidCard)
        );
        assertTrue(exception.getMessage().contains("Invalid credit card format"));
    }
    
    @ParameterizedTest
    @DisplayName("Null or empty input should throw ValueError")
    @NullSource
    @EmptySource
    void testNullOrEmptyCreditCard(String invalidCard) {
        CreditCardValidator.ValueError exception = assertThrows(
            CreditCardValidator.ValueError.class,
            () -> CreditCardValidator.checkifValidCreditCard(invalidCard)
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }
    
    @Test
    @DisplayName("isValid method should correctly validate against regex")
    void testIsValidMethod() {
        String regex = "^[1-9]\\d{15}$";
        
        // Valid cases
        assertTrue(CreditCardValidator.isValid("1234567890123456", regex));
        assertTrue(CreditCardValidator.isValid("9876543210987654", regex));
        
        // Invalid cases
        assertFalse(CreditCardValidator.isValid("0123456789012345", regex)); // Starts with zero
        assertFalse(CreditCardValidator.isValid("12345678901234", regex));   // Too short
        assertFalse(CreditCardValidator.isValid("123456789012345678", regex)); // Too long
        assertFalse(CreditCardValidator.isValid("12345678901234x6", regex)); // Non-digit
    }
    
    @Test
    @DisplayName("Credit card validation with mocked isValid method")
    void testCreditCardValidationWithMockedIsValid() {
        // Use a spy to mock the static method isValid
        try (var mockedValidator = mockStatic(CreditCardValidator.class, invocation -> {
            if (invocation.getMethod().getName().equals("isValid")) {
                return invocation.callRealMethod();
            }
            if (invocation.getMethod().getName().equals("checkifValidCreditCard")) {
                return invocation.callRealMethod();
            }
            return invocation.callRealMethod();
        })) {
            
            // Mock isValid to return true
            mockedValidator.when(() -> CreditCardValidator.isValid(eq("1234567890123456"), anyString())).thenReturn(true);
            
            // Test valid case with mocked isValid
            assertTrue(CreditCardValidator.checkifValidCreditCard("1234567890123456"));
            
            // Verify isValid was called with correct parameters
            mockedValidator.verify(() -> CreditCardValidator.isValid("1234567890123456", "^[1-9]\\d{15}$"));
            
            // Mock isValid to return false
            mockedValidator.when(() -> CreditCardValidator.isValid(eq("invalid"), anyString())).thenReturn(false);
            
            // Test invalid case with mocked isValid
            assertThrows(
                CreditCardValidator.ValueError.class,
                () -> CreditCardValidator.checkifValidCreditCard("invalid")
            );
        }
    }
}