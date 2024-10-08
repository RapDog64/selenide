package com.codeborne.selenide;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.codeborne.selenide.DownloadOptions.file;
import static com.codeborne.selenide.DownloadOptions.using;
import static com.codeborne.selenide.FileDownloadMode.FOLDER;
import static com.codeborne.selenide.FileDownloadMode.HTTPGET;
import static com.codeborne.selenide.FileDownloadMode.PROXY;
import static com.codeborne.selenide.files.FileFilters.none;
import static com.codeborne.selenide.files.FileFilters.withExtension;
import static org.assertj.core.api.Assertions.assertThat;

final class DownloadOptionsTest {
  @Test
  void defaultOptions() {
    DownloadOptions options = using(PROXY);

    assertThat(options.getMethod()).isEqualTo(PROXY);
    assertThat(options.timeout()).isNull();
    assertThat(options.getFilter()).isEqualTo(none());
  }

  @Test
  void customTimeout() {
    DownloadOptions options = using(PROXY).withTimeout(9999);

    assertThat(options.getMethod()).isEqualTo(PROXY);
    assertThat(options.timeout()).isEqualTo(Duration.ofMillis(9999));
    assertThat(options.getFilter()).isEqualTo(none());
  }

  @Test
  void customFileFilter() {
    DownloadOptions options = using(FOLDER).withExtension("pdf");

    assertThat(options.getMethod()).isEqualTo(FOLDER);
    assertThat(options.timeout()).isNull();
    assertThat(options.getFilter()).usingRecursiveComparison().isEqualTo(withExtension("pdf"));
  }

  @Test
  void customSettings() {
    DownloadOptions options = using(FOLDER).withExtension("ppt").withTimeout(Duration.ofMillis(1234));

    assertThat(options.getMethod()).isEqualTo(FOLDER);
    assertThat(options.timeout()).isEqualTo(Duration.ofMillis(1234));
    assertThat(options.getFilter()).usingRecursiveComparison().isEqualTo(withExtension("ppt"));
  }

  @Test
  void printsOptionsToTestReport() {
    assertThat(using(PROXY))
      .hasToString("method: PROXY");

    assertThat(using(PROXY).withTimeout(9999))
      .hasToString("method: PROXY, timeout: 9999 ms");

    assertThat(using(HTTPGET).withTimeout(9999).withExtension("ppt"))
      .hasToString("method: HTTPGET, timeout: 9999 ms, with extension \"ppt\"");

    assertThat(using(FOLDER).withFilter(withExtension("exe")))
      .hasToString("method: FOLDER, with extension \"exe\"");

    assertThat(using(FOLDER).withExtension("exe"))
      .hasToString("method: FOLDER, with extension \"exe\"");

    assertThat(file().withExtension("exe"))
      .hasToString("with extension \"exe\"");
  }
}
