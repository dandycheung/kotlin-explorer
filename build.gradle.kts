@file:Suppress("UnstableApiUsage")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import kotlin.io.path.listDirectoryEntries

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
}

version = "1.6.6"
val baseName = "Kotlin Explorer"

kotlin {
    jvm {
        @Suppress("OPT_IN_USAGE")
        mainRun {
            mainClass = "dev.romainguy.kotlin.explorer.KotlinExplorerKt"
        }
    }

    jvmToolchain {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(17)
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs) {
                    exclude(group = "org.jetbrains.compose.material")
                }
                implementation(compose.components.resources)
                implementation(libs.collection)
                implementation(libs.compose.backhandler)
                implementation(libs.compose.material3)
                implementation(libs.compose.splitpane)
                implementation(libs.jewel)
                implementation(libs.jewel.decorated)
                implementation(libs.jewel.markdown.core)
                implementation(libs.jewel.markdown.intUiStandaloneStyling)
                implementation(libs.jna)
                implementation(libs.lifecycle)
                implementation(libs.lifecycle.compose)
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.skiko.mac)
                implementation(libs.skiko.linux)
                implementation(libs.rsyntaxtextarea)
                implementation(libs.rstaui)
                implementation(project(":token-makers"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.junit4)
                implementation(libs.kotlin.test)
                implementation(libs.truth)
            }
        }
    }
}

composeCompiler {
    stabilityConfigurationFiles = listOf(layout.projectDirectory.file("compose-stability.config"))
    reportsDestination = layout.buildDirectory.dir("compose-compiler")
}

compose.desktop {
    application {
        mainClass = "dev.romainguy.kotlin.explorer.KotlinExplorerKt"
        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
        nativeDistributions {
            modules("jdk.unsupported")

            targetFormats(TargetFormat.Dmg, TargetFormat.Exe)

            packageVersion = version.toString()
            packageName = baseName
            description = baseName
            vendor = "Romain Guy"
            licenseFile = rootProject.file("LICENSE")

            macOS {
                dockName = "Kotlin Explorer"
                iconFile = file("art/app-icon/icon.icns")
                bundleID = "dev.romainguy.kotlin.explorer"
            }

            windows {
                menuGroup = "Kotlin Explorer"
                iconFile = file("art/app-icon/icon.ico")
            }
        }
    }
}

val currentArch: String = when (val osArch = System.getProperty("os.arch")) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported OS arch: $osArch")
}

/**
 * TODO: workaround for https://github.com/JetBrains/compose-multiplatform/issues/4976.
 */
val renameDmg by tasks.registering(Copy::class) {
    group = "distribution"
    description = "Rename the DMG file"

    val packageReleaseDmg = tasks.named<AbstractJPackageTask>("packageReleaseDmg")
    // build/compose/binaries/main-release/dmg/*.dmg
    val fromFile = packageReleaseDmg.map { task ->
        task.destinationDir.asFile.get().toPath().listDirectoryEntries("$baseName*.dmg").single()
    }

    from(fromFile)
    into(fromFile.map { it.parent })
    rename {
        "kotlin-explorer-$currentArch-$version.dmg"
    }
}

tasks.assemble {
    dependsOn(renameDmg)
}
