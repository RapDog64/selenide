package org.selenide.videorecorder.core;

import com.codeborne.selenide.impl.AttachmentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;

import static com.codeborne.selenide.impl.Plugins.inject;
import static com.codeborne.selenide.impl.ThreadNamer.named;
import static java.lang.Integer.toHexString;
import static java.lang.System.nanoTime;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.selenide.videorecorder.core.VideoSaveMode.ALL;

/**
 * Created by Serhii Bryt
 * 07.05.2024 11:57
 */
public class VideoRecorder {
  private static final Logger log = LoggerFactory.getLogger(VideoRecorder.class);
  private static final VideoConfiguration config = new VideoConfiguration();
  private final AttachmentHandler attachmentHandler = inject();

  private final ScheduledExecutorService screenshooter = newScheduledThreadPool(1, named("video-recorder:screenshots:"));
  private final ScheduledExecutorService videoMerger = newScheduledThreadPool(1, named("video-recorder:stream:"));
  private final int fps;
  private final Queue<Screenshot> screenshots = new ConcurrentLinkedQueue<>();
  private final ScreenShooter screenShooterTask;
  private final VideoMerger videoMergerTask;

  public VideoRecorder() {
    fps = config.fps();
    screenShooterTask = new ScreenShooter(currentThread().getId(), screenshots);
    videoMergerTask = new VideoMerger(currentThread().getId(), config.videoFolder(), fps, config.crf(), screenshots);
  }

  public String videoUrl() {
    return videoMergerTask.videoUrl();
  }

  public void start() {
    log.info("Starting screenshooter every {} nanoseconds to achieve fps {}", delayBetweenFramesNanos(), fps);
    startScreenShooter();
    if (mergeVideoOnTheFly()) {
      startVideoMerger();
    }
  }

  private void startScreenShooter() {
    log.debug("Start screen shooter x {} {}", delayBetweenFramesNanos(), NANOSECONDS);
    screenshooter.scheduleAtFixedRate(screenShooterTask, 0, delayBetweenFramesNanos(), NANOSECONDS);
  }

  private void startVideoMerger() {
    log.debug("Start video merger x {} {}", 1, MILLISECONDS);
    videoMerger.scheduleWithFixedDelay(videoMergerTask, 0, 1, MILLISECONDS);
  }

  /**
   * FPS times per second
   */
  private long delayBetweenFramesNanos() {
    return SECONDS.toNanos(1) / fps;
  }

  /**
   * Complete video processing and save the video file
   */
  public void finish() {
    if (!mergeVideoOnTheFly() && !screenshots.isEmpty()) {
      startVideoMerger();
    }

    log.debug("Stopping video recorder...");

    try {
      screenshooter.shutdown();
      stop("Screenshooter", screenshooter, 1000);
      screenshooter.shutdownNow();
      screenShooterTask.finish();

      videoMerger.shutdown();
      stop("Video merger", videoMerger, config.videoProcessingTimeout());
      videoMergerTask.finish();

      log.info("Video recorded: {}", videoUrl());
      attachmentHandler.attach(videoMergerTask.videoFile().toFile());
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  /**
   * Stop video processing and delete the video file
   */
  public void cancel() {
    screenShooterTask.cancel();
    screenshooter.shutdownNow();
    videoMergerTask.rollback();
    videoMerger.shutdownNow();
  }

  private void stop(String name, ScheduledExecutorService threadPool, long timeoutMs) throws InterruptedException {
    long start = nanoTime();
    if (!threadPool.awaitTermination(timeoutMs, MILLISECONDS)) {
      log.warn("{} thread hasn't completed in {} ms.", name, timeoutMs);
    }
    else {
      log.debug("{} thread stopped in {} ms.", name, NANOSECONDS.toMillis(nanoTime() - start));
    }
  }

  private boolean mergeVideoOnTheFly() {
    return config.saveMode() == ALL;
  }

  @Override
  public String toString() {
    return "%s{fps:%s, queueSize:%s}@%s".formatted(getClass().getSimpleName(), fps, screenshots.size(), toHexString(hashCode()));
  }
}
