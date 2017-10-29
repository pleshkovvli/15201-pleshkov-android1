package ru.nsu.ccfit.pleshkov.notebook.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

abstract class BaseDatabaseActivity : BaseActivity(), JobHolder {

    override val job = Job()

    protected val dbPresenter = DatabasePresenter()
    protected lateinit var initDatabaseTask: Deferred<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbPresenter.begin(this)

        initDatabaseTask = dbPresenter.initDatabases()
    }

    override fun onDestroy() {
        job.cancel()
        dbPresenter.end()
        super.onDestroy()
    }
}
