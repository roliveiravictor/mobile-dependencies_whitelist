package com.mercadolibre.android.gradle.baseplugin.core.action.modules.dexcount

import com.getkeepsafe.dexcount.DexCountExtension
import com.getkeepsafe.dexcount.DexMethodCountPlugin
import com.mercadolibre.android.gradle.baseplugin.core.components.DEXCOUNT_PROPERTY
import com.mercadolibre.android.gradle.baseplugin.core.components.JSON_CONSTANT
import com.mercadolibre.android.gradle.baseplugin.core.domain.interfaces.Module
import org.gradle.api.Project

/**
 * This is the module in charge of configuring dex count in case it is required.
 */
class DexCountModule : Module() {
    /**
     * This is the method that checks if Dexcount is required, if so configures it.
     */
    override fun configure(project: Project) {
        if (project.hasProperty(DEXCOUNT_PROPERTY)) {
            project.plugins.apply(DexMethodCountPlugin::class.java)

            findExtension<DexCountExtension>(project)?.apply {
                // more config options: https://github.com/KeepSafe/dexcount-gradle-plugin#configuration
                format = JSON_CONSTANT
                includeClassCount = true
                includeFieldCount = true
                includeTotalMethodCount = true
            }
        }
    }
}
