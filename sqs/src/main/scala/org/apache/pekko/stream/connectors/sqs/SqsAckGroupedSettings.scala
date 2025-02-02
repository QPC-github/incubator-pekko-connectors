/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) since 2016 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream.connectors.sqs

import scala.concurrent.duration._
import org.apache.pekko.util.JavaDurationConverters._

final class SqsAckGroupedSettings private (val maxBatchSize: Int,
    val maxBatchWait: scala.concurrent.duration.FiniteDuration,
    val concurrentRequests: Int) {

  require(concurrentRequests > 0)
  require(
    maxBatchSize > 0 && maxBatchSize <= 10,
    s"Invalid value for maxBatchSize: $maxBatchSize. It should be 0 < maxBatchSize < 10, due to the Amazon SQS requirements.")

  def withMaxBatchSize(value: Int): SqsAckGroupedSettings = copy(maxBatchSize = value)

  /** Scala API */
  def withMaxBatchWait(value: scala.concurrent.duration.FiniteDuration): SqsAckGroupedSettings =
    copy(maxBatchWait = value)

  /** Java API */
  def withMaxBatchWait(value: java.time.Duration): SqsAckGroupedSettings =
    withMaxBatchWait(
      scala.concurrent.duration.FiniteDuration(value.toMillis, java.util.concurrent.TimeUnit.MILLISECONDS))

  def withConcurrentRequests(value: Int): SqsAckGroupedSettings = copy(concurrentRequests = value)

  private def copy(maxBatchSize: Int = maxBatchSize,
      maxBatchWait: scala.concurrent.duration.FiniteDuration = maxBatchWait,
      concurrentRequests: Int = concurrentRequests): SqsAckGroupedSettings =
    new SqsAckGroupedSettings(maxBatchSize = maxBatchSize,
      maxBatchWait = maxBatchWait,
      concurrentRequests = concurrentRequests)

  override def toString =
    s"""SqsAckGroupedSettings(maxBatchSize=$maxBatchSize,maxBatchWait=$maxBatchWait,concurrentRequests=$concurrentRequests)"""

}

object SqsAckGroupedSettings {
  val Defaults = new SqsAckGroupedSettings(
    maxBatchSize = 10,
    maxBatchWait = 500.millis,
    concurrentRequests = 1)

  /** Scala API */
  def apply(): SqsAckGroupedSettings = Defaults

  /** Java API */
  def create(): SqsAckGroupedSettings = Defaults

  /** Scala API */
  def apply(
      maxBatchSize: Int,
      maxBatchWait: scala.concurrent.duration.FiniteDuration,
      concurrentRequests: Int): SqsAckGroupedSettings = new SqsAckGroupedSettings(
    maxBatchSize,
    maxBatchWait,
    concurrentRequests)

  /** Java API */
  def create(
      maxBatchSize: Int,
      maxBatchWait: java.time.Duration,
      concurrentRequests: Int): SqsAckGroupedSettings = new SqsAckGroupedSettings(
    maxBatchSize,
    maxBatchWait.asScala,
    concurrentRequests)
}
