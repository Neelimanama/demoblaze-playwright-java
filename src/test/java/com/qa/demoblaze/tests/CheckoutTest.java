package com.qa.demoblaze.tests;

import com.qa.demoblaze.pages.CartPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("checkout")
@DisplayName("Checkout - place an order")
class CheckoutTest extends BaseTest {

    private static final String VALID_CARD = "4111111111111111";

    @Test
    @Tag("TC04")
    @Tag("smoke")
    @DisplayName("TC04 - A logged-in user places an order and receives an accurate confirmation")
    void loggedInUserPlacesOrderWithAccurateConfirmation() {
        logInAsNewUser();
        addToCart("Phones", "Nokia lumia 1520");
        addToCart("Monitors", "Apple monitor 24");
        CartPage cart = openCart();
        double cartTotal = sum(cart.items());

        placeOrder(Map.of(
                "name", "Jane Tester", "country", "United Kingdom", "city", "Cardiff",
                "card", VALID_CARD, "month", "09", "year", "2026"));

        assertThat(confirmation().root()).isVisible();
        assertThat(confirmation().heading()).hasText("Thank you for your purchase!");

        Map<String, String> details = confirmation().details();
        assertThat(details.get("Amount")).isEqualTo(format(cartTotal) + " USD");
        assertThat(details.get("Name")).isEqualTo("Jane Tester");
        assertThat(details.get("Card Number")).isEqualTo(VALID_CARD);
        assertThat(details.get("Id")).as("order id").matches("\\d+");

        confirmation().confirm();
        assertThat(page).hasURL(Pattern.compile(".*/index\\.html$"));
        assertThat(cart().open().rows()).hasCount(0);
    }

    @Test
    @Tag("TC04")
    @Tag("defect")
    @Tag("BUG-01")
    @DisplayName("TC04 - The order confirmation shows today's date")
    void confirmationShowsTodaysDate() {
        logInAsNewUser();
        addToCart("Phones", "Nokia lumia 1520");
        addToCart("Monitors", "Apple monitor 24");
        openCart();

        placeOrder("Jane Tester", VALID_CARD);
        assertThat(confirmation().root()).isVisible();

        LocalDate today = LocalDate.now();
        String expected = today.getDayOfMonth() + "/" + today.getMonthValue() + "/" + today.getYear();
        assertThat(confirmation().details().get("Date")).as("order date (d/m/yyyy)").isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}")
    @Tag("TC05")
    @Tag("negative")
    @DisplayName("TC05 - An order cannot be placed without mandatory details")
    @CsvSource(delimiter = '|', textBlock = """
            missing name | ''          | 4111111111111111
            missing card | Jane Tester | ''
            both missing | ''          | ''
            """)
    void orderNeedsMandatoryDetails(String caseName, String name, String card) {
        home().open();
        addToCart("Laptops", "Sony vaio i5");
        openCart();

        placeOrder(name, card);
        expectAlert("Please fill out Name and Creditcard.");
        assertNoPurchaseConfirmation();
    }

    @Test
    @Tag("TC05")
    @Tag("defect")
    @Tag("BUG-02")
    @DisplayName("TC05 - An order cannot be placed when the cart is empty")
    void orderCannotBePlacedWithEmptyCart() {
        home().open();
        assertThat(openCart().rows()).hasCount(0);

        placeOrder("Jane Tester", VALID_CARD);
        assertNoPurchaseConfirmation();
    }

    @ParameterizedTest(name = "{0}")
    @Tag("TC05")
    @Tag("defect")
    @Tag("BUG-03")
    @DisplayName("TC05 - An order is rejected when the card number is not valid")
    @CsvSource(delimiter = '|', textBlock = """
            letters   | not-a-card
            too short | 123
            """)
    void orderRejectedForInvalidCardNumber(String userName, String card) {
        home().open();
        addToCart("Laptops", "Sony vaio i5");
        openCart();

        placeOrder("Jane Tester", card);
        assertNoPurchaseConfirmation();
    }
}
