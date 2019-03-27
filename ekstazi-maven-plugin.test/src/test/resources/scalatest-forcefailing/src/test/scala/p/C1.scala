package p;

import collection.mutable.Stack
import org.scalatest._

class C1 extends FlatSpec {

  "A Stack" should "pop values in last-in-first-out order" in {
    if (true) throw new RuntimeException();
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    assert(stack.pop() === 2)
    assert(stack.pop() === 1)
  }
}
