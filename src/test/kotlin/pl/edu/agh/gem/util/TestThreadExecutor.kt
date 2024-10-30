package pl.edu.agh.gem.util

import java.util.concurrent.Executor

class TestThreadExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }
}
