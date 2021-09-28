package com.codeborne.selenide;

import com.codeborne.selenide.proxy.SelenideProxyServer;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static com.codeborne.selenide.CheckResult.Action.ACCEPT;
import static com.codeborne.selenide.CheckResult.Action.CONTINUE;
import static com.codeborne.selenide.Condition.and;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.attributeMatching;
import static com.codeborne.selenide.Condition.be;
import static com.codeborne.selenide.Condition.checked;
import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.cssValue;
import static com.codeborne.selenide.Condition.disabled;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.have;
import static com.codeborne.selenide.Condition.hidden;
import static com.codeborne.selenide.Condition.id;
import static com.codeborne.selenide.Condition.name;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.or;
import static com.codeborne.selenide.Condition.selected;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.type;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Mocks.elementWithAttribute;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class ConditionTest {
  private final WebDriver webDriver = new DummyWebDriver();
  private final SelenideProxyServer proxy = mock(SelenideProxyServer.class);
  private final SelenideConfig config = new SelenideConfig();
  private final Driver driver = new DriverStub(config, new Browser("opera", false), webDriver, proxy);

  @Test
  void displaysHumanReadableName() {
    assertThat(visible).hasToString("visible");
    assertThat(hidden).hasToString("hidden");
    assertThat(attribute("lastName", "Malkovich")).hasToString("attribute lastName=\"Malkovich\"");
  }

  @Test
  void value() {
    WebElement element = elementWithAttribute("value", "John Malkovich");
    assertThat(Condition.value("Peter").check(driver, element).action).isEqualTo(CONTINUE);
    assertThat(Condition.value("john").check(driver, element).action).isEqualTo(ACCEPT);
    assertThat(Condition.value("john malkovich").check(driver, element).action).isEqualTo(ACCEPT);
    assertThat(Condition.value("John").check(driver, element).action).isEqualTo(ACCEPT);
    assertThat(Condition.value("John Malkovich").check(driver, element).action).isEqualTo(ACCEPT);
    assertThat(Condition.value("malko").check(driver, element).action).isEqualTo(ACCEPT);
  }

  @Test
  void valueToString() {
    assertThat(Condition.value("John Malkovich"))
      .hasToString("value 'John Malkovich'");
  }

  @Test
  void elementIsVisible() {
    assertThat(visible.check(driver, elementWithVisibility(true)).action).isEqualTo(ACCEPT);
    assertThat(visible.check(driver, elementWithVisibility(false)).action).isEqualTo(CONTINUE);
  }

  private WebElement elementWithVisibility(boolean isVisible) {
    WebElement element = mock(WebElement.class);
    when(element.isDisplayed()).thenReturn(isVisible);
    return element;
  }

  @Test
  void elementExists() {
    assertThat(exist.check(driver, elementWithVisibility(true)).action).isEqualTo(ACCEPT);
    assertThat(exist.check(driver, elementWithVisibility(false)).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementExists_returnsFalse_ifItThrowsException() {
    WebElement element = mock(WebElement.class);
    when(element.isDisplayed()).thenThrow(new StaleElementReferenceException("ups"));
    assertThat(exist.check(driver, element).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementIsHidden() {
    assertThat(hidden.check(driver, elementWithVisibility(false)).action).isEqualTo(ACCEPT);
    assertThat(hidden.check(driver, elementWithVisibility(true)).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementIsHiddenWithStaleElementException() {
    WebElement element = mock(WebElement.class);
    doThrow(new StaleElementReferenceException("Oooops")).when(element).isDisplayed();
    assertThat(hidden.check(driver, element).action).isEqualTo(ACCEPT);
  }

  @Test
  void elementHasAttribute() {
    assertThat(attribute("name").check(driver, elementWithAttribute("name", "selenide")).action).isEqualTo(ACCEPT);
    assertThat(attribute("name").check(driver, elementWithAttribute("name", "")).action).isEqualTo(ACCEPT);
    assertThat(attribute("name").check(driver, elementWithAttribute("id", "id3")).action).isEqualTo(ACCEPT);
  }

  @Test
  void elementHasAttributeWithGivenValue() {
    assertThat(attribute("name", "selenide").check(driver, elementWithAttribute("name", "selenide")).action).isEqualTo(ACCEPT);
    assertThat(attribute("name", "selenide").check(driver, elementWithAttribute("name", "selenide is great")).action).isEqualTo(CONTINUE);
    assertThat(attribute("name", "selenide").check(driver, elementWithAttribute("id", "id2")).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementHasAttributeMatching() {
    assertThat(attributeMatching("name", "selenide").check(driver, elementWithAttribute("name", "selenide")).action).isEqualTo(ACCEPT);
    assertThat(attributeMatching("name", "selenide.*").check(driver, elementWithAttribute("name", "selenide is great")).action).isEqualTo(ACCEPT);
    assertThat(attributeMatching("name", "selenide.*").check(driver, elementWithAttribute("id", "selenide")).action).isEqualTo(CONTINUE);
    assertThat(attributeMatching("name", "value.*").check(driver, elementWithAttribute("name", "another value")).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementHasValue() {
    assertThat(Condition.value("selenide").check(driver, elementWithAttribute("value", "selenide")).action).isEqualTo(ACCEPT);
    assertThat(Condition.value("selenide").check(driver, elementWithAttribute("value", "selenide is great")).action).isEqualTo(ACCEPT);
    assertThat(Condition.value("selenide").check(driver, elementWithAttribute("value", "is great")).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementHasName() {
    assertThat(name("selenide").check(driver, elementWithAttribute("name", "selenide")).action).isEqualTo(ACCEPT);
    assertThat(name("selenide").check(driver, elementWithAttribute("name", "selenide is great")).action).isEqualTo(CONTINUE);
  }

  @Test
  void checksValueOfTypeAttribute() {
    assertThat(type("radio").check(driver, elementWithAttribute("type", "radio")).action).isEqualTo(ACCEPT);
    assertThat(type("radio").check(driver, elementWithAttribute("type", "radio-button")).action).isEqualTo(CONTINUE);
  }

  @Test
  void checksValueOfIdAttribute() {
    assertThat(id("selenide").check(driver, elementWithAttribute("id", "selenide")).action).isEqualTo(ACCEPT);
    assertThat(id("selenide").check(driver, elementWithAttribute("id", "selenide is great")).action).isEqualTo(CONTINUE);
  }

  @Test
  void checksValueOfClassAttribute() {
    assertThat(cssClass("btn").check(driver, elementWithAttribute("class", "btn btn-warning")).action).isEqualTo(ACCEPT);
    assertThat(cssClass("btn-warning").check(driver, elementWithAttribute("class", "btn btn-warning")).action).isEqualTo(ACCEPT);
    assertThat(cssClass("active").check(driver, elementWithAttribute("class", "btn btn-warning")).action).isEqualTo(CONTINUE);
    assertThat(cssClass("").check(driver, elementWithAttribute("class", "btn btn-warning active")).action).isEqualTo(CONTINUE);
    assertThat(cssClass("active").check(driver, elementWithAttribute("href", "no-class")).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementHasCssValue() {
    assertThat(cssValue("display", "none").check(driver, elementWithCssStyle("display", "none")).action).isEqualTo(ACCEPT);
    assertThat(cssValue("font-size", "24").check(driver, elementWithCssStyle("font-size", "20")).action).isEqualTo(CONTINUE);
  }

  private WebElement elementWithCssStyle(String propertyName, String value) {
    WebElement element = mock(WebElement.class);
    when(element.getCssValue(propertyName)).thenReturn(value);
    return element;
  }

  @Test
  void elementHasClassToString() {
    assertThat(cssClass("Foo")).hasToString("css class 'Foo'");
  }

  @Test
  void elementEnabled() {
    assertThat(enabled.check(driver, elementWithEnabled(true)).action).isEqualTo(ACCEPT);
    assertThat(enabled.check(driver, elementWithEnabled(false)).action).isEqualTo(CONTINUE);
  }

  private WebElement elementWithEnabled(boolean isEnabled) {
    WebElement element = mock(WebElement.class);
    when(element.isEnabled()).thenReturn(isEnabled);
    return element;
  }

  @Test
  void elementEnabledActualValue() {
    assertThat(enabled.actualValue(driver, elementWithEnabled(true))).isEqualTo("enabled");
    assertThat(enabled.actualValue(driver, elementWithEnabled(false))).isEqualTo("disabled");
  }

  @Test
  void elementDisabled() {
    assertThat(disabled.check(driver, elementWithEnabled(false)).action).isEqualTo(ACCEPT);
    assertThat(disabled.check(driver, elementWithEnabled(true)).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementDisabledActualValue() {
    assertThat(disabled.actualValue(driver, elementWithEnabled(true))).isEqualTo("enabled");
    assertThat(disabled.actualValue(driver, elementWithEnabled(false))).isEqualTo("disabled");
  }

  @Test
  void elementSelected() {
    assertThat(selected.check(driver, elementWithSelected(true)).action).isEqualTo(ACCEPT);
    assertThat(selected.check(driver, elementWithSelected(false)).action).isEqualTo(CONTINUE);
  }

  private WebElement elementWithSelected(boolean isSelected) {
    WebElement element = mock(WebElement.class);
    when(element.isSelected()).thenReturn(isSelected);
    return element;
  }

  @Test
  void elementSelectedActualValue() {
    assertThat(selected.actualValue(driver, elementWithSelected(true))).isEqualTo("true");
    assertThat(selected.actualValue(driver, elementWithSelected(false))).isEqualTo("false");
  }

  @Test
  void elementChecked() {
    assertThat(checked.check(driver, elementWithSelected(true)).action).isEqualTo(ACCEPT);
    assertThat(checked.check(driver, elementWithSelected(false)).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementCheckedActualValue() {
    assertThat(checked.actualValue(driver, elementWithSelected(true))).isEqualTo("true");
    assertThat(checked.actualValue(driver, elementWithSelected(false))).isEqualTo("false");
  }

  @Test
  void elementNotCondition() {
    assertThat(not(checked).check(driver, elementWithSelected(false)).action).isEqualTo(ACCEPT);
    assertThat(not(checked).check(driver, elementWithSelected(true)).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementNotConditionActualValue() {
    assertThat(not(checked).actualValue(driver, elementWithSelected(false))).isEqualTo("false");
    assertThat(not(checked).actualValue(driver, elementWithSelected(true))).isEqualTo("true");
  }

  @Test
  void elementAndCondition() {
    WebElement element = mockElement(true, "text");
    assertThat(and("selected with text", be(selected), have(text("text"))).check(driver, element).action).isEqualTo(ACCEPT);
    assertThat(and("selected with text", not(be(selected)), have(text("text"))).check(driver, element).action).isEqualTo(CONTINUE);
    assertThat(and("selected with text", be(selected), have(text("incorrect"))).check(driver, element).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementAndConditionActualValue() {
    WebElement element = mockElement(false, "text");
    Condition condition = and("selected with text", be(selected), have(text("text")));
    assertThat(condition.actualValue(driver, element)).isNullOrEmpty();
    assertThat(condition.check(driver, element).action).isEqualTo(CONTINUE);
    assertThat(condition.actualValue(driver, element)).isEqualTo("false");
  }

  @Test
  void elementAndConditionToString() {
    WebElement element = mockElement(false, "text");
    Condition condition = and("selected with text", be(selected), have(text("text")));
    assertThat(condition).hasToString("selected with text: be selected and have text 'text'");
    assertThat(condition.check(driver, element).action).isEqualTo(CONTINUE);
    assertThat(condition).hasToString("selected with text: be selected and have text 'text'");
  }

  @Test
  void elementOrCondition() {
    WebElement element = mockElement(false, "text");
    when(element.isDisplayed()).thenReturn(true);
    assertThat(or("Visible, not Selected", visible, checked).check(driver, element).action).isEqualTo(ACCEPT);
    assertThat(or("Selected with text", checked, text("incorrect")).check(driver, element).action).isEqualTo(CONTINUE);
  }

  @Test
  void elementOrConditionActualValue() {
    WebElement element = mockElement(false, "text");
    Condition condition = or("selected with text", be(selected), have(text("text")));
    assertThat(condition.actualValue(driver, element)).isEqualTo("false, null");
    assertThat(condition.check(driver, element).action).isEqualTo(ACCEPT);
  }

  @Test
  void elementOrConditionToString() {
    WebElement element = mockElement(false, "text");
    Condition condition = or("selected with text", be(selected), have(text("text")));
    assertThat(condition).hasToString("selected with text: be selected or have text 'text'");
    assertThat(condition.check(driver, element).action).isEqualTo(ACCEPT);
  }

  @Test
  void conditionBe() {
    Condition condition = be(visible);
    assertThat(condition).hasToString("be visible");
  }

  @Test
  void conditionHave() {
    Condition condition = have(attribute("name"));
    assertThat(condition).hasToString("have attribute name");
  }

  @Test
  void conditionMissingElementSatisfiesCondition() {
    Condition condition = attribute("name");
    assertThat(condition.missingElementSatisfiesCondition()).isFalse();
  }

  @Test
  void conditionToString() {
    Condition condition = attribute("name").because("it's awesome");
    assertThat(condition).hasToString("attribute name (because it's awesome)");
  }

  @Test
  void shouldHaveText_doesNotAccept_nullParameter() {
    //noinspection ConstantConditions
    assertThatThrownBy(() -> text(null))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Argument must not be null or empty string. Use $.shouldBe(empty) or $.shouldHave(exactText(\"\").");
  }

  @Test
  void shouldHaveText_doesNotAccept_emptyString() {
    assertThatThrownBy(() -> text(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Argument must not be null or empty string. Use $.shouldBe(empty) or $.shouldHave(exactText(\"\").");
  }

  @Test
  void shouldHaveText_accepts_blankNonEmptyString() {
    text(" ");
    text("  ");
    text("\t");
    text("\n");
  }

  private WebElement mockElement(boolean isSelected, String text) {
    WebElement element = mock(WebElement.class);
    when(element.isSelected()).thenReturn(isSelected);
    when(element.getText()).thenReturn(text);
    return element;
  }
}
