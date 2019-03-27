package csw.acceptance.runner

import java.io.File

import coursier._
import coursier.core.Configuration
import coursier.util.Task.sync
import coursier.util._

import scala.collection.JavaConverters._

class TestModule(projectName: String, version: String) {

  val testArtifact = Dependency(
    Module(
      Organization("com.github.tmtsoftware.csw"),
      ModuleName(s"${projectName}_2.12")
    ),
    version,
    Configuration.test
  )

  val files: Seq[File] = Fetch()
    .allArtifactTypes()
    .addDependencies(testArtifact)
    .addRepositories(Repositories.jitpack)
    .run()

  val classpath: String = files.mkString(":")

  val testJarRunpath: String =
    files
      .map(_.toString)
      .find(x ⇒ x.contains(projectName) && x.contains("tests.jar"))
      .getOrElse("")

  val cmds = List(
    "java",
    "-cp",
    classpath,
    "org.scalatest.tools.Runner",
    "-oDF",
    "-l",
    "csw.commons.tags.FileSystemSensitive",
    "-l",
    "csw.commons.tags.LoggingSystemSensitive",
    "-R",
    testJarRunpath
  )

  def run(): Process = {
    val builder = new ProcessBuilder(cmds.asJava).inheritIO()
    val process = builder.start()
    process.onExit().get()
  }
}