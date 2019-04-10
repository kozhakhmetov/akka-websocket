name := "DARInternship"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.8"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.19" // or whatever the latest version is
libraryDependencies += "com.rabbitmq" % "amqp-client" % "5.7.0" // for rabbit