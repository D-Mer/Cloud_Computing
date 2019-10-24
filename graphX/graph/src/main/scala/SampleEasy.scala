import java.io.{File, PrintWriter}
import java.util.UUID

import MyGraphX.{now, savePath}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.graphstream.graph.implementations.{AbstractEdge, SingleGraph, SingleNode}

object SampleEasy {
  def main(args: Array[String]): Unit = {
    //屏蔽日志
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    // SparkContext
    val sparkConf = new SparkConf()
      .setAppName("helloGraphX")
      .setMaster("local[*]")
    val sc = new SparkContext(sparkConf)
    //构建点RDD[(Long, VD)]
    val verticesRDD: RDD[(VertexId, String)] = sc.makeRDD(Seq(
      (1L, "小明"),
      (2L, "小李"),
      (133L, "小王"),
      (138L, "小张")
    ))

    /** 这个如果要设置为无向图，
     * 边必须不能重复，
     * 所以最好先用分布式计算将边处理为无向的，
     * 即用filter(e => e.srcId > e.dstId => ...) */
    // 构建边RDD[Edge[ED]]
    val edgesRDD: RDD[Edge[String]] = sc.makeRDD(Seq(
      Edge(1, 133, "asd"),
      Edge(1, 138, "12"),
      Edge(2, 133, "5"),
      Edge(133, 138, "87"),
      Edge(1, 2, "42")
    ))

    /* 使用文件构建点和边 */
    val path1 = "E:/temp/vertexes.csv"
    val path2 = "E:/temp/edges.csv"
    // 顶点RDD[顶点的id,顶点的属性值]
    //    val verticesRDD: RDD[(VertexId, String)] = sc.textFile(path1).map { line =>
    //      val vertexId = line.split(",")(0).toLong
    //      val vertexName = line.split(",")(1)
    //      (vertexId, vertexName)
    //    }

    /** 这个如果要设置为无向图，
     * 边必须不能重复，
     * 所以最好先用分布式计算将边处理为无向的，
     * 即用filter(e => e.srcId > e.dstId => ...) */
    //    val edgesRDD: RDD[Edge[String]] = sc.textFile(path2).map { line =>
    //      val arr = line.split(",")
    //      val src = arr(0).toLong
    //      val dst = arr(1).toLong
    //      if (src > dst) {
    //        Edge(dst, src, arr(2))
    //      } else {
    //        Edge(src, dst, arr(2))
    //      }
    //    }

    // Graph = 点RDD + 边RDD
    var srcGraph = Graph(verticesRDD, edgesRDD)

    srcGraph = srcGraph.subgraph(epred = e => {
      val se: Set[Long] = Set(1L, 2L)
      se.contains(e.srcId) || se.contains(e.dstId)
    })

    val graph: SingleGraph = new SingleGraph("test")
    // 设置图的展示属性
    graph.addAttribute("ui.stylesheet.css", "url:(stylesheet.css)")
    graph.addAttribute("ui.quality")
    graph.addAttribute("ui.antialias")

    // 加载并设置点的内容和格式
    for ((id, name) <- srcGraph.vertices.collect()) {
      val node = graph.addNode(id.toString).asInstanceOf[SingleNode]
      node.addAttribute("ui.label", name)
    }
    // 加载并设置边的内容和格式
    for (Edge(x, y, weight) <- srcGraph.edges.collect()) {
      val edge = graph.addEdge(x.toString ++ "--" ++ y.toString, x.toString, y.toString, false).asInstanceOf[AbstractEdge]
      edge.addAttribute("ui.label", weight)
    }
    saveGraph(srcGraph, "")
    graph.display()
    sc.stop()
  }
  def saveGraph(graph: Graph[String, String], pathPrefix: String): Unit = {
    println("将生成的节点写入文件..." + now())
    //将所有节点写入文件
    val writerV = new PrintWriter(new File(savePath + pathPrefix + "vs.csv"))
    writerV.write("id,label,name,degree\r\n")
    val degrees = graph.degrees.collect.toMap
    graph.vertices.collect.foreach(item => writerV.write(item._1.toString + "," + item._2.toString + "," + item._2.toString + "," + degrees.apply(item._1) + "\r\n"))
    writerV.close()
    println("生成的节点写入文件完成 " + now())

    println("将生成的边写入文件..." + now())
    //将所有带权边写入文件
    val writerE = new PrintWriter(new File(savePath + pathPrefix + "es.csv"))
    writerE.write("Source,Target,weight,label\r\n")
    for (Edge(x, y, weight) <- graph.edges.collect()) {
      //println(x.toString + " : " + y.toString + " : " + weight.toString)
      writerE.write(x.toString + "," + y.toString + "," + weight.toString + "," + weight.toString + "\r\n")
    }
    writerE.close()
    println("将生成的边写入文件完成 " + now())
  }

}
