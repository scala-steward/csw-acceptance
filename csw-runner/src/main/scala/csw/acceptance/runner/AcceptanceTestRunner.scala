package csw.acceptance.runner

import java.net.URLClassLoader

import org.scalatest.tools.Runner

class AcceptanceTestRunner(testProjectName: String) {

  private val testJarRunpath =
    getClass.getClassLoader
      .asInstanceOf[URLClassLoader]
      .getURLs
      .map(_.getPath)
      .find(x ⇒ x.contains(testProjectName) && x.contains("tests.jar"))
      .getOrElse("")

  def run(args: Array[String]): Unit =
    if (runTests(scalaTestParams)) System.exit(0)
    else System.exit(1)

  private val scalaTestParams: Array[String] = Array(
      "-oDF",
      "-C",
      "csw.acceptance.running.FileAcceptanceTestReporter",
      "-l",
      "csw.commons.tags.FileSystemSensitive",
      "-l",
      "csw.commons.tags.LoggingSystemSensitive",
      "-R",
      testJarRunpath
    )

  private def printReport(): Unit = {
    println("=" * 100)
    println(s"Running tests from jar: [$testJarRunpath]")
    println("=" * 100)
  }

  private def runTests(params: Array[String]): Boolean =
    if (!params.isEmpty) { printReport(); Runner.run(params)}
    else true
}
