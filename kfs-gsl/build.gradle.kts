import org.gradle.internal.impldep.com.fasterxml.jackson.databind.EnumNamingStrategies.CamelCaseStrategy
import org.jetbrains.kotlin.load.java.structure.impl.convertCanonicalNameToQName

kotlin {
    kjna {
        generate {
            packages {
                add("kfs.gsl.specfunc") {

                    // Disabled packages will have no function implementations
                    // Use the 'isAvailable()' companion method to check at runtime
                    enabled = true

                    addHeader("gsl/gsl_specfunc.h", "GSLSpecialFunctions")
                    addHeader("gsl/gsl_linalg.h", "GSLLinearAlgebra")


                    libraries = listOf("gsl")
                }
            }
        }
    }
    sourceSets {
        jvmMain.dependencies {
            implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.7.3")
            implementation("org.jetbrains.lets-plot:lets-plot-batik:4.3.3")
        }
    }
}