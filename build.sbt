enablePlugins(JavaAppPackaging)

name := "log-all"

scalaSource in Compile := baseDirectory.value / "app"

resourceDirectory in Compile := baseDirectory.value / "app"

scalaVersion := "2.13.1"

libraryDependencies := Seq(
  "com.typesafe.play" %% "play-netty-server" % "2.7.3",
  "org.slf4j" % "slf4j-simple" % "1.7.21",
)
