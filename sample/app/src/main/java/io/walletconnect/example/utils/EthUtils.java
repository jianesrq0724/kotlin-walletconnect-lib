package io.walletconnect.example.utils;


import android.util.Log;

import org.reactivestreams.Publisher;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.walletconnect.example.database.DataLitePal;
import io.walletconnect.example.eth.Web3jBean;

public class EthUtils {

    public static final String PLATFORM_HECO = "heco";

    public static Web3jBean mHecoMdexWeb3jBean = new Web3jBean(PLATFORM_HECO, "https://http-mainnet.hecochain.com", 128, "0xed7d5f38c79115ca12fe6c0041abb22f0a06c300",
            "0x5545153ccfca01fbd7dd11c0b23ba694d9509a6f", "0xa71edc38d189767582c38a3145b5873052c3e47a", "0x0298c2b32eae4da002a15f36fdf7615bea3da047", "0x499B6E03749B4bAF95F9E70EeD5355b138EA6C31");


    public static String getHexStr(String value) {
        BigInteger valueBigInteger = new BigInteger(value);
        return getHexStr(valueBigInteger);
    }

    public static String getHexStr(BigInteger value) {
        String hexValue = Numeric.toHexStringNoPrefix(value);
        return "0x" + hexValue;
    }


    public static void getNonce(Web3jBean web3jBean, String from) {
        Disposable subscribe = getNonceFlowable(web3jBean, from)
                .subscribe(new Consumer<BigInteger>() {
                    @Override
                    public void accept(BigInteger bigInteger) throws Exception {

                    }
                }, Throwable::printStackTrace);
    }


    public static Flowable<BigInteger> getNonceFlowable(Web3jBean web3jBean, String fromAddress) {
        Flowable<BigInteger> flowable = web3jBean.web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
                .flowable()
                .compose(RxUtils.rxSchedulerNewThread())
                .flatMap((io.reactivex.functions.Function<EthGetTransactionCount, Publisher<BigInteger>>) ethGetTransactionCount -> {
                    BigInteger nonce = ethGetTransactionCount.getTransactionCount();

                    Log.i("TAG", fromAddress + " , nonce: " + nonce);
                    return Flowable.just(nonce);
                })
                .doOnError(Throwable::printStackTrace);
        return flowable;
    }

    public static void getGasPrice(Web3jBean web3jBean) {
        long lastTime = System.currentTimeMillis();
        Disposable subscribe = web3jBean.web3j.ethGasPrice()
                .flowable()
                .compose(RxUtils.rxSchedulerNewThread())
                .subscribe(ethGasPrice -> {

                    BigInteger gasPriceWei = ethGasPrice.getGasPrice();
                    BigDecimal gasPrice = Convert.fromWei(gasPriceWei.toString(), Convert.Unit.GWEI);
                    LogUtils.e(web3jBean.platform + " gasPrice: " + gasPrice + " , rpcUrl: " + web3jBean.rpcUrl);

                    DataLitePal.getInstance().setMinGasPrice(web3jBean.platform, gasPrice);
                }, Throwable::printStackTrace);

    }


    public static BigDecimal gasPriceDecimalPoint(String platform, BigDecimal gasPrice) {
        int scale = 1;
        return numberDecimalPoint(gasPrice, scale);
    }

    public static BigDecimal numberDecimalPoint(BigDecimal num, int scale) {
        return num.setScale(scale, RoundingMode.HALF_UP);
    }

    public static String getHexGWei(String platform) {
        BigDecimal minGasPrice = DataLitePal.getInstance().getMinGasPrice(platform);
        String hexGWei = getHexStr(Convert.toWei(minGasPrice.toString(), Convert.Unit.GWEI).toBigInteger().toString());
        return hexGWei;
    }

}
