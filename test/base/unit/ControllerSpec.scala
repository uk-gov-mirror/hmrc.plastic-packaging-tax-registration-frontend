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

package base.unit

import java.lang.reflect.Field
import base.{MockAuthAction, PptTestData}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.test.Helpers.contentAsString
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import play.twirl.api.Html
import spec.PptTestData
import uk.gov.hmrc.plasticpackagingtax.registration.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.registration.controllers.actions.{
  AuthAction,
  SaveAndComeBackLater,
  SaveAndContinue
}
import uk.gov.hmrc.plasticpackagingtax.registration.models.SignedInUser
import uk.gov.hmrc.plasticpackagingtax.registration.models.request.AuthenticatedRequest

import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpec
    extends AnyWordSpecLike with MockRegistrationConnector with MockitoSugar with Matchers
    with GuiceOneAppPerSuite with MockAuthAction with BeforeAndAfterEach with DefaultAwaitTimeout
    with MockJourneyAction with PptTestData {

  import utils.FakeRequestCSRFSupport._

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val config: AppConfig = mock[AppConfig]

  protected val saveAndContinueFormAction: (String, String) = (SaveAndContinue.toString, "")

  protected val saveAndComeBackLaterFormAction: (String, String) =
    (SaveAndComeBackLater.toString, "")

  def getRequest(session: (String, String) = "" -> ""): Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "").withSession(session).withCSRFToken

  protected def viewOf(result: Future[Result]): Html = Html(contentAsString(result))

  protected def postRequest(body: JsValue): Request[AnyContentAsJson] =
    postRequest
      .withJsonBody(body)
      .withCSRFToken

  def authRequest(
    headers: Headers = Headers(),
    user: SignedInUser = PptTestData.newUser("123", Some("333"))
  ): AuthenticatedRequest[AnyContentAsEmpty.type] =
    new AuthenticatedRequest(
      FakeRequest().withHeaders(headers),
      user,
      user.enrolments.getEnrolment(AuthAction.pptEnrolmentKey).flatMap(
        e => e.getIdentifier(AuthAction.pptEnrolmentIdentifierName).map(i => i.value)
      )
    )

  protected def postRequestEncoded(
    form: AnyRef,
    formAction: (String, String) = saveAndContinueFormAction
  ): Request[AnyContentAsFormUrlEncoded] = {
    val bodyForm: Seq[(String, String)] = getTuples(form) :+ formAction
    postRequest
      .withFormUrlEncodedBody(bodyForm: _*)
      .withCSRFToken
  }

  protected def postJsonRequestEncoded(
    body: (String, String)*
  ): Request[AnyContentAsFormUrlEncoded] =
    postRequest
      .withFormUrlEncodedBody(body: _*)
      .withCSRFToken

  private def postRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "")

  protected def getTuples(cc: AnyRef): Seq[(String, String)] =
    cc.getClass.getDeclaredFields.foldLeft(Map.empty[String, String]) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> getValue(f, cc))
    }.toList

  private def getValue(field: Field, cc: AnyRef): String =
    field.get(cc) match {
      case c: Option[_] if c.isDefined => c.get.toString
      case c: String                   => c
      case _                           => ""
    }

}
