name := "zauberstuhl"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.scalaj" %% "scalaj-http" % "1.1.4",
  "org.apache.commons" % "commons-email" % "1.3",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.2"
)

play.Project.playScalaSettings
