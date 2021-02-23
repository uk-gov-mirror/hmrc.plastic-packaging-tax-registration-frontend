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

package uk.gov.hmrc.plasticpackagingtax.registration.models.registration

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.plasticpackagingtax.registration.forms.FullName
import uk.gov.hmrc.plasticpackagingtax.registration.views.model.TaskStatus

class PrimaryContactDetailsSpec extends AnyWordSpec with Matchers {

  "Primary Contact Details TaskStatus" should {

    "be NOT_STARTED " when {
      "primary contact details is empty" in {
        val contactDetails = PrimaryContactDetails()
        contactDetails.status mustBe TaskStatus.NotStarted
      }
    }

    "be COMPLETED " when {
      "primary contact details are all filled in" in {
        val contactDetails =
          PrimaryContactDetails(fullName = Some(FullName("FirstName", "LastName")),
                                jobTitle = Some("jobTitle"),
                                phoneNumber = Some("1234")
          )
        contactDetails.status mustBe TaskStatus.Completed
      }
    }

    "be IN PROGRESS " when {
      "any fields in primary contact details are filled in" in {
        val contactDetails =
          PrimaryContactDetails(fullName = Some(FullName("FirstName", "LastName")))
        contactDetails.status mustBe TaskStatus.InProgress
      }
    }
  }
}
