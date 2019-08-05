package ro.luca1152.vcs.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.kotcrab.vis.ui.widget.Menu
import com.kotcrab.vis.ui.widget.MenuBar
import com.kotcrab.vis.ui.widget.MenuItem
import ktx.app.clearScreen
import ktx.inject.Context
import ro.luca1152.vcs.utils.UIStage

class MainScreen(context: Context) : ScreenAdapter() {
    // Injected objects
    private val uiStage: UIStage = context.inject()
    private val skin: Skin = context.inject()

    private val rootTable = Table(skin).apply {
        setFillParent(true)
        val menu = Menu("Menu").apply {
            addItem(MenuItem("Item", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    println("clicked")
                }
            }))
        }
        val menuBar = MenuBar().apply {
            addMenu(menu)
        }
        add(menuBar.table)
    }

    init {
        uiStage.addActor(rootTable)
        handleInput()
    }

    private fun handleInput() {
        Gdx.input.inputProcessor = InputMultiplexer().apply {
            addProcessor(uiStage)
        }
    }

    override fun render(delta: Float) {
        update(delta)
        draw()
    }

    private fun update(delta: Float) {
        uiStage.act(delta)
    }

    private fun draw() {
        clearScreen(1f, 1f, 1f)
        uiStage.draw()
    }
}