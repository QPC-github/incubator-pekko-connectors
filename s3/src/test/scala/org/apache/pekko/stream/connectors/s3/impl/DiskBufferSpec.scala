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

package org.apache.pekko.stream.connectors.s3.impl

import java.nio.BufferOverflowException
import java.nio.file.Files

import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.stream.connectors.testkit.scaladsl.LogCapturing
import pekko.stream.scaladsl.{ Sink, Source }
import pekko.testkit.{ EventFilter, TestKit }
import pekko.util.ByteString
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{ Millis, Seconds, Span }

class DiskBufferSpec(_system: ActorSystem)
    extends TestKit(_system)
    with AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures
    with Eventually
    with LogCapturing {

  def this() = this(ActorSystem("DiskBufferSpec"))

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(200, Millis))

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "DiskBuffer" should
  "emit a chunk on its output containing the concatenation of all input values" in {
    val result = Source(Vector(ByteString(1, 2, 3, 4, 5), ByteString(6, 7, 8, 9, 10, 11, 12), ByteString(13, 14)))
      .via(new DiskBuffer(1, 200, None))
      .runWith(Sink.seq)
      .futureValue

    result should have size 1
    val chunk = result.head
    chunk shouldBe a[DiskChunk]
    val diskChunk = chunk.asInstanceOf[DiskChunk]
    chunk.size should be(14)
    diskChunk.data.runWith(Sink.seq).futureValue should be(
      Seq(ByteString(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)))
  }

  it should "fail if more than maxSize bytes are fed into it" in {
    EventFilter[BufferOverflowException](occurrences = 1).intercept {
      whenReady(
        Source(Vector(ByteString(1, 2, 3, 4, 5), ByteString(6, 7, 8, 9, 10, 11, 12), ByteString(13, 14)))
          .via(new DiskBuffer(1, 10, None))
          .runWith(Sink.seq)
          .failed) { e =>
        e shouldBe a[BufferOverflowException]
      }
    }
  }

  it should "delete its temp file after N materializations" in {
    val tmpDir = Files.createTempDirectory("DiskBufferSpec").toFile()
    val before = tmpDir.list().size
    val chunk = Source(Vector(ByteString(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)))
      .via(new DiskBuffer(2, 200, Some(tmpDir.toPath)))
      .runWith(Sink.seq)
      .futureValue
      .head

    chunk shouldBe a[DiskChunk]
    val source = chunk.asInstanceOf[DiskChunk].data

    tmpDir.list().size should be(before + 1)

    source.runWith(Sink.ignore).futureValue
    tmpDir.list().size should be(before + 1)

    source.runWith(Sink.ignore).futureValue
    eventually {
      tmpDir.list().size should be(before)
    }

  }
}
