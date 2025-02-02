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

package org.apache.pekko.stream.connectors.sns.javadsl

import java.util.concurrent.CompletionStage

import org.apache.pekko
import pekko.stream.connectors.sns.SnsPublishSettings
import pekko.stream.javadsl.{ Flow, Keep, Sink }
import pekko.{ Done, NotUsed }
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.{ PublishRequest, PublishResponse }

/**
 * Java API
 * Amazon SNS publisher factory.
 */
object SnsPublisher {

  /**
   * creates a [[pekko.stream.javadsl.Flow Flow]] to publish messages to a SNS topic using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createFlow(topicArn: String,
      settings: SnsPublishSettings,
      snsClient: SnsAsyncClient): Flow[String, PublishResponse, NotUsed] =
    pekko.stream.connectors.sns.scaladsl.SnsPublisher.flow(topicArn, settings)(snsClient).asJava

  /**
   * creates a [[pekko.stream.javadsl.Flow Flow]] to publish messages to a SNS topic using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createFlow(topicArn: String, snsClient: SnsAsyncClient): Flow[String, PublishResponse, NotUsed] =
    pekko.stream.connectors.sns.scaladsl.SnsPublisher.flow(topicArn, SnsPublishSettings())(snsClient).asJava

  /**
   * creates a [[pekko.stream.javadsl.Flow Flow]] to publish messages to a SNS topic using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createPublishFlow(topicArn: String,
      settings: SnsPublishSettings,
      snsClient: SnsAsyncClient): Flow[PublishRequest, PublishResponse, NotUsed] =
    pekko.stream.connectors.sns.scaladsl.SnsPublisher.publishFlow(topicArn, settings)(snsClient).asJava

  /**
   * creates a [[pekko.stream.javadsl.Flow Flow]] to publish messages to a SNS topic using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createPublishFlow(topicArn: String, snsClient: SnsAsyncClient): Flow[PublishRequest, PublishResponse, NotUsed] =
    pekko.stream.connectors.sns.scaladsl.SnsPublisher.publishFlow(topicArn, SnsPublishSettings())(
      snsClient).asJava

  /**
   * creates a [[pekko.stream.javadsl.Flow Flow]] to publish messages to a SNS topics based on the message topic arn using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createPublishFlow(settings: SnsPublishSettings,
      snsClient: SnsAsyncClient): Flow[PublishRequest, PublishResponse, NotUsed] =
    pekko.stream.connectors.sns.scaladsl.SnsPublisher.publishFlow(settings)(snsClient).asJava

  /**
   * creates a [[pekko.stream.javadsl.Flow Flow]] to publish messages to a SNS topics based on the message topic arn using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createPublishFlow(snsClient: SnsAsyncClient): Flow[PublishRequest, PublishResponse, NotUsed] =
    pekko.stream.connectors.sns.scaladsl.SnsPublisher.publishFlow(SnsPublishSettings())(snsClient).asJava

  /**
   * creates a [[pekko.stream.javadsl.Sink Sink]] to publish messages to a SNS topic using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createSink(topicArn: String,
      settings: SnsPublishSettings,
      snsClient: SnsAsyncClient): Sink[String, CompletionStage[Done]] =
    createFlow(topicArn, settings, snsClient)
      .toMat(Sink.ignore(), Keep.right[NotUsed, CompletionStage[Done]])

  /**
   * creates a [[pekko.stream.javadsl.Sink Sink]] to publish messages to a SNS topic using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createSink(topicArn: String, snsClient: SnsAsyncClient): Sink[String, CompletionStage[Done]] =
    createFlow(topicArn, SnsPublishSettings(), snsClient)
      .toMat(Sink.ignore(), Keep.right[NotUsed, CompletionStage[Done]])

  /**
   * creates a [[pekko.stream.javadsl.Sink Sink]] to publish messages to a SNS topic using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createPublishSink(topicArn: String,
      settings: SnsPublishSettings,
      snsClient: SnsAsyncClient): Sink[PublishRequest, CompletionStage[Done]] =
    createPublishFlow(topicArn, settings, snsClient)
      .toMat(Sink.ignore(), Keep.right[NotUsed, CompletionStage[Done]])

  /**
   * creates a [[pekko.stream.javadsl.Sink Sink]] to publish messages to a SNS topic using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createPublishSink(topicArn: String, snsClient: SnsAsyncClient): Sink[PublishRequest, CompletionStage[Done]] =
    createPublishFlow(topicArn, SnsPublishSettings(), snsClient)
      .toMat(Sink.ignore(), Keep.right[NotUsed, CompletionStage[Done]])

  /**
   * creates a [[pekko.stream.javadsl.Sink Sink]] to publish messages to a SNS topics based on the message topic arn using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createPublishSink(settings: SnsPublishSettings,
      snsClient: SnsAsyncClient): Sink[PublishRequest, CompletionStage[Done]] =
    createPublishFlow(settings, snsClient)
      .toMat(Sink.ignore(), Keep.right[NotUsed, CompletionStage[Done]])

  /**
   * creates a [[pekko.stream.javadsl.Sink Sink]] to publish messages to a SNS topics based on the message topic arn using an [[software.amazon.awssdk.services.sns.SnsAsyncClient SnsAsyncClient]]
   */
  def createPublishSink(snsClient: SnsAsyncClient): Sink[PublishRequest, CompletionStage[Done]] =
    createPublishFlow(SnsPublishSettings(), snsClient)
      .toMat(Sink.ignore(), Keep.right[NotUsed, CompletionStage[Done]])
}
