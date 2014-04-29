import sbt._
import sbt.Keys._
import net.virtualvoid.sbt.graph.Plugin._
import com.typesafe.sbt.packager.MappingsHelper._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
import sbtbuildinfo.Plugin._

object MyBuild extends Build {

  // prompt shows current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  // TODO move these vals to V
  val myScalaVersion = "2.10.4"

  lazy val buildSettings = Defaults.defaultSettings ++ graphSettings ++ Seq( // must include Defaults.defaultSettings somewhere (early) in the chain
    organization := "org.nefilim",
    version      := "0.3",
    scalaVersion := myScalaVersion
  )

  lazy val myBuildInfoSettings = buildInfoSettings ++ Seq(
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, BuildInfoKey.action("buildTime") { new java.util.Date() }),
    buildInfoPackage := "org.nefilim.generated"
  )

  lazy val myPackagerSettings = packageArchetype.java_application ++ deploymentSettings ++ Seq(
    mappings in (Compile, packageBin) ~= { _.filterNot { case (_, name) => // TODO not working, fix
      Seq("/bin").contains(name)
    }},
    packagedArtifacts in Universal ~= { _.filterNot { case (artifact, file) => artifact.`type`.contains("zip")}},
    publish <<= publish.dependsOn(publish in Universal),
    publishLocal <<= publishLocal.dependsOn(publishLocal in Universal)
  )

  lazy val defaultSettings = buildSettings ++ Publish.settings ++ Seq(
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls"),
    testOptions in Test += Tests.Argument("-oDF"),
    incOptions := incOptions.value.withNameHashing(true)
  )

  lazy val snsMonitorProject = Project(
    id = "snsMonitor",
    base = file("."),
    settings = defaultSettings ++ myBuildInfoSettings ++ myPackagerSettings ++ Seq(
      libraryDependencies ++= Dependencies.autoScaleGroupMonitor
    )
  )
}
