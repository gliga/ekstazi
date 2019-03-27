package p;

import org.scalatest._

class C1Suite extends FunSuite {

  new Another1();

  test("Outside class") {
    val outside = new Another2();
    assert(outside != null);
  }
}
