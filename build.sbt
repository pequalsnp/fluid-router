ThisBuild / resolvers ++= Seq(
    "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
    Resolver.mavenLocal
)

name := "Fluid Router"

version := "0.1-SNAPSHOT"

organization := "com.kylegalloway"

ThisBuild / scalaVersion := "2.12.10"

val flinkVersion = "1.10.0"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.slf4j" % "slf4j-simple" % "1.7.30"
)

val httpDependencies = Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5.11",
  "io.spray" %%  "spray-json" % "1.3.4"
)

val mongodbDependencies = Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.6.0"
)

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies,
    libraryDependencies ++= httpDependencies,
    libraryDependencies ++= mongodbDependencies
  )

assembly / mainClass := Some("com.kylegalloway.fluidrouter.Job")

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
                                   Compile / run / mainClass,
                                   Compile / run / runner
                                  ).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)
