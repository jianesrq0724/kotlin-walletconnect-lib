package io.walletconnect.example

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import io.walletconnect.example.utils.Convert
import kotlinx.android.synthetic.main.screen_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Address
import org.walletconnect.Session
import org.walletconnect.nullOnThrow
import java.util.*


class MainActivity : Activity(), Session.Callback {

    private var txRequest: Long? = null
    private var metaMaskPackName: String? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onStatus(status: Session.Status) {
        when (status) {
            Session.Status.Approved -> sessionApproved()
            Session.Status.Closed -> sessionClosed()
            Session.Status.Connected, Session.Status.Disconnected, is Session.Status.Error -> {
                // Do Stuff
            }
        }
    }

    override fun onMethodCall(call: Session.MethodCall) {
    }

    private fun sessionApproved() {
        Log.d("sessionApproved", "钱包地址：${ExampleApplication.session.approvedAccounts()}")
        if (ExampleApplication.session.approvedAccounts() != null) {
            uiScope.launch {
                screen_main_status.text =
                    "Connected: ${ExampleApplication.session.approvedAccounts()}"
                screen_main_connect_button.visibility = View.GONE
                screen_main_disconnect_button.visibility = View.VISIBLE
                screen_main_tx_button.visibility = View.VISIBLE

            }
        } else {
            Toast.makeText(
                this, "Request for MetaMask authorization connection denied", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sessionClosed() {
        Log.d("sessionClosed", "sessionClosed")
        uiScope.launch {
            screen_main_status.text = "Disconnected"
            screen_main_connect_button.visibility = View.VISIBLE
            screen_main_disconnect_button.visibility = View.GONE
            screen_main_tx_button.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_main)

        metaMaskPackName = CommUtils.getAppInfo(this)
    }

    override fun onStart() {
        super.onStart()
        initialSetup()
        screen_main_connect_button.setOnClickListener {
            ExampleApplication.resetSession()
            ExampleApplication.session.addCallback(this)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(ExampleApplication.config.toWCUri())
            startActivity(i)
        }
        screen_main_disconnect_button.setOnClickListener {
            ExampleApplication.session.kill()
        }
        screen_main_tx_button.setOnClickListener {
            toSendEth()
//            toSendToken()
//            toSignMessage()
        }
    }



    private fun toSignMessage() {

        val encodedFunction =
            "0xa9059cbb00000000000000000000000054f8fb804f1c0d3f8d3787ebd48141114cb9550000000000000000000000000000000000000000000000000000000000000f4240"

        val txRequest = System.currentTimeMillis()
        ExampleApplication.session.performMethodCall(
            Session.MethodCall.SignMessage(
                txRequest,
                "0x8d7804d45a4c73d3ffa213647a3107f9209d298a",
                "test"
            ), ::handleResponse
        )
        this.txRequest = txRequest

        //主动跳转至MetaMask
        CommUtils.openApp(this, metaMaskPackName)

    }


    private fun toSendToken() {
        val from =
            ExampleApplication.session.approvedAccounts()?.first() ?: return

//        val function = Function(
//            "transfer",
//            Arrays.asList(Address(toAddress), Uint256(value)),
//            listOf(object : TypeReference<Type?>() {})
//        )

        val encodedFunction =
            "0xa9059cbb00000000000000000000000054f8fb804f1c0d3f8d3787ebd48141114cb9550000000000000000000000000000000000000000000000000000000000000f4240"

        val txRequest = System.currentTimeMillis()
        ExampleApplication.session.performMethodCall(
            Session.MethodCall.SendTransaction(
                txRequest,
                from,
                "0x0298c2b32eae4da002a15f36fdf7615bea3da047",
                "12064",
                Convert.toWei("3", Convert.Unit.GWEI).toBigInteger().toString(),
                "70000",
                "0",
                encodedFunction
            ), ::handleResponse
        )
        this.txRequest = txRequest

        //主动跳转至MetaMask
        CommUtils.openApp(this, metaMaskPackName)
    }


    private fun toSendEth() {
        val from =
            ExampleApplication.session.approvedAccounts()?.first() ?: return
        val txRequest = System.currentTimeMillis()
        ExampleApplication.session.performMethodCall(
            Session.MethodCall.SendTransaction(
                txRequest,
                from,
                "0x431900bF806508044D7218f635e5615baA462880",
                "2",
                Convert.toWei("3", Convert.Unit.GWEI).toBigInteger().toString(),
                "21000",
                Convert.toWei("0.001", Convert.Unit.ETHER).toBigInteger().toString(),
                ""
            ), ::handleResponse
        )
        this.txRequest = txRequest

        //主动跳转至MetaMask
        CommUtils.openApp(this, metaMaskPackName)
    }

    private fun initialSetup() {
        val session = nullOnThrow { ExampleApplication.session } ?: return
        session.addCallback(this)
        sessionApproved()
    }

    private fun handleResponse(resp: Session.MethodCall.Response) {
        Log.d(
            "#####", "handleResponse: ${resp.toString()}"
        )//error=Error(code=-32000, message=User rejected the transaction)
        if (resp.id == txRequest) {
            txRequest = null
            if (resp.result == null) {
                uiScope.launch {
                    screen_main_response.visibility = View.VISIBLE
                    screen_main_response.text =
                        "Last response: " + ((resp.error?.message as? String) ?: "Unknown response")
                }
            } else {
                uiScope.launch {
                    screen_main_response.visibility = View.VISIBLE
                    screen_main_response.text =
                        "Last response: " + ((resp.result as? String) ?: "Unknown response")
                }
            }

            //这里应该主动把当前APP打开
            CommUtils.setTopApp(this)
        }
    }

    override fun onDestroy() {
        ExampleApplication.session.removeCallback(this)
        super.onDestroy()
    }
}
