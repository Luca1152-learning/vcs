package ro.luca1152.vcs

import com.badlogic.gdx.ApplicationAdapter
import ktx.inject.Context
import ro.luca1152.vcs.screens.MainScreen
import ro.luca1152.vcs.utils.UICamera
import ro.luca1152.vcs.utils.UIStage
import ro.luca1152.vcs.utils.UIViewport

class MyVcs : ApplicationAdapter() {
    private val context = Context()

    override fun create() {
        initializeDependencyInjection()
    }

    private fun initializeDependencyInjection() {
        context.run {
            bindSingleton(this@MyVcs)
            bindSingleton(MainScreen(this))
            bindSingleton(UICamera())
            bindSingleton(UIViewport(this))
            bindSingleton(UIStage(this))
        }
    }
}