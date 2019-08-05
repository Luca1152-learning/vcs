package ro.luca1152.vcs.objects

import com.badlogic.gdx.Gdx

class Repository(private val name: String) {
    private val internalPath = "$name/.vcs"
    private val codePath = "$name"

    fun initialize() {
        Gdx.files.local("$internalPath/a").run {
            writeString("a", false)
            delete()
        }
    }
}