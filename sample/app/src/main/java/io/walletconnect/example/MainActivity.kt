package io.walletconnect.example

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.screen_main.*
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import io.walletconnect.example.util.LogUtils
import io.walletconnect.example.util.SPUtils
import io.walletconnect.example.util.eth.EthUtils
import io.walletconnect.example.util.eth.FunctionEncoderUtils
import io.walletconnect.example.util.eth.Web3jBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.walletconnect.Session
import org.walletconnect.nullOnThrow


class MainActivity : Activity(), Session.Callback {

    private var txRequest: Long? = null
    private var metaMaskPackName: String? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)


    private val mWeb3jBean: Web3jBean = EthUtils.mDiGiFTTestWeb3jBean
//    private val mWeb3jBean: Web3jBean = EthUtils.mHecoWeb3jBean

    override fun onStatus(status: Session.Status) {
        when (status) {
            Session.Status.Approved -> sessionApproved()
            Session.Status.Closed -> sessionClosed()
            Session.Status.Connected -> sessionConnected()
            Session.Status.Disconnected -> sessionDisconnected()
            Session.Status.AuthFailed -> sessionAuthFailed()
            is Session.Status.Error -> {
                // Do Stuff
            }
        }
    }

    override fun onMethodCall(call: Session.MethodCall) {
        Log.e("onMethodCall", "onMethodCall: $call")
    }

    private fun sessionAuthFailed() {
        Log.e("status", "sessionAuthFailed")
        uiScope.launch {
            screen_main_status.text = "Session Rejected"
        }
    }

    private fun sessionApproved() {
        Log.e("status", "sessionApproved")
        if (ExampleApplication.storage.list().isNotEmpty()) {
            Log.e(
                "sessionApproved",
                "钱包地址：${
                    ExampleApplication.session.approvedAccounts()?.first()
                } 链ID: ${ExampleApplication.session.approvedChainId()}"
            )
            SPUtils.getInstance(applicationContext, "test_walletConnect")
                .put("walletId", ExampleApplication.session.approvedAccounts()?.first().toString())
            SPUtils.getInstance(applicationContext, "test_walletConnect")
                .put("chainId", ExampleApplication.session.approvedChainId()!!)

            uiScope.launch {
                screen_main_status.text = "Connected"
                tv_wallet_address.text = ExampleApplication.session.approvedAccounts()?.first()
                tv_chain_id.text = ExampleApplication.session.approvedChainId().toString()
                screen_main_connect_button.isEnabled = false
                screen_main_connect_button.setBackgroundResource(R.drawable.btn_shape_bg_enabled)
                screen_main_connect_button.setTextColor(resources.getColor(R.color.black))

                screen_main_disconnect_button.isEnabled = true
                screen_main_disconnect_button.setBackgroundResource(R.drawable.btn_shape_bg)
                screen_main_disconnect_button.setTextColor(resources.getColor(R.color.white))

                /*screen_main_disconnect_button.visibility = View.VISIBLE
                screen_main_tx_button.visibility = View.VISIBLE
                screen_main_sign_button.visibility = View.VISIBLE*/
            }
        }
    }

    private fun sessionClosed() {
        Log.e("status", "sessionClosed")
        ExampleApplication.storage.remove("ffd70e47-8634-4eba-95e9-81d7d1ee3bc3")//根据handshakeTopic去删除, 先固定这个值使用
        SPUtils.getInstance(applicationContext, "test_walletConnect").remove("walletId")
        SPUtils.getInstance(applicationContext, "test_walletConnect").remove("chainId")
        screen_main_connect_button.isEnabled = true
        screen_main_connect_button.setBackgroundResource(R.drawable.btn_shape_bg)
        screen_main_connect_button.setTextColor(resources.getColor(R.color.white))
        screen_main_disconnect_button.isEnabled = false
        screen_main_disconnect_button.setBackgroundResource(R.drawable.btn_shape_bg_enabled)
        screen_main_disconnect_button.setTextColor(resources.getColor(R.color.black))
        tv_wallet_address.text = ""
        tv_chain_id.text = ""

        uiScope.launch {
            screen_main_status.text = "Disconnected"
        }
    }

    private fun sessionConnected() {
        Log.e("status", "sessionConnected")
    }

    private fun sessionDisconnected() {
        Log.e("status", "sessionDisconnected")
    }

    override fun onStart() {
        super.onStart()
        Log.e("TAG", "onStart")
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_main)

        metaMaskPackName = CommUtils.getAppInfo(this)

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


        btn_auth_sign.setOnClickListener {
            if (ExampleApplication.session != null) {
                val txRequest = ConstantAction.singAction
                ExampleApplication.session.performMethodCall(
                    Session.MethodCall.SignMessage(
                        1,
                        "0xc2a5306d3D9a0b22D9Ff0260d3ca68f4179C3fd8",
                        "4920616d206d652c206120646966666572656e7420636f6c6f722066697265776f726b7321"
                    ),
                    ::handleResponse
                )
                this.txRequest = txRequest

                //主动跳转至MetaMask
                CommUtils.openApp(this, metaMaskPackName)
            } else {
                Toast.makeText(this, "请先建立连接", Toast.LENGTH_SHORT).show()
            }
        }


        btn_name_query.setOnClickListener {
            getTokenName()
        }


        btn_amountA_query.setOnClickListener {
            getAmountA()
        }

        btn_amountB_query.setOnClickListener {
            getAmountB()
        }

        btn_flag_query.setOnClickListener {
            getFlag()
        }

        btn_initAddLiquidity_query.setOnClickListener {
            val from =
                ExampleApplication.session.approvedAccounts()?.first() ?: ""

            val subscribe = EthUtils.getNonceFlowable(mWeb3jBean, from)
                .subscribe({ nonce ->
                    toInitAddLiquidity(from, nonce.toString())
                }, Throwable::printStackTrace)
        }


        btn_addLiquidity_send.setOnClickListener {
            val from =
                ExampleApplication.session.approvedAccounts()?.first() ?: ""
            val subscribe = EthUtils.getNonceFlowable(mWeb3jBean, from)
                .subscribe({ nonce ->
                    toAddLiquidity(from, nonce.toString())
                }, Throwable::printStackTrace)
        }

        btn_buy_send.setOnClickListener {
            val from =
                ExampleApplication.session.approvedAccounts()?.first() ?: ""
            val subscribe = EthUtils.getNonceFlowable(mWeb3jBean, from)
                .subscribe({ nonce ->
                    toAddLiquidity(from, nonce.toString())
                }, Throwable::printStackTrace)
        }

        btn_buy_send.setOnClickListener {
            val from =
                ExampleApplication.session.approvedAccounts()?.first() ?: ""
            val subscribe = EthUtils.getNonceFlowable(mWeb3jBean, from)
                .subscribe({ nonce ->
                    toBuy(from, nonce.toString())
                }, Throwable::printStackTrace)
        }

        btn_sell_send.setOnClickListener {
            val from =
                ExampleApplication.session.approvedAccounts()?.first() ?: ""
            val subscribe = EthUtils.getNonceFlowable(mWeb3jBean, from)
                .subscribe({ nonce ->
                    toSell(from, nonce.toString())
                }, Throwable::printStackTrace)
        }

    }


    private fun toInitAddLiquidity(fromAddress: String, nonce: String) {

        val erc20Address = mWeb3jBean.mdexAddress

        val encodedFunction =
            FunctionEncoderUtils.getInitAddLiquidityEncoder()
        LogUtils.e("encodedFunction:  $encodedFunction")

        toSessionSendTransaction(
            System.currentTimeMillis(),
            fromAddress,
            nonce,
            erc20Address,
            encodedFunction
        )

    }


    private fun toAddLiquidity(fromAddress: String, nonce: String) {

        val erc20Address = mWeb3jBean.mdexAddress

        var token1Value = edt_amountA_content.text.toString()
        var token2Value = edt_amountB_content.text.toString()


        val encodedFunction =
            FunctionEncoderUtils.getAddLiquidityEncoder(token1Value, token2Value)
        LogUtils.e("encodedFunction:  $encodedFunction")

        toSessionSendTransaction(
            System.currentTimeMillis(),
            fromAddress,
            nonce,
            erc20Address,
            encodedFunction
        )

    }

    private fun toBuy(fromAddress: String, nonce: String) {

        val erc20Address = mWeb3jBean.mdexAddress

        var numValue = edt_buy_content.text.toString()

        val encodedFunction =
            FunctionEncoderUtils.getPlaceOrderBuyEncoder(numValue)
        LogUtils.e("encodedFunction:  $encodedFunction")

        toSessionSendTransaction(
            System.currentTimeMillis(),
            fromAddress,
            nonce,
            erc20Address,
            encodedFunction
        )

    }

    private fun toSell(fromAddress: String, nonce: String) {

        val erc20Address = mWeb3jBean.mdexAddress

        var numValue = edt_sell_content.text.toString()

        val encodedFunction =
            FunctionEncoderUtils.getPlaceOrderSellEncoder(numValue)
        LogUtils.e("encodedFunction:  $encodedFunction")

        toSessionSendTransaction(
            System.currentTimeMillis(),
            fromAddress,
            nonce,
            erc20Address,
            encodedFunction
        )

    }


    private fun toSessionSendTransaction(
        txRequest: Long,
        fromAddress: String,
        nonce: String,
        tokenAddress: String,
        encodedFunction: String
    ) {
        toSessionSendTransaction(txRequest, fromAddress, nonce, tokenAddress, "0", encodedFunction)
    }


    private fun toSessionSendTransaction(
        txRequest: Long,
        fromAddress: String,
        nonce: String,
        tokenAddress: String,
        value: String,
        encodedFunction: String
    ) {
        val hexNonce = EthUtils.getHexStr(nonce)
        val hexGWei = EthUtils.getHexGWei(mWeb3jBean.platform)
        var hexGasLimit = EthUtils.getHexStr("70000")

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

    private fun getTokenName() {
        val tokenAddress = mWeb3jBean.mdexAddress
        var subscribe = EthUtils.getTokenNameFlowable(mWeb3jBean, tokenAddress)
            .subscribe({ tokenName ->
                run {
                    LogUtils.e("$tokenAddress,tokenName:  $tokenName")
                    tv_name_result.text = tokenName
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


    private fun getTokenSymbol() {
        val tokenAddress = mWeb3jBean.mdexAddress
        var subscribe = EthUtils.getTokenSymbolFlowable(mWeb3jBean, tokenAddress)
            .subscribe({ symbol ->
                run {
                    LogUtils.e("$tokenAddress,symbol:  $symbol")
                }
            }, Throwable::printStackTrace)
    }


    private fun getAmountA() {
        val tokenAddress = mWeb3jBean.mdexAddress
        var subscribe = EthUtils.getAmountAFlowable(mWeb3jBean, tokenAddress)
            .subscribe({ amountA ->
                run {
                    LogUtils.e("$tokenAddress,amountA:  $amountA")
                    tv_amountA_result.text = amountA.toString()
                }
            }, Throwable::printStackTrace)
    }

    private fun getAmountB() {
        val tokenAddress = mWeb3jBean.mdexAddress
        var subscribe = EthUtils.getAmountBFlowable(mWeb3jBean, tokenAddress)
            .subscribe({ amountB ->
                run {
                    LogUtils.e("$tokenAddress,tokenName:  $amountB")
                    tv_amountB_result.text = amountB.toString()
                }
            }, Throwable::printStackTrace)
    }

    private fun getFlag() {
        val tokenAddress = mWeb3jBean.mdexAddress
        var subscribe = EthUtils.getFlagFlowable(mWeb3jBean, tokenAddress)
            .subscribe({ flag ->
                run {
                    LogUtils.e("$tokenAddress,tokenName:  $flag")
                    tv_flag_result.text = flag
                }
            }, Throwable::printStackTrace)
    }


    private fun initialSetup() {
        val curWalletId =
            SPUtils.getInstance(applicationContext, "test_walletConnect").getString("walletId")
        if (!TextUtils.isEmpty(curWalletId)) {
            Log.e("TAG", "重新连接")
            ExampleApplication.reConnect(applicationContext)
            ExampleApplication.session.addCallback(this)

            sessionApproved()
        } else {
            Log.e("TAG", "建立授权连接")
            val session = nullOnThrow { ExampleApplication.session } ?: return
            session.addCallback(this)
        }
    }

    private fun handleResponse(resp: Session.MethodCall.Response) {
        Log.e(
            "#####",
            "handleResponse: ${resp.toString()}"
        )//error=Error(code=-32000, message=User rejected the transaction)
        if (resp.id == ConstantAction.singAction) {
            txRequest = null
            if (resp.result == null) {
                uiScope.launch {
                    tv_sign_result.text =
                        "Last response: " + ((resp.error?.message as? String) ?: "Unknown response")
                }
            } else {
                uiScope.launch {
                    tv_sign_result.text =
                        "Last response: " + ((resp.result as? String) ?: "Unknown response")
                }
            }

            //这里应该主动把当前APP打开
            CommUtils.setTopApp(this)
        } else if (resp.id == txRequest) {
            txRequest = null
            if (resp.result == null) {
                uiScope.launch {
                    val content =
                        "Last response: " + ((resp.error?.message as? String) ?: "Unknown response")
                    LogUtils.e(content)
                }
            } else {
                uiScope.launch {
                    val content =
                        "Last response: " + ((resp.result as? String) ?: "Unknown response")
                    LogUtils.e(content)
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
