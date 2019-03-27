name := "Something"
version := "2.3"
organization := "ch.epfl.lara"

scalaVersion := "2.11.2"

scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature"
)

javacOptions += "-Xlint:unchecked"

unmanagedBase <<= baseDirectory { base => base / ".." / ".." / ".." / ".." / ".." / "org.ekstazi.scalatest" / "target" }

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % "2.11.2",
    "org.scalatest" %% "scalatest" % "2.2.0" % "test",
    "com.typesafe.akka" %% "akka-actor" % "2.3.4"
)

Keys.fork in run := true

Keys.fork in Test := true

logBuffered in Test := false

testOptions in Test += Tests.Argument("-oDF")

fork in Test := true

javaOptions in Test += "-javaagent:/home/milos/.m2/repository/org/ekstazi/org.ekstazi.core/4.5.2/org.ekstazi.core-4.5.2.jar=mode=scalatest"

parallelExecution in Test := false

sourcesInBase in Compile := false
