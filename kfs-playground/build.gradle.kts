kotlin {
    kjna {
        generate {
            packages {
                add("kfs.playground"){
                    addHeader(project.projectDir.resolve("playground.h").absolutePath, "Playground")
                }
                add("kfs.gsl.specfunc") {
                    addHeader("gsl/gsl_specfunc.h", "GSLSpecialFunctions")
                    libraries = listOf("gsl")
                }
            }
        }
    }
}