package com.qa.demoblaze.tests;

import com.microsoft.playwright.Page;
import com.qa.demoblaze.context.PlaywrightExtension;
import com.qa.demoblaze.context.TestContext;
import com.qa.demoblaze.pages.CartPage;
import com.qa.demoblaze.pages.HomePage;
import com.qa.demoblaze.pages.NavBar;
import com.qa.demoblaze.pages.OrderConfirmation;
import com.qa.demoblaze.pages.ProductPage;
import com.qa.demoblaze.pages.SignUpModal;
import com.qa.demoblaze.utils.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Common setup and reusable user actions for all tests. Each test gets a fresh browser context
 * from {@link PlaywrightExtension}; a new test class instance is created per test, so the fields
 * below never leak between tests (and tests can run in parallel).
 */
@ExtendWith(PlaywrightExtension.class)
public abstract class BaseTest {

    protected TestContext ctx;
    protected Page page;

    /** Product title -> price shown on the product page when it was added to the cart. */
    protected final Map<String, Double> addedProducts = new LinkedHashMap<>();
    /** Product title -> price shown on its card in the category listing. */
    protected final Map<String, Double> listingPrices = new LinkedHashMap<>();

    @BeforeEach
    void injectContext(TestContext ctx) {
        this.ctx = ctx;
        this.page = ctx.page();
    }

    // ---------- page objects ----------

    protected HomePage home() {
        return new HomePage(page);
    }

    protected NavBar nav() {
        return new NavBar(page);
    }

    protected CartPage cart() {
        return new CartPage(page);
    }

    protected OrderConfirmation confirmation() {
        return new OrderConfirmation(page);
    }

    // ---------- reusable actions ----------

    /** Waits for the next native alert and checks its message*/
    protected void expectAlert(String expectedMessage) {
        String actualMessage = ctx.dialogs().waitForNext();
        assertEquals(expectedMessage, actualMessage, "Browser alert text");
    }

    /** Registers a new user through sign-up modal*/
    protected TestUser signUpNewUser() {
        TestUser user = TestUser.unique();

        home().open();
        SignUpModal signUp = nav().openSignUp();
        signUp.enterCredentials(user.username(), user.password());
        signUp.submit();

        expectAlert("Sign up successful.");
        assertThat(signUp.root()).isHidden();
        return user;
    }

    /** Logs in and checks the "Welcome <username>" text appears*/
    protected void logIn(TestUser user) {
        nav().openLogin()
             .enterCredentials(user.username(), user.password())
             .submitExpectingSuccess();
        assertThat(nav().welcomeUser()).hasText("Welcome " + user.username());
    }

    /** Creates a new user and logs in with it*/
    protected TestUser logInAsNewUser() {
        TestUser user = signUpNewUser();
        logIn(user);
        return user;
    }

    /** Adds a product to the cart and remembers its prices for later */
    protected void addToCart(String category, String product) {
        //Open the category and note the price in the list
        HomePage home = home().open().selectCategory(category);
        listingPrices.put(product, home.listingPrice(product));

        //Open the product page and note its price
        ProductPage productPage = home.openProduct(product);
        assertThat(productPage.title()).hasText(product);
        double price = productPage.price();

        //Add to cart and check the alert
        productPage.addToCart();
        String alertMessage = ctx.dialogs().waitForNext();
        assertTrue(alertMessage.startsWith("Product added"),
                "Unexpected alert after adding to cart: " + alertMessage);

        //same product added twice
        double previousTotal = addedProducts.getOrDefault(product, 0.0);
        addedProducts.put(product, previousTotal + price);
    }

    /** Opens the cart and wait until all products are shown */
    protected CartPage openCart() {
        CartPage cartPage = cart().open();
        assertThat(cartPage.rows()).hasCount(addedProducts.size());
        return cartPage;
    }

    protected void placeOrder(Map<String, String> details) {
        cart().placeOrder().fill(details).purchase();
    }

    protected void placeOrder(String name, String card) {
        placeOrder(Map.of("name", name, "card", card));
    }


    protected void assertNoPurchaseConfirmation() {
        page.waitForTimeout(1_000);
        assertThat(confirmation().root()).isHidden();
    }

    // ---------- helpers ----------

    // Adds up all prices in the map
    protected static double sum(Map<String, Double> items) {
        double total = 0;
        for (double price : items.values()) {
            total = total + price;
        }
        return total;
    }

    /** The site renders whole-number prices without decimals, e.g. 1220 not 1220.0. */
    protected static String format(double amount) {
        BigDecimal rounded = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        return rounded.stripTrailingZeros().toPlainString();
    }
}
