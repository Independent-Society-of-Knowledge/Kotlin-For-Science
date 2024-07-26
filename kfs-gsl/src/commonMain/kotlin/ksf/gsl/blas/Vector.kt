package ksf.gsl.blas

import com.iskportal.kfs.binder.*
import kfs.gsl.GSLBLAS


class Vector(val view: Pointer, val length: Long) {
    infix fun dot(vector: Vector): Double {
        val res = NativeDouble(0.0)
        GSLBLAS.gsl_blas_ddot(view, vector.view, res.segment.ptr)

        return res.value
    }
}
