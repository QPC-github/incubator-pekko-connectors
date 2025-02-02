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

package org.apache.pekko.stream.connectors.recordio.scaladsl

import org.apache.pekko
import pekko.NotUsed
import pekko.stream.connectors.recordio.impl.RecordIOFramingStage
import pekko.stream.scaladsl.Flow
import pekko.util.ByteString

/**
 * Scala API
 *
 * Provides a flow that can separate records from an incoming RecordIO-formatted [[pekko.util.ByteString]] stream.
 */
object RecordIOFraming {

  /**
   * Returns a flow that parses an incoming RecordIO stream and emits the identified records.
   *
   * The incoming stream is expected to be a concatenation of records of the format:
   *
   *   [record length]\n[record data]
   *
   * The parser ignores whitespace before or after each record. It is agnostic to the record data contents.
   *
   * The flow will emit each record's data as a byte string.
   *
   * @param maxRecordLength The maximum record length allowed. If a record is indicated to be longer, this Flow will fail the stream.
   */
  def scanner(maxRecordLength: Int = Int.MaxValue): Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString].via(new RecordIOFramingStage(maxRecordLength)).named("recordIOFraming")
}
