/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.core.testing.util

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziOptions.CompareOptions
import com.github.takahirom.roborazzi.RoborazziOptions.RecordOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.accompanist.testharness.TestHarness
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import org.robolectric.RuntimeEnvironment

val DefaultRoborazziOptions =
    RoborazziOptions(
        compareOptions = CompareOptions(changeThreshold = 0f), // Pixel-perfect matching
        recordOptions = RecordOptions(resizeScale = 0.5), // Reduce the size of the PNGs
    )

enum class DefaultTestDevices(val description: String, val spec: String) {
    PHONE("phone", "spec:shape=Normal,width=640,height=360,unit=dp,dpi=480"),
    FOLDABLE("foldable", "spec:shape=Normal,width=673,height=841,unit=dp,dpi=480"),
    TABLET("tablet", "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480"),
}
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureMultiDevice(
    screenshotName: String,
    body: @Composable () -> Unit,
) {
    DefaultTestDevices.values().forEach {
        this.captureForDevice(it.description, it.spec, screenshotName, body = body)
    }
}

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureForDevice(
    deviceName: String,
    deviceSpec: String,
    screenshotName: String,
    roborazziOptions: RoborazziOptions = DefaultRoborazziOptions,
    darkMode: Boolean = false,
    body: @Composable () -> Unit,
) {
    val (width, height, dpi) = extractSpecs(deviceSpec)

    // Set qualifiers from specs
    RuntimeEnvironment.setQualifiers("w${width}dp-h${height}dp-${dpi}dpi")

    this.activity.setContent {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
        ) {
            TestHarness(darkMode = darkMode) {
                body()
            }
        }
    }
    this.onRoot()
        .captureRoboImage(
            "src/test/screenshots/${screenshotName}_$deviceName.png",
            roborazziOptions = roborazziOptions,
        )
}

/**
 * Takes four screenshots combining light/dark and default/dynamic themes.
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureMultiTheme(
    name: String,
    overrideFileName: String? = null,
    shouldCompareDarkMode: Boolean = true,
    shouldCompareDynamicTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkModeValues = if (shouldCompareDarkMode) listOf(true, false) else listOf(false)
    val dynamicThemingValues = if (shouldCompareDynamicTheme) listOf(true, false) else listOf(false)

    val darkMode = mutableStateOf(true)
    val dynamicTheming = mutableStateOf(false)

    this.setContent {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
        ) {
            NiaTheme(
                darkTheme = darkMode.value,
                disableDynamicTheming = !dynamicTheming.value,
            ) {
                key(darkMode.value, dynamicTheming.value) { // Necessary sometimes (e.g. animations)
                    content()
                }
            }
        }
    }
    darkModeValues.forEach { darkModeValue ->
        darkMode.value = darkModeValue
        val darkModeDesc = if (darkModeValue) "dark" else "light"

        dynamicThemingValues.forEach { dynamicThemingValue ->
            dynamicTheming.value = dynamicThemingValue
            val dynamicThemingDesc = if (dynamicThemingValue) "dynamic" else "default"

            val filename = overrideFileName ?: name
            this.onRoot()
                .captureRoboImage(
                    "src/test/screenshots/" +
                        "$name/${filename}_${darkModeDesc}_$dynamicThemingDesc.png",
                    roborazziOptions = DefaultRoborazziOptions,
                )
        }
    }
}

/**
 * Extracts some properties from the spec string. Note that this function is not exhaustive.
 */
private fun extractSpecs(deviceSpec: String): TestDeviceSpecs {
    val specs = deviceSpec.substringAfter("spec:")
        .split(",").map { it.split("=") }.associate { it[0] to it[1] }
    val width = specs["width"]?.toInt() ?: 640
    val height = specs["height"]?.toInt() ?: 480
    val dpi = specs["dpi"]?.toInt() ?: 480
    return TestDeviceSpecs(width, height, dpi)
}

data class TestDeviceSpecs(val width: Int, val height: Int, val dpi: Int)
