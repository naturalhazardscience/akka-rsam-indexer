name := "akka-rsam-indexer"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.6.4"
libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-actor"              % akkaVersion,
  "com.typesafe.akka"      %% "akka-testkit"            % akkaVersion   % "test",
  "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % "7.6.0",
  "org.scalatest"          %% "scalatest"               % "3.0.0"       % "test"
)

fork in run := true
cancelable in Global :=true
