package ru.netology.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.data.DataHelper;
import ru.netology.page.DashboardPage;
import ru.netology.page.LoginPage;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneyTransferTest {
    DashboardPage dashboardPage;
    DataHelper.CardInfo firstCardInfo;
    DataHelper.CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setUp() {
        var loginPage = open("http:localhost:9999", LoginPage.class);
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCode();
        dashboardPage = verificationPage.validVerify(verificationCode);
        firstCardInfo = DataHelper.getFirstCardInfo();
        secondCardInfo = DataHelper.getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(firstCardInfo);
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
    }

    @Test
    void shouldTransferFromFirstToSecond() {
        var amount = DataHelper.generateValidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance - amount;
        var expectedBalanceSecondCard = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        dashboardPage.reloadDashboardPage();
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertAll(() -> assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
                () -> assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard));
    }

    @Test
    void shouldGetErrorMessageIfAmountMoreBalance() {
        var amount = DataHelper.generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);
        assertAll(() -> transferPage.findErrorMessage("Выполнена попытка перевода суммы, превышающей остаток на карте списания"),
                () -> dashboardPage.reloadDashboardPage(),
                () -> assertEquals(firstCardBalance, dashboardPage.getCardBalance(firstCardInfo)),
                        () -> assertEquals(secondCardBalance, dashboardPage.getCardBalance(secondCardInfo)));
    }

}