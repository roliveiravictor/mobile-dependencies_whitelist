package com.mercadolibre.android.gradle.jacoco

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Created by ngiagnoni on 3/11/15.
 */
public class JacocoAndroidPlugin implements Plugin<Project> {

    /**
     * The project.
     */
    private Project project;

    @Override
    public void apply(Project project){
        this.project = project

        // We apply jacoco plugin allowing us to create Unit tests code coverage report
        project.apply plugin: 'jacoco'
        project.jacoco.toolVersion = "0.7.1.201405082137"

        createJacocoTasks()
    }

    private void createJacocoTasks() {
        if (project.android == null){
            throw new GradleException("You should apply \"android\" plugin to make this one work.")
        }

        createJacocoReportTasks()
        createCleanJacocoTasks()
    }

    /**
     * Creates the tasks to generate Jacoco report, one per variant depending on Unit and Instrumentation tests.
     * [incubating]
     */
    private void createJacocoReportTasks() {
        def variants = null;

        try {
            variants = project.android.applicationVariants
        } catch (Exception e) {
            variants = project.android.libraryVariants
        }

        variants.all { variant ->

            //Define local variables to avoid accessing multiple times to the buildType object.
            def buildTypeName = variant.buildType.name
            def capitalizedBuildTypeName = buildTypeName.capitalize()

            def taskName = "jacoco${capitalizedBuildTypeName}"
            def testTaskName = "test${capitalizedBuildTypeName}"

            //Create and retrieve necesary tasks
            def jacocoTask = project.tasks.create taskName, JacocoReport
            def unitTest = project.tasks.findByName(testTaskName)

            //Define JacocoTasks and it's configuration
            jacocoTask.description = "Generate Jacoco code coverage report after running tests for ${buildTypeName} build variant. [incubating]"
            jacocoTask.group = "Reporting"

            //By convention this is the sources folder
            jacocoTask.sourceDirectories = project.files("src/main/java")

            //Here is where execution data files are created. ConnectedAndroidTest and Test tasks generates them.
            jacocoTask.executionData = project.files("build/jacoco/${testTaskName}.exec")

            //Ignore auto-generated classes
            jacocoTask.classDirectories = project.fileTree(dir: "./build/intermediates/classes/${buildTypeName}", excludes: [
                    '**/R.class',
                    '**/R$*.class',
                    '**/BuildConfig.class'
            ])

            //Enable both reports
            jacocoTask.reports.xml.enabled = true
            jacocoTask.reports.html.enabled = true

            jacocoTask.dependsOn unitTest

            //If testCoverage is not enabled, Android Jacoco' plugin will not instrumentate project classes
            if (variant.buildType.testCoverageEnabled){
                project.logger.warn("WARNING: You should DISABLE \"android.buildTypes.${buildTypeName}.testCoverageEnabled\" in your build.gradle in order to make \"${taskName}\" run succesfully in \"${project.name}\".")
            }
        }
    }

    private void createCleanJacocoTasks() {
        def task = project.tasks.create 'cleanJacocoFiles'
        task.setDescription('Clean all Jacoco related files.')

        task.doLast {
            File file = project.file("./jacoco.exec")
            if (!file.delete()){
                throw new GradleException("Cannot delete \"jacoco.exec\" file. Check if some process is using it and close it.")
            }
        }

        task.onlyIf {
            File file = project.file("./jacoco.exec")
            return file.exists()
        }

        task.getOutputs().upToDateWhen {
            File file = project.file("./jacoco.exec")
            return !file.exists()
        }

        def cleanTask = project.tasks.findByName("clean")
        cleanTask.finalizedBy task
    }
}
