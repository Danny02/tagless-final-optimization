name := "doobie-batch-optimization"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions ++= Seq(
  "-feature",
  "-language:higherKinds",
  "-language:existentials",
  "-language:implicitConversions",
  "-language:postfixOps"
)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.typelevel" %% "cats-effect" % "1.0.0",
  "co.fs2" %% "fs2-core" % "1.0.0",
  "org.tpolecat" %% "doobie-core" % "0.6.0",
//  "org.tpolecat" %% "doobie-h2" % "0.6.0",
//  "org.tpolecat" %% "doobie-hikari" % "0.6.0"
)
