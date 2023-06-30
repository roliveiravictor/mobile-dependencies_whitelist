package com.mercadolibre.android.gradle.baseplugin.core.action.modules.lint.dependencies

/**
 * This class is the representation of a dependency within a project.
 * @param group This variable represents the dependency group.
 * @param name This variable represents the dependency name.
 * @param version This variable represents the dependency version.
 * @param expires This variable represents the dependency exprires.
 * @param alpha This variable represents whether the dependency is alpha or not.
 */
data class Dependency(
    val group: String?,
    val name: String?,
    val version: String?,
    val expires: String?,
    val alpha: Boolean?
)
