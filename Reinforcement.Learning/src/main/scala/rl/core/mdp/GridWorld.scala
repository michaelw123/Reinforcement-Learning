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
package rl.core.mdp

import breeze.linalg.{DenseMatrix, DenseVector, ImmutableNumericOps, argmax, sum}
import breeze.numerics.abs
import rl.utils.rounded

import scala.annotation.tailrec
import rl.utils._
/**
  * Created by Michael Wang on 2017-12-09.
  */
object GridWorld {
  trait gridWorldAction extends Action
  object gridWorldAction {
    sealed
    case class North(override val value:Int=0) extends gridWorldAction
    case class East(override val value:Int=1) extends gridWorldAction
    case class South(override val value:Int=2) extends gridWorldAction
    case class West(override val value:Int=3) extends gridWorldAction
  }
  
  class gridWorldState(val id:(Int, Int), var value:Double) extends State[(Int, Int)]

  object gridWorldAgent extends Agent[gridWorldAction, DenseMatrix, gridWorldState]{
    def observe[VF <: ValueFunction, P <: Policy[gridWorldState, gridWorldAction], E <: Environment[DenseMatrix,gridWorldState, gridWorldAction]](env:E, policy:P)(implicit vf:VF): DenseMatrix[gridWorldState] = {
      @tailrec
      def iterating:Unit = {
        val newStates = observeOnce
        val delta: Double = sum(abs(env.getCurrentStates.map(a => a.value) - newStates.map(b => b.value)))
        val max=newStates.map(b => b.value).max
        println(s"delta=$delta, max=$max")
        //println(newStates.map(a => a.value))
        env.update(newStates)
        if (delta > exitDelta) {
          iterating
        } else {
          observeAndUpdatePolicy
          if (!policy.isStable) {
            iterating
          }
        }
      }
      //value iteration
      @tailrec
      def looping:Unit = {
        val backupStates = env.getCurrentStates
        val newStates = observeOnce
        //for (i <- 0 until 1) {
          //val newStates = observeOnce
          env.update(newStates)
          val r = newStates.map(a => rounded(1, a.value))
          //println(s"Epoch $i: $r")
        //}
        observeAndUpdatePolicy
        val delta: Double = sum(abs(backupStates.map(a => a.value) - newStates.map(b => b.value)))
        if (!policy.isStable || delta > 0.1) {
          looping
        }

      }
        def observeOnce: DenseMatrix[gridWorldState] = {
          val newStates = env.stateSpace
          newStates.map(state => {
            val action = policy.bestAction(state)
            val vrp = env.rewards(state, action).map(x => (x._1, x._2, x._3 * policy.actionProb(state, action)))
            state.value=vf.value(state, vrp) - env.cost(state, action)
          })
          newStates
        }
        def observeAndUpdatePolicy = {
        val newStates = env.stateSpace
        newStates.map(state => {
          var values = Map[gridWorldAction, Double]()
          val actions = policy.applicableActions(state)
          for (action <- actions) {
            val vrp = env.rewards(state, action).map(x => (x._1, x._2, x._3 * policy.actionProb(state, action)))
            values += (action -> (vf.value(state, vrp)- env.cost(state, action)))
          }
          policy.update(state, values.maxBy(_._2)._1)
        })
      }
      (valueIteration, policyIteration, exitDelta, epoch) match {
        case (false, false, 0.0, _) => looping
        case (false, false, _ , _) => loopingWithExitDelta
        case (false, true, _ , _) => policyIteration
        case (true, _, _, _) => valueIteration
      }
//      exitDelta match {
//        case 0.0 => looping
//        case _ => iterating
//      }
      env.getCurrentStates
    }
  }
}
