/*
 * Copyright (c) 2013-2014 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
import sbt._

object Dependencies {

  val resolutionRepos = Seq(
  )

  object V {
    val awsSDK = "1.7.6"
    val typesafeLogging = "1.1.0"
    val scalaTest = "2.0"
    val jodaTime = "2.3"
    val slf4j = "1.7.6"
    val logback = "1.1.1"

    val akka = "2.3.2"
    val spray = "1.3.1"
    val typesafeConfig = "1.2.0"

    val sprayJson = "1.2.6"
    val json4s = "3.2.8"
    val scalaChefClient = "0.3"
    val scalaHipChatClient = "0.1"
  }

  object Libraries {
    val awsSDK = "com.amazonaws"             % "aws-java-sdk"              % V.awsSDK
    val scalaTest= "org.scalatest"           %% "scalatest"                % V.scalaTest % "test"
    val typesafeLogging = "com.typesafe"     %% "scalalogging-slf4j"       % V.typesafeLogging
    val jodaTime = "joda-time"               % "joda-time"                % V.jodaTime
    val logback = "ch.qos.logback"           % "logback-classic"          % V.logback
    val slf4jJCL = "org.slf4j"               % "jcl-over-slf4j"           % V.slf4j
    val slf4jlog4j = "org.slf4j"             % "log4j-over-slf4j"         % V.slf4j
    val slf4jAPI = "org.slf4j"               % "slf4j-api"                % V.slf4j

    val sprayClient = "io.spray"             % "spray-client"   % V.spray
    val sprayRouting = "io.spray"            % "spray-routing"  % V.spray
    val sprayCan = "io.spray"                % "spray-can"      % V.spray
    val sprayHTTP = "io.spray"               % "spray-http"     % V.spray
    val sprayHTTPx = "io.spray"              % "spray-httpx"    % V.spray
    val sprayTestKit = "io.spray"            % "spray-testkit"  % V.spray
    val sprayJson = "io.spray"               %% "spray-json"    % V.sprayJson

    val json4sNative = "org.json4s"          %% "json4s-native" % V.json4s
    val json4sJackon = "org.json4s"          %% "json4s-jackson" % V.json4s
    val json4sExtensions = "org.json4s"      %% "json4s-ext"    % V.json4s

    val akka = "com.typesafe.akka"           %% "akka-actor"    % V.akka
    val akkaKernel = "com.typesafe.akka"     %% "akka-kernel"   % V.akka
    val akkaRemote = "com.typesafe.akka"     %% "akka-remote"   % V.akka
    val akkaTestKit = "com.typesafe.akka"    %% "akka-testkit"  % V.akka % "test"
    val akkaLogging = "com.typesafe.akka"    %% "akka-slf4j"    % V.akka

    val typesafeConfig = "com.typesafe"      % "config"         % V.typesafeConfig
    val scalaChefClient = "org.nefilim"     %% "chefclient"    % V.scalaChefClient
    val scalaHipChatClient = "org.nefilim"     %% "hipchatclient"    % V.scalaHipChatClient
  }

  import Libraries._

  val autoScaleGroupMonitor = Seq(awsSDK, akka, akkaTestKit, akkaLogging, typesafeLogging, 
    sprayClient, sprayCan, sprayHTTP, sprayRouting, sprayJson, sprayTestKit, scalaTest, logback,
    scalaChefClient, scalaHipChatClient)
}
