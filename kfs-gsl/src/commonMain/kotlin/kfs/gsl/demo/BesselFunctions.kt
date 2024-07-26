package kfs.gsl.demo

import kfs.gsl.GSLSpecialFunctions
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.letsPlot

fun main(){


    val data = (0..4)
        .map { n ->
            (0..1000)
                .map { it / 100.0 }
                .map { x ->
                    x to GSLSpecialFunctions.gsl_sf_bessel_Jn(n, x)
                }.toMap() to n

        }

    var plt = letsPlot(
        mapOf(
            "x" to data.flatMap { it.first.keys },
            "y" to data.flatMap { it.first.values },
            "group" to data.flatMap { List(it.first.keys.size){n->"j_${it.second}"} },
        )
    )

    plt += geomLine{
        x = "x" ; y="y"; group = "group"; color = "group"
    }


    plt.show()
}