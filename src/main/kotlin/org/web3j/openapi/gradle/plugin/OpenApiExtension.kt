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

import groovy.lang.Closure
import org.gradle.api.Project
import org.web3j.gradle.plugin.Web3jExtension
import java.util.InvalidPropertiesFormatException

open class OpenApiExtension(
    private val project: Project,
) : Web3jExtension(project) {

    var openApi: OpenApiConfiguration = OpenApiConfiguration(project)

    fun openapi(closure: Closure<*>): OpenApiConfiguration {
        project.configure(openApi, closure)
        return openApi
    }

    override fun setGeneratedPackageName(generatedPackageName: String?) {
        if (generatedPackageName?.contains(".{0}") == true) {
            throw InvalidPropertiesFormatException("The .{0} notation is not accepted when generating an Web3j-OpenAPI project !")
        }
        super.setGeneratedPackageName("$generatedPackageName.wrappers")
    }

    override fun getDefaultGeneratedPackageName(project: Project): String = "org.web3j.openapi.wrappers"
}
