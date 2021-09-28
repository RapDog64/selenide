package com.codeborne.selenide;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.codeborne.selenide.CheckResult.Action.ACCEPT;
import static com.codeborne.selenide.CheckResult.Action.CONTINUE;

/**
 * @since 5.25.0
 */
public class CheckResult implements Serializable {
  private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
  public final Action action;
  public final Object actualValue;
  public final LocalDateTime timestamp;

  public CheckResult(Action action, @Nullable Object actualValue, LocalDateTime timestamp) {
    this.action = action;
    this.actualValue = actualValue;
    this.timestamp = timestamp;
  }

  public CheckResult(Action action, @Nullable Object actualValue) {
    this(action, actualValue, LocalDateTime.now());
  }

  public CheckResult(boolean checkSucceeded, @Nullable Object actualValue) {
    this(checkSucceeded ? ACCEPT : CONTINUE, actualValue);
  }

  public enum Action {
    ACCEPT,
    CONTINUE
  }

  @Override
  public String toString() {
    return String.format("%s @ %s%n", actualValue, timeFormat.format(timestamp));
  }

  @Override
  public boolean equals(Object object) {
    return object == this || object instanceof CheckResult && equals((CheckResult) object);
  }

  private boolean equals(CheckResult that) {
    return this.action == that.action && Objects.equals(this.actualValue, that.actualValue);
  }

  @Override
  public int hashCode() {
    return 31 * action.hashCode() + (actualValue == null ? 0 : actualValue.hashCode());
  }
}
