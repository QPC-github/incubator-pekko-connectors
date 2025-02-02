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

package org.apache.pekko.stream.connectors.pravega.scaladsl

import org.apache.pekko
import pekko.annotation.ApiMayChange
import pekko.stream.scaladsl.{ Flow, Keep, Sink, Source }
import pekko.{ Done, NotUsed }
import pekko.stream.connectors.pravega.impl.{ PravegaFlow, PravegaSource }
import pekko.stream.connectors.pravega.{ PravegaEvent, PravegaReaderGroupManager, ReaderSettings, WriterSettings }
import io.pravega.client.ClientConfig
import io.pravega.client.stream.ReaderGroup

import scala.concurrent.Future

@ApiMayChange
object Pravega {

  def readerGroupManager(scope: String, clientConfig: ClientConfig) = new PravegaReaderGroupManager(scope, clientConfig)

  /**
   * Messages are read from a Pravega stream.
   *
   * Materialized value is a [[Future]] which completes to [[Done]] as soon as the Pravega reader is open.
   */
  def source[A](readerGroup: ReaderGroup, readerSettings: ReaderSettings[A]): Source[PravegaEvent[A], Future[Done]] =
    Source.fromGraph(new PravegaSource(readerGroup, readerSettings))

  /**
   * Incoming messages are written to Pravega stream and emitted unchanged.
   */
  def flow[A](scope: String, streamName: String, writerSettings: WriterSettings[A]): Flow[A, A, NotUsed] =
    Flow.fromGraph(new PravegaFlow(scope, streamName, writerSettings))

  /**
   * Incoming messages are written to Pravega.
   */
  def sink[A](scope: String, streamName: String, writerSettings: WriterSettings[A]): Sink[A, Future[Done]] =
    Flow[A].via(flow(scope, streamName, writerSettings)).toMat(Sink.ignore)(Keep.right)

}
