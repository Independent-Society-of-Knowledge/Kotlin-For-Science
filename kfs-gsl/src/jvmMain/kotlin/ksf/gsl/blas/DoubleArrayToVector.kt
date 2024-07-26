package ksf.gsl.blas

import com.iskportal.kfs.binder.native
import com.iskportal.kfs.binder.ptr
import com.iskportal.kfs.binder.segment
import kfs.gsl.GSLBLAS


actual val DoubleArray.vector: Vector
    get() {
        val view = GSLBLAS.gsl_vector_view_array(this.segment.native.ptr, this.size.toLong())
        return Vector(view, size.toLong())
    }