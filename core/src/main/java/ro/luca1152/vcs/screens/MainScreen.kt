package ro.luca1152.vcs.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.github.difflib.text.DiffRowGenerator
import com.kotcrab.vis.ui.widget.*
import ktx.app.clearScreen
import ktx.inject.Context
import ro.luca1152.vcs.json.Config
import ro.luca1152.vcs.objects.Repository
import ro.luca1152.vcs.utils.UIStage
import ro.luca1152.vcs.utils.ui.*


class MainScreen(context: Context) : ScreenAdapter() {
    // Injected objects
    private val uiStage: UIStage = context.inject()
    private val skin: Skin = context.inject()
    private val config: Config = context.inject()

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
                    uiStage.addActor(NewBranchWindow(context))
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
            addItem(MenuItem("Revert...", object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    uiStage.addActor(RevertWindow(context, this@MainScreen))
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
    private val codeDiffLabel = Label("a", skin, "default")
    private val codeDiffWindow = object : VisWindow("Code Difference") {
        init {
            add(VisScrollPane(codeDiffLabel.apply { setAlignment(Align.topLeft) })).grow()
        }

        override fun act(delta: Float) {
            super.act(delta)

            if (selectedFile == null || !::repository.isInitialized) {
                codeDiffLabel.setText("")
            } else {
                codeDiffLabel.setText("")
                val generator = DiffRowGenerator.create()
                    .showInlineDiffs(true)
                    .mergeOriginalRevised(true)
                    .inlineDiffByWord(true)
                    .oldTag { "[RED]---" }
                    .newTag { "[GREEN]+++" }
                    .build()

                val oldFile = if (selectedFile == null || config.getLatestCommitForCurrentBranch() == "" ||
                    !repository.getCommitFromHashedName(config.getLatestCommitForCurrentBranch())!!.tree!!.blobs.containsKey(
                        selectedFile!!.path()
                    )
                ) "" else
                    repository.getContentFromHashedFileName(
                        repository.getCommitFromHashedName(config.getLatestCommitForCurrentBranch())!!.tree!!.blobs.getValue(
                            selectedFile!!.path()
                        ).hashedFileName
                    )
                val newFile = selectedFile!!.readString()
                val rows = generator.generateDiffRows(listOf(oldFile), listOf(newFile))
                var rowCount = 1
                var subtractsFound = 0
                var plusesFound = 0
                rows.forEach {
                    val stringToAppend =
                        "${if (subtractsFound % 2 == 0) "[LIGHT_GRAY]$rowCount${if (subtractsFound % 2 == 1) "[RED]" else if (plusesFound % 2 == 1) "[GREEN]" else "[WHITE]"}" else ""}    ${it.oldLine}"

                    if (codeDiffLabel.textEquals("")) codeDiffLabel.setText(stringToAppend)
                    else codeDiffLabel.setText("${codeDiffLabel.text}\n$stringToAppend")
                    subtractsFound += it.oldLine.length - it.oldLine.replace("---", "").length
                    plusesFound += it.oldLine.length - it.oldLine.replace("+++", "").length
                    rowCount = if (subtractsFound % 2 == 0) rowCount + 1 else rowCount
                }
            }
        }
    }

    private val commitMessageLeftColumn = VisTable().apply {
        add(RescanButton(this@MainScreen)).growX().expandY().row()
        add(CommitButton(this@MainScreen)).growX().expandY().row()
    }

    val commitMessageTextField = VisTextField()

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
    var selectedFile: FileHandle? = null

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