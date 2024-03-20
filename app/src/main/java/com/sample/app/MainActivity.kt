package com.sample.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sample.app.ui.theme.MyAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    val dispatcher = Dispatchers.IO
    val scope = CoroutineScope(dispatcher)
    val chanel = Channel<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
        val result = mutableStateFlows()

        scope.launch {
            delay(12000)
            result.collect{
                Log.d("sample", "collected $it")
            }
        }
    }

    private fun flowProducer() =
        flow<Int> {
            listOf<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9).forEach {
                delay(1000)
                Log.d("produced", it.toString())
                emit(it)
            }
        }

    private fun sharedFlowProducer(): Flow<Int> {
        val mutable = MutableSharedFlow<Int>()
        scope.launch {
            listOf<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9).forEach {
                mutable.emit(it)
//                Log.d("sample", "produced $it")
                delay(1000)
            }
        }
        return mutable
    }

    private fun mutableStateFlows(): Flow<Int> {
        val mutable = MutableStateFlow<Int>(10)
        scope.launch {
            listOf<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9).forEach {
                mutable.emit(it)
//                Log.d("sample", "produced $it")
                delay(1000)
            }
        }
        return mutable
    }

    private fun flowConsumer() =
        scope.launch { flowProducer().map {}.onStart { }.onCompletion { }.collect() }

    private fun sharedFlowConsumer(delayInMS: Long = 0) =
        scope.launch {
            delay(delayInMS)
            sharedFlowProducer().map {it}.collect {
                Log.d("sample", "collected $it")
            }
        }

    private fun channelProducer() {
        scope.launch {
            chanel.send(1)
            chanel.send(2)
        }
    }

    private fun channelConsumer() {
        scope.launch {
            chanel.receive()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyAppTheme {
        Greeting("Android")
    }
}