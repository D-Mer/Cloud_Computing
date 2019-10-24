import java.io.{BufferedReader, File, FileReader, FileWriter, InputStreamReader, PrintWriter}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import spire.std.byte

//采用mllib包而不是Ml包，mllib包基于RDD，而ml包基于DataFrame，且没有数据降维操作
object kMeansTest {
  val path = "E:\\temp\\"
  val labelPath = "E:\\temp\\allOut70.csv"
  val dataPath = "file:///E:/temp/Result.csv"
  val kcost = "k-cost.csv"
  val center = "centers.csv"
  val points = "points.csv"

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("KMeansTest").setMaster("local[2]")
    val sc = new SparkContext(conf)
    val rawtxt = sc.textFile(this.dataPath)

    //将文本内容转化为Double类型的Vector集合
    val allData = rawtxt.map {
      line =>
        Vectors.dense(line.split(",").map(_.toDouble))
    }
    allData.cache()
    //分为20个子集，最多迭代50次

    //输出各个k的cost用来选择k
    val writer1 = new FileWriter(new File(path + kcost))
    writer1.write("k,cost")
    for (a <- 1 to 100) {
      val kMeansModel = KMeans.train(allData, a, 1000)
      val kMeansCost = kMeansModel.computeCost(allData)
      writer1.write(a + ",")
      writer1.write(kMeansCost.toString + "\n")
    }
    writer1.close()

    // 在上面的代价函数中发现k值在30 - 50开始变得平缓
    // 循环生成k值从30 到 50的结果
    for (i <- 30 until 51) {
      val file = new File(labelPath)
      val fr = new FileReader(file)
      val fileReader = new BufferedReader(fr)
      val writer2 = new PrintWriter(new File(path + i + center))

      //写入迭代50次后生成的20个中心点

      val kMeansModel = KMeans.train(allData, i, 100000, 100000)
      val centers = kMeansModel.clusterCenters
      for (a <- centers.indices) {
        val line = centers(a).toString
        writer2.write(line.substring(1, line.length - 1) + "\n")
      }

      writer2.close()
      //输出代价，代价越低越好
      val kMeansCost = kMeansModel.computeCost(allData)
      println("K-means Cost: " + kMeansCost)
      //writer.write(kMeansCost.toString)
      //TODO collect无法导致分布式，但写文件必须Collect
      //Predict的结果是预测这个数据是哪个类簇里的，vec就是职业中的一行
      fileReader.readLine()
      val writer3 = new PrintWriter(new File(path + i + points))
      allData.collect().foreach {
        vec => {
          val line = vec.toString
          writer3.write(fileReader.readLine().split(",")(0) + "," + kMeansModel.predict(vec).toString + "," + line.substring(1, line.length - 1) + "\n")
        }

      }
      fileReader.close()
      fr.close()

    }

  }


}

