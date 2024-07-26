#! /bin/bash

export LD_LIBRARY_PATH="/lib/x86_64-linux-gnu/"
export JAVA_HOME="$HOME/.gradle/jdks/eclipse_adoptium-22-amd64-linux/jdk-22.0.1+8"
export JAVA_TOOL_OPTIONS="--enable-native-access=ALL-UNNAMED"


./gradlew  kfs-gsl:jvmRun -DmainClass=kfs.gsl.demo.BLASDotBenchmarkKt
