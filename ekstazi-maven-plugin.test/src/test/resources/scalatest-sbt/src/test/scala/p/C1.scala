package p;

import collection.mutable.Stack
import org.scalatest._

class C1 extends FlatSpec {

  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    assert(stack.pop() === 2)
    assert(stack.pop() === 1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[String]
    intercept[NoSuchElementException] {
      emptyStack.pop()
    }
  }
}

class C1Suite extends FunSuite {
  // new testClassA1;
  test("AS: A should have ASCII value 41 hex") {
    // try {
    //   throw new RuntimeException();
    // } catch {
    //   case ex : Exception => ex.printStackTrace();
    // }

    assert('A' === 0x41)
  }

  test("Outside class") {
    val outside = new C1Outside();
    assert(outside != null);
  }
}
