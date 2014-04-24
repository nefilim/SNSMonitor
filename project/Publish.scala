import sbt.Keys._
import sbt._

object Publish {
  final val Snapshot = "-SNAPSHOT"

  lazy val settings = Seq(
    crossPaths := true,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,
    publishArtifact in Test := false,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := (
      <url>https://github.com/nefilim/ScalaChefClient</url>
        <licenses>
          <license>
            <name>GNU General Public License (GPL)</name>
            <url>http://www.gnu.org/licenses/gpl.txt</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>https://github.com/nefilim/SNSMonitor.git</url>
          <connection>scm:git:https://github.com/nefilim/SNSMonitor.git</connection>
        </scm>
        <developers>
          <developer>
            <id>nefilim</id>
            <name>Peter van Rensburg</name>
            <url>http://www.nefilim.org</url>
          </developer>
        </developers>),
    organizationName := "org.nefilim",
    organizationHomepage := Some(url("http://www.github.com/nefilim")),
    publishMavenStyle := false,
    // Maven central cannot allow other repos.
    // TODO - Make sure all artifacts are on central.
    pomIncludeRepository := { x => false }
  )

}

