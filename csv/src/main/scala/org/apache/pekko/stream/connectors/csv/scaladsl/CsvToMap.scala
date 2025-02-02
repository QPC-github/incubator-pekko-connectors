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

package org.apache.pekko.stream.connectors.csv.scaladsl

import java.nio.charset.{ Charset, StandardCharsets }

import org.apache.pekko
import pekko.NotUsed
import pekko.stream.connectors.csv.impl.{ CsvToMapAsStringsStage, CsvToMapStage }
import pekko.stream.scaladsl.Flow
import pekko.util.ByteString

object CsvToMap {

  /**
   * A flow translating incoming [[scala.List]] of [[pekko.util.ByteString]] to a map of String and ByteString using the stream's first
   * element's values as keys.
   * @param charset the charset to decode [[pekko.util.ByteString]] to [[scala.Predef.String]], defaults to UTF-8
   */
  def toMap(charset: Charset = StandardCharsets.UTF_8): Flow[List[ByteString], Map[String, ByteString], NotUsed] =
    Flow.fromGraph(
      new CsvToMapStage(columnNames = None,
        charset,
        combineAll = false,
        customFieldValuePlaceholder = Option.empty,
        headerPlaceholder = Option.empty))

  /**
   * A flow translating incoming [[scala.List]] of [[pekko.util.ByteString]] to a map of String keys and values using the stream's first
   * element's values as keys.
   * @param charset the charset to decode [[pekko.util.ByteString]] to [[scala.Predef.String]], defaults to UTF-8
   */
  def toMapAsStrings(charset: Charset = StandardCharsets.UTF_8): Flow[List[ByteString], Map[String, String], NotUsed] =
    Flow.fromGraph(
      new CsvToMapAsStringsStage(columnNames = None,
        charset,
        combineAll = false,
        customFieldValuePlaceholder = Option.empty,
        headerPlaceholder = Option.empty))

  /**
   * A flow translating incoming [[scala.List]] of [[pekko.util.ByteString]] to a map of String and ByteString
   * using the stream's first element's values as keys. If the header values are shorter than the data (or vice-versa)
   * placeholder elements are used to extend the shorter collection to the length of the longer.
   * @param charset the charset to decode [[pekko.util.ByteString]] to [[scala.Predef.String]], defaults to UTF-8
   * @param customFieldValuePlaceholder placeholder used when there is more data than headers.
   * @param headerPlaceholder placeholder used when there are more headers than data.
   */
  def toMapCombineAll(
      charset: Charset = StandardCharsets.UTF_8,
      customFieldValuePlaceholder: Option[ByteString] = None,
      headerPlaceholder: Option[String] = None): Flow[List[ByteString], Map[String, ByteString], NotUsed] =
    Flow.fromGraph(
      new CsvToMapStage(columnNames = None,
        charset,
        combineAll = true,
        customFieldValuePlaceholder = customFieldValuePlaceholder,
        headerPlaceholder = headerPlaceholder))

  /**
   * A flow translating incoming [[scala.List]] of [[pekko.util.ByteString]] to a map of String keys and values
   * using the stream's first element's values as keys. If the header values are shorter than the data (or vice-versa)
   * placeholder elements are used to extend the shorter collection to the length of the longer.
   * @param charset the charset to decode [[pekko.util.ByteString]] to [[scala.Predef.String]], defaults to UTF-8
   * @param customFieldValuePlaceholder placeholder used when there is more data than headers.
   * @param headerPlaceholder placeholder used when there are more headers than data.
   */
  def toMapAsStringsCombineAll(
      charset: Charset = StandardCharsets.UTF_8,
      customFieldValuePlaceholder: Option[String] = None,
      headerPlaceholder: Option[String] = None): Flow[List[ByteString], Map[String, String], NotUsed] =
    Flow.fromGraph(
      new CsvToMapAsStringsStage(columnNames = None,
        charset,
        combineAll = true,
        customFieldValuePlaceholder = customFieldValuePlaceholder,
        headerPlaceholder = headerPlaceholder))

  /**
   * A flow translating incoming [[scala.List]] of [[pekko.util.ByteString]] to a map of String and ByteString using the given headers
   * as keys.
   * @param headers column names to be used as map keys
   */
  def withHeaders(headers: String*): Flow[List[ByteString], Map[String, ByteString], NotUsed] =
    Flow.fromGraph(
      new CsvToMapStage(Some(headers.toList),
        StandardCharsets.UTF_8,
        combineAll = false,
        customFieldValuePlaceholder = Option.empty,
        headerPlaceholder = Option.empty))

  /**
   * A flow translating incoming [[scala.List]] of [[pekko.util.ByteString]] to a map of String keys and values using the given headers
   * as keys.
   * @param charset the charset to decode [[pekko.util.ByteString]] to [[scala.Predef.String]]
   * @param headers column names to be used as map keys
   */
  def withHeadersAsStrings(
      charset: Charset,
      headers: String*): Flow[List[ByteString], Map[String, String], NotUsed] =
    Flow.fromGraph(
      new CsvToMapAsStringsStage(Some(headers.toList),
        charset,
        combineAll = false,
        customFieldValuePlaceholder = Option.empty,
        headerPlaceholder = Option.empty))
}
