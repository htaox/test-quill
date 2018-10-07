package com.eztier.rest.responses

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

import com.eztier.postgres.eventstore.models.Patient

object SearchPatientJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val detailedMessageFormat = jsonFormat1(Patient)
}

