package ro.luca1152.vcs.screens

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.assets.AssetManager
import ktx.inject.Context
import ro.luca1152.vcs.MyVcs
import ro.luca1152.vcs.utils.Assets

class LoadingScreen(private val context: Context) : ScreenAdapter() {
    // Injected objects
    private val manager: AssetManager = context.inject()
    private val myVcs: MyVcs = context.inject()

    init {
        loadAssets()
    }

    private fun loadAssets() {
        manager.load(Assets.uiSkin)
    }

    override fun render(delta: Float) {
        if (manager.update()) {
            bindSingletons()
            changeScreen()
        }
    }

    private fun bindSingletons() {
        context.run {
            bindSingleton(manager.get(Assets.uiSkin))
            bindSingleton(MainScreen(context))
        }
    }

    private fun changeScreen() {
        myVcs.screen = context.inject<MainScreen>()
    }

    private fun update() {
        manager.update()
    }
}