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

import breeze.linalg.DenseVector
import rl.core.mdp.Environment
import rl.core.mdp.FlatWorld.flatWorldAction.{East, North, South, West}
import rl.core.mdp.FlatWorld.{flatWorldAction, flatWorldAgent, flatWorldPolicy, flatWorldState}
import rl.utils.rounded


/**
  * Created by Michael Wang on 2017-12-17.
  * To test Dynamic Programming for MDP
  */
object dptest extends App{
  val SIZE = 16
  val Y = 4

  implicit object flatWorldEnv extends Environment[DenseVector, flatWorldState, flatWorldAction]{
    def stateSpace:DenseVector[flatWorldState] = DenseVector.tabulate[flatWorldState](SIZE) { i => new flatWorldState(i, 0)}
    def actionSpace:Seq[flatWorldAction]= Seq(new North, new East, new South, new West)
    def getStates:DenseVector[flatWorldState] = currentStates
    var currentStates = stateSpace
    override def reward(state: flatWorldState, action: flatWorldAction): (flatWorldState, Double) = {
      val r = (action, state.id) match {
        case (_, 0 | 15) => (state.id, 0)
        case (a:North, 1 | 2 | 3) => (state.id, -1)
        case (a:North, _) => (state.id - 4, -1)
        case (a:East, 3 | 7 | 11) => (state.id, -1)
        case (a:East, _) => (state.id + 1, -1)
        case (a:South, 12 | 13 | 14) => (state.id, -1)
        case (a:South, _) => (state.id + 4, -1)
        case (a:West, 4 | 8 | 12) => (state.id, -1)
        case (a:West, _) => (state.id - 1, -1)
      }
      (flatWorldEnv.getStates(r._1), r._2)
    }
  }
  implicit val policy:flatWorldPolicy = new flatWorldPolicy

  import rl.core.mdp.ValueFunctions.Bellman
  Bellman.setDiscount(1.0)

  val result = flatWorldAgent.setEpoch(1000)
   // .setExitDelta(0.001)
    .observe

  println(result.map(a => rounded(3, a.value)))

}
