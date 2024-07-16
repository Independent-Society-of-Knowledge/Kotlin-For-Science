import kfs.gsl.specfunc.GSLLinearAlgebra
import kfs.gsl.specfunc.GSLSpecialFunctions
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot


fun main() = try {
    println(GSLLinearAlgebra::class.java)
    val data =  (0..3000)
        .map { it / 100.0 }
        .map { it to GSLSpecialFunctions.gsl_sf_bessel_j0(it) }


    var plt = letsPlot{
        x = data.map { it.first }
        y = data.map { it.second }
    }
    plt += geomLine()
    plt += ggtitle("bessel_j0")

    plt.show()

} catch (e: Throwable) {
    e.printStackTrace()
}