import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.util.IntParam

/**
 * Receives text from multiple rawNetworkStreams and counts how many '\n' delimited
 * lines have the word 'the' in them. This is useful for benchmarking purposes. This
 * will only work with spark.streaming.util.RawTextSender running on all worker nodes
 * and with Spark using Kryo serialization (set Java property "spark.serializer" to
 * "org.apache.spark.serializer.KryoSerializer").
 * Usage: RawNetworkGrep <numStreams> <host> <port> <batchMillis>
 *   <numStream> is the number rawNetworkStreams, which should be same as number
 *               of work nodes in the cluster
 *   <host> is "localhost".
 *   <port> is the port on which RawTextSender is running in the worker nodes.
 *   <batchMillise> is the Spark Streaming batch duration in milliseconds.
 */
object Benchmark {
  def main(args: Array[String]) {
    if (args.length != 4) {
      System.err.println("Usage: RawNetworkGrep <numStreams> <host> <port> <batchMillis>")
      System.exit(1)
    }

    val (numStreams, host, port, batchMillis) = (args(0).toInt, args(1), args(2).toInt, args(3).toInt)
    val sparkConf = new SparkConf()
    sparkConf.setAppName("BenchMark")
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    sparkConf.set("spark.executor.extraJavaOptions", " -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+AggressiveOpts -XX:FreqInlineSize=300 -XX:MaxInlineSize=300 ")
    if (sparkConf.getOption("spark.master") == None) {
      // Master not set, as this was not launched through Spark-submit. Setting master as local."
      sparkConf.setMaster("local[*]")
    }

    // Create the context
    val ssc = new StreamingContext(sparkConf, Duration(batchMillis))

    val rawStreams = (1 to numStreams).map(_ =>
      ssc.rawSocketStream[String](host, port, StorageLevel.MEMORY_ONLY_SER)).toArray
    val union = ssc.union(rawStreams)
    union.count().map(c => s"Received $c records").print()
    ssc.start()
    ssc.awaitTermination()
  }
}

