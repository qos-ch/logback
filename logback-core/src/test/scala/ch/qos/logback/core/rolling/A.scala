package ch.qos.logback.core.rolling

import collection.mutable.ListBuffer
import org.junit.Test

/**
 * Created by IntelliJ IDEA.
 * User: ceki
 * Date: 27.12.10
 * Time: 14:19
 * To change this template use File | Settings | File Templates.
 */

class A {


  val initial = List(1,2, 3, 5)
  def mon[B](f: Int => B): List[B] = {
    val b = new ListBuffer[B]
    var these = initial
    while (!these.isEmpty) {
      var that = f(these.head)
      b += that
      these = these.tail
    }
    b.toList
  }

  def asString(in: Any):String = {
    "-"+ in.toString;
  }

  @Test
  def doTest() {
     val res: List[String] = mon(asString);
     println(res)


  }

}