plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLib)
}

kotlin {
    androidTarget {
        namespace = "com.example.apuniliapp.shared"
        compileSdk = 35
        minSdk = 24

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.junit)
        }
        androidMain.dependencies {
        }
        iosMain.dependencies {
        }
    }
}

