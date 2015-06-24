package io.getclump

import utest._

object ClumpApiSpec extends Spec {

  val tests = TestSuite {

    "the Clump object" - {

      "allows to create a constant clump" - {

        "from a future (Clump.future)" - {

          "success" - {
            "optional" - {
              "defined" - {
                assert(clumpResult(Clump.future(Future.successful(Some(1)))) == Some(1))
              }
              "undefined" - {
                assert(clumpResult(Clump.future(Future.successful(None))) == None)
              }
            }
            "non-optional" - {
              assert(clumpResult(Clump.future(Future.successful(1))) == Some(1))
            }
          }

          "failure" - {
            intercept[IllegalStateException] {
              clumpResult(Clump.future(Future.failed(new IllegalStateException)))
            }
          }
        }

        "from a value (Clump.apply)" - {
          "propogates exceptions" - {
            val clump = Clump { throw new IllegalStateException }
            intercept[IllegalStateException] {
              clumpResult(clump)
            }
          }

          "no exception" - {
            assert(clumpResult(Clump(1)) == Some(1))
          }
        }

        "from a value (Clump.value)" - {
          assert(clumpResult(Clump.value(1)) == Some(1))
        }

        "from a value (Clump.successful)" - {
          assert(clumpResult(Clump.successful(1)) == Some(1))
        }

        "from an option (Clump.value)" - {

          "defined" - {
            assert(clumpResult(Clump.value(Option(1))) == Option(1))
          }

          "empty" - {
            assert(clumpResult(Clump.value(None)) == None)
          }
        }

        "failed (Clump.exception)" - {
          intercept[IllegalStateException] {
            clumpResult(Clump.exception(new IllegalStateException))
          }
        }

        "failed (Clump.failed)" - {
          intercept[IllegalStateException] {
            clumpResult(Clump.failed(new IllegalStateException))
          }
        }
      }

      "allows to create a clump traversing multiple inputs (Clump.traverse)" - {
        "list" - {
          val inputs = List(1, 2, 3)
          val clump = Clump.traverse(inputs)(i => Clump.value(i + 1))
          assert(clumpResult(clump) == Some(List(2, 3, 4)))
        }
        "set" - {
          val inputs = Set(1, 2, 3)
          val clump = Clump.traverse(inputs)(i => Clump.value(i + 1))
          assert(clumpResult(clump) == Some(Set(2, 3, 4)))
        }
        "seq" - {
          val inputs = Seq(1, 2, 3)
          val clump = Clump.traverse(inputs)(i => Clump.value(i + 1))
          assert(clumpResult(clump) == Some(Seq(2, 3, 4)))
        }
      }

      "allows to collect multiple clumps - only one (Clump.collect)" - {
        "list" - {
          val clumps = List(Clump.value(1), Clump.value(2))
          assert(clumpResult(Clump.collect(clumps)) == Some(List(1, 2)))
        }
        "set" - {
          val clumps = Set(Clump.value(1), Clump.value(2))
          assert(clumpResult(Clump.collect(clumps)) == Some(Set(1, 2)))
        }
        "seq" - {
          val clumps = Seq(Clump.value(1), Clump.value(2))
          assert(clumpResult(Clump.collect(clumps)) == Some(Seq(1, 2)))
        }
      }

      "allows to create an empty Clump (Clump.empty)" - {
        assert(clumpResult(Clump.empty) == None)
      }

      "allows to join clumps" - {

        def c(int: Int) = Clump.value(int)

        "2 instances" - {
          val clump = Clump.join(c(1), c(2))
          assert(clumpResult(clump) == Some(1, 2))
        }
        "3 instances" - {
          val clump = Clump.join(c(1), c(2), c(3))
          assert(clumpResult(clump) == Some(1, 2, 3))
        }
        "4 instances" - {
          val clump = Clump.join(c(1), c(2), c(3), c(4))
          assert(clumpResult(clump) == Some(1, 2, 3, 4))
        }
        "5 instances" - {
          val clump = Clump.join(c(1), c(2), c(3), c(4), c(5))
          assert(clumpResult(clump) == Some(1, 2, 3, 4, 5))
        }
        "6 instances" - {
          val clump = Clump.join(c(1), c(2), c(3), c(4), c(5), c(6))
          assert(clumpResult(clump) == Some(1, 2, 3, 4, 5, 6))
        }
        "7 instances" - {
          val clump = Clump.join(c(1), c(2), c(3), c(4), c(5), c(6), c(7))
          assert(clumpResult(clump) == Some(1, 2, 3, 4, 5, 6, 7))
        }
        "8 instances" - {
          val clump = Clump.join(c(1), c(2), c(3), c(4), c(5), c(6), c(7), c(8))
          assert(clumpResult(clump) == Some(1, 2, 3, 4, 5, 6, 7, 8))
        }
        "9 instances" - {
          val clump = Clump.join(c(1), c(2), c(3), c(4), c(5), c(6), c(7), c(8), c(9))
          assert(clumpResult(clump) == Some(1, 2, 3, 4, 5, 6, 7, 8, 9))
        }
        "10 instances" - {
          val clump = Clump.join(c(1), c(2), c(3), c(4), c(5), c(6), c(7), c(8), c(9), c(10))
          assert(clumpResult(clump) == Some(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        }
      }
    }

    "a Clump instance" - {

      "can be mapped to a new clump" - {

        "using simple a value transformation (clump.map)" - {
          assert(clumpResult(Clump.value(1).map(_ + 1)) == Some(2))
        }

        "using a transformation that creates a new clump (clump.flatMap)" - {
          "both clumps are defined" - {
            assert(clumpResult(Clump.value(1).flatMap(i => Clump.value(i + 1))) == Some(2))
          }
          "initial clump is undefined" - {
            assert(clumpResult(Clump.value(None).flatMap(i => Clump.value(2))) == None)
          }
        }
      }

      "can be joined with another clump and produce a new clump with the value of both (clump.join)" - {
        "both clumps are defined" - {
          assert(clumpResult(Clump.value(1).join(Clump.value(2))) == Some(1, 2))
        }
        "one of them is undefined" - {
          assert(clumpResult(Clump.value(1).join(Clump.value(None))) == None)
        }
      }

      "allows to recover from failures" - {

        "using a function that recovers using a new value (clump.handle)" - {
          "exception happens" - {
            val clump =
              Clump.exception(new IllegalStateException).handle {
                case e: IllegalStateException => Some(2)
              }
            assert(clumpResult(clump) == Some(2))
          }
          "exception doesn't happen" - {
            val clump =
              Clump.value(1).handle {
                case e: IllegalStateException => None
              }
            assert(clumpResult(clump) == Some(1))
          }
          "exception isn't caught" - {
            val clump =
              Clump.exception(new NullPointerException).handle {
                case e: IllegalStateException => Some(1)
              }
            intercept[NullPointerException] {
              clumpResult(clump)
            }
          }
        }

        "using a function that recovers using a new value (clump.recover)" - {
          "exception happens" - {
            val clump =
              Clump.exception(new IllegalStateException).recover {
                case e: IllegalStateException => Some(2)
              }
            assert(clumpResult(clump) == Some(2))
          }
          "exception doesn't happen" - {
            val clump =
              Clump.value(1).recover {
                case e: IllegalStateException => None
              }
            assert(clumpResult(clump) == Some(1))
          }
          "exception isn't caught" - {
            val clump =
              Clump.exception(new NullPointerException).recover {
                case e: IllegalStateException => Some(1)
              }
            intercept[NullPointerException] {
              clumpResult(clump)
            }
          }
        }

        "using a function that recovers the failure using a new clump (clump.rescue)" - {
          "exception happens" - {
            val clump =
              Clump.exception(new IllegalStateException).rescue {
                case e: IllegalStateException => Clump.value(2)
              }
            assert(clumpResult(clump) == Some(2))
          }
          "exception doesn't happen" - {
            val clump =
              Clump.value(1).rescue {
                case e: IllegalStateException => Clump.value(None)
              }
            assert(clumpResult(clump) == Some(1))
          }
          "exception isn't caught" - {
            val clump =
              Clump.exception(new NullPointerException).rescue {
                case e: IllegalStateException => Clump.value(1)
              }
            intercept[NullPointerException] {
              clumpResult(clump)
            }
          }
        }

        "using a function that recovers the failure using a new clump (clump.recoverWith)" - {
          "exception happens" - {
            val clump =
              Clump.exception(new IllegalStateException).recoverWith {
                case e: IllegalStateException => Clump.value(2)
              }
            assert(clumpResult(clump) == Some(2))
          }
          "exception doesn't happen" - {
            val clump =
              Clump.value(1).recoverWith {
                case e: IllegalStateException => Clump.value(None)
              }
            assert(clumpResult(clump) == Some(1))
          }
          "exception isn't caught" - {
            val clump =
              Clump.exception(new NullPointerException).recoverWith {
                case e: IllegalStateException => Clump.value(1)
              }
            intercept[NullPointerException] {
              clumpResult(clump)
            }
          }
        }

        "using a function that recovers using a new value (clump.fallback) on any exception" - {
          "exception happens" - {
            val clump = Clump.exception(new IllegalStateException).fallback(Some(1))
            assert(clumpResult(clump) == Some(1))
          }

          "exception doesn't happen" - {
            val clump = Clump.value(1).fallback(Some(2))
            assert(clumpResult(clump) == Some(1))
          }
        }

        "using a function that recovers using a new clump (clump.fallbackTo) on any exception" - {
          "exception happens" - {
            val clump = Clump.exception(new IllegalStateException).fallbackTo(Clump.value(1))
            assert(clumpResult(clump) == Some(1))
          }

          "exception doesn't happen" - {
            val clump = Clump.value(1).fallbackTo(Clump.value(2))
            assert(clumpResult(clump) == Some(1))
          }
        }
      }

      "can have its result filtered (clump.filter)" - {
        assert(clumpResult(Clump.value(1).filter(_ != 1)) == None)
        assert(clumpResult(Clump.value(1).filter(_ == 1)) == Some(1))
      }

      "uses a covariant type parameter" - {
        trait A
        class B extends A
        class C extends A
        val clump: Clump[List[A]] = Clump.traverse(List(new B, new C))(Clump.value(_))
      }

      "allows to defined a fallback value (clump.orElse)" - {
        "undefined" - {
          assert(clumpResult(Clump.empty.orElse(1)) == Some(1))
        }
        "defined" - {
          assert(clumpResult(Clump.value(Some(1)).orElse(2)) == Some(1))
        }
      }

      "allows to defined a fallback clump (clump.orElse)" - {
        "undefined" - {
          assert(clumpResult(Clump.empty.orElse(Clump.value(1))) == Some(1))
        }
        "defined" - {
          assert(clumpResult(Clump.value(Some(1)).orElse(Clump.value(2))) == Some(1))
        }
      }

      "can represent its result as a collection (clump.list) when its type is a collection" - {
        "list" - {
          assert(awaitResult(Clump.value(List(1, 2)).list) == List(1, 2))
        }
        "set" - {
          assert(awaitResult(Clump.value(Set(1, 2)).list) == Set(1, 2))
        }
        "seq" - {
          assert(awaitResult(Clump.value(Seq(1, 2)).list) == Seq(1, 2))
        }
        "not a collection" - {
          compileError("Clump.value(1).flatten")
        }
      }

      "can provide a result falling back to a default (clump.getOrElse)" - {
        "initial clump is undefined" - {
          assert(awaitResult(Clump.value(None).getOrElse(1)) == 1)
        }

        "initial clump is defined" - {
          assert(awaitResult(Clump.value(Some(2)).getOrElse(1)) == 2)
        }
      }

      "has a utility method (clump.apply) for unwrapping optional result" - {
        assert(awaitResult(Clump.value(1).apply()) == 1)
        intercept[NoSuchElementException] {
          awaitResult(Clump.value[Int](None)())
        }
      }

      "can be made optional (clump.optional) to avoid lossy joins" - {
        val clump: Clump[String] = Clump.empty
        val optionalClump: Clump[Option[String]] = clump.optional
        assert(clumpResult(optionalClump) == Some(None))

        val valueClump: Clump[String] = Clump.value("foo")
        assert(clumpResult(valueClump.join(clump)) == None)
        assert(clumpResult(valueClump.join(optionalClump)) == Some("foo", None))
      }
    }
  }
}