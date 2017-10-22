package ru.nsu.ccfit.pleshkov.notebook.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.coroutines.experimental.*
import ru.nsu.ccfit.pleshkov.notebook.presenter.DatabasePresenter
import kotlin.coroutines.experimental.CoroutineContext

interface JobHolder {
    val job: Job

    fun <T> jobAsync(
            context: CoroutineContext = DefaultDispatcher,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> T
    ) = async(job + context, start, block)
}

val View.contextJob: Job get() = (context as? JobHolder)?.job ?: NonCancellable

abstract class BaseDatabaseActivity : AppCompatActivity(), JobHolder {
    protected abstract val layoutId: Int

    override val job = Job()

    protected val dbPresenter = DatabasePresenter()

    protected lateinit var initDatabaseTask: Deferred<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        dbPresenter.begin(this)

        initDatabaseTask = dbPresenter.initDatabases()
    }

    protected fun startActivityAnimated(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        job.cancel()
        dbPresenter.end()
        super.onDestroy()
    }
}
