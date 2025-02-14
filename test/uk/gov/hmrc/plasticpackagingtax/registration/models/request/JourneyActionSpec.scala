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

import base.PptTestData
import base.unit.ControllerSpec
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.{reset, verify}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.{Headers, Result, Results}
import play.api.test.Helpers.await
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.http.logging.RequestId
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, InternalServerException}
import uk.gov.hmrc.plasticpackagingtax.registration.connectors.DownstreamServiceError
import uk.gov.hmrc.plasticpackagingtax.registration.models.registration.Registration

import scala.concurrent.{ExecutionContext, Future}

class JourneyActionSpec extends ControllerSpec {

  private val responseGenerator = mock[JourneyRequest[_] => Future[Result]]
  private val actionRefiner     = new JourneyAction(mockRegistrationConnector)(ExecutionContext.global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRegistrationConnector, responseGenerator)
    given(responseGenerator.apply(any())).willReturn(Future.successful(Results.Ok))
  }

  "action refine" should {
    "permit request" when {
      "enrolmentId found" in {
        given(mockRegistrationConnector.find(refEq("123"))(any[HeaderCarrier])).willReturn(
          Future.successful(Right(Option(Registration("123"))))
        )

        await(
          actionRefiner.invokeBlock(authRequest(user = PptTestData.newUser("123", Some("123"))),
                                    responseGenerator
          )
        ) mustBe Results.Ok
      }
    }

    "pass through headers" when {
      "enrolmentId found" in {
        val headers = Headers().add(HeaderNames.xRequestId -> "req1")
        given(mockRegistrationConnector.find(refEq("123"))(any[HeaderCarrier])).willReturn(
          Future.successful(Right(Option(Registration("123"))))
        )

        await(
          actionRefiner.invokeBlock(
            authRequest(headers, user = PptTestData.newUser("123", Some("123"))),
            responseGenerator
          )
        ) mustBe Results.Ok

        getHeaders.requestId mustBe Some(RequestId("req1"))
      }
    }

    "create registration" when {
      "registration details not found" in {
        given(mockRegistrationConnector.find(refEq("999"))(any[HeaderCarrier])).willReturn(
          Future.successful(Right(None))
        )
        given(
          mockRegistrationConnector.create(refEq(Registration("999")))(any[HeaderCarrier])
        ).willReturn(Future.successful(Right(Registration("999"))))

        await(
          actionRefiner.invokeBlock(authRequest(user = PptTestData.newUser("123", Some("999"))),
                                    responseGenerator
          )
        ) mustBe Results.Ok
      }
    }
  }

  def getHeaders: HeaderCarrier = {
    val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
    verify(mockRegistrationConnector).find(refEq("123"))(captor.capture())
    captor.getValue
  }

  "throw exception" when {
    "enrolmentId not found" in {
      intercept[InsufficientEnrolments] {
        await(
          actionRefiner.invokeBlock(authRequest(user = PptTestData.newUser("123", None)),
                                    responseGenerator
          )
        )
      }
    }

    "enrolmentId is empty" in {
      intercept[InsufficientEnrolments] {
        await(
          actionRefiner.invokeBlock(authRequest(user = PptTestData.newUser("123", Some(""))),
                                    responseGenerator
          )
        )
      }
    }

    "cannot load user registration" in {
      given(mockRegistrationConnector.find(refEq("123"))(any[HeaderCarrier])).willReturn(
        Future.successful(
          Left(DownstreamServiceError("error", new InternalServerException("error")))
        )
      )

      intercept[DownstreamServiceError] {
        await(
          actionRefiner.invokeBlock(authRequest(user = PptTestData.newUser("123", Some("123"))),
                                    responseGenerator
          )
        )
      }
    }
  }
}
