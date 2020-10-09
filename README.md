Web3j OpenAPI Gradle Plugin
============================

Gradle plugin that generates [Web3j-OpenAPI](https://github.com/web3j/web3j-openapi) 
project from Solidity smart contracts.
It smoothly integrates with your project's build lifecycle by adding specific tasks that can be also
run independently.

## Plugin configuration

The minimum Gradle version to run the plugin is `5.+`.

PS: No need to add the `kotlin plugin` to your project. It already comes with the `web3j-openapi-gradle-plugin`.

### Using the `buildscript` convention

To install the Web3j Plugin using the old Gradle `buildscript` convention, you should add 
the following to the first line of your build file:

```groovy
buildscript {
    repositories {
        mavenCentral()
        maven { url 'https://repo.gradle.org/gradle/libs-releases' }
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath 'org.web3j.openapi:web3j-openapi-gradle-plugin:4.6.4'
    }
}

apply plugin: 'org.web3j.openapi'
```

### Using the plugins DSL

Alternatively, if you are using the more modern plugins DSL, add the following line to your 
build file:

```groovy
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url 'https://repo.gradle.org/gradle/libs-releases' }
    }
}

plugins {
    id "org.web3j.openapi" version "4.6.4"
}
```

Then run your project containing Solidity contracts:

```
./gradlew build
```

After applying the plugin, the base directory for generated code (by default 
`$buildDir/generated/sources/web3j`) will contain a directory containing the generated
project for all the contracts specified in the configuration.

## Code generation

The `web3j` DSL allows to configure the generated code, e.g.:

```groovy
web3j {
    generatedPackageName = 'com.mycompany'
    generatedFilesBaseDir = "$buildDir/custom/destination"
    excludedContracts = ['Ownable']
    openapi {
        contextPath = "api"
    }
}
```

The properties accepted by the `openapi` DSL are listed in the following table: 

|  Name                   | Type       | Default value                       | Description |
|-------------------------|:----------:|:-----------------------------------:|-------------|
| `projectName`           | `String`   | `${rootProject.name}` or `OpenAPI`  | Generated Web3j-OpenAPI project name. |
| `contextPath`           | `String`   | `$projectName`                      | Generated Web3j-OpenAPI context path `/{contextPath}/...`. |
| `generateServer`        | `Boolean`  | `true`                              | Whether to generate the API implementation or only the interfaces |

Check the [web3j-gradle-plugin](https://github.com/web3j/web3j-gradle-plugin#code-generation) 
for the options accepted by the `web3j` DSL.

The `generatedPackageName` is a `.` separated list of words. It will be converted to lower case during the generation.

The default value for the `generatePackageName` is : `org.web3j.openapi`

**The `.{0}` is not accepted in the case we are generating an `OpenAPI` project**.

## Source sets

By default, all `.sol` files in `$projectDir/src/main/solidity` will be processed by the plugin.
To specify and add different source sets, use the `sourceSets` DSL:

```groovy
sourceSets {
    main {
        solidity {
            srcDir { 
                "my/custom/path/to/solidity" 
             }
        }
    }
}
```

Check the [Solidity Plugin](https://github.com/web3j/solidity-gradle-plugin)
documentation to configure the smart contracts source code directories.

Output directories for generated Web3j-OpenAPI project
will be added to your build automatically.

## Swagger UI

The Swagger UI page will be found on : `http://{host}:{port}/swagger-ui`, after running the project `./gradlew run` and specifying the [Web3j-OpenAPI](https://github.com/web3j/web3j-openapi) runtime configuration.

## Plugin tasks

The `Web3j-OpenAPI` Gradle plugin adds tasks to your project build using 
a naming convention on a per source set basis
(i.e. `generateWeb3jOpenApi`, `generate[SourceSet]Web3jOpenApi`).

To obtain a list and description of all added tasks, run the command:

```
./gradlew tasks --all
```

[web3j]: https://web3j.io/

