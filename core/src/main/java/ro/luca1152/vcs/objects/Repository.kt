package ro.luca1152.vcs.objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Json
import ktx.inject.Context
import ro.luca1152.vcs.AppRules
import ro.luca1152.vcs.screens.MainScreen
import ro.luca1152.vcs.utils.HashUtils

class Repository(context: Context, private val name: String) {
    // Injected objects
    private val appRules: AppRules = context.inject()
    private val mainScreen: MainScreen = context.inject()

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
            if (!it.name().contains("/.vcs") && !it.isDirectory) {
                if (isFileUnstaged(it)) {
                    unstagedFiles.add(it)
                } else {
                    val hashedFileName = getHashedFileName(it)
                    val latestCommitHashedName = appRules.latestCommitOnCurrentBranchHashedName
                    if (latestCommitHashedName == "") {
                        stagedFiles.add(it)
                    } else {
                        val file = Gdx.files.local("$internalPath/objects/$latestCommitHashedName")
                        val latestCommit = Json().fromJson(Commit::class.java, file.readString())
                        var foundFile = false
                        latestCommit.tree?.blobs?.forEach {
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
        return !Gdx.files.local(getHashedFileName(file)).exists()
    }

    fun getHashedFileName(file: FileHandle) = HashUtils.sha1("${file.path()}${file.readString()}")

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
}