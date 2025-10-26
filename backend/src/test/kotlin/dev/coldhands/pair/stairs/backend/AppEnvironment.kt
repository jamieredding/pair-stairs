package dev.coldhands.pair.stairs.backend

class AppEnvironment {
    val recorder = FakeRecorderHttp()
    val client = MyMathApp(recorder)
}