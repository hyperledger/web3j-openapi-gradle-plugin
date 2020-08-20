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

open class OpenApiConfiguration(project: Project) {

    /** Contracts ABIs list. Can be folders or files.  */
    var contractsAbis: List<String> = emptyList()

    /** Contracts BINs list. Can be folders or files.  */
    var contractsBins: List<String> = emptyList()

    /**
	 * Context path for the generated OpenAPI path.
	 *
	 * The resulting URIs path will be :
	 * <code> /{contextPath}/{contractName}/... </code>
	 */
    var contextPath: String = project.rootProject.name
        set(value) {
            field = value.removePrefix("/")
        }
}
