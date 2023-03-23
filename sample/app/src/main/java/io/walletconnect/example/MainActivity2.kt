package io.walletconnect.example

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import io.walletconnect.example.util.eth.FunctionEncoderUtils
import io.walletconnect.example.util.eth.Web3jBean
import io.walletconnect.example.util.eth.EthUtils
import io.walletconnect.example.util.eth.EthUtils.mHecoWeb3jBean
import io.walletconnect.example.util.LogUtils
import kotlinx.android.synthetic.main.screen_main.*
import kotlinx.android.synthetic.main.screen_main.screen_main_connect_button
import kotlinx.android.synthetic.main.screen_main.screen_main_disconnect_button
import kotlinx.android.synthetic.main.screen_main.screen_main_status
import kotlinx.android.synthetic.main.screen_main_2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.walletconnect.Session
import org.walletconnect.nullOnThrow
import org.web3j.utils.Convert


class MainActivity2 : Activity(), Session.Callback {

    private var txRequest: Long? = null
    private var metaMaskPackName: String? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)

    //    private val mWeb3jBean: Web3jBean = mDiGiFTTestWeb3jBean
    private val mWeb3jBean: Web3jBean = mHecoWeb3jBean

    override fun onStatus(status: Session.Status) {
        when (status) {
            Session.Status.Approved -> sessionApproved()
            Session.Status.Closed -> sessionClosed()
            Session.Status.Connected, Session.Status.Disconnected, is Session.Status.Error -> {

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

                EthUtils.getGasPrice(mWeb3jBean)
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
        setContentView(R.layout.screen_main_2)
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

            // TODO:
//            getTokenSymbol()
//            getTokenName()
//            EthUtils.getErc20Balance(mWeb3jBean, "0x8d7804d45a4C73D3FFa213647a3107f9209d298A", mWeb3jBean.USDT)

        }
        screen_main_disconnect_button.setOnClickListener {
            ExampleApplication.session.kill()
        }
        screen_main_tx_button.setOnClickListener {
//            getNonceSendEth()
//            getNonceSendToken()
//            toSignMessage()

            val from =
                ExampleApplication.session.approvedAccounts()?.first() ?: ""

            val subscribe = EthUtils.getNonceFlowable(mWeb3jBean, from)
                .subscribe({ nonce ->
                    toAddLiquidity(from, nonce.toString())
                }, Throwable::printStackTrace)

        }
    }


    private fun toSignMessage() {

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


    private fun getNonceSendToken() {

        val from =
            ExampleApplication.session.approvedAccounts()?.first() ?: return

        val subscribe = EthUtils.getNonceFlowable(mWeb3jBean, from)
            .subscribe({ nonce ->
                toSendToken(from, nonce.toString())
            }, Throwable::printStackTrace)

    }


    private fun toSendToken(fromAddress: String, nonce: String) {

        val number = "0.001"
        val toAddress = "0x54f8Fb804f1C0D3f8D3787EBd48141114Cb95500"

        // husd 地址
        val erc20Address = "0x0298c2b32eae4da002a15f36fdf7615bea3da047"
        val erc20DecimalPoint = 8

        val encodedFunction =
            FunctionEncoderUtils.getTransferEncoder(toAddress, number, erc20DecimalPoint)
        LogUtils.e("encodedFunction:  $encodedFunction")

        toSessionSendTransaction(fromAddress, nonce, erc20Address, encodedFunction)

    }

    private fun toInitAddLiquidity(fromAddress: String, nonce: String) {

        val erc20Address = mWeb3jBean.mdexAddress

        val encodedFunction =
            FunctionEncoderUtils.getInitAddLiquidityEncoder()
        LogUtils.e("encodedFunction:  $encodedFunction")

        toSessionSendTransaction(fromAddress, nonce, erc20Address, encodedFunction)

    }

    private fun toAddLiquidity(fromAddress: String, nonce: String) {

        val erc20Address = mWeb3jBean.mdexAddress

        val encodedFunction =
            FunctionEncoderUtils.getAddLiquidityEncoder("1.2", "2.3")
        LogUtils.e("encodedFunction:  $encodedFunction")

        toSessionSendTransaction(fromAddress, nonce, erc20Address, encodedFunction)

    }


    private fun toSessionSendTransaction(
        fromAddress: String,
        nonce: String,
        tokenAddress: String,
        encodedFunction: String
    ) {
        toSessionSendTransaction(fromAddress, nonce, tokenAddress, "0", encodedFunction)
    }


    private fun toSessionSendTransaction(
        fromAddress: String,
        nonce: String,
        tokenAddress: String,
        value: String,
        encodedFunction: String
    ) {
        val hexNonce = EthUtils.getHexStr(nonce)
        val hexGWei = EthUtils.getHexGWei(mWeb3jBean.platform)
        var hexGasLimit = EthUtils.getHexStr("70000")

        val txRequest = System.currentTimeMillis()
        ExampleApplication.session.performMethodCall(
            Session.MethodCall.SendTransaction(
                txRequest,
                fromAddress,
                tokenAddress,
                hexNonce,
                hexGWei,
                hexGasLimit,
                value,
                encodedFunction
            ), ::handleResponse
        )
        this.txRequest = txRequest

        //主动跳转至MetaMask
        CommUtils.openApp(this, metaMaskPackName)

    }

    private fun getNonceSendEth() {

        val from =
            ExampleApplication.session.approvedAccounts()?.first() ?: return

        val subscribe = EthUtils.getNonceFlowable(mWeb3jBean, from)
            .subscribe({ nonce ->
                toSendEth(from, nonce.toString())
            }, Throwable::printStackTrace)

    }


    private fun toSendEth(fromAddress: String, nonce: String) {

        val toAddress = "0x54f8Fb804f1C0D3f8D3787EBd48141114Cb95500"

        val hexValue =
            EthUtils.getHexStr(Convert.toWei("0.001", Convert.Unit.ETHER).toBigInteger().toString())

        toSessionSendTransaction(fromAddress, nonce, toAddress, hexValue, "")

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

    private fun getTokenSymbol() {
        val tokenAddress = mWeb3jBean.mdexAddress
        var subscribe = EthUtils.getTokenSymbolFlowable(mWeb3jBean, tokenAddress)
            .subscribe({ symbol ->
                run {
                    LogUtils.e("$tokenAddress,symbol:  $symbol")
                }
            }, Throwable::printStackTrace)
    }

    private fun getTokenName() {
        val tokenAddress = mWeb3jBean.mdexAddress
        var subscribe = EthUtils.getTokenNameFlowable(mWeb3jBean, tokenAddress)
            .subscribe({ symbol ->
                run {
                    LogUtils.e("$tokenAddress,name:  $symbol")
                }
            }, Throwable::printStackTrace)
    }

    private fun getTokenDecimal() {
        val tokenAddress = mWeb3jBean.USDT
        var subscribe = EthUtils.getTokenDecimalFlowable(mWeb3jBean, tokenAddress)
            .subscribe({ decimals ->
                run {
                    LogUtils.e("decimals:  $decimals")
                }
            }, Throwable::printStackTrace)
    }


    override fun onDestroy() {
        ExampleApplication.session.removeCallback(this)
        super.onDestroy()
    }

}
