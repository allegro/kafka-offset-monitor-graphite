import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object KafkaUtilsBuild extends Build {

  lazy val graphiteReporter = Project("graphiteReporter", file("."), settings = graphiteReporterSettings)

  def graphiteReporterSettings =  assemblySettings ++ Seq(

    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.5",
    name := "kafka-offset-monitor-graphite",

    mergeStrategy in assembly := {
      case "about.html" => MergeStrategy.discard
      case x =>
        val oldStrategy = (mergeStrategy in assembly).value
        oldStrategy(x)
    },

    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "18.0",
      "com.quantifind" % "kafkaoffsetmonitor_2.10" % "0.3.0-SNAPSHOT",
      "io.dropwizard.metrics" % "metrics-graphite" % "3.1.0",

      "org.scalatest" % "scalatest_2.10" % "2.2.4" % "test",
      "com.jayway.awaitility" % "awaitility" % "1.6.1" % "test"
    ),

    excludedJars in assembly := {
      val cp = (fullClasspath in assembly).value
      cp filter {_.data.getName == "KafkaOffsetMonitor-assembly-0.3.0-SNAPSHOT.jar"}
    },

    resolvers ++= Seq(
      "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
      "java m2" at "http://download.java.net/maven/2",
      "twitter repo" at "http://maven.twttr.com"
    )
  )
}
