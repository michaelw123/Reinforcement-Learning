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

import breeze.linalg.DenseMatrix
/**
  * Created by Michael Wang on 12/04/2017.
  */
object MDP {
  trait Action
  trait Value
  trait Reward
  trait State

  trait Stateable[A, V, R, S]{
    def availableActions:Seq[A]
    def value:V
    def apply(action:A):(S, R)
  }
  trait Environment
  trait Agent[S, A, R, V] {
    def decision(state:S, action:A):(S, R)
  }

//  trait StateSpace
//  trait StateSpaceable[S] {
//    def allStates:DenseMatrix[S]
//  }
  trait Policy[S]
  trait DeterministicPolicy[S, A] extends Policy[S] {
    def pi(state:S):A
  }
  trait  StochasticPolicy[S, A] extends Policy[S] {
    def pi(state:S, action:A):Double
  }
//  trait MDPEnvironment[S<:State, CS[S]] extends Environment {
//    def allActions: Seq[Action]
//    def allStates:CS[S]
//  }

//  trait Actionable[A <: Action]  {
//  }
//  trait Valueable[V <: Value] {
//    def unapply:V
//  }
//  trait Rewardable[R <: Reward] {
//    def unapply:R
//  }
//  trait Policyable[P <: Policy, S <:State, A <:Action] {
//      def nextAction(s:S):A
//  }
//  trait Statable[CA[Action], S<:State, CS[S]] {
    //def availableActions : CA[Action]
   // def availableStates : CS[S]


}
