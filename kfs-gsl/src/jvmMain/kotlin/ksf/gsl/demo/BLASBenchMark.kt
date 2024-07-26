package ksf.gsl.demo

import ksf.gsl.blas.Vector
import ksf.gsl.blas.vector


fun main() {

    val a = doubleArrayOf(1.0, 1.0, 1.0).vector
    val b = doubleArrayOf(2.0, 2.0, 2.0).vector

    println(a dot b)

}