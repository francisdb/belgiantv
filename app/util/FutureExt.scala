package util

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.Exception.nonFatalCatch
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

object FutureExt {

  class Deferred[+T](f: () => Future[T]) {
    def run(): Future[T] = f()
  }

  object Deferred {
    def apply[T](future: => Future[T]): Deferred[T] =
      new Deferred(() => nonFatalCatch.either(future).fold(Future.failed, identity))
  }

  def traverseWithBoundedParallelism[T, R](
    parallelism: Int = Runtime.getRuntime.availableProcessors()
  )(input: Iterable[T])(f: T => Future[R])(implicit ec: ExecutionContext): Future[Seq[R]] = {
    val operations = input.map(t => Deferred(f(t))).toSeq
    val deferred   = runWithBoundedParallelism(parallelism)(operations)
    deferred.run()
  }

  // from http://stackoverflow.com/questions/27085085/sequencing-scala-futures-with-bounded-parallelism-without-messing-around-with-e
  def runWithBoundedParallelism[T](
    parallelism: Int = Runtime.getRuntime.availableProcessors()
  )(operations: Iterable[Deferred[T]])(implicit ec: ExecutionContext): Deferred[Seq[T]] =
    if (parallelism > 0) Deferred {
      val indexedOps = operations.toIndexedSeq // index for faster access

      val promise = Promise[Seq[T]]()

      val acc       = new CopyOnWriteArrayList[(Int, T)] // concurrent acc
      val nextIndex = new AtomicInteger(parallelism)     // keep track of the next index atomically

      // this is not tail recursive but does not blow up because these are running async
      def run(operation: Deferred[T], index: Int): Unit =
        operation.run().onComplete {
          case Success(value) =>
            acc.add((index, value)) // accumulate result value

            if (acc.size == indexedOps.size) { // we've done
              import scala.jdk.CollectionConverters._
              // in concurrent setting next line may be called multiple times, that's why trySuccess instead of success
              promise.trySuccess(acc.asScala.view.sortBy(_._1).map(_._2).toList)
            } else {
              val next = nextIndex.getAndIncrement() // get and inc atomically
              if (next < indexedOps.size) { // run next operation if exists
                run(indexedOps(next), next)
              }
            }
          case Failure(NonFatal(t)) =>
            promise.tryFailure(t) // same here (may be called multiple times, let's prevent stdout pollution)
        }

      if (operations.nonEmpty) {
        indexedOps.view.take(parallelism).zipWithIndex.foreach((run _).tupled) // run as much as allowed
        promise.future
      } else {
        Future.successful(Seq.empty)
      }
    } else {
      throw new IllegalArgumentException("Parallelism must be positive")
    }

}
