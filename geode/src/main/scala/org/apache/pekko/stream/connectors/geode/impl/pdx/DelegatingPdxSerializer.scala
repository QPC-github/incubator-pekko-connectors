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

package org.apache.pekko.stream.connectors.geode.impl.pdx

import java.util.Properties

import org.apache.pekko.annotation.InternalApi
import org.apache.geode.cache.Declarable
import org.apache.geode.pdx.{ PdxReader, PdxSerializer, PdxWriter }

/**
 * Geode ClientCache does not support more than one serializer.
 * <br>
 * This serializer delegates to lazily registered serializer.
 */
@InternalApi
private[geode] class DelegatingPdxSerializer(
    isPdxCompat: (Class[_], Class[_]) => Boolean) extends PdxSerializer
    with Declarable {

  private var serializers = Map[Class[_], PdxSerializer]()

  def register[V](serializer: PdxSerializer, clazz: Class[V]): Unit = synchronized {
    if (!serializers.contains(clazz))
      serializers += (clazz -> serializer)
  }

  /**
   * Marshalls a class with a registered serializer.
   *
   * @return true on success
   */
  override def toData(o: scala.Any, out: PdxWriter): Boolean =
    serializers.get(o.getClass).map(_.toData(o, out)).isDefined

  /**
   * Unmarshalls with registered serializer.
   * <br>
   * Tries to find a registered serializer for a given class
   * <ul>
   * <li>Lookup on class basis</li>
   * <li>Iterating through all serializer to find a compatible one</li>
   * </ul>
   * By doing this, a java pojo can be unmarshalled from a scala case class (and vice versa)
   *
   * @return unmarshalled class or null
   */
  override def fromData(clazz: Class[_], in: PdxReader): AnyRef =
    serializers
      .get(clazz)
      .map(_.fromData(clazz, in))
      .orElse(serializers.collectFirst {
        case (c, ser) if isPdxCompat(c, clazz) =>
          val v = ser.fromData(clazz, in)
          if (v != null) register(ser, clazz)
          v
      })
      .orNull

  override def init(props: Properties): Unit = {}
}
