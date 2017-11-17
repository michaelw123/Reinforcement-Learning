package rl.kArmBandit

import breeze.linalg._
import breeze.numerics.{exp, log, sqrt}
import breeze.stats._
import breeze.plot._
import breeze.stats.distributions.{Binomial, Rand, RandBasis, ThreadLocalRandomGenerator}
import org.apache.commons.math3.random.MersenneTwister

import scala.annotation.tailrec

/*
  * Created by Michael Wang on 10/30/2017.
  */
class Bandit (kArm: Int = 10, epsilon:Double = 0, stepSize:Double = 0.1, incremental:Boolean=false, initial:Double = 0, ucb:Int = 0, gradient:Int=0) {
  val trueQ = DenseVector.fill[Double](kArm)(math.random)
  val qEstimation = DenseVector.fill[Double](kArm)(initial)
  val actionCount = Array.fill[Int](kArm)(0)
  var time = 0
  var averageReward = 0.0
  def getAction = //scala.util.Random.nextInt(10)
    (epsilon, ucb, gradient) match {
      case (0, 0, 0) => argmax(qEstimation)
      case (_, 0, 0) => if (Binomial(1, epsilon).draw == 1) scala.util.Random.nextInt(kArm) else  argmax(qEstimation)
      case (_, _, 0) => argmax(
        for ((est, count) <- qEstimation.toArray zip actionCount) yield  est + ucb * sqrt(log(time+1)/(count+1))
      )
//      case (_, _, _) => {
//        val expEstimation = qEstimation.map( a => exp(a))
//        val sum = sum(expEstimation)
//        val actionProb = expEstimation.map(a => a/sum)
//        Rand.choose(qEstimation, actionProb)
//      }
  }
  def takeAction(arm:Int) = {
    val reward = trueQ.valueAt(arm) + math.random
    time += 1
    averageReward = (time -1)/time * averageReward + reward/time
    actionCount(arm) = actionCount(arm)+1
    if (incremental) {
      qEstimation(arm) += stepSize * (reward - qEstimation(arm)) //constant stepsize
    } else {
        qEstimation(arm) += 1.0 / actionCount(arm) * (reward - qEstimation(arm))  //sample - average
    }
    reward
  }
  def bestAction:Int = argmax(trueQ)
}
object kArmBandit extends App{
  //ucbEpsilonGreedy(1000, 4000)
  incrementalEpsilonGreedy(1000, 4000)
  epsilonGreedyAverageRewards(1000, 4000)
  //epsilonGreedyBestActions(1000, 4000)
  optimisticEpsilonGreedy(1000, 6000)
  def ucbEpsilonGreedy(nBandits:Int, time:Int) = {
   val bandits = (new Array[Bandit](nBandits)).map(_ => new Bandit(10, 0.1, 0.1, true, 0, 2))
   val (bestActions, average) = banditSimulation(nBandits, time + 1, bandits)
   val f = Figure()
   val p = f.subplot(0)
   p += plot(linspace(0, time+1, time+1), mean(average(::, *)).inner, colorcode=color(0.1))
   p.xlabel = "Steps"
   p.ylabel = "Best average with initial value of "+0
   p.title = "epslon ="+0.1
 }
  def optimisticEpsilonGreedy(nBandits:Int, time:Int) = {
    val epsilons = Seq(0, 0.005, 0.01, 0.1)
    val initails = Seq(0, 1, 5)
    for (initial <- initails) {
      val bandits = (new Array[Bandit](nBandits)).map(_ => new Bandit(10, 0.1, 0.1, true, initial))
      val (bestActions, average) = banditSimulation(nBandits, time + 1, bandits)
      val f = Figure()
      val p = f.subplot(0)
      p += plot(linspace(0, time+1, time+1), sum(bestActions(::, *)).inner, colorcode=color(0.1))
      p.xlabel = "Steps"
      p.ylabel = "Best Action count with initial value of "+initial
      p.title = "epslon ="+0.1
    }

  }
  def incrementalEpsilonGreedy(nBandits:Int, time:Int) = {
    val epsilons = Seq(0, 0.005, 0.01, 0.1)
    for (epslon <- epsilons) {
      val bandits = (new Array[Bandit](nBandits)).map(_ => new Bandit(10, epslon, 0.2, true))
      val (bestActions, average) = banditSimulation(nBandits, time + 1, bandits)
      val f = Figure()
      val p = f.subplot(0)
      p += plot(linspace(0, time+1, time+1), sum(bestActions(::, *)).inner, colorcode=color(epslon))
      p.xlabel = "Steps"
      p.ylabel = "Best Action count"
      p.title = "epslon ="+epslon
    }

  }
  def epsilonGreedyBestActions(nBandits:Int, time:Int) = {
    val epsilons = Seq(0.005, 0.01, 0.1)
    for (epslon <- epsilons) {
      val bandits = (new Array[Bandit](nBandits)).map(_ => new Bandit(10, epslon, 0.1))
      val (bestActions, average) = banditSimulation(nBandits, time + 1, bandits)
      val f = Figure()
      val p = f.subplot(0)
      p += plot(linspace(0, time+1, time+1), sum(bestActions(::, *)).inner, colorcode=color(epslon))
      p.xlabel = "Steps"
      p.ylabel = "Best Action count"
      p.title = "epslon ="+epslon
    }
  }

  def epsilonGreedyAverageRewards(nBandits:Int, time:Int) = {
    val epsilons = Seq(0.005, 0.01, 0.1)
    val colors = Seq("BLUE", "RED", "BLACK")

    for (epslon <- epsilons) {
      val bandits = (new Array[Bandit](nBandits)).map(_ => new Bandit(10, epslon, 0.1))
      val (bestActions, average) = banditSimulation(nBandits, time + 1, bandits)
      val f = Figure()
      val p = f.subplot(0)
      p += plot(linspace(0, time+1, time+1), mean(average(::, *)).inner, colorcode=color(epslon))
      p.xlabel = "Steps"
      p.ylabel = "Average Rewards"
      p.title = "epslon ="+epslon
    }
  }
  private def  color(epslon:Double):String = epslon match {
    case 0 => "BLACK"
    case 0.1 => "RED"
    case 0.01 => "BLUE"
    case 0.001 => "YELLOW"
    case _ => "RED"
  }
  def banditSimulation(n:Int, time:Int, bandits:Array[Bandit]) = {
    val bestActionCounts = DenseMatrix.zeros[Double] (bandits.length, time)
    val averageRewards = DenseMatrix.zeros[Double] (bandits.length, time)
    for (i <- 0 until n; t <- 1 until time ) {
          val bandit = bandits(i)
          val action = bandit.getAction
          val reward = bandit.takeAction(action)
          averageRewards(i, t) += reward
          if (action == bandit.bestAction) bestActionCounts(i, t) += 1
    }
    (bestActionCounts, averageRewards.map(_/bandits.length) )
  }
}

object ExtendedRand extends RandBasis(new ThreadLocalRandomGenerator(new MersenneTwister()) {
  def weightedChoose[T](c: Iterable[T], w:Iterable[Double]):Rand[T] = new Rand[T] {
    def draw() = {
      val sample = Rand.uniform.draw
      var index = 0
      val d = go(0.0, w.toArray)
      val elems = c.iterator
      var i = 1
      var e = elems.next()
      while(i < d) {
        e = elems.next()
        i += 1
      }
      e
      @tailrec
      def go(s:Double, a:Array[Double]):Int = {
        index += 1
        if (s+a.head > sample) index
        else go(s+a.head, a.tail)
      }

    }
  }
}

//private final case class WeightedRand[@specialized(Int, Double) T, @specialized(Int, Double) U](rand: Rand[T], weights:DenseVector[Double]) extends Rand[U] {
//  def draw() = {
//    val sample = Rand.uniform.draw
//    var sum:Double = 0.0
//    val weightsArray:Array[Double] = weights.toArray
//    var index = 0
//    val d = go(sum, weightsArray)
//    @tailrec
//    def go(s:Double, a:Array[Double]):Int = {
//      index += 1
//      if (s+a.head > sample) index
//      else go(s+a.head, a.tail)
//    }
//    d
//  }
//  //override def drawOpt() = rand.drawOpt().map(func)
//  //override def map[E](f : U=>E):Rand[E] = WeightedRand(rand, (x:T) => f(func(x)))
//}