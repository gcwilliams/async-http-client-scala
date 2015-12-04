package uk.co.gcwilliams.http.async

import org.scalatest.{FlatSpec, Matchers}

/**
 * The task tests specs
 *
 * Created by GWilliams on 02/12/2015.
 */
class TaskSpec extends FlatSpec with Matchers {

  "Task(value)" should "wrap a value in a task and result in resolve" in {

    // arrange
    val number = 123

    // act
    val task = Task(number)

    // assert
    task.fork(n => n should be (number), e => fail())
  }

  "Task(exception)" should "wrap a value in a task and result in reject" in {

    // arrange
    val error = new RuntimeException

    // act
    val task = Task(error)

    // assert
    task.fork(_ => fail(), e => e should be (error))
  }

  "Task.map" should "map over the value in a task" in {

    // arrange
    val number = 123

    // act
    val task = Task(number).map(n => n * 2)

    // assert
    task.fork(n => n should be (246), e => fail())
  }

  "Task.chain" should "chain two tasks together" in {

    // arrange
    val taskOne = Task(123)
    val taskTwo = Task(123)

    // act
    val task =taskOne.chain(one => taskTwo.map(two => one + two))

    // assert
    task.fork(n => n should be (246), e => fail())
  }
}
