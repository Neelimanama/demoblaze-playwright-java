package com.qa.demoblaze.tests;

import com.qa.demoblaze.utils.TestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;



@Tag("account")
@DisplayName("User account - sign up, log in and log out")
class AccountTest extends BaseTest {

    @Test
    @Tag("TC01")
    @Tag("smoke")
    @DisplayName("TC01 - A new user can sign up, log in and log out")
    void newUserCanSignUpLogInAndLogOut() {
        TestUser user = logInAsNewUser();

        //Check the navbar shows the logged-in state
        assertThat(nav().welcomeUser()).isVisible();
        assertThat(nav().welcomeUser()).hasText("Welcome " + user.username());
        assertThat(nav().logoutLink()).isVisible();
        assertThat(nav().loginLink()).isHidden();
        assertThat(nav().signUpLink()).isHidden();

        //Log out and check the navbar is back to the logged-out state
        nav().logOut();
        assertLoggedOut();
    }

    /**
     * {existing} is replaced with a user registered at the start of the test; {unknown} with a
     * freshly generated username that has never been registered. '' is an empty value.
     */
    @ParameterizedTest(name = "{0}")
    @Tag("TC02")
    @Tag("negative")
    @DisplayName("TC02 - Login is rejected for invalid credentials")
    @CsvSource(delimiter = '|', textBlock = """
            wrong password      | {existing} | WrongPass!123 | Wrong password.
            user does not exist | {unknown}  | Anything!123  | User does not exist.
            blank username      | ''         | Anything!123  | Please fill out Username and Password.
            blank password      | {existing} | ''            | Please fill out Username and Password.
            """)
    void loginIsRejectedForInvalidCredentials(String caseName, String username, String password, String alert) {
        TestUser existing = signUpNewUser();
        home().open();

        String resolvedUsername = switch (username) {
            case "{existing}" -> existing.username();
            case "{unknown}" -> TestUser.unique().username();
            default -> username;
        };
        nav().openLogin().enterCredentials(resolvedUsername, password).submit();

        expectAlert(alert);
        assertLoggedOut();
    }

    // Checks the navbar shows the logged-out state
    private void assertLoggedOut() {
        assertThat(nav().loginLink()).isVisible();
        assertThat(nav().signUpLink()).isVisible();
        assertThat(nav().logoutLink()).isHidden();
        assertThat(nav().welcomeUser()).isHidden();
    }
}
