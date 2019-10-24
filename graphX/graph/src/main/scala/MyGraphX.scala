import java.io.{BufferedReader, File, InputStream, InputStreamReader, PrintWriter}
import java.text.SimpleDateFormat
import java.util.{Date, UUID}

import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.graphstream.graph.implementations.{AbstractEdge, SingleGraph, SingleNode}
import org.graphstream.ui.swingViewer.View
import org.graphstream.ui.swingViewer.Viewer


object MyGraphX {

  //数据源文件
  val path = "file:///E:/temp/AllEng.csv"
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  //生成的原始图的点和边文件的路径和名称，因为要保存和过滤后的文件所以切割了一下
  val savePath = "E:\\temp\\"
  val VertexesFileName = "Vertexes.csv"
  val EdgesFileName = "Edges.csv"
  var VMapNameKey: Map[String, VertexId] = Map()
  var VMapIDKey: Map[VertexId, String] = Map()

  def main(args: Array[String]): Unit = {
    //屏蔽日志
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    // SparkContext
    val sparkConf = new SparkConf()
      .setAppName("a")
      .setMaster("local[*]")
    val sc = new SparkContext(sparkConf)

    println("从文件中读取所有点..." + now())
    // 从文件中读取所有技术名词
    val nameRDD: RDD[Set[String]] = sc.textFile(path)
      .map { line =>
      val temp = line.split(",")
      if (temp(10).isEmpty) {
        Set()
      } else {
        temp(10).split(";").toSet[String]
      }
    }
    println("从文件中读取所有点完成 " + now())

    println("开始生成所有点集..." + now())
    //生成点集
    val Vertexes: RDD[(VertexId, String)] =
      nameRDD.flatMap(set => {
      var m: Map[VertexId, String] = Map()
      set.foreach(name => {
        m += (getUUID(name) -> name)
      })
      m
    })
    println("所有点集生成完成 " + now())

    println("开始生成所有点集map..." + now())
    //生成点的名称和id的map
    Vertexes.collect.foreach(item => {
      VMapIDKey += item._1 -> item._2
      VMapNameKey += item._2 -> item._1
    })
    println("所有点集map生成完成 " + now())

    println("从文件中读取所有边..." + now())
    //从文件里提取所有的边
    val EdgeList: RDD[(VertexId, VertexId)] =
      sc.textFile(path).repartition(3) flatMap { line =>
      var items = line.split(",")(10).split(";")
      items = items.distinct
      var tempResult: List[(VertexId, VertexId)] = List()
      var long1 = 0L
      var long2 = 0L
      for (i <- items.indices) {
        long1 = getUUID(items(i))
        for (j <- i + 1 until items.length) {
          long2 = getUUID(items(j))
          if (long1 <= long2) {
            tempResult :+= (long1, long2)
          } else {
            tempResult :+= (long2, long1)
          }
        }
      }
      tempResult
    }
    println("从文件中读取所有边完成 " + now())

    println("开始生成边集..." + now())
    println(EdgeList.count())
    val edges = EdgeList.map(edge => (edge, 1L))
      .reduceByKey((a, b) => a + b)
      .map(edge => Edge(edge._1._1, edge._1._2, edge._2))
    //    println("输出边集：")
    //    edges.foreach(e => println(e))
    println("边集生成完成 " + now())

    println("开始构建原始图..." + now())
    var srcGraph = Graph(Vertexes, edges)
    println("构建原始图完成 " + now())

    //    println("输出所有点度的信息")
    //    srcGraph.degrees.foreach(i => println(i))

    println("找出图中度大于0的顶点..." + now())
    srcGraph = filterByDegree(srcGraph, 0)

    //保存当前的图
    saveGraph(srcGraph, "")

    //    println("找出图中度大于0的顶点..." + now())
    //    filteredVertexes = srcGraph.degrees.filter(v => v._2 > 0).map(v => v._1 -> VMapIDKey(v._1))
    //    //filteredVertexes.foreach(e => println(e))

    println("过滤图中权重小于 500 的边..." + now())
    val weight500Graph = filterByWeight(srcGraph, 10)
    saveGraph(weight500Graph, "weight10")

    println("过滤图中权重小于 1000 的边..." + now())
    val weight1000Graph = filterByWeight(srcGraph, 20)
    saveGraph(weight1000Graph, "weight20")

    println("过滤图中权重小于 2000 的边..." + now())
    val weight2000Graph = filterByWeight(srcGraph, 30)
    saveGraph(weight2000Graph, "weight30")

    println("过滤图中权重小于 2000 的边..." + now())
    val weight22000Graph = filterByWeight(srcGraph, 40)
    saveGraph(weight22000Graph, "weight40")

    println("过滤图中权重小于 2000 的边..." + now())
    val weight220000Graph = filterByWeight(srcGraph, 50)
    saveGraph(weight220000Graph, "weight50")

//    //筛选出边权重最高的10条边所连的所有端点及其相互的联系
//    filterByWeightTop(srcGraph, 10, "src")
//    filterByWeightTop(weight1000Graph, 10, "weight1000")
//    filterByWeightTop(weight2000Graph, 10, "weight2000")
//
//    //筛选出点度数最高的10个点所连的所有权重>500的边的端点及其相互的联系
//    filterByDegreeTop(srcGraph, 10, "src")
//    filterByDegreeTop(weight1000Graph, 10, "weight1000")
//    filterByDegreeTop(weight2000Graph, 10, "weight2000")
//
//    //筛选与名字为VName的点的边权重大于x的所有点的子图
//    filterByVNameWeight(srcGraph, "bug", 300L)
//    filterByVNameWeight(srcGraph, "bug", 500L)
//    filterByVNameWeight(srcGraph, "bug", 1000L)
//    filterByVNameWeight(srcGraph, "java", 500L)
//    filterByVNameWeight(srcGraph, "java", 1000L)
//    filterByVNameWeight(srcGraph, "java", 1500L)
//
//    //筛选与名字为VName的点的边权重Top 10的所有点的子图
///*    filterByVNameTop(srcGraph, "bug", 10)
//    filterByVNameTop(srcGraph, "java", 10)*/
//
//    val reader = new BufferedReader(new InputStreamReader(System.in))
//    reader.readLine()
//    reader.close()
//    sc.stop()
//  }
//
//  // 筛选出边权重最高的10条边所连的所有端点及其相互的联系
//  def filterByWeightTop(graph: Graph[String, Long], topNum: Int, prefix: String): Unit = {
//    val WeightTop10 = graph.edges.sortBy(_.attr, ascending = false).take(10)
//    val WeightTop10Graph = graph.subgraph(vpred = (id, name) => {
//      var keys: Set[VertexId] = Set()
//      WeightTop10.foreach(e => {
//        keys += e.srcId
//        keys += e.dstId
//      })
//      keys.contains(id)
//    })
//    saveGraph(WeightTop10Graph, prefix + "WeightTop" + topNum)
//    //showGraph(WeightTop10Graph)
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
    //showGraph(WeightTop10Graph)
  }

  //筛选与名字为name点的边权重大于x的所有点的子图
  def filterByVNameWeight(graph: Graph[String, Long], VName: String, weight: Long): Unit = {
    val subGraph = graph.subgraph(epred = e => {
      (e.dstId == VMapNameKey(VName) || e.srcId == VMapNameKey(VName)) && e.attr > weight
    })
    saveGraph(filterByDegree(subGraph, 0), VName + "Weight" + weight)
  }

  //筛选与名字为name点的边权重Top n的点的子图
/*  def filterByVNameTop(graph: Graph[String, Long], VName: String, n: Int): Unit = {
    val subGraph = graph.subgraph(epred = e => {
      e.dstId == VMapNameKey(VName) || e.srcId == VMapNameKey(VName)
    })
    saveGraph(filterByDegree(subGraph, 0), VName + "Top" + n)
  }*/

  def showGraph(srcGraph: Graph[String, Long]): Unit = {
    println("加载并设置点的内容和格式..." + now())
    val graph: SingleGraph = new SingleGraph("test")
    // 设置图的展示属性
    graph.addAttribute("ui.stylesheet", "url:(stylesheet.css)")
    graph.addAttribute("ui.quality")
    graph.addAttribute("ui.antialias")

    for ((id, name) <- srcGraph.vertices.collect) {
      //for ((id, name) <- srcGraph.vertices.collect()) {
      val node = graph.addNode(id.toString).asInstanceOf[SingleNode]
      node.addAttribute("ui.label", name)
    }
    println("加载并设置点的内容和格式完成 " + now())

    println("加载并设置边的内容和格式..." + now())
    for (Edge(x, y, weight) <- srcGraph.edges.collect) {
      //for (Edge(x, y, weight) <- srcGraph.edges.collect()) {
      val edge = graph.addEdge(x.toString ++ "--" ++ y.toString, x.toString, y.toString, false).asInstanceOf[AbstractEdge]
      edge.addAttribute("ui.label", weight.toString)
      //输出图中所有实际的边
      //println(x.toString + " : " + y.toString + " : " + weight.toString)
    }
    println("加载并设置边的内容和格式完成 " + now())

    println("所有步骤完成！开始展示图形..." + now())

    graph.display()

  }

  def filterByWeight(graph: Graph[String, Long], weight: Long): Graph[String, Long] = {
    val temG = graph.subgraph(epred = e => e.attr > weight)
    filterByDegree(temG, 0)
  }

  def filterByDegree(graph: Graph[String, Long], degree: Int): Graph[String, Long] = {
    val degrees = graph.degrees.collect.toMap
    graph.subgraph(vpred = (id, name) => degrees.keySet.contains(id) && degrees(id) > 0)
  }

  def saveGraph(graph: Graph[String, Long], pathPrefix: String): Unit = {
    println("将生成的节点写入文件..." + now())
    //将所有节点写入文件
    val writerV = new PrintWriter(new File(savePath + pathPrefix + VertexesFileName))
    writerV.write("id,label,name,degree\r\n")
    val degrees = graph.degrees.collect.toMap
    graph.vertices.collect.foreach(item => writerV.write(item._1.toString + "," + item._2.toString + "," + item._2.toString + "," + degrees.apply(item._1) + "\r\n"))
    writerV.close()
    println("生成的节点写入文件完成 " + now())

    println("将生成的边写入文件..." + now())
    //将所有带权边写入文件
    val writerE = new PrintWriter(new File(savePath + pathPrefix + EdgesFileName))
    writerE.write("Source,Target,weight,label\r\n")
    for (Edge(x, y, weight) <- graph.edges.collect()) {
      //println(x.toString + " : " + y.toString + " : " + weight.toString)
      writerE.write(x.toString + "," + y.toString + "," + weight.toString + "," + weight.toString + "\r\n")
    }
    writerE.close()
    println("将生成的边写入文件完成 " + now())
  }

  def now(): String = {
    dateFormat.format(new Date)
  }

  def getUUID(name: String): Long = {
    UUID.nameUUIDFromBytes(name.getBytes).getMostSignificantBits
  }

}
