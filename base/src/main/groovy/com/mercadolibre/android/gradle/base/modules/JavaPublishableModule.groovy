package com.mercadolibre.android.gradle.base.modules

import com.mercadolibre.android.gradle.base.publish.*
import org.gradle.api.Project

/**
 * Created by saguilera on 7/21/17.
 */
class JavaPublishableModule extends PublishableModule {

    private Project project

    @Override
    void configure(Project project) {
        super.configure(project)

        this.project = project

        applyPlugins()
        createTasks()
    }

    private void applyPlugins() {
        project.afterEvaluate {
            project.rootProject.buildscript.configurations.classpath.each {
                if (it.path.contains(JACOCO_PLUGIN_CLASSPATH)) {
                    project.apply plugin: 'com.mercadolibre.android.gradle.jacoco'
                }
            }
        }
    }

    protected void createTask(PublishJarTask task, def libraryVariant, String theTaskName) {
        task.create(new PublishTask.Builder().with {
            project = this.project
            variant = libraryVariant
            taskName = theTaskName
            return it
        })
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    private void createTasks() {
        // JAR projects dont have local publishes since gradle already provides
        // the `install` task for it

        project.sourceSets.each {
            if (it.name != 'test') {
                createTask(new PublishJarAlphaTask(), it, "publishJarAlpha${it.name}")
                createTask(new PublishJarReleaseTask(), it, "publishJarRelease${it.name}")
                createTask(new PublishJarExperimentalTask(), it, "publishJarExperimental${it.name}")
            }

            if (it.name == 'release') {
                // If release, create mirror tasks without the flavor name
                createTask(new PublishJarAlphaTask(), it, "publishJarAlpha")
                createTask(new PublishJarReleaseTask(), it, "publishJarRelease")
                createTask(new PublishJarExperimentalTask(), it, "publishJarExperimental")

                // And also mirror them without the Jar suffix too
                createTask(new PublishJarAlphaTask(), it, "publishAlpha")
                createTask(new PublishJarReleaseTask(), it, "publishRelease")
                createTask(new PublishJarExperimentalTask(), it, "publishExperimental")
            }
        }

    }

}
