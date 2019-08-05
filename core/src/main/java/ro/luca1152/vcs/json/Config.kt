package ro.luca1152.vcs.json

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import ro.luca1152.vcs.objects.Repository

class Config {
    var branches = arrayListOf<String>()
    var latestCommit = mutableMapOf<String, String>()
    var currentBranch = ""

    fun getLatestCommitForCurrentBranch() = if (latestCommit.containsKey(currentBranch)) latestCommit[currentBranch]!! else ""

    fun setLatestCommitForCurrentBranch(repository: Repository, commitFileName: String) {
        latestCommit[currentBranch] = commitFileName
        flush(repository)
    }

    fun flush(repository: Repository) {
        Gdx.files.local("${repository.internalPath}/.config").writeString(Json().prettyPrint(this), false)
    }
}