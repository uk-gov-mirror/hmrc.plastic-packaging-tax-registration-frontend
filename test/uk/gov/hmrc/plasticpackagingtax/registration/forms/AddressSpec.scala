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

package uk.gov.hmrc.plasticpackagingtax.registration.forms

import base.unit.CommonTestUtils
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import uk.gov.hmrc.plasticpackagingtax.registration.forms.Address.{
  addressLine1,
  addressLine2,
  addressLine3,
  businessName,
  county,
  postCode,
  townOrCity
}

class AddressSpec extends AnyWordSpec with Matchers with CommonTestUtils {
  "Address validation rules" should {

    "return success" when {

      "all address fields are valid" in {

        val input = Map(businessName -> "Some Company",
                        addressLine1 -> "Address Line 1",
                        addressLine2 -> "Address Line 2",
                        addressLine3 -> "Address Line 3",
                        townOrCity   -> "Town or City",
                        county       -> "County",
                        postCode     -> "LS4 1RH"
        )

        val form = Address.form().bind(input)
        form.errors.size mustBe 0
      }

      "address mandatory fields are valid" in {

        val input = Map(addressLine1 -> "Address Line 1 .'-&",
                        townOrCity -> "Town or City .'-&",
                        postCode   -> "LS4 1RH"
        )

        val form = Address.form().bind(input)
        form.errors.size mustBe 0
      }
    }

    "return errors" when {

      "mandatory fields provided with empty data" in {

        val input = Map(addressLine1 -> "", townOrCity -> "", postCode -> "")
        val expectedErrors =
          Seq(FormError(addressLine1, "primaryContactDetails.address.addressLine1.empty.error"),
              FormError(townOrCity, "primaryContactDetails.address.townOrCity.empty.error"),
              FormError(postCode, "primaryContactDetails.address.postCode.empty.error")
          )

        testFailedValidationErrors(input, expectedErrors)
      }

      "contains incorrect data" in {

        val input = Map(businessName -> "Some Company!!!!^^%",
                        addressLine1 -> "Address Line 1888888888888888888888888888888888888",
                        addressLine2 -> "Address Line 2%%$%$%$%",
                        addressLine3 -> "Address Line 3**********",
                        townOrCity   -> "Town or City££$£$£$+",
                        county       -> "County£$£$+",
                        postCode     -> "LS4 1RH £$£$£$++---"
        )
        val expectedErrors =
          Seq(FormError(businessName, "primaryContactDetails.address.businessName.format.error"),
              FormError(addressLine1, "primaryContactDetails.address.addressLine1.format.error"),
              FormError(addressLine2, "primaryContactDetails.address.addressLine2.format.error"),
              FormError(addressLine3, "primaryContactDetails.address.addressLine3.format.error"),
              FormError(townOrCity, "primaryContactDetails.address.townOrCity.format.error"),
              FormError(county, "primaryContactDetails.address.county.format.error"),
              FormError(postCode, "primaryContactDetails.address.postCode.format.error")
          )

        testFailedValidationErrors(input, expectedErrors)
      }
    }
  }

  def testFailedValidationErrors(
    input: Map[String, String],
    expectedErrors: Seq[FormError]
  ): Unit = {
    val form = Address.form().bind(input)
    expectedErrors.foreach(form.errors must contain(_))
  }

}
