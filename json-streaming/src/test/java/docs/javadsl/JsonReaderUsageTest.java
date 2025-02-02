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

package docs.javadsl;

import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.stream.connectors.json.javadsl.JsonReader;
import org.apache.pekko.stream.connectors.testkit.javadsl.LogCapturingJunit4;
import org.apache.pekko.stream.javadsl.*;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.apache.pekko.util.ByteString;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonReaderUsageTest {
  @Rule public final LogCapturingJunit4 logCapturing = new LogCapturingJunit4();

  private static ActorSystem system;

  @Test
  public void jsonParser() throws InterruptedException, ExecutionException, TimeoutException {
    final String firstDoc = "{\"name\":\"test1\"}";
    final String secondDoc = "{\"name\":\"test2\"}";
    final String thirdDoc = "{\"name\":\"test3\"}";

    final ByteString doc =
        ByteString.fromString(
            "{"
                + "\"size\": 3,"
                + "\"rows\": ["
                + "{\"id\": 1, \"doc\":"
                + firstDoc
                + "},"
                + "{\"id\": 2, \"doc\":"
                + secondDoc
                + "},"
                + "{\"id\": 3, \"doc\":"
                + thirdDoc
                + "}"
                + "]}");

    // #usage
    final CompletionStage<List<ByteString>> resultStage =
        Source.single(doc).via(JsonReader.select("$.rows[*].doc")).runWith(Sink.seq(), system);
    // #usage

    resultStage
        .thenAccept(
            (list) -> {
              assertThat(
                  list,
                  hasItems(
                      ByteString.fromString(firstDoc),
                      ByteString.fromString(secondDoc),
                      ByteString.fromString(thirdDoc)));
            })
        .toCompletableFuture()
        .get(5, TimeUnit.SECONDS);
  }

  @BeforeClass
  public static void setup() throws Exception {
    system = ActorSystem.create();
  }

  @AfterClass
  public static void teardown() throws Exception {
    TestKit.shutdownActorSystem(system);
  }
}
