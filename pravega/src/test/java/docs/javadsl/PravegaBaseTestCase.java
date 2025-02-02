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

import java.net.URI;
import java.util.UUID;

import org.apache.pekko.stream.connectors.pravega.PravegaPekkoTestCaseSupport;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.apache.pekko.testkit.javadsl.TestKit;

import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;

public abstract class PravegaBaseTestCase extends PravegaPekkoTestCaseSupport {

  protected String newGroup() {
    return "java-test-group-" + UUID.randomUUID().toString();
  }

  protected static String newScope() {
    return "java-test-scope-" + UUID.randomUUID().toString();
  }

  protected String newStreamName() {
    return "java-test-topic-" + UUID.randomUUID().toString();
  }

  protected static String newTableName() {
    return "java-test-table-" + UUID.randomUUID().toString();
  }

  @BeforeClass
  public static void setup() {
    init();
  }

  public static void createScope(String scope) {
    StreamManager streamManager = StreamManager.create(URI.create("tcp://localhost:9090"));

    if (streamManager.createScope(scope)) LOGGER.info("Created scope [{}]", scope);
    else LOGGER.info("Scope [{}] already exists", scope);
    StreamConfiguration streamConfig =
        StreamConfiguration.builder().scalingPolicy(ScalingPolicy.fixed(1)).build();

    streamManager.close();
  }

  public void createStream(String scope, String streamName) {
    StreamManager streamManager = StreamManager.create(URI.create("tcp://localhost:9090"));

    if (streamManager.createScope(scope)) LOGGER.info("Created scope [{}]", scope);
    else LOGGER.info("Scope [{}] already exists", scope);
    StreamConfiguration streamConfig =
        StreamConfiguration.builder().scalingPolicy(ScalingPolicy.fixed(1)).build();
    if (streamManager.createStream(scope, streamName, streamConfig))
      LOGGER.info("Created stream [{}] in scope [{}]", streamName, scope);
    else LOGGER.info("");

    streamManager.close();
  }

  @AfterClass
  public static void teardown() {
    TestKit.shutdownActorSystem(system);
  }
}
