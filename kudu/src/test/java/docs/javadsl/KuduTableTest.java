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
import org.apache.pekko.NotUsed;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.stream.connectors.kudu.KuduAttributes;
import org.apache.pekko.stream.connectors.kudu.KuduTableSettings;
import org.apache.pekko.stream.connectors.kudu.javadsl.KuduTable;
import org.apache.pekko.stream.connectors.testkit.javadsl.LogCapturingJunit4;
import org.apache.pekko.stream.javadsl.Flow;
import org.apache.pekko.stream.javadsl.Keep;
import org.apache.pekko.stream.javadsl.Sink;
import org.apache.pekko.stream.javadsl.Source;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.CreateTableOptions;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.PartialRow;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class KuduTableTest {
  @Rule public final LogCapturingJunit4 logCapturing = new LogCapturingJunit4();

  private static ActorSystem system;
  private static Schema schema;

  private static KuduTableSettings<Person> tableSettings;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create();

    // #configure
    // Kudu Schema
    List<ColumnSchema> columns = new ArrayList<>(2);
    columns.add(new ColumnSchema.ColumnSchemaBuilder("key", Type.INT32).key(true).build());
    columns.add(new ColumnSchema.ColumnSchemaBuilder("value", Type.STRING).build());
    schema = new Schema(columns);

    // Converter function
    Function<Person, PartialRow> kuduConverter =
        person -> {
          PartialRow partialRow = schema.newPartialRow();
          partialRow.addInt(0, person.id);
          partialRow.addString(1, person.name);
          return partialRow;
        };

    // Kudu table options
    List<String> rangeKeys = Collections.singletonList("key");
    CreateTableOptions createTableOptions =
        new CreateTableOptions().setNumReplicas(1).setRangePartitionColumns(rangeKeys);

    // Alpakka settings
    KuduTableSettings<Person> tableSettings =
        KuduTableSettings.create("tablenameSink", schema, createTableOptions, kuduConverter);
    // #configure
    KuduTableTest.tableSettings = tableSettings;
  }

  @AfterClass
  public static void teardown() throws KuduException {
    TestKit.shutdownActorSystem(system);
  }

  @Test
  public void sink() throws Exception {
    // #sink
    final Sink<Person, CompletionStage<Done>> sink =
        KuduTable.sink(tableSettings.withTableName("Sink"));

    CompletionStage<Done> o =
        Source.from(Arrays.asList(100, 101, 102, 103, 104))
            .map((i) -> new Person(i, String.format("name %d", i)))
            .runWith(sink, system);
    // #sink
    assertEquals(Done.getInstance(), o.toCompletableFuture().get(5, TimeUnit.SECONDS));
  }

  @Test
  public void flow() throws Exception {
    // #flow
    Flow<Person, Person, NotUsed> flow = KuduTable.flow(tableSettings.withTableName("Flow"));

    CompletionStage<List<Person>> run =
        Source.from(Arrays.asList(200, 201, 202, 203, 204))
            .map((i) -> new Person(i, String.format("name_%d", i)))
            .via(flow)
            .toMat(Sink.seq(), Keep.right())
            .run(system);
    // #flow
    assertEquals(5, run.toCompletableFuture().get(5, TimeUnit.SECONDS).size());
  }

  @Test
  public void customClient() throws Exception {
    // #attributes
    final String masterAddress = "localhost:7051";
    final KuduClient client = new KuduClient.KuduClientBuilder(masterAddress).build();
    system.registerOnTermination(
        () -> {
          try {
            client.shutdown();
          } catch (KuduException e) {
            e.printStackTrace();
          }
        });

    final Flow<Person, Person, NotUsed> flow =
        KuduTable.flow(tableSettings.withTableName("Flow"))
            .withAttributes(KuduAttributes.client(client));
    // #attributes

    CompletionStage<List<Person>> run =
        Source.from(Arrays.asList(200, 201, 202, 203, 204))
            .map((i) -> new Person(i, String.format("name_%d", i)))
            .via(flow)
            .toMat(Sink.seq(), Keep.right())
            .run(system);

    assertEquals(5, run.toCompletableFuture().get(5, TimeUnit.SECONDS).size());
  }
}

class Person {
  int id;
  String name;

  public Person(int i, String name) {
    this.id = i;
    this.name = name;
  }
}
