package jp.kroyeeg.intellijenvfileplugin

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

    fun test() {
        println(">>>>>>>>>>>>. ${System.getenv("HOGE")}")
        assert(true)
    }
}
