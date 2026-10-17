import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    java
    kotlin("kapt")
    id("com.github.gmazzo.buildconfig")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")

    kapt("com.google.auto.service:auto-service:1.0.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")

    testImplementation(kotlin("test-junit"))
    testImplementation("com.bennyhuo.kotlin:kotlin-compile-testing-extensions:2.4.0-1.3.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

buildConfig {
    packageName("$group.trimindent")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${project.property("KOTLIN_PLUGIN_ID")}\"")
}
