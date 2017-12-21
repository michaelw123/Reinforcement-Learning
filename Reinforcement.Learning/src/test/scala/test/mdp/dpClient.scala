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

import breeze.linalg.{DenseMatrix, DenseVector}
import rl.core.mdp.Environment
import rl.core.mdp.FlatWorld.flatWorldAction
import test.mdp.dpClient.flatWorldAction.{East, North, South, West}
import rl.core.mdp.FlatWorld.{flatWorldAgent, flatWorldPolicy, flatWorldState}
import rl.utils.rounded


/**
  * Created by Michael Wang on 2017-12-17.
  * To test Dynamic Programming for MDP
  */
object dpClient extends App{
  implicit object flatWorldEnv extends Environment[DenseVector, flatWorldState, flatWorldAction]{
    val SIZE = 16
    def stateSpace:DenseVector[flatWorldState] = DenseVector.tabulate[flatWorldState](SIZE) { i => new flatWorldState(i, 0)}
    val actionSpace:Seq[flatWorldAction]= Seq(new North, new East, new South, new West)
    def getStates:DenseVector[flatWorldState] = currentStates
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
        case (_, _) => (state.id, 0) //shall not be here
      }
      (flatWorldEnv.getCurrentStates(r._1), r._2)
    }
    override def transactionProb(state:flatWorldState, action:flatWorldAction, nextState:flatWorldState):Double  = 0.25
    override def cost(state:flatWorldState, action:flatWorldAction):Double = 0.0
    override def reward(state:flatWorldState, action:flatWorldAction, nextState:flatWorldState):Double  =  reward(state, action)._2
    override def cost(state:flatWorldState, action:flatWorldAction, nextState:flatWorldState):Double  = 0.0
    override def availableTransactions(state:flatWorldState):Seq[(flatWorldAction, flatWorldState)] = {
      val actions = availableActions(state)
      for (action <- actions) yield (action, reward(state, action)._1)
    }
    override def availableActions(state:flatWorldState):Seq[flatWorldAction] = Seq(new North, new East, new South, new West)
  }
  implicit val policy:flatWorldPolicy = new flatWorldPolicy{
    //var actionProb : Seq[(Int, flatWorldAction, Double)] = Seq.tabulate(flatWorldEnv.stateSpace.length * flatWorldEnv.actionSpace.length)(i => (i, new North, 0.25) )
//     val actionProb : DenseMatrix[Double] = DenseMatrix.tabulate[Double] (flatWorldEnv.stateSpace.length, flatWorldEnv.actionSpace.length){
//      (i,j) =>0.25
//    }
//    override def getActionProb(state:flatWorldState,  action:flatWorldAction):Double = actionProb(state.id, action.value)
//    override def updateActionProb(state:flatWorldState, action:flatWorldAction, value:Double):Unit =  actionProb(state.id, action.value) = value
 //   override def availableActions(state: flatWorldState): Seq[flatWorldAction] = Seq(new North, new East, new South, new West)
  }
  object flatWorldAction {
    sealed
    case class North(override val value:Int = 0) extends flatWorldAction
    case class East(override val value:Int = 1) extends flatWorldAction
    case class South(override val value:Int = 2) extends flatWorldAction
    case class West(override val value:Int = 3) extends flatWorldAction
  }

  //import rl.core.mdp.ValueFunctions.Bellman
  //Bellman.setDiscount(0.9)

  import rl.core.mdp.ValueFunctions.optimalValueIteration

  flatWorldEnv.update(flatWorldEnv.stateSpace)

  val result = flatWorldAgent.setEpoch(1000)
    //.setExitDelta(0.001)
    .observe

  println(result.map(a => rounded(3, a.value)))

}