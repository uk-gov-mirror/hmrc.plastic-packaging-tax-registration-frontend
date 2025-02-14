/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.plasticpackagingtax.registration.models.request

import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.plasticpackagingtax.registration.connectors.RegistrationConnector
import uk.gov.hmrc.plasticpackagingtax.registration.models.registration.Registration
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class JourneyAction @Inject() (registrationConnector: RegistrationConnector)(implicit
  val exec: ExecutionContext
) extends ActionRefiner[AuthenticatedRequest, JourneyRequest] {

  private val logger = Logger(this.getClass)

  override protected def refine[A](
    request: AuthenticatedRequest[A]
  ): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    request.enrolmentId.filter(_.trim.nonEmpty) match {
      case Some(id) =>
        loadOrCreateRegistration(id).map {
          case Right(reg)  => Right(new JourneyRequest[A](request, reg, Some(id)))
          case Left(error) => throw error
        }
      case None =>
        logger.warn(s"Enrolment not present, throwing")
        throw InsufficientEnrolments()
    }
  }

  private def loadOrCreateRegistration[A](id: String)(implicit headerCarrier: HeaderCarrier) =
    registrationConnector.find(id).flatMap {
      case Right(reg) =>
        reg
          .map(r => Future.successful(Right(r)))
          .getOrElse(registrationConnector.create(Registration(id)))
      case Left(error) => Future.successful(Left(error))
    }

  override protected def executionContext: ExecutionContext = exec
}
