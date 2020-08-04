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

import org.gradle.api.Project
import org.web3j.abi.datatypes.Address.DEFAULT_LENGTH
import java.nio.file.Paths

open class OpenApiExtension(
    project: Project
) {
    companion object {
        /** Extension name used in Gradle build files.  */
        const val NAME = "openapi"
    }

    /** Generated OpenAPI project name.
	 *  Default : "{rootProjectName}Api}"
	 */
    var projectName: String = "${project.rootProject.name}Api"

    /** Generated package name for Web3j-OpenAPI project.
	 *  Default : "{group}.openapi"
	 */
    var generatedPackageName: String = "${project.group}.openapi"
        set(value) {
            field = value.removeSuffix(".{0}")
        }

    /** Excluded contract names for Web3j-OpenApi generation.  */
    var excludedContracts: List<String> = emptyList()

    /** Included contract names for Web3j-OpenApi generation.  */
    var includedContracts: List<String> = emptyList()

    /** Contracts ABIs list. Can be folders or files.  */
    var contractsAbis: List<String> = emptyList()

    /** Contracts BINs list. Can be folders or files.  */
    var contractsBins: List<String> = emptyList()

    /** Bit length for network addresses.  */
    var addressBitLength = DEFAULT_LENGTH / java.lang.Byte.SIZE

    /**
	 * Context path for the generated OpenAPI path.
	 *
	 * The resulting URIs path will be :
	 * <code> /{contextPath}/{contractName}/... </code>
	 */
    var contextPath: String = projectName
        set(value) {
            field = value.removePrefix("/")
        }

    /**
	 * Output directory of the generated project.
	 * Default : build/generated/source/web3j/main/{projectName}
	 */
    var generatedFilesBaseDir: String = Paths.get(
            project.buildDir.absolutePath,
            "generated",
            "source",
            "web3j",
            "main",
            projectName
    ).toString()

    /**
     * Checks whether to generate the SwaggerUI for the generated project.
     * Set to false not to generate it.
	 * Default : true.
     */
    var generateSwaggerUI: Boolean = true
}
