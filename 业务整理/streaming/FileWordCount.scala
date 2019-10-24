package nju.streaming

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.UpdateOptions
import com.mongodb.spark.MongoConnector
import com.mongodb.spark.config.WriteConfig
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.bson.Document

object FileWordCount {
  def main(args: Array[String]) {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    //设置数据库参数
    val dbName = "homework"
    val mongoDBList = "172.19.240.210:27017"
    val userName = "hadoop"
    val passwd = "hadoop"
    val mongo = MongoCon(dbName, mongoDBList, userName, passwd)
    //初始化spark
    val path = "hdfs://172.19.240.210:9000/streaming"
    val sparkConf = new SparkConf().setAppName("FileWordCount").setMaster("spark://172.19.240.210:7077")
    val ssc = new StreamingContext(sparkConf, Seconds(3))

    //后面可复用的函数
    val universalUpsertFunction: Document => Document = { doc => new Document("$inc", new Document("count", doc.get("count"))) }

    val salaryReduceFunction: ((Int, Array[Int]), (Int, Array[Int])) => (Int, Array[Int]) = {
      (a, b) => {
        val list1 = a._2
        val list2 = b._2
        val list = Array(0, 0, 0, 0, 0, 0, 0, 0)
        for (i <- 0 to 7) {
          list(i) = list1(i) + list2(i)
        }
        (a._1 + b._1, list)
      }
    }

    val salaryDocumentFunction: ((String, (Int, Array[Int]))) => Document = { cityResult => {
      val document = new Document("type", cityResult._1)
      document.append("count", cityResult._2._1)
      for (i <- 0 to 7) {
        document.append("salary" + (i + 1).toString + "_count", cityResult._2._2(i))
      }
      document
    }
    }

    val salaryUpsertFunction: Document => Document = { doc => {
      val temp = new Document("count", doc.get("count"))
      for (i <- 0 to 7) {
        temp.append("salary" + (i + 1).toString + "_count", doc.get("salary" + (i + 1).toString + "_count"))
      }
      new Document("$inc", temp)
    }
    }

    //总输入流
    val lines = ssc.textFileStream(path).filter(line => line.split(",").length == 20)

    //统计学历要求
    val writeConfigOfEduCount = mongo.produceConfg("education_count")
    val edus = lines.map(line => {
      var str = line.split(",")(17)
      if (str == "学历不限") str = "不限"
      str
    })
    val edusPairs = edus.map(edu => (edu, 1))
    val eduResults = edusPairs.reduceByKey((a, b) => a + b)
    val eduDocument = eduResults.map(eduResult => {
      new Document("type", eduResult._1).append("count", eduResult._2)
    })
    eduDocument.foreachRDD(rdd => {
      saveAndUpdate(rdd, writeConfigOfEduCount, universalUpsertFunction);
    })

    //统计职业需求量
    val writeConfigOfJobCount = mongo.produceConfg("job_count")
    val filteredLines = lines.filter(line => line.split(",")(9) != " ")
    val jobNeedPairs = filteredLines.map(line => (line.split(",")(9), line.split(",")(3).toInt))
    val jobNeedResults = jobNeedPairs.reduceByKey((a, b) => a + b)
    val jobNeedDocument = jobNeedResults.map(jobNeedResult => new Document("type", jobNeedResult._1).append("count", jobNeedResult._2))
    jobNeedDocument.foreachRDD(rdd => saveAndUpdate(rdd, writeConfigOfJobCount, universalUpsertFunction))

    //统计职业种类
    val writeConfigOfJobClass = mongo.produceConfg("job_class_count")
    val jobClassesPairs = lines.map(line => (line.split(",")(5), (line.split(",")(3).toInt, handleSalaryString(line.split(",")(15)))))
    val jobClassPairs = jobClassesPairs.flatMap(item => item._1.split("/").map(jobClass => (jobClass, item._2)))
    val jobClassResults = jobClassPairs.reduceByKey(salaryReduceFunction)
    val jobClassDocuments = jobClassResults.map(salaryDocumentFunction)
    jobClassDocuments.foreachRDD(rdd => saveAndUpdate(rdd, writeConfigOfJobClass, salaryUpsertFunction))

    //统计城市
    val writeConfigOfCity = mongo.produceConfg("city_count")
    val cityPairs = lines.map(line => (line.split(",")(12), (line.split(",")(3).toInt, handleSalaryString(line.split(",")(15)))))
    val cityResults = cityPairs.reduceByKey(salaryReduceFunction)
    val cityDocuments = cityResults.map(salaryDocumentFunction)
    cityDocuments.foreachRDD(rdd => saveAndUpdate(rdd, writeConfigOfCity, salaryUpsertFunction))

    ssc.start()
    ssc.awaitTermination()
  }

  //处理数据中的薪资字段
  def handleSalaryString(input: String): Array[Int] = {
    var str = input
    val res = Array(0, 0, 0, 0, 0, 0, 0, 0)
    val salaryRank = Array(0, 3000, 5000, 8000, 10000, 15000, 20000, 30000)
    if (str.split("-").length != 2) return res
    if (str.charAt(str.length - 1) == '?')
      str = str.substring(0, str.length - 1)
    var min = 0
    var max = 0
    try {
      min = str.split("-")(0).toInt
      max = str.split("-")(1).toInt
    } catch {
      case ex: Exception => {
        return res
      }
    }
    val average = (min + max) / 2
    var i = 0
    for (i <- 1 to 7) {
      if (average < salaryRank(i)) {
        res(i - 1) = 1
        return res
      } else if (i == 7) {
        res(i) = 1
        return res
      }
    }
    res
  }

  //封装update mongodb数据库的操作
  def saveAndUpdate(rdd: RDD[Document], writeConfig: WriteConfig,
                    upsertFunction: Document => Document): Unit = {
    val DefaultMaxBatchSize = 1024
    val mongoConnector = MongoConnector(writeConfig.asOptions) // Gets the connector
    rdd.foreachPartition(iter => if (iter.nonEmpty) {
      mongoConnector.withCollectionDo(writeConfig, { collection: MongoCollection[Document] => // The underlying Java driver MongoCollection instance.
        val updateOptions = new UpdateOptions().upsert(true)
        iter.grouped(DefaultMaxBatchSize).foreach(batch =>

          // Change this block to update / upsert as needed
          batch.map { doc =>
            collection.updateOne(new Document("type", doc.get("type")), upsertFunction(doc), updateOptions)
          })
      })
    })
  }
}