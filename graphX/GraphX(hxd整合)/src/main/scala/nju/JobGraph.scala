package nju

import java.text.SimpleDateFormat
import java.util.{Date, UUID}

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.{SparkConf, SparkContext}

object JobGraph {

  //数据源文件
  val path = "hdfs://172.19.240.210:9000/jars/noun0.csv"
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  var VMapIDKey: Map[VertexId, String] = Map()
  var VMapNameKey: Map[String, VertexId] = Map()
  var hadoopConnector = new ConnectHadoop("/graphX/")
  val VertexesFileName = "Vertexes.csv"
  val EdgesFileName = "Edges.csv"

  def main(args: Array[String]): Unit = {
    hadoopConnector.initDir()
    //屏蔽日志
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    // SparkContext
    val sparkConf = new SparkConf()
      .setAppName("JobGraph")
      .setMaster("spark://hadoop-master:7077")
    val sc = new SparkContext(sparkConf)

    val VMapIDs = new MapAccumulator[VertexId, String]
    var VMapNames = new MapAccumulator[String, VertexId]
    sc.register(VMapIDs, "VMapIDs")
    sc.register(VMapNames, "VMapName")
    val RDD_Parts = sc.defaultMinPartitions

    println("从文件中读取所有点..." + now())
    val Vertexes = sc.textFile(path)
      .map(line => line.split(",")(10))
      .filter(line => !line.isEmpty)
      .flatMap(words => words.split(";"))
      .map(name => getUUID(name) -> name)
    println("所有点集生成完成 " + now())

    println("开始生成所有点集map..." + now())
    Vertexes.foreach { points =>
      VMapIDs.add(points._1 -> points._2)
      VMapNames.add(points._2 -> points._1)
    }
    VMapIDKey = VMapIDs.value
    VMapNameKey = VMapNames.value

    println("所有点集map生成完成 " + now())

    println("从文件中读取所有边..." + now())
    val edges = sc.textFile(path, RDD_Parts)
      .map(line => line.split(",")(10))
      .filter(lines => !lines.isEmpty)
      .map(words => words.split(";").distinct)
      .flatMap { pointsInLine =>
        var edgesInLine: List[(VertexId, VertexId)] = List()
        var long1 = 0L
        var long2 = 0L
        for (i <- pointsInLine.indices) {
          long1 = getUUID(pointsInLine(i))
          for (j <- i + 1 until pointsInLine.length) {
            long2 = getUUID(pointsInLine(j))
            if (long1 <= long2) {
              edgesInLine :+= (long1, long2)
            } else {
              edgesInLine :+= (long2, long1)
            }
          }
        }
        edgesInLine
      }
      .map(edge => (edge, 1L))
      .reduceByKey(_ + _)
      .map(edge => Edge(edge._1._1, edge._1._2, edge._2))
    println("边集生成完成 " + now())

    println("开始构建原始图..." + now())
    var srcGraph = Graph(Vertexes, edges)
    println("找出图中度大于0的顶点..." + now())
    srcGraph = filterByDegree(srcGraph, 0)
    srcGraph.cache()
    println("构建原始图完成 " + now())
    //保存当前的图
    saveGraph(srcGraph, "")

    println("过滤图中权重小于 500 的边..." + now())
    val weight500Graph = filterByWeight(srcGraph, 500)
    saveGraph(weight500Graph, "weight1000")

    println("过滤图中权重小于 1000 的边..." + now())
    val weight1000Graph = filterByWeight(srcGraph, 1000)
    saveGraph(weight1000Graph, "weight1000")

    println("过滤图中权重小于 2000 的边..." + now())
    val weight2000Graph = filterByWeight(srcGraph, 2000)
    saveGraph(weight2000Graph, "weight2000")

    //筛选出边权重最高的10条边所连的所有端点及其相互的联系
    filterByWeightTop(srcGraph, 10, "src")
    filterByWeightTop(weight1000Graph, 10, "weight1000")
    filterByWeightTop(weight2000Graph, 10, "weight2000")

    //筛选出点度数最高的10个点所连的所有权重>500的边的端点及其相互的联系
    filterByDegreeTop(srcGraph, 10, "src")
    filterByDegreeTop(weight1000Graph, 10, "weight1000")
    filterByDegreeTop(weight2000Graph, 10, "weight2000")

    //筛选与名字为VName的点的边权重大于x的所有点的子图
    filterByVNameWeight(srcGraph, "bug", 300L)
    filterByVNameWeight(srcGraph, "bug", 500L)
    filterByVNameWeight(srcGraph, "bug", 1000L)
    filterByVNameWeight(srcGraph, "java", 500L)
    filterByVNameWeight(srcGraph, "java", 1000L)
    filterByVNameWeight(srcGraph, "java", 1500L)

    sc.stop()
  }


  // 筛选出边权重最高的10条边所连的所有端点及其相互的联系
  def filterByWeightTop(graph: Graph[String, Long], topNum: Int, prefix: String): Unit = {
    val WeightTop10 = graph.edges.sortBy(_.attr, ascending = false).take(10)
    val WeightTop10Graph = graph.subgraph(vpred = (id, name) => {
      var keys: Set[VertexId] = Set()
      WeightTop10.foreach(e => {
        keys += e.srcId
        keys += e.dstId
      })
      keys.contains(id)
    })
    saveGraph(WeightTop10Graph, prefix + "WeightTop" + topNum)
  }

  //筛选出度数最大的10个点所连接的所有点的子图
  def filterByDegreeTop(graph: Graph[String, Long], topNum: Int, prefix: String): Unit = {
    val DegreeTop10 = graph.degrees.sortBy(_._2, ascending = false).take(10)
    var DegreeTop10Graph = graph.subgraph(epred = e => {
      val keys = DegreeTop10.toMap
      (keys.contains(e.srcId) || keys.contains(e.dstId)) && e.attr > 500
    }
    )
    DegreeTop10Graph = filterByDegree(DegreeTop10Graph, 0)
    saveGraph(DegreeTop10Graph, prefix + "DegreeTop" + topNum)
  }

  //筛选与名字为name点的边权重大于x的所有点的子图
  def filterByVNameWeight(graph: Graph[String, Long], VName: String, weight: Long): Unit = {
    val subGraph = graph.subgraph(epred = e => {
      (e.dstId == VMapNameKey(VName) || e.srcId == VMapNameKey(VName)) && e.attr > weight
    })
    saveGraph(filterByDegree(subGraph, 0), VName + "Weight" + weight)
  }

  def filterByWeight(graph: Graph[String, Long], weight: Long): Graph[String, Long] = {
    val temG = graph.subgraph(epred = e => e.attr > weight)
    filterByDegree(temG, 0)
  }

  def filterByDegree(graph: Graph[String, Long], degree: Int): Graph[String, Long] = {
    val degrees = graph.degrees.collect.toMap
    graph.subgraph(vpred = (id, vd) => degrees.keySet.contains(id) && degrees(id) > 0)
  }

  def saveGraph(graph: Graph[String, Long], pathPrefix: String): Unit = {
    var outString = new StringBuilder
    println("将生成的节点写入文件..." + now())
    outString.append("id,label,name,degree\n")
    val degrees = graph.degrees.collect.toMap
    graph.vertices.collect.foreach(item => {
      outString.append(item._1.toString + "," + item._2.toString + "," + item._2.toString + "," + degrees.apply(item._1) + "\r\n")
    })
    hadoopConnector.createFile(pathPrefix + VertexesFileName, outString.toString)
    println("生成的节点写入文件完成 " + now())

    println("将生成的边写入文件..." + now())
    outString = new StringBuilder
    outString.append("Source,Target,weight,label\n")
    for (Edge(x, y, weight) <- graph.edges.collect()) {
      outString.append(x.toString + "," + y.toString + "," + weight.toString + "," + weight.toString + "\n")
    }
    hadoopConnector.createFile(pathPrefix + EdgesFileName, outString.toString)
    println("将生成的边写入文件完成 " + now())
  }

  def now(): String = {
    dateFormat.format(new Date)
  }

  def getUUID(name: String): Long = {
    UUID.nameUUIDFromBytes(name.getBytes).getMostSignificantBits
  }

}
