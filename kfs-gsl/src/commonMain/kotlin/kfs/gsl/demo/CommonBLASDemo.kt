package kfs.gsl.demo

import ksf.gsl.blas.vector
import kotlin.random.Random
import kotlin.system.measureNanoTime


fun main() {
    val native = (0..100).map {
        measureNanoTime {
            val v1 = (0..100000).map { Random.nextDouble() }.toDoubleArray().vector
            val v2 = (0..100000).map { Random.nextDouble() }.toDoubleArray().vector

            println(v1 dot v2)
        }
    }.average()

    val kotlin = (0..100).map {
        measureNanoTime {
            val v1 = (0..100000).map { Random.nextDouble() }.toDoubleArray()
            val v2 = (0..100000).map { Random.nextDouble() }.toDoubleArray()

            var acc = 0.0
            for (index in v1.indices) {
                acc += v1[index]*v2[index]
            }
            println(acc)
        }
    }.average()


    println("relative performance: ${native/kotlin} (less is better)")
}