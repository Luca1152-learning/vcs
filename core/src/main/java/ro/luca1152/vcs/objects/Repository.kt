package ro.luca1152.vcs.objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Json
import ktx.inject.Context
import ro.luca1152.vcs.json.Config
import ro.luca1152.vcs.screens.MainScreen
import ro.luca1152.vcs.utils.HashUtils
import ro.luca1152.vcs.utils.UIStage
import ro.luca1152.vcs.utils.ui.NoCommitMessageWindow
import ro.luca1152.vcs.utils.ui.NothingToCommitWindow
import ro.luca1152.vcs.utils.ui.SuccessfulCommitWindow

class Repository(private val context: Context, private val name: String) {
    // Injected objects
    private val config: Config = context.inject()
    private val mainScreen: MainScreen = context.inject()
    private val uiStage: UIStage = context.inject()

    val internalPath = "$name/.vcs"
    private val codePath = name

    val unstagedFiles = arrayListOf<FileHandle>()
    val stagedFiles = arrayListOf<FileHandle>()

    fun initialize() {
        Gdx.files.local("$internalPath/objects/a").run {
            writeString("", false)
            delete()
        }
        Gdx.files.local("$codePath/.ignore").writeString("", false)

        config.run {
            branches.add("master")
            currentBranch = "master"
            latestCommit["master"] = ""
        }
        val json = Json().toJson(config)
//        Gdx.files.local("$internalPath/.config").writeString(Json().toJson(config), false)
    }

    fun refreshStagedFiles() {
        unstagedFiles.clear()
        stagedFiles.clear()

        Gdx.files.local(codePath).list().forEach {
            if (!isFileIgnored(it) && !it.isDirectory) {
                if (isFileUnstaged(it)) {
                    unstagedFiles.add(it)
                } else {
                    val hashedFileName = getHashedFileName(it)
                    val commit = getCommitFromHashedName(config.getLatestCommitForCurrentBranch())
                    if (commit == null) {
                        stagedFiles.add(it)
                    } else {
                        var foundFile = false
                        commit.tree?.blobs?.forEach {
                            if (it.value.hashedFileName == hashedFileName) {
                                foundFile = true
                            }
                        }
                        if (!foundFile || !commit.tree!!.blobs.containsKey(it.path())) {
                            unstagedFiles.add(it)
                        }
                    }
                }
            }
        }
        getCommitFromHashedName(config.getLatestCommitForCurrentBranch())?.tree?.blobs?.forEach {
            var isFileDeleted = true
            Gdx.files.local(codePath).list().forEach { file ->
                if (file.path() == it.key) {
                    isFileDeleted = false
                }
            }
            if (isFileDeleted) {
                unstagedFiles.add(Gdx.files.local(it.key))
            }
        }

        mainScreen.run {
            shouldUpdateStagedChanges = true
            shouldUpdateUnstagedChanges = true
        }
    }

    fun isFileUnstaged(file: FileHandle): Boolean {
        return !Gdx.files.local("${internalPath}/objects/${getHashedFileName(file)}").exists()
    }

    fun getHashedFileName(file: FileHandle) = getHashedFileNameFromString("${file.path()}${file.readString()}")

    fun getHashedFileNameFromString(string: String) = HashUtils.sha1(string)

    fun stageFile(file: FileHandle) {
        if (file.exists()) {
            Gdx.files.local("$internalPath/objects/${getHashedFileName(file)}").writeString(file.readString(), false)
        }
        stagedFiles.add(file)
        unstagedFiles.remove(file)
    }

    fun unstageFile(file: FileHandle) {
        Gdx.files.local("$internalPath/objects/${getHashedFileName(file)}").run {
            if (exists()) {
                delete()
                unstagedFiles.add(file)
                stagedFiles.remove(file)
            }
        }
    }

    fun commitStaged() {
        if (stagedFiles.size != 0) {
            if (mainScreen.commitMessageTextField.text == "") {
                uiStage.addActor(NoCommitMessageWindow(context))
            } else {
                val commitTree = Tree()
                stagedFiles.forEach {
                    if (it.exists()) {
                        commitTree.blobs[it.path()] = Blob().apply {
                            hashedFileName = getHashedFileName(it)
                        }
                    } else {
                        commitTree.blobs[it.path()] = Blob().apply {
                            hashedFileName = getHashedFileNameFromString(it.path())
                        }
                    }
                }
                stagedFiles.clear()

                val latestCommit = getCommitFromHashedName(config.getLatestCommitForCurrentBranch())
                if (latestCommit != null) {
                    latestCommit.tree?.blobs?.forEach {
                        if (!commitTree.blobs.containsKey(it.key)) {
                            commitTree.blobs[it.key] = it.value
                        }
                    }
                }

                val commit = Commit().apply {
                    tree = commitTree
                    if (config.getLatestCommitForCurrentBranch() != "") {
                        headName = config.getLatestCommitForCurrentBranch()
                    }
                    message = mainScreen.commitMessageTextField.text
                    mainScreen.commitMessageTextField.text = ""
                }
                val jsonCommit = Json().toJson(commit)
                val commitFileName = getHashedFileNameFromString(jsonCommit)
                commit.hashedFileName = commitFileName

                Gdx.files.local("$internalPath/objects/$commitFileName").writeString(jsonCommit, false)

                stagedFiles.clear()
                config.setLatestCommitForCurrentBranch(this, commitFileName)

                mainScreen.shouldUpdateUnstagedChanges = true
                mainScreen.shouldUpdateStagedChanges = true
                mainScreen.selectedFile = null

                uiStage.addActor(SuccessfulCommitWindow(context))
            }
        } else {
            uiStage.addActor(NothingToCommitWindow(context))
        }
    }

    fun getCommitFromHashedName(name: String): Commit? {
        val file = Gdx.files.local("$internalPath/objects/$name")
        return if (file.exists() && name != "") Json().fromJson(Commit::class.java, file.readString()) else null
    }

    fun getContentFromHashedFileName(hashedFileName: String): String {
        return Gdx.files.local("$internalPath/objects/$hashedFileName").readString()
    }

    fun revertToCommit(commit: Commit) {
        deleteAllCode()
        commit.tree?.blobs?.forEach {
            Gdx.files.local(it.key).writeString(getContentFromHashedFileName(it.value.hashedFileName), false)
        }
        refreshStagedFiles()
    }

    fun deleteAllCode() {
        Gdx.files.local(codePath).list().forEach {
            if (!it.isDirectory && !isFileIgnored(it) && it.name() != "$codePath/.ignore") {
                it.delete()
            }
        }
    }

    private fun isFileIgnored(file: FileHandle): Boolean {
        if (file.path().contains("/.vcs")) return true
        val ignoreFile = Gdx.files.local("$codePath/.ignore")
        if (ignoreFile.exists()) {
            ignoreFile.readString().split(System.getProperty("line.separator")).forEach {
                if (it.endsWith("*") && file.path().substring(name.length + 1).startsWith(it.substring(0, it.length - 2))) return true
                else if (it.startsWith("*") && file.path().endsWith(it.substring(1))) return true
            }
        }
        return false
    }
}