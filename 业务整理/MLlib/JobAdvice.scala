import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}

//采用mllib包而不是Ml包，mllib包基于RDD，而ml包基于DataFrame，且没有数据降维操作
object JobAdvice {
  val dataPath = "hdfs://172.19.240.210:9000/MLlib/MLlib_for_show.csv"
  var hadoopConnector = new ConnectHadoop("/MLlib_out/")
  hadoopConnector.deleteFileDir("/MLlib_out/");

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("JobAdvice").setMaster("spark://hadoop-master:7077")
    val sc = new SparkContext(conf)
    val rawtxt = sc.textFile(this.dataPath)

    //将文本内容转化为Double类型的Vector集合
    val allData = rawtxt.map (line => Vectors.dense(line.split(",").map(_.toDouble)))

    allData.cache()
    var outString = new StringBuilder

    //分为k个核，最多迭代1000次
    val kMeansModel = KMeans.train(allData, k = 40, 1000)
    //Predict的结果是预测这个数据是哪个类簇里的
    allData.map { v =>
      kMeansModel.predict(v).toString
    }.saveAsTextFile("hdfs://172.19.240.210:9000/MLlib_out/")
    outString = new StringBuilder
    //写入迭代后生成的中心点
    val centers = kMeansModel.clusterCenters
    for (a <- centers.indices) {
      val line = centers(a).toString
      outString.append(line.substring(1, line.length - 1) + "\n")
    }
    hadoopConnector.createFile("MLlib_out_data_center_show.csv", outString.toString)
    val kMeansCost = kMeansModel.computeCost(allData)
    println("K-means Cost: " + kMeansCost)

    // 每轮最多迭代100次，输出各个k的cost用来选择k
    for (a <- 1 to 100) {
      val kMeansModel = KMeans.train(allData, a, 100)
      val kMeansCost = kMeansModel.computeCost(allData)
      outString.append(a + "," + kMeansCost.toString + "\n")
      println(a)
    }
    hadoopConnector.createFile("MLlib_out_k_cost.csv", outString.toString)

  }

}

