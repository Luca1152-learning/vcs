package ro.luca1152.vcs.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.kotcrab.vis.ui.widget.*
import ktx.app.clearScreen
import ktx.inject.Context
import ro.luca1152.vcs.utils.UIStage
import ro.luca1152.vcs.utils.ui.NewRepositoryWindow

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
                    uiStage.addActor(NewRepositoryWindow(context))
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

    private val unstagedChangesWindow = VisWindow("Unstaged Changes").apply {
        val scrollPane = ScrollPane(VisTable(), skin, "list").apply {
            setFlickScroll(false)
            fadeScrollBars = false
        }
    }

    private val stagedChangesWindow = VisWindow("Staged Changes").apply {
        val scrollPane = ScrollPane(VisTable(), skin, "list").apply {
            setFlickScroll(false)
            fadeScrollBars = false
        }
    }

    private val leftColumn = Table(skin).apply {
        add(unstagedChangesWindow).width(172f).height(225f).padBottom(10f).row()
        add(stagedChangesWindow).width(172f).height(225f)
    }

    private val codeDiffWindow = VisWindow("Code Difference").apply {

    }

    private val rightColum = Table(skin).apply {
        add(codeDiffWindow).width(713f).height(261f)
    }

    private val rootTable = Table(skin).apply {
        setFillParent(true)
        add(dropDownButtonsRowTable).top().left().padLeft(7f).row()
        add(leftColumn).expand().left().top()
        add(rightColum).expand().right().top()
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