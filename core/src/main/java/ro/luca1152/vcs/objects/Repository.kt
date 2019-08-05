package ro.luca1152.vcs.objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Json
import ktx.inject.Context
import ro.luca1152.vcs.AppRules
import ro.luca1152.vcs.screens.MainScreen
import ro.luca1152.vcs.utils.HashUtils
import ro.luca1152.vcs.utils.UIStage
import ro.luca1152.vcs.utils.ui.NoCommitMessageWindow
import ro.luca1152.vcs.utils.ui.NothingToCommitWindow
import ro.luca1152.vcs.utils.ui.SuccessfulCommitWindow

class Repository(private val context: Context, private val name: String) {
    // Injected objects
    private val appRules: AppRules = context.inject()
    private val mainScreen: MainScreen = context.inject()
    private val uiStage: UIStage = context.inject()

    private val internalPath = "$name/.vcs"
    private val codePath = name

    val unstagedFiles = arrayListOf<FileHandle>()
    val stagedFiles = arrayListOf<FileHandle>()

    fun initialize() {
        Gdx.files.local("$internalPath/a").run {
            writeString("a", false)
            delete()
        }
        Gdx.files.local("$codePath/.ignore").writeString("", false)
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
                    val commit = getCommitFromHashedName(appRules.latestCommitOnCurrentBranchHashedName)
                    if (commit == null) {
                        stagedFiles.add(it)
                    } else {
                        var foundFile = false
                        commit.tree?.blobs?.forEach {
                            if (it.value.hashedFileName == hashedFileName) {
                                foundFile = true
                            }
                        }
                        if (!foundFile) {
                            stagedFiles.add(it)
                        }
                    }
                }
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
        Gdx.files.local("$internalPath/objects/${getHashedFileName(file)}").writeString(file.readString(), false)
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
                    commitTree.blobs[it.path()] = Blob().apply {
                        hashedFileName = getHashedFileName(it)
                    }
                }
                stagedFiles.clear()

                val latestCommit = getCommitFromHashedName(appRules.latestCommitOnCurrentBranchHashedName)
                if (latestCommit != null) {
                    latestCommit.tree?.blobs?.forEach {
                        if (!commitTree.blobs.containsKey(it.key)) {
                            commitTree.blobs[it.key] = it.value
                        }
                    }
                }

                val commit = Commit().apply {
                    tree = commitTree
                    if (appRules.latestCommitOnCurrentBranchHashedName != "") {
                        headName = appRules.latestCommitOnCurrentBranchHashedName
                    }
                    message = mainScreen.commitMessageTextField.text
                    mainScreen.commitMessageTextField.text = ""
                }
                val jsonCommit = Json().toJson(commit)
                val commitFileName = getHashedFileNameFromString(jsonCommit)
                commit.hashedFileName = commitFileName

                Gdx.files.local("$internalPath/objects/$commitFileName").writeString(jsonCommit, false)

                stagedFiles.clear()
                appRules.latestCommitOnCurrentBranchHashedName = commitFileName

                mainScreen.shouldUpdateUnstagedChanges = true
                mainScreen.shouldUpdateStagedChanges = true

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

    fun isFileIgnored(file: FileHandle): Boolean {
        if (file.path().contains("/.vcs")) return true
        val ignoreFile = Gdx.files.local("$codePath/.ignore").readString()
        ignoreFile.split(System.getProperty("line.separator")).forEach {
            println("$it vs ${file.path()}")
            if (it.endsWith("*") && file.path().substring(name.length + 1).startsWith(it.substring(0, it.length - 2))) return true
            else if (it.startsWith("*") && file.path().endsWith(it.substring(1))) return true
            else if (file.path().substring(0, file.path().length - 2).startsWith(it)) return true
        }
        return false
    }
}