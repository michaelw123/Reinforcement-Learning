/*
 * Copyright (c) 2017 Michael Wang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package test.mdp

import rl.mdp.GridWorldMDP._

/**
  * Created by Michael Wang on 12/06/2017.
  */
object mdpTest extends App{

  val state = new gridWorldState(0,1)
  val aa = gridWorldReward(9.0)

  val qq = aa match {
    case  gridWorldReward(9.1) => "match"
    case _ => "not match"
  }
  println(aa)
  println(qq)

  val config = (new BellmanConfig)
    .setX(5)
    .setY(5)
    .setActionProb(0.25)
    .setDiscount(0.9)
    .setPolicy(gridWorldPolicy)
    .setEpisodes(1000)

  gridWorldAgent.setConfig(config)
  val allstates = config.allStates

  println(allstates.map(a => (a.x, a.y)))
  val aState = new gridWorldState(2, 4)
  println(aState.availableActions)

  val aDecision = gridWorldAgent.decision(aState, South)
  println(aDecision._1.x, aDecision._1.y, aDecision._2.reward)

  println(config.getX, config.getY)

  val result = gridWorldAgent.runAlgorithm(config)

  println(result.map(a => rounded(3, a.value)))


  def rounded(x: Int, n:Double) = {
    val w = math.pow(10, x)
    (n * w).toLong.toDouble / w
  }
}
