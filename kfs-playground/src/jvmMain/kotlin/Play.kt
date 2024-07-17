import com.iskportal.kfs.binder.CStruct
import com.iskportal.kfs.binder.read
import kfs.gsl.specfunc.jextract.gsl_sf_result
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import com.iskportal.kfs.binder.segment
import kfs.gsl.specfunc.jextract.gsl_matrix_uchar


data class gsl_sf_result_kt(val `val`: Double, val err: Double) : CStruct {
    companion object {
        val cStructClass = gsl_sf_result::class.java
    }
}

fun main() {
    try {
        val segment = gsl_sf_result_kt(1.0, 2.0).segment
        println(segment.toArray(ValueLayout.JAVA_BYTE).joinToString(","))
        val value = segment.read(gsl_sf_result_kt::class)
        println(value)

    } catch (e: Throwable) {
        e.printStackTrace()
    }
}