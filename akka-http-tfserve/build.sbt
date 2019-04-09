

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.5"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.18"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.18"
libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % "1.21.0"

libraryDependencies += "org.platanios" %% "tensorflow" % "0.4.1" classifier sys.props.getOrElse("tensorflow.build", "darwin-cpu-x86_64")
libraryDependencies += "default" %"models_proto_2.12" % "0.1.0-SNAPSHOT"
