package io.walletconnect.example

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.squareup.moshi.Moshi
import io.walletconnect.example.server.BridgeServer
import io.walletconnect.example.util.SPUtils
import okhttp3.OkHttpClient
import org.walletconnect.Session
import org.walletconnect.impls.*
import org.walletconnect.nullOnThrow
import java.io.File

class ExampleApplication : MultiDexApplication() {

    private var mContext: Context? = null

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        initMoshi()
        initClient()
        initBridge()
        initSessionStorage()
    }

    private fun initClient() {
        client = OkHttpClient.Builder().build()
    }

    private fun initMoshi() {
        moshi = Moshi.Builder().build()
    }


    private fun initBridge() {
        bridge = BridgeServer(moshi)
        bridge.start()
    }

    private fun initSessionStorage() {
        storage = FileWCSessionStore(File(cacheDir, "session_store.json").apply { createNewFile() }, moshi)
//        Log.e("TAG","storage = ${storage.list()}")
    }

    companion object {
        private lateinit var client: OkHttpClient
        private lateinit var moshi: Moshi
        private lateinit var bridge: BridgeServer
        lateinit var storage: WCSessionStore
        lateinit var config: Session.Config
        lateinit var session: Session

        fun resetSession() {
            nullOnThrow { session }?.clearCallbacks()
//            val key = ByteArray(32).also { Random().nextBytes(it) }.toNoPrefixHexString()
            config = Session.Config("ffd70e47-8634-4eba-95e9-81d7d1ee3bc3", "https://bridge.walletconnect.org", "10d842ec755f67ed37de894811d2b641e1e752f3a91cec05d64ed4b7735cb8c8")
//                Session.Config(UUID.randomUUID().toString(), "https://bridge.walletconnect.org", key)
            session = WCSession(config.toFullyQualifiedConfig(),
                    MoshiPayloadAdapter(moshi),
                    storage,
                    OkHttpTransport.Builder(client, moshi),
                    Session.PeerMeta(name = "Android Test App")
            )
            session.offer()
        }

        fun reConnect(context: Context){
            val uri =
                "wc:ffd70e47-8634-4eba-95e9-81d7d1ee3bc3@1?bridge=https%3A%2F%2Fbridge.walletconnect.org&key=10d842ec755f67ed37de894811d2b641e1e752f3a91cec05d64ed4b7735cb8c9"
            config = Session.Config.fromWCUri(uri)
            session = WCSession(config.toFullyQualifiedConfig(),
                MoshiPayloadAdapter(moshi),
                storage,
                OkHttpTransport.Builder(client, moshi),
                Session.PeerMeta(name = "Android Test App")
            )

            session.init()
            session.approve(listOf( SPUtils.getInstance(context,"test_walletConnect").getString("walletId")), SPUtils.getInstance(context,"test_walletConnect").getLong("chainId"))
        }

    }


}
