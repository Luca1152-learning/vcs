package ro.luca1152.vcs

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kotcrab.vis.ui.VisUI
import ktx.inject.Context
import ro.luca1152.vcs.screens.LoadingScreen
import ro.luca1152.vcs.utils.UICamera
import ro.luca1152.vcs.utils.UIStage
import ro.luca1152.vcs.utils.UIViewport

class MyVcs : Game() {
    private val context = Context()

    override fun create() {
        VisUI.load()
        initializeDependencyInjection()
        setScreen(context.inject<LoadingScreen>())
    }

    private fun initializeDependencyInjection() {
        context.run {
            bindSingleton(Gdx.app.getPreferences("MyVcs"))
            bindSingleton(AppRules(this))
            bindSingleton(this@MyVcs)
            bindSingleton(AssetManager())
            bindSingleton(SpriteBatch() as Batch)
            bindSingleton(UICamera())
            bindSingleton(UIViewport(this))
            bindSingleton(UIStage(this))
            bindSingleton(LoadingScreen(this))
        }
    }
}