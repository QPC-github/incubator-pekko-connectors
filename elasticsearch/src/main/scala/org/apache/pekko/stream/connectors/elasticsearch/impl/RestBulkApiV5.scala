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

package org.apache.pekko.stream.connectors.elasticsearch.impl

import org.apache.pekko
import pekko.annotation.InternalApi
import pekko.stream.connectors.elasticsearch.Operation._
import pekko.stream.connectors.elasticsearch.{ MessageWriter, WriteMessage }
import spray.json._

import scala.collection.immutable

/**
 * Internal API.
 *
 * REST API implementation for some Elasticsearch 5 version.
 * https://www.elastic.co/guide/en/elasticsearch/reference/5.6/docs-bulk.html
 */
@InternalApi
private[impl] final class RestBulkApiV5[T, C](indexName: String,
    typeName: String,
    versionType: Option[String],
    allowExplicitIndex: Boolean,
    messageWriter: MessageWriter[T])
    extends RestBulkApi[T, C] {

  private lazy val typeNameTuple = "_type" -> JsString(typeName)

  def toJson(messages: immutable.Seq[WriteMessage[T, C]]): String =
    messages
      .map { message =>
        val sharedFields = constructSharedFields(message)
        val tuple: (String, JsObject) = message.operation match {
          case Index =>
            val fields = Seq(
              optionalNumber("_version", message.version),
              optionalString("version_type", versionType),
              optionalString("_id", message.id)).flatten
            "index" -> JsObject(sharedFields ++ fields: _*)
          case Create => "create" -> JsObject(sharedFields ++ optionalString("_id", message.id): _*)
          case Update | Upsert =>
            val fields =
              ("_id" -> JsString(message.id.get)) +: Seq(
                optionalNumber("_version", message.version),
                optionalString("version_type", versionType)).flatten
            "update" -> JsObject(sharedFields ++ fields: _*)
          case Delete =>
            val fields =
              ("_id" -> JsString(message.id.get)) +: Seq(
                optionalNumber("_version", message.version),
                optionalString("version_type", versionType)).flatten
            "delete" -> JsObject(sharedFields ++ fields: _*)
          case Nop => "" -> JsObject()
        }
        if (tuple._1.nonEmpty)
          JsObject(tuple).compactPrint + messageToJson(message, message.source.fold("")(messageWriter.convert))
        else
          ""
      }
      .filter(_.nonEmpty) match {
      case Nil => "" // if all NOPs
      case x   => x.mkString("", "\n", "\n")
    }

  override def constructSharedFields(message: WriteMessage[T, C]): Seq[(String, JsString)] = {
    val operationFields = if (allowExplicitIndex) {
      Seq("_index" -> JsString(message.indexName.getOrElse(indexName)), typeNameTuple)
    } else {
      Seq(typeNameTuple)
    }

    operationFields ++ message.customMetadata.map { case (field, value) => field -> JsString(value) }
  }
}
