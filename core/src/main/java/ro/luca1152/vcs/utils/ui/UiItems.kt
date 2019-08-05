package ro.luca1152.vcs.utils.ui

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.kotcrab.vis.ui.widget.*
import ktx.inject.Context
import ro.luca1152.vcs.utils.UIViewport

class NewRepositoryWindow(context: Context) : VisWindow("New Repository...") {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()

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