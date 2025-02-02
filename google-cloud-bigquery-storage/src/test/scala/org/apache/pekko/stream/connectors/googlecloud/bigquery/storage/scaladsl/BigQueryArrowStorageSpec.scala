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

package org.apache.pekko.stream.connectors.googlecloud.bigquery.storage.scaladsl

import org.apache.pekko
import pekko.stream.connectors.googlecloud.bigquery.storage.impl.SimpleRowReader
import pekko.stream.connectors.googlecloud.bigquery.storage.{ BigQueryStorageSettings, BigQueryStorageSpecBase }
import pekko.stream.connectors.testkit.scaladsl.LogCapturing
import pekko.stream.scaladsl.Sink
import com.google.cloud.bigquery.storage.v1.arrow.{ ArrowRecordBatch, ArrowSchema }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BigQueryArrowStorageSpec
    extends BigQueryStorageSpecBase(21002)
    with AnyWordSpecLike
    with BeforeAndAfterAll
    with Matchers
    with LogCapturing {

  "BigQueryArrowStorage.readArrow" should {

    val reader = new SimpleRowReader(ArrowSchema(serializedSchema = GCPSerializedArrowSchema))
    val expectedRecords = reader.read(ArrowRecordBatch(GCPSerializedArrowTenRecordBatch, 10))

    "stream the results for a query in records merged" in {
      BigQueryArrowStorage
        .readRecordsMerged(Project, Dataset, Table, None)
        .withAttributes(mockBQReader())
        .runWith(Sink.seq)
        .futureValue shouldBe Seq.fill(DefaultNumStreams * ResponsesPerStream)(expectedRecords)
    }

    "stream the results for a query in records" in {
      BigQueryArrowStorage
        .readRecords(Project, Dataset, Table, None)
        .withAttributes(mockBQReader())
        .map(a => a.reduce((a, b) => a.merge(b)))
        .flatMapMerge(100, identity)
        .runWith(Sink.seq)
        .futureValue shouldBe Seq.fill(DefaultNumStreams * ResponsesPerStream)(expectedRecords).flatten
    }

    "stream the results for a query merged" in {
      BigQueryArrowStorage
        .readMerged(Project, Dataset, Table, None)
        .withAttributes(mockBQReader())
        .map(s => s._2.map(b => (s._1, b)))
        .flatMapMerge(100, identity)
        .runWith(Sink.seq)
        .futureValue shouldBe Vector.fill(DefaultNumStreams * ResponsesPerStream)(
        (ArrowSchema(serializedSchema = GCPSerializedArrowSchema),
          ArrowRecordBatch(GCPSerializedArrowTenRecordBatch, 10)))
    }

    "stream the results for a query" in {
      val streamRes = BigQueryArrowStorage
        .read(Project, Dataset, Table, None)
        .withAttributes(mockBQReader())
        .runWith(Sink.seq)
        .futureValue
        .head

      val schema = streamRes._1
      schema shouldBe ArrowSchema(serializedSchema = GCPSerializedArrowSchema)

      val recordBatch = streamRes._2
        .reduce((a, b) => a.merge(b))
        .withAttributes(mockBQReader())
        .runWith(Sink.seq)
        .futureValue
        .head

      val rowReader = new SimpleRowReader(schema)
      val records = rowReader.read(recordBatch)

      records shouldBe expectedRecords
    }

  }

  def mockBQReader(host: String = bqHost, port: Int = bqPort) = {
    val reader = GrpcBigQueryStorageReader(BigQueryStorageSettings(host, port))
    BigQueryStorageAttributes.reader(reader)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    startMock()
  }

  override def afterAll(): Unit = {
    stopMock()
    system.terminate()
    super.afterAll()
  }

}
