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

    // Colors
    private val bgColor = skin.getColor("t-medium-dark")

    private val repositoryMenuBar = MenuBar().apply {
        addMenu(Menu("Repository").apply {
            addItem(MenuItem("New...", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                }
            }))
            addItem(MenuItem("Open...", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                }
            }))
        })
    }

    private val branchMenuBar = MenuBar().apply {
        addMenu(Menu("Branch").apply {
            addItem(MenuItem("Create...", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                }
            }))
            addItem(MenuItem("Checkout...", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                }
            }))
        })
    }

    private val actionsMenuBar = MenuBar().apply {
        addMenu(Menu("Actions").apply {
            addItem(MenuItem("Commit...", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                }
            }))
            addItem(MenuItem("Merge...", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                }
            }))
        })
    }

    private val dropDownButtonsRowTable = Table(skin).apply {
        add(repositoryMenuBar.table)
        add(branchMenuBar.table)
        add(actionsMenuBar.table)
    }

    private val rootTable = Table(skin).apply {
        setFillParent(true)
        add(dropDownButtonsRowTable).expand().top().left()
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
        clearScreen(bgColor.r, bgColor.g, bgColor.b)
        uiStage.draw()
    }
}