package ro.luca1152.vcs.utils.ui

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.*
import ktx.inject.Context
import ro.luca1152.vcs.AppRules
import ro.luca1152.vcs.objects.Commit
import ro.luca1152.vcs.objects.Repository
import ro.luca1152.vcs.screens.MainScreen
import ro.luca1152.vcs.utils.UIStage
import ro.luca1152.vcs.utils.UIViewport

class NewRepositoryWindow(context: Context) : VisWindow("New Repository...") {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()
    private val mainScreen: MainScreen = context.inject()

    private val repositoryNameLabel = VisLabel("Name:")
    private val repositoryNameField = VisTextField()
    private val repositoryNameTable = VisTable().apply {
        add(repositoryNameLabel).padRight(5f)
        add(repositoryNameField)
    }
    private val cancelButton = VisTextButton("Cancel").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@NewRepositoryWindow.remove()
            }
        })
    }
    private val createButton = VisTextButton("Create").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                mainScreen.repository = Repository(context, repositoryNameField.text).apply {
                    initialize()
                }
                this@NewRepositoryWindow.remove()
            }
        })
    }
    private val buttonsTable = VisTable().apply {
        add(cancelButton).padRight(5f)
        add(createButton)
    }

    init {
        add(repositoryNameTable).padBottom(10f).row()
        add(buttonsTable).expand().right()
        setPosition(uiViewport.worldWidth / 2f - prefWidth / 2f, uiViewport.worldHeight / 2f - prefHeight / 2f)
        width = 210f
        height = 90f
    }
}

class OpenRepositoryWindow(context: Context) : VisWindow("Open Repository...") {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()
    private val mainScreen: MainScreen = context.inject()

    private val repositoryNameLabel = VisLabel("Name:")
    private val repositoryNameField = VisTextField()
    private val repositoryNameTable = VisTable().apply {
        add(repositoryNameLabel).padRight(5f)
        add(repositoryNameField)
    }
    private val cancelButton = VisTextButton("Cancel").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@OpenRepositoryWindow.remove()
            }
        })
    }
    private val openButton = VisTextButton("Open").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                mainScreen.repository = Repository(context, repositoryNameField.text).apply {
                    refreshStagedFiles()
                }
                this@OpenRepositoryWindow.remove()
            }
        })
    }
    private val buttonsTable = VisTable().apply {
        add(cancelButton).padRight(5f)
        add(openButton)
    }

    init {
        add(repositoryNameTable).padBottom(10f).row()
        add(buttonsTable).expand().right()
        setPosition(uiViewport.worldWidth / 2f - prefWidth / 2f, uiViewport.worldHeight / 2f - prefHeight / 2f)
        width = 210f
        height = 90f
    }
}

class StageButton(context: Context, file: FileHandle, isStaged: Boolean) : VisTextButton(file.name()) {
    // Injected objects
    private val mainScreen: MainScreen = context.inject()

    init {
        label.setAlignment(Align.left)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                if (tapCount == 1) {
                    mainScreen.selectedFile = file
                } else if (tapCount == 2) {
                    when (isStaged) {
                        true -> mainScreen.repository.unstageFile(file)
                        false -> mainScreen.repository.stageFile(file)
                    }
                    mainScreen.shouldUpdateUnstagedChanges = true
                    mainScreen.shouldUpdateStagedChanges = true
                }
            }
        })
    }
}

class SuccessfulCommitWindow(context: Context) : VisWindow("") {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()

    private val repositoryNameLabel = VisLabel("Successful commit!")
    private val closeButton = VisTextButton("Close").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@SuccessfulCommitWindow.remove()
            }
        })
    }

    init {
        add(repositoryNameLabel).padBottom(10f).row()
        add(closeButton).expand()
        setPosition(uiViewport.worldWidth / 2f - prefWidth / 2f, uiViewport.worldHeight / 2f - prefHeight / 2f)
        width = 210f
        height = 90f
    }
}

class NothingToCommitWindow(context: Context) : VisWindow("") {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()

    private val repositoryNameLabel = VisLabel("Nothing to commit...")
    private val closeButton = VisTextButton("Close").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@NothingToCommitWindow.remove()
            }
        })
    }

    init {
        add(repositoryNameLabel).padBottom(10f).expand().row()
        add(closeButton).expand()
        setPosition(uiViewport.worldWidth / 2f - prefWidth / 2f, uiViewport.worldHeight / 2f - prefHeight / 2f)
        width = 210f
        height = 90f
    }
}

class NoCommitMessageWindow(context: Context) : VisWindow("") {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()

    private val repositoryNameLabel = VisLabel("Please enter a commit\nmessage...")
    private val closeButton = VisTextButton("Close").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@NoCommitMessageWindow.remove()
            }
        })
    }

    init {
        add(repositoryNameLabel).padBottom(10f).expand().row()
        add(closeButton).expand()
        setPosition(uiViewport.worldWidth / 2f - prefWidth / 2f, uiViewport.worldHeight / 2f - prefHeight / 2f)
        width = 210f
        height = 100f
    }
}

class RescanButton(mainScreen: MainScreen) : VisTextButton("Rescan") {
    init {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                try {
                    mainScreen.repository.refreshStagedFiles()
                } catch (e: Throwable) {
                }
            }
        })
    }
}

class CommitButton(mainScreen: MainScreen) : VisTextButton("Commit") {
    init {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                try {
                    mainScreen.repository.commitStaged()
                } catch (e: Throwable) {
                }
            }
        })
    }
}

class RevertWindow(context: Context, private val mainScreen: MainScreen) : VisWindow("Revert commit...") {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()
    private val uiStage: UIStage = context.inject()
    private val appRules: AppRules = context.inject()

    private val previousCommitsTable = VisTable().apply {
        val commits = getAllCommits()
        commits.forEach {
            val button = VisTextButton(it.message).apply {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        super.clicked(event, x, y)
                        uiStage.addActor(RevertToCommitWindow(context, it, mainScreen))
                    }
                })
            }
            add(button).growX().padBottom(5f).row()
        }
    }

    private val scrollPane = VisScrollPane(previousCommitsTable).apply {
        setFlickScroll(false)
        fadeScrollBars = false
        setScrollingDisabled(true, false)
    }

    private val closeButton = VisTextButton("Close").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@RevertWindow.remove()
            }
        })
    }

    private fun getAllCommits(): ArrayList<Commit> {
        val commitsList = arrayListOf<Commit>()
        try {
            mainScreen.repository.run {
                var latestCommit = getCommitFromHashedName(appRules.latestCommitOnCurrentBranchHashedName)
                while (latestCommit != null) {
                    commitsList.add(latestCommit)
                    latestCommit = getCommitFromHashedName(latestCommit.headName)
                }
            }
        } catch (e: Throwable) {
        }
        return commitsList
    }

    init {
        name = "RevertWindow"
        add(scrollPane).grow().row()
        add(closeButton).expandY()
        setPosition(uiViewport.worldWidth / 2f - prefWidth / 2f, uiViewport.worldHeight / 2f - prefHeight / 2f)
        width = 270f
        height = 160f
    }
}


class RevertToCommitWindow(context: Context, commit: Commit, mainScreen: MainScreen) : VisWindow("") {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()
    private val uiStage: UIStage = context.inject()

    private val repositoryNameLabel = VisLabel("Are you sure you want to\nrevert the repository?")
    private val revertButton = VisTextButton("Revert").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@RevertToCommitWindow.remove()
                uiStage.root.findActor<RevertWindow>("RevertWindow")?.remove()
                mainScreen.repository.revertToCommit(commit)
            }
        })
    }
    private val closeButton = VisTextButton("Close").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@RevertToCommitWindow.remove()
            }
        })
    }
    private val buttonsTable = VisTable().apply {
        add(revertButton).padRight(10f)
        add(closeButton)
    }

    init {
        add(repositoryNameLabel).padBottom(10f).expand().row()
        add(buttonsTable).expand().right()
        setPosition(uiViewport.worldWidth / 2f - prefWidth / 2f, uiViewport.worldHeight / 2f - prefHeight / 2f)
        width = 210f
        height = 130f
    }
}

class NewBranchWindow(context: Context) : VisWindow("New Branch...") {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()
    private val mainScreen: MainScreen = context.inject()

    private val repositoryNameLabel = VisLabel("Name:")
    private val repositoryNameField = VisTextField()
    private val repositoryNameTable = VisTable().apply {
        add(repositoryNameLabel).padRight(5f)
        add(repositoryNameField)
    }
    private val cancelButton = VisTextButton("Cancel").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@NewBranchWindow.remove()
            }
        })
    }
    private val createButton = VisTextButton("Create").apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                mainScreen.repository = Repository(context, repositoryNameField.text).apply {
                    initialize()
                }
                this@NewBranchWindow.remove()
            }
        })
    }
    private val buttonsTable = VisTable().apply {
        add(cancelButton).padRight(5f)
        add(createButton)
    }

    init {
        add(repositoryNameTable).padBottom(10f).row()
        add(buttonsTable).expand().right()
        setPosition(uiViewport.worldWidth / 2f - prefWidth / 2f, uiViewport.worldHeight / 2f - prefHeight / 2f)
        width = 210f
        height = 90f
    }
}

