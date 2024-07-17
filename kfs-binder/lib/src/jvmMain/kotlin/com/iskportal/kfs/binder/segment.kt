package com.iskportal.kfs.binder
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

val DoubleArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val IntArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val ShortArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val ByteArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val CharArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val FloatArray.segment: MemorySegment get() = MemorySegment.ofArray(this)
val LongArray.segment: MemorySegment get() = MemorySegment.ofArray(this)



inline val <reified T : CStruct> T.segment: MemorySegment
    get() {

        val cType = T::class.companionObject!!.java.getDeclaredMethod("getCStructClass")
            .invoke(T::class.companionObjectInstance) as Class<*>


        val seg = cType.getMethod("allocate", SegmentAllocator::class.java)
            .invoke(null, Arena.ofAuto()) as MemorySegment

        T::class.memberProperties
            .filter { it.name != "Companion" }
            .map { it.name to it.javaGetter!! }
            .forEach {
                cType.getMethod(it.first, MemorySegment::class.java, it.second.returnType)
                    .invoke(null, seg, it.second.invoke(this))
            }
        return seg
    }

fun <T : CStruct> MemorySegment.read(clazz: KClass<T>): T {

    val cType = clazz.companionObject!!.java.getDeclaredMethod("getCStructClass")
        .invoke(clazz.companionObjectInstance) as Class<*>


    val constructor = clazz.constructors.first()
    val cParams = constructor.parameters

    val args = clazz.memberProperties
        .filter { it.name != "Companion" }
        .associate {
            cParams.first { p -> p.name == it.name } to cType.getMethod(it.name, MemorySegment::class.java)
                .invoke(null, this).also { a ->
                    println("resolved: ${it.name}: ${a}")
                }
        }


    return constructor.callBy(args)
}
