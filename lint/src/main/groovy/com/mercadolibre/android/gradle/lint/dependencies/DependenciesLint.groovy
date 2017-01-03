package com.mercadolibre.android.gradle.lint.dependencies

import com.mercadolibre.android.gradle.lint.Lint
import groovy.json.JsonSlurper

/**
 * Class that lints the dependencies in the project checking that it only
 * compiles the whitelisted ones
 *
 * Author: Santi Aguilera
 */
class DependenciesLint implements Lint {

    /**
     * Array with whitelisted dependencies
     */
    def WHITELIST_DEPENDENCIES

    /**
     * Endpoint from where to fetch the whitelist dependencies json
     *
     * Endpoint MUST NOT contain redirects.
     * Endpoint MUST NOT need auth / specific headers / query params / etc.
     * Endpoint MUST BE  a /GET/
     *
     * Json format MUST be:
     * {
     *     "whitelist" : [ ":dependency1", ":dependency2", ... , ":dependencyN" ]
     * }
     */
    private static final String WHITELIST_ENDPOINT = "https://raw.githubusercontent.com/mercadolibre/mobile-dependencies_whitelist/v1.0.0/android-whitelist.json"

    /**
     * Checks the dependencies the project contains are in the whitelist
     * 
     * This throws GradleException if errors are found.
     */
    def lint(def project, def variants) {
        setUpWhitelist()

        def hasFailed = false
        def DEFAULT_GRADLE_VALUE = "unspecified" // Generated by Gradle/Maven by default for a project
        
        /**
         * Closure to report a forbidden dependency as error
         */
        def report = { message ->
            println message
            hasFailed = true
        }

        // Core logic
        def analizeDependency = { dependency, String packageName ->
            // The ASCII chars make the stdout look red.
            def dependencyFullName = "${dependency.group}:${dependency.name}:${dependency.version}"
            def message = "\u001b[31m" + "Forbidden dependency: <${dependencyFullName}>" + "\u001b[0m"
            def publishName = "${project.publisher.groupId}:${project.publisher.artifactId}"

            /**
             * - Dependency cant be found in whitelist
             * - Isnt "unspecified" the name of the dependency
             * - Dependency isnt from the same group (you CAN compile dependencies from your own module)
             * Only if all of the above meet it will error.
             */
            if (!dependencyIsInWhitelist(dependencyFullName)
                    && !dependencyFullName.contains(DEFAULT_GRADLE_VALUE)
                    && !(dependencyFullName.contains(packageName) || dependencyFullName.contains(publishName))) {
                report(message)
            }
        }

        String debugPackageName // For the default compile deps, we will use debug package

        // Check dependencies of each variant available first
        variants.each { variant ->
            def variantName = variant.name
            String packageName = variant.applicationId

            /**
             * Since each variant can have its own package name, we need for the 
             * default compile mode a package to check against for the
             * local dependencies. Since we use mostly debug, we are choosing
             * as default package the debug one.
             */
            if (variantName == "debug") {
                debugPackageName = packageName
            }

            project.configurations.all { configuration ->
                if (configuration.name == "${variantName}Compile") {
                    configuration.dependencies.each { analizeDependency(it, packageName) }
                }
            }
        }

        // Check the default compiling deps
        project.configurations.compile.dependencies.each { analizeDependency(it, debugPackageName) }

        return hasFailed
    }

    /**
     * Returns the task name
     */
    def name() {
        return "lintDependencies"
    }

    /**
    * Method to check if a part of a string is contained in
    * at least one of the strings of the array
    * eg array = [ "abc", "def", "ghi" ]
    * array.containsPartOf("ab") -> true
    * array.containsPartOf("hi") -> true
    * 
    * Supports regular expressions for the array values.
    */
    def dependencyIsInWhitelist(def dependency) {
        for (def whitelistDep : WHITELIST_DEPENDENCIES) {
            if (dependency =~ /${whitelistDep}/) {
                return true
            }
        }
        
        return false
    }

    def setUpWhitelist() {
        WHITELIST_DEPENDENCIES = new ArrayList<String>()

        new URL(WHITELIST_ENDPOINT).openConnection().with { conn ->
            def jsonSlurper = new JsonSlurper().parseText(conn.inputStream.text)
            jsonSlurper.whitelist.each { dependency ->
                WHITELIST_DEPENDENCIES.add(dependency)
            }
        }
    }

}
