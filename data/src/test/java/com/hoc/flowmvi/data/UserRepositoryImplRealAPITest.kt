package com.hoc.flowmvi.data

import android.util.Log
import com.hoc.flowmvi.core.dispatchers.AppCoroutineDispatchers
import com.hoc.flowmvi.domain.repository.UserRepository
import com.hoc.flowmvi.test_utils.rightValueOrThrow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalTime
@ExperimentalStdlibApi
class UserRepositoryImplRealAPITest : KoinTest {
  @get:Rule
  val koinRuleTest = KoinTestRule.create {
    printLogger(Level.DEBUG)

    modules(
      dataModule,
      module {
        factory<AppCoroutineDispatchers> {
          object : AppCoroutineDispatchers {
            override val main: CoroutineDispatcher get() = Main
            override val io: CoroutineDispatcher get() = IO
            override val mainImmediate: CoroutineDispatcher get() = Main.immediate
          }
        }
      }
    )
  }

  @get:Rule
  val timberRule = TimberRule()

  private val userRepo by inject<UserRepository>()

  @Test
  fun getUsers() = runBlocking {
    kotlin.runCatching {
      val result = userRepo
        .getUsers()
        .first()
      assertTrue(result.isRight())
      assertTrue(result.rightValueOrThrow.isNotEmpty())
    }
    Unit
  }
}

class TimberRule : TestWatcher() {
  private val tree = ConsoleTree()

  override fun starting(description: Description) {
    Timber.plant(tree)
  }

  override fun finished(description: Description) {
    Timber.uproot(tree)
  }
}

class ConsoleTree : Timber.DebugTree() {
  private val anonymousClassPattern = """(\$\d+)+$""".toRegex()

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    val dateTime = LocalDateTime.now().format(dateTimeFormatter)
    val priorityChar = when (priority) {
      Log.VERBOSE -> 'V'
      Log.DEBUG -> 'D'
      Log.INFO -> 'I'
      Log.WARN -> 'W'
      Log.ERROR -> 'E'
      Log.ASSERT -> 'A'
      else -> '?'
    }

    println("$dateTime $priorityChar/$tag: $message")
  }

  override fun createStackElementTag(element: StackTraceElement): String {
    val className = element.className
    val tag = if (anonymousClassPattern.containsMatchIn(className)) {
      anonymousClassPattern.replace(className, "")
    } else {
      className
    }
    return tag.substringAfterLast('.')
  }
}
