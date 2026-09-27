package com.qa.demoblaze.tests;

import com.qa.demoblaze.pages.CartPage;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("cart")
@DisplayName("Shopping cart")
class CartTest extends BaseTest {

    @Test
    @Tag("TC03")
    @Tag("smoke")
    @DisplayName("TC03 - cart has correct total")
    void cartHasCorrectTotal() {
        home().open();

        addToCart("Phones", "Samsung galaxy s6");
        addToCart("Laptops", "MacBook Pro");
        addToCart("Monitors", "ASUS Full HD");

        CartPage cart = openCart();

        assertThat(cart.items()).containsExactlyInAnyOrderEntriesOf(addedProducts);
        assertThat(cart.total()).hasText(format(sum(cart.items())));

        cart.delete("MacBook Pro");
        addedProducts.remove("MacBook Pro");

        assertThat(cart.rows()).hasCount(2);
        assertThat(cart.items()).doesNotContainKey("MacBook Pro");
        assertThat(cart.total()).hasText(format(sum(cart.items())));
    }
}
