package com.iskportal.kfs.binder.plugin

import com.squareup.kotlinpoet.*
import org.gradle.api.Project
import java.io.File
import java.lang.foreign.Arena
import java.lang.foreign.SegmentAllocator
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class Poetry(val project: Project, val clazz: Class<*>, val srcDir: File) {

    val acceptableFunctions
        get() = clazz.methods.filter { Modifier.isStatic(it.modifiers) }.filterNot { it.isSynthetic }
            .filterNot { it.name.contains("$") }

    val jvmMain get() = srcDir.resolve("jvmMain")
    val commonMain get() = srcDir.resolve("commonMain")
    val jsMain get() = srcDir.resolve("jsMain")


    fun makePoet() {
        val packageName = clazz.packageName.substringBeforeLast(".")
        val className = clazz.simpleName
        val klass = ClassName(packageName, className)

        val common = FileSpec.builder(klass)
            .addType(
                TypeSpec.classBuilder(klass)
                    .addModifiers(KModifier.EXPECT)
                    .addType(
                        TypeSpec.companionObjectBuilder()
                            .apply { acceptableFunctions.forEach { addFunction(it.asFunctionSpec.build()) } }
                            .build()
                    )
                    .build()
            ).build()

        common.writeTo(commonMain)


        val jvm = FileSpec.builder(klass)
            .addType(
                TypeSpec
                    .classBuilder(klass)
                    .addModifiers(KModifier.ACTUAL)
                    .addType(
                        TypeSpec
                            .companionObjectBuilder()
                            .addModifiers(KModifier.ACTUAL)
                            .apply {
                                acceptableFunctions.forEach {
                                    addFunction(
                                        it.asFunctionSpec
                                            .addModifiers(KModifier.ACTUAL)
                                            .addStatement(
                                                "return ${clazz.canonicalName}.${it.name}(${
                                                    it.parameters?.joinToString(",") {
                                                        if(SegmentAllocator::class.java.isAssignableFrom(it.type))
                                                            "java.lang.foreign.Arena.ofAuto()"
                                                        else
                                                            it.name
                                                    } ?: ""
                                                })"
                                            )
                                            .build()
                                    )
                                }
                            }
                            .build()
                    )
                    .build()
            )
            .build()

        jvm.writeTo(jvmMain)
    }

    private val Method.asFunctionSpec
        get() = FunSpec.builder(this.name)
            .addParameters(parameters.filterNot { SegmentAllocator::class.java.isAssignableFrom(it.type) }.map { ParameterSpec.builder(it.name, it.type).build() })
            .returns(returnType)
}