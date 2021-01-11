import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-27" % "2.23.0",
    "uk.gov.hmrc"             %% "govuk-template"             % "5.55.0-play-27",
    "uk.gov.hmrc"             %% "play-ui"                    % "8.11.0-play-27",
    "uk.gov.hmrc"             %% "play-frontend-govuk"        % "0.49.0-play-26",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "0.16.0-play-26",
    "org.webjars.npm"         %  "govuk-frontend"             % "3.4.0",
    "org.webjars.npm"         %  "hmrc-frontend"              % "1.12.0",
    "ai.x"                    %% "play-json-extensions"       % "0.42.0",
    "com.typesafe.play"       %% "play-json-joda"             % "2.9.1"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "2.23.0"                % Test,
    "org.scalatest"           %% "scalatest"                % "3.1.2"                 % Test,
    "org.jsoup"               %  "jsoup"                    % "1.10.2"                % Test,
    "com.typesafe.play"       %% "play-test"                % current                 % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.35.10"               % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"                 % "test, it"
  )
}
