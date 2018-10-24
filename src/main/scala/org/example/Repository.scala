package org.example

import cats._
import cats.data.Const
import cats.effect.IO
import cats.implicits._
import org.example.sphynx.Optimizer

trait Repository[F[_]] {

  def getName(id: String): F[String]

  def getNames(ids: List[String]): F[List[String]]
}

object TestRepo {
  def apply[F[_]: Applicative] = new Repository[F] {
    override def getName(id: String): F[String] = {
      println(s"get name of '$id'")
      (id.toUpperCase * 2).pure[F]
    }

    override def getNames(ids: List[String]): F[List[String]] = {
      println(s"get for many at once '$ids'")
      ids.map(_.toUpperCase).pure[F]
    }
  }
}

object TestApp extends App {

  def program[F[_]: Applicative, G[_]: Traverse](names: G[String])(
      R: Repository[F]): F[G[String]] = {
    names.traverse(R.getName)
  }

  def wrappedProgram[G[_]: Traverse](names: G[String]) =
    new sphynx.ApplicativeProgram[Repository, G[String]] {
      def apply[F[_]: Applicative](alg: Repository[F]): F[G[String]] =
        program(names)(alg)
    }

  implicit def lookupOptimizer[F[_]: Monad] =
    new Optimizer[Repository, F] {
      type M = List[String]

      def monoidM = implicitly

      def monadF = implicitly

      type ConstList[A] = Const[List[String], A]
      def extract = new Repository[ConstList] {
        def getName(id: String): Const[List[String], String] = Const(List(id))

        def getNames(ids: List[String]): Const[List[String], List[String]] =
          Const(ids)
      }

      def rebuild(ids: List[String],
                  interp: Repository[F]): F[Repository[F]] = {
        val ds = ids.distinct

        interp
          .getNames(ds)
          .map(names => {
            val lookup = ds.zip(names).toMap

            new Repository[F] {
              override def getName(id: String): F[String] = {
                lookup.get(id) match {
                  case Some(v) => v.pure[F]
                  case None    => interp.getName(id)
                }
              }

              override def getNames(ids: List[String]): F[List[String]] = {
                val (_, torequest) = ids.partition(lookup.keySet.contains)

                val inner =
                  if (torequest.isEmpty) Map[String, String]().pure[F]
                  else
                    interp
                      .getNames(torequest)
                      .map(nn => torequest.zip(nn).toMap)

                val total = inner.map2(lookup.pure[F])(_ ++ _)

                total.map(t => ids.map(t.get).flatten)
              }
            }
          })
      }
    }

  import Optimizer._

  println(
    wrappedProgram(List("Fritz", "Bernd", "Fritz"))
      .optimize(TestRepo[IO])
      .unsafeRunSync()
  )
}
