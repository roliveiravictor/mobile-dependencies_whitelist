package com.mercadolibre.android.gradle.library

import com.mercadolibre.android.gradle.baseplugin.core.action.configurers.AndroidConfigurer
import com.mercadolibre.android.gradle.baseplugin.core.action.configurers.PluginConfigurer
import com.mercadolibre.android.gradle.baseplugin.core.basics.AbstractPlugin
import com.mercadolibre.android.gradle.baseplugin.core.components.LIBRARY_PLUGINS
import com.mercadolibre.android.gradle.library.core.action.configurers.LibraryModuleConfigurer

/**
 * BaseAppPlugin is in charge of configuring the app module of the repository where it is being applied.
 */
open class BaseLibraryPlugin : AbstractPlugin() {

    override val configurers = arrayListOf(
        PluginConfigurer(LIBRARY_PLUGINS),
        AndroidConfigurer(),
        LibraryModuleConfigurer()
    )
}
