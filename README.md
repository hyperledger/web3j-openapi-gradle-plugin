web3j OpenAPI Gradle Plugin
============================

Gradle plugin that generates [Web3j-OpenAPI](https://github.com/web3j/web3j-openapi) project from Solidity smart contracts.
It smoothly integrates with your project's build lifecycle by adding specific tasks that can be also
run independently.

## Plugin configuration

The minimum Gradle version to run the plugin is `5.+`.

### Using the `buildscript` convention

To install the web3j Plugin using the old Gradle `buildscript` convention, you should add 
the following to the first line of your build file:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.web3j:web3j-openapi-gradle-plugin:4.5.11'
    }
}

apply plugin: 'org.web3j.openapi'
```

### Using the plugins DSL (To be checked again)

Alternatively, if you are using the more modern plugins DSL, add the following line to your 
build file:

```groovy
plugins {
    id 'org.web3j.openapi' version '4.5.11'
}
```

Then run your project containing Solidity contracts:

```
./gradlew build
```

After applying the plugin, the base directory for generated code (by default 
`$buildDir/generated/source/web3j`) will contain a directory containing the generated
project for all the contracts specified in the configuration.

### Project dependencies

The plugin requires the [gradle-tooling-api](https://docs.gradle.org/current/userguide/embedding.html) dependency to be declared in your project
**in case you want to generate the `SwaggerUI`**. If that is the case, add the following dependency at the top of your `build.gradle` file :

```groovy
buildscript{
    repositories {
        mavenCentral()

        dependencies{
            maven {
                url 'https://repo.gradle.org/gradle/libs-releases'
            }
        }
    }
}
```

## Code generation

The `openapi` DSL allows to configure the generated code, e.g.:

```groovy
openapi {
    generatedPackageName = 'com.mycompany'
    generatedFilesBaseDir = "$buildDir/custom/destination"
    excludedContracts = ['Ownable']
    projectName = "TestProject"
    generateSwaggerUI = false
}
```

The properties accepted by the DSL are listed in the following table: 

|  Name                   | Type       | Default value                       | Description |
|-------------------------|:----------:|:-----------------------------------:|-------------|
| `generatedPackageName`  | `String`   | `${group}.openapi`                  | Generated project package name. |
| `generatedFilesBaseDir` | `String`   | `$buildDir/generated/source/web3j`  | Generated Web3j-OpenAPI project output directory. |
| `excludedContracts`     | `String[]` | `[]`                                | Excluded contract names from Web3j-OpenAPI generation |
| `includedContracts`     | `String[]` | `[]`                                | Included contract names from Web3j-OpenAPI generation. Has preference over `excludedContracts`. |
| `projectName`           | `String`   | `${rootProject.name}Api`            | Generated Web3j-OpenAPI project name. |
| `addressBitLength`      | `int`      | `160`                               | Supported address length in bits, by default Ethereum addresses. |
| `contextPath`           | `String`   | `projectName`                       | Generated Web3j-OpenAPI context path `/{contextPath}/...`. |
| `generateSwaggerUI`     | `Boolean`  | `true`                              | Generate a [SwaggerUI](https://swagger.io/tools/swagger-ui/) along with the Web3j-OpenAPI project. Don't forget to add the dependency above if you set this to true. |
| `contractsAbis`         | `String[]` | `[]`                                | Extra contracts ABIS to use for the Web3j-OpenAPI generation |
| `contractsBins`         | `String[]` | `[]`                                | Extra contracts BINs to use for the Web3j-OpenAPI generation |

The `generatedPackageName` is a `.` separated list of words. It will be converted to lower case during the generation.

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

Also, you can add more `ABIs` and `BINs` to the generation via 
the `contractsAbis` and `contractsBins` properties (check the table above).

Output directories for generated Web3j-OpenAPI project
will be added to your build automatically.

## Plugin tasks

The ``Web3j-OpenAPI-gradle`` pluginadds tasks to your project build using 
a naming convention on a per source set basis
(i.e. `generateWeb3jOpenAPI`, `generate[SourceSet]Web3jOpenAPI`).

To obtain a list and description of all added tasks, run the command:

```
./gradlew tasks --all
```

[web3j]: https://web3j.io/
