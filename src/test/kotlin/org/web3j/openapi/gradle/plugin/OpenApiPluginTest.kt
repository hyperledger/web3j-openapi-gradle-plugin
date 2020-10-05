/*
 * Copyright 2020 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.openapi.gradle.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

class OpenApiPluginTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun generateOpenApiTest() {
        val resource = javaClass.classLoader.getResource("solidity/StandardToken.sol")!!
        val sourceDir: File = File(resource.file).parentFile
        val packageName = "com.test"

        val buildFileContent = """
			plugins {
                id 'org.web3j.openapi'
            }
            web3j {
                generatedPackageName = '$packageName'
            }
			sourceSets {
				main {
					solidity {
						srcDir {'${sourceDir.absolutePath}'}
					}
				}
			}
            repositories {
                mavenLocal()
                mavenCentral()
                maven {
                    url 'https://oss.sonatype.org/content/repositories/snapshots'
                }
            }
		""".trimIndent()

        val buildFile: File = File(testProjectDir, "build.gradle").apply { createNewFile() }
        buildFile.writeText(buildFileContent)

        val gradleRunner = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withPluginClasspath()
                .forwardOutput()

        val buildResult = gradleRunner.build()
        assertNotNull(buildResult.task(":generateWeb3jOpenAPI"))
        assertEquals(SUCCESS, buildResult.task(":generateWeb3jOpenAPI")!!.outcome)

        val outputFolder = File(
                Paths.get(
                        testProjectDir.absolutePath,
                        "build",
                        "generated",
                        "source",
                        "web3j",
                        "main",
                        "kotlin",
                        packageName.replace(".", "/")
                ).toString()
        )
        assertNotNull(outputFolder.list())
        assertNotEquals(0, outputFolder.list()!!.size)
    }
}
