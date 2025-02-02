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

package org.apache.pekko.stream.connectors.file.scaladsl

import java.nio.charset.{ Charset, StandardCharsets }
import java.nio.file.Path

import org.apache.pekko
import pekko.NotUsed
import pekko.stream.scaladsl.Source
import pekko.util.ByteString

import scala.concurrent.duration.FiniteDuration

/**
 * Scala API:
 * Factory methods for the `FileTailSource`
 */
object FileTailSource {

  /**
   * Scala API: Read the entire contents of a file, and then when the end is reached, keep reading
   * newly appended data. Like the unix command `tail -f`.
   *
   * Aborting the stage can be done by combining with a [[akka.stream.KillSwitch]]
   *
   * @param path             a file path to tail
   * @param maxChunkSize     The max emitted size of the `ByteString`s
   * @param startingPosition Offset into the file to start reading
   * @param pollingInterval  When the end has been reached, look for new content with this interval
   */
  def apply(path: Path,
      maxChunkSize: Int,
      startingPosition: Long,
      pollingInterval: FiniteDuration): Source[ByteString, NotUsed] =
    Source.fromGraph(new pekko.stream.connectors.file.impl.FileTailSource(path, maxChunkSize, startingPosition,
      pollingInterval))

  /**
   * Scala API: Read the entire contents of a file as text lines, and then when the end is reached, keep reading
   * newly appended data. Like the unix command `tail -f`.
   *
   * If a line is longer than `maxChunkSize` the stream will fail.
   *
   * Aborting the stage can be done by combining with a [[akka.stream.KillSwitch]]
   *
   * @param path            a file path to tail
   * @param maxLineSize     The max emitted size of the `ByteString`s
   * @param pollingInterval When the end has been reached, look for new content with this interval
   * @param lf              The character or characters used as line separator, default is fetched from OS
   * @param charset         The charset of the file, defaults to UTF-8
   */
  def lines(path: Path,
      maxLineSize: Int,
      pollingInterval: FiniteDuration,
      lf: String = System.getProperty("line.separator"),
      charset: Charset = StandardCharsets.UTF_8): Source[String, NotUsed] =
    apply(path, maxLineSize, 0, pollingInterval)
      .via(pekko.stream.scaladsl.Framing.delimiter(ByteString.fromString(lf, charset.name), maxLineSize,
        false))
      .map(_.decodeString(charset))

}
