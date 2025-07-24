import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.logging)
            implementation(libs.slf4j.api)
            implementation(libs.logback.classic)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.nukrs.windroid.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WinDroid Toolbox"
            packageVersion = "1.0.0"
            description = "Android设备管理工具箱"
            copyright = "© 2024 WinDroid Toolbox. All rights reserved."
            vendor = "WinDroid Team"
            
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
                menuGroup = "WinDroid Toolbox"
                upgradeUuid = "61DAB35E-17CB-43B8-B24D-A9C57C7C6A9E"
                
                // 创建快捷方式和菜单项
                shortcut = true  // 创建桌面快捷方式
                menu = true      // 创建开始菜单项
                console = false  // 不显示控制台窗口
                
                // 安装包详细信息
                packageName = "WinDroid Toolbox"
                
                // JVM 参数
                jvmArgs += listOf(
                    "-Dfile.encoding=UTF-8",
                    "-Dsun.java2d.uiScale=1.0"
                )
            }
            
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
            
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
            }
        }
    }
}
