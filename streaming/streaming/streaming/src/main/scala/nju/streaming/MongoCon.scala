package nju.streaming

import com.mongodb.spark.config.WriteConfig

case class MongoCon(databaseName: String, mongoDBList: String, userName: String, passwd: String) {

  def produceConfg(collectionName: String): WriteConfig = {
    val uri = "mongodb://" + userName + ":" + passwd + "@" + mongoDBList + "/" + databaseName + "." + collectionName
    val options = scala.collection.mutable.Map(
      //大多数情况下从primary的replica set读，当其不可用时，从其secondary members读
      "readPreference.name" -> "primaryPreferred",
      "spark.mongodb.input.uri" -> uri,
      "spark.mongodb.output.uri" -> uri)
    WriteConfig(options)
  }
}
