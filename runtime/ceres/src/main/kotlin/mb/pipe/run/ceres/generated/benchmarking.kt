package mb.pipe.run.ceres.generated

import com.google.inject.Binder
import com.google.inject.Module
import mb.ceres.BuildContext
import mb.ceres.Builder
import mb.ceres.None
import mb.ceres.PathStampers
import mb.ceres.bindBuilder
import mb.ceres.builderMapBinder
import mb.pipe.run.ceres.path.WalkContents
import mb.pipe.run.ceres.path.plus
import mb.pipe.run.ceres.path.resolve
import mb.pipe.run.ceres.util.Tuple2
import mb.pipe.run.ceres.util.Tuple3
import mb.pipe.run.ceres.util.Tuple5
import mb.pipe.run.ceres.util.list
import mb.pipe.run.ceres.util.plus
import mb.pipe.run.ceres.util.tuple
import mb.vfs.path.PPath
import mb.vfs.path.PPaths

class main_benchmarking : Builder<ArrayList<String>, ArrayList<ArrayList<PPath>>> {
  override val id = "main_benchmarking"
  override fun BuildContext.build(input: ArrayList<String>): ArrayList<ArrayList<PPath>> {
    val benchmarkJar = requireOutput(build::class.java, None.instance)
    val pkg = "io.usethesource.criterion"
    val benchmarks: ArrayList<Tuple2<String, String>> = list(tuple("set", "${pkg}.JmhSetBenchmarks.(timeInsert)\$"), tuple("map", "${pkg}.JmhMapBenchmarks.(timeInsert)\$"))
    val javaSrcDir = resolve("./criterion/src/main/java/io/usethesource/criterion/impl/persistent")
    val scalaSrcDir = resolve("./criterion/src/main/scala/io/usethesource/criterion/impl/persistent/scala")
    val subjects: ArrayList<Tuple3<String, String, ArrayList<PPath>>> = list(tuple("clojure", "VF_CLOJURE", requireOutput(WalkContents::class.java, WalkContents.Input(javaSrcDir + "clojure", null)) + resolve("./lib/clojure.jar")), tuple("champ", "VF_CHAMP", requireOutput(WalkContents::class.java, WalkContents.Input(javaSrcDir + "champ", null)) + resolve("./lib/champ.jar")), tuple("scala", "VF_SCALA", requireOutput(WalkContents::class.java, WalkContents.Input(scalaSrcDir, null)) + resolve("./lib/scala.jar")), tuple("javaslang", "VF_JAVASLANG", requireOutput(WalkContents::class.java, WalkContents.Input(javaSrcDir + "javaslang", null)) + resolve("./lib/javaslang.jar")), tuple("unclejim", "VF_UNCLEJIM", requireOutput(WalkContents::class.java, WalkContents.Input(javaSrcDir + "unclejim", null)) + resolve("./lib/unclejim.jar")), tuple("dexx", "VF_DEXX", requireOutput(WalkContents::class.java, WalkContents.Input(javaSrcDir + "dexx", null)) + resolve("./lib/dexx.jar")), tuple("pcollections", "VF_PCOLLECTIONS", requireOutput(WalkContents::class.java, WalkContents.Input(javaSrcDir + "pcollections", null)) + resolve("./lib/pcollections.jar")))
    val jvmArgs = list("-Xms4G", "-Xmx4G", "-XX:-TieredCompilation", "-XX:+UseCompressedOops")
    val jmhArgs = list("-wi", "1", "-i", "1", "-r", "1", "-f", "0", "-gc", "true", "-v", "NORMAL", "-foe", "true", "-p", "producer=PURE_INTEGER", "-p", "sampleDataSelection=MATCH", "-p", "size=16")
    return subjects.map { subject -> benchmarks.map { benchmark -> requireOutput(run_benchmark::class.java, run_benchmark.Input(benchmarkJar, jvmArgs, jmhArgs, benchmark, subject)) }.toCollection(ArrayList<PPath>()) }.toCollection(ArrayList<ArrayList<PPath>>())
  }
}

class build : Builder<None, PPath> {
  override val id = "build"
  override fun BuildContext.build(input: None): PPath {
    val pomFile = resolve("./criterion/pom.xml")
    require(pomFile, PathStampers.modified)
    requireOutput(WalkContents::class.java, WalkContents.Input(resolve("./criterion/src"), PPaths.extensionsPathWalker(list("java", "scala")))).forEach { file -> require(file, PathStampers.modified) }
    mb.pipe.run.ceres.process.execute(list("/usr/local/bin/mvn", "verify", "-f", "${pomFile}"))
    val benchmarkJar = resolve("./criterion/target/benchmarks.jar")
    require(benchmarkJar, PathStampers.modified)
    return benchmarkJar
  }
}

class run_benchmark : Builder<run_benchmark.Input, PPath> {
  data class Input(val jar: PPath, val jvmArgs: ArrayList<String>, val jmhArgs: ArrayList<String>, val benchmark: Tuple2<String, String>, val subject: Tuple3<String, String, ArrayList<PPath>>) : Tuple5<PPath, ArrayList<String>, ArrayList<String>, Tuple2<String, String>, Tuple3<String, String, ArrayList<PPath>>> {
    constructor(tuple: Tuple5<PPath, ArrayList<String>, ArrayList<String>, Tuple2<String, String>, Tuple3<String, String, ArrayList<PPath>>>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  override val id = "run_benchmark"
  override fun BuildContext.build(input: run_benchmark.Input): PPath {
    val (benchmarkName, benchmarkPattern) = input.benchmark
    val (subjectName, subjectId, subjectDeps) = input.subject
    val csv = resolve("./results/${benchmarkName}_${subjectName}.csv")
    subjectDeps.forEach { dep -> require(dep, PathStampers.modified) }
    require(input.jar, PathStampers.modified)
    mb.pipe.run.ceres.process.execute(list("java") + input.jvmArgs + list("-jar", "${input.jar}") + benchmarkPattern + list("-p", "valueFactoryFactory=${subjectId}") + input.jmhArgs + list("-rff", "${csv}"))
    generate(csv, PathStampers.hash)
    return csv
  }
}


class CeresBuilderModule_benchmarking : Module {
  override fun configure(binder: Binder) {
    val builders = binder.builderMapBinder()

    binder.bindBuilder<run_benchmark>(builders, "run_benchmark")
    binder.bindBuilder<build>(builders, "build")
    binder.bindBuilder<main_benchmarking>(builders, "main_benchmarking")
  }
}
