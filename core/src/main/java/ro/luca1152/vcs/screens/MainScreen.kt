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
import ro.luca1152.vcs.objects.Repository
import ro.luca1152.vcs.utils.UIStage
import ro.luca1152.vcs.utils.ui.*

class MainScreen(context: Context) : ScreenAdapter() {
    // Injected objects
    private val uiStage: UIStage = context.inject()
    private val skin: Skin = context.inject()

    // Colors
    private val bgColor = skin.getColor("t-medium-dark")

    // UI
    private val repositoryMenuBar = MenuBar().apply {
        addMenu(Menu("Repository").apply {
            addItem(MenuItem("New...", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    uiStage.addActor(NewRepositoryWindow(context))
                }
            }))
            addItem(MenuItem("Open...", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    uiStage.addActor(OpenRepositoryWindow(context))
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
                    if (::repository.isInitialized) {
                        repository.commitStaged()
                    }
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

    var shouldUpdateUnstagedChanges = false
    private val unstagedChangesWindow = object : VisWindow("Unstaged Changes") {
        private val entriesTable = VisTable()

        val scrollPane = VisScrollPane(entriesTable).apply {
            setFlickScroll(false)
            fadeScrollBars = false
        }

        init {
            add(scrollPane).grow().top()
        }

        override fun act(delta: Float) {
            super.act(delta)
            if (shouldUpdateUnstagedChanges) {
                shouldUpdateUnstagedChanges = false
                updateEntries()
            }
        }

        fun updateEntries() {
            entriesTable.run {
                setFillParent(true)
                clearChildren()
                top()
                repository.unstagedFiles.forEach {
                    add(StageButton(context, it, false)).growX().left().row()
                }
            }
        }
    }

    var shouldUpdateStagedChanges = false
    private val stagedChangesWindow = object : VisWindow("Staged Changes") {
        private val entriesTable = VisTable()

        val scrollPane = ScrollPane(entriesTable, skin, "list").apply {
            setFlickScroll(false)
            fadeScrollBars = false
        }

        init {
            add(scrollPane).grow().top()
        }

        override fun act(delta: Float) {
            super.act(delta)
            if (shouldUpdateStagedChanges) {
                shouldUpdateStagedChanges = false
                updateEntries()
            }
        }

        fun updateEntries() {
            entriesTable.run {
                setFillParent(true)
                clearChildren()
                top()
                repository.stagedFiles.forEach {
                    add(StageButton(context, it, true)).growX().left().row()
                }
            }
        }
    }

    private val stagingLeftColumn = Table(skin).apply {
        add(unstagedChangesWindow).width(172f).height(225f).padBottom(10f).row()
        add(stagedChangesWindow).width(172f).height(225f)
    }

    private val codeDiffWindow = VisWindow("Code Difference").apply {

    }

    private val commitMessageLeftColumn = VisTable().apply {
        add(RescanButton()).growX().expandY().row()
        add(CommitButton()).growX().expandY().row()
    }

    private val commitMessageTextField = VisTextField()

    private val commitMessageRightColumn = VisTable().apply {
        add(VisWindow("Commit Message: ").apply {
            add(commitMessageTextField).grow().padBottom(5f)
        }).grow()
    }

    private val commitMessageTable = VisTable().apply {
        add(commitMessageLeftColumn).padLeft(-10f).growY()
        add(commitMessageRightColumn).padLeft(20f).grow()
    }

    private val rightColumn = Table(skin).apply {
        add(codeDiffWindow).width(713f).height(380f).row()
        add(commitMessageTable).grow().height(70f).padTop(10f)
    }

    private val rootTable = Table(skin).apply {
        setFillParent(true)
        add(dropDownButtonsRowTable).top().left().padLeft(15f).row()
        add(stagingLeftColumn).expand().left().top()
        add(rightColumn).expand().right().top()
    }

    // VCS
    lateinit var repository: Repository

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