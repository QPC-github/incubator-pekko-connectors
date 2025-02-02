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

import org.apache.pekko.Done;
// #init-system
import org.apache.pekko.actor.ActorSystem;
// #init-system
import org.apache.pekko.stream.connectors.aws.eventbridge.javadsl.EventBridgePublisher;
import org.apache.pekko.stream.javadsl.Sink;
import org.apache.pekko.stream.javadsl.Source;
import org.apache.pekko.testkit.javadsl.TestKit;

// #init-client
import java.net.URI;
import com.github.pjfanning.pekkohttpspi.PekkoHttpClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;
// #init-client

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import software.amazon.awssdk.services.eventbridge.model.CreateEventBusRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EventBridgePublisherTest {

  static ActorSystem system;
  static EventBridgeAsyncClient eventBridgeClient;
  static String eventBusArn;

  static final String endpoint = "http://localhost:4587";

  private static PutEventsRequestEntry detailEntry(String detail) {
    return PutEventsRequestEntry.builder().detail(detail).build();
  }

  private static PutEventsRequest detailPutEventsRequest(String detail) {
    return PutEventsRequest.builder().entries(detailEntry(detail)).build();
  }

  @BeforeClass
  public static void setUpBeforeClass() throws ExecutionException, InterruptedException {
    system = ActorSystem.create("EventBridgePublisherTest");
    eventBridgeClient = createEventBridgeClient();
    eventBusArn =
        eventBridgeClient
            .createEventBus(
                CreateEventBusRequest.builder()
                    .name("alpakka-java-eventbus-" + UUID.randomUUID().toString())
                    .build())
            .get()
            .eventBusArn();
  }

  @AfterClass
  public static void tearDownAfterClass() {
    TestKit.shutdownActorSystem(system);
  }

  static EventBridgeAsyncClient createEventBridgeClient() {
    // #init-client

    final EventBridgeAsyncClient awsClient =
        EventBridgeAsyncClient.builder()
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create("x", "x")))
            .endpointOverride(URI.create(endpoint))
            .region(Region.EU_CENTRAL_1)
            .httpClient(PekkoHttpClient.builder().withActorSystem(system).build())
            .build();

    system.registerOnTermination(() -> awsClient.close());
    // #init-client

    return awsClient;
  }

  void documentation() {
    // #init-system
    ActorSystem system = ActorSystem.create();
    // #init-system
  }

  @Test
  public void sinkShouldPutDetailEntry() throws Exception {
    CompletionStage<Done> completion =
        // #run-events-entry
        Source.single(detailEntry("message"))
            .runWith(EventBridgePublisher.sink(eventBridgeClient), system);

    // #run-events-entry
    assertThat(completion.toCompletableFuture().get(2, TimeUnit.SECONDS), is(Done.getInstance()));
  }

  @Test
  public void sinkShouldPutEventsRequest() throws Exception {
    CompletionStage<Done> completion =
        // #run-events-request
        Source.single(detailPutEventsRequest("message"))
            .runWith(EventBridgePublisher.publishSink(eventBridgeClient), system);

    // #run-events-request
    assertThat(completion.toCompletableFuture().get(2, TimeUnit.SECONDS), is(Done.getInstance()));
  }

  @Test
  public void flowShouldPutDetailEntry() throws Exception {
    CompletionStage<Done> completion =
        // #flow-events-entry
        Source.single(detailEntry("message"))
            .via(EventBridgePublisher.flow(eventBridgeClient))
            .runWith(Sink.foreach(res -> System.out.println(res)), system);

    // #flow-events-entry
    assertThat(completion.toCompletableFuture().get(2, TimeUnit.SECONDS), is(Done.getInstance()));
  }

  @Test
  public void flowShouldPutEventsRequest() throws Exception {
    CompletionStage<Done> completion =
        // #flow-events-request
        Source.single(detailPutEventsRequest("message"))
            .via(EventBridgePublisher.publishFlow(eventBridgeClient))
            .runWith(Sink.foreach(res -> System.out.println(res)), system);

    // #flow-events-request
    assertThat(completion.toCompletableFuture().get(2, TimeUnit.SECONDS), is(Done.getInstance()));
  }
}
