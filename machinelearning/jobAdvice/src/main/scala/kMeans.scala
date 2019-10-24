import java.io.PrintWriter
import java.io.{File, PrintWriter}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors

//采用mllib包而不是Ml包，mllib包基于RDD，而ml包基于DataFrame，且没有数据降维操作
object kMeans {
  val labelPath = "E:\\temp\\allOut70.csv"
  val dataPath = "file:///E:/temp/Result.csv"
  val centerPath = "E:\\temp\\centers.txt"
  val pointsPath = "E:\\temp\\points.txt"
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


    val writer1 = new PrintWriter(new File(centerPath))
    //    for( a<- 1 to 100){
    //      val kMeansModel = KMeans.train(allData, a, 1000)
    //      val kMeansCost = kMeansModel.computeCost(allData)
    //      writer.write(a+"\n")
    //      writer.write(kMeansCost.toString+"\n")
    //    }
    //    writer.close()
    //写入迭代50次后生成的20个中心点
    val kMeansModel = KMeans.train(allData, 20, 100)
    kMeansModel.clusterCenters.foreach(v =>writer1.write(v.toString+"\n"))
    writer1.close()
    //输出代价，代价越低越好
    val kMeansCost = kMeansModel.computeCost(allData)
    println("K-means Cost: "+ kMeansCost)
    //writer.write(kMeansCost.toString)
    //TODO collect无法导致分布式，但写文件必须Collect
    //Predict的结果是预测这个数据是哪个类簇里的，vec就是职业中的一行
    val writer2 = new PrintWriter(new File(pointsPath))
    allData.collect().foreach{
      vec => writer2.write(kMeansModel.predict(vec).toString+":"+vec.toString+"\n")
    }
    writer2.close()
  }
}
