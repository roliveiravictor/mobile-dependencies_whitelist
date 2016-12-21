package com.mercadolibre.android.gradle.lint.dependencies

import com.mercadolibre.android.gradle.lint.Lint

/**
 * Class that lints the dependencies in the project checking that it only
 * compiles the whitelisted ones
 *
 * Author: Santi Aguilera
 */
class DependenciesLint implements Lint {

    def ALLOWED_DEPENDENCIES = [ "com.mercadolibre.android.sdk" ]
    
    /**
     * Checks the dependencies the project contains are in the whitelist
     * 
     * This throws GradleException if errors are found.
     */
    def lint(def project) {
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
        project.configurations.each { conf ->
            conf.dependencies.each { dep ->
                // The ASCII chars make the stdout look red.
                def message = "\u001b[31m" + "Forbidden dependency: <${dep.group}:${dep.name}:${dep.version}>" + "\u001b[0m"
                // If its a library it can only contain dependencies from the LIBRARY group, if its an application only from APPLICATION's group
                if (!dependencyIsInWhitelist(dep.group) && dep.name != DEFAULT_GRADLE_VALUE) {
                    report(message)
                } 
            }
        }

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
        for (def whitelistDep : ALLOWED_DEPENDENCIES) {
            if (dependency =~ /${whitelistDep}/) {
                return true
            }
        }
        
        return false
    }

}
