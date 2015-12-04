package uk.co.gcwilliams.http.async

/**
 * A task companion object with factory methods
 *
 */
object Task {

  /**
   * Wraps the value inside the task
   *
   * @param value the value
   * @return the task
   */
  def apply[A](value: A) = new Task[A]((resolve, reject) => resolve(value))

  /**
   * Wraps the value inside the task
   *
   * @param error the error
   * @return the task
   */
  def apply[A](error: Exception) = new Task[A]((resolve, reject) => reject(error))
}

/**
 * A task class, representing values dependent on time
 *
 * Created by GWilliams on 02/12/2015.
 */
class Task[+A](private val computation : (A => Unit, Exception => Unit) => Unit) {

  /**
   * Maps over the task, converting any value contained within
   *
   * @param mapper the mapping function
   * @return the task
   */
  def map[B](mapper: (A => B)) : Task[B] = {
    new Task[B]((resolve, reject) => computation(value => resolve(mapper(value)), reject))
  }

  /**
   * Chains the tasks
   *
   * @param mapper the mapping function
   * @return the task
   */
  def chain[B](mapper: (A => Task[B])) = {
    new Task[B]((resolve, reject) => computation(value => mapper(value).fork(resolve, reject), reject))
  }

  /**
   * Forks the task, running any underlying operations
   *
   * @param resolve the resolve callback
   * @param reject the reject callback
   */
  def fork(resolve: A => Unit, reject: Exception => Unit) = {
    computation(resolve, reject)
  }
}
