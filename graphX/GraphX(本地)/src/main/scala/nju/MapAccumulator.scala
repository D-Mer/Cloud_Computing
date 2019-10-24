package nju

import org.apache.spark.util.AccumulatorV2

class MapAccumulator[T, K] extends AccumulatorV2[(T, K), Map[T, K]] {
  private var _resultMap: Map[T, K] = Map()

  override def isZero: Boolean = _resultMap.isEmpty

  override def copy(): MapAccumulator[T,K] = {
    val newMyAcc = new MapAccumulator[T,K]
    newMyAcc._resultMap = this._resultMap
    newMyAcc
  }

  override def reset(): Unit = _resultMap = Map()

  override def add(v: (T, K)): Unit = _resultMap += v._1 -> v._2

  override def merge(other: AccumulatorV2[(T, K), Map[T, K]]): Unit = other match {
    case o: MapAccumulator[T, K] => _resultMap = _resultMap ++ o._resultMap
    case _ => throw new UnsupportedOperationException(
      s"Cannot merge ${this.getClass.getName} with ${other.getClass.getName}")
  }

  override def value: Map[T, K] = _resultMap
}
