name := "zauberstuhl"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.scalaj" %% "scalaj-http" % "1.1.4"
)

play.Project.playScalaSettings
