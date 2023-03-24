package io.walletconnect.example.util.eth;


import android.text.TextUtils;
import android.util.Log;

import org.reactivestreams.Publisher;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.walletconnect.example.bean.Web3jBean;
import io.walletconnect.example.database.DataLitePal;
import io.walletconnect.example.util.LogUtils;
import io.walletconnect.example.util.Numeric;
import io.walletconnect.example.util.RxUtils;
import io.walletconnect.example.util.StringUtils;

public class EthUtils {

    public static final String PLATFORM_HECO = "heco";

    public static final String PLATFORM_DIGIFT_TEST = "digift";

    public static final String PLATFORM_GOERLI_TEST = "GoerLi";

    public static Web3jBean mHecoWeb3jBean = new Web3jBean(PLATFORM_HECO, "https://http-mainnet.hecochain.com", 128, "0xed7d5f38c79115ca12fe6c0041abb22f0a06c300",
            "0x5545153ccfca01fbd7dd11c0b23ba694d9509a6f", "0xa71edc38d189767582c38a3145b5873052c3e47a", "0x0298c2b32eae4da002a15f36fdf7615bea3da047", "0x499B6E03749B4bAF95F9E70EeD5355b138EA6C31");

    // digift test
    public static Web3jBean mDiGiFTTestWeb3jBean = new Web3jBean(PLATFORM_DIGIFT_TEST, "https://ethnode.digifttest.com", 15, "0x14D5Ca5ac738f6d7D0012B5768A948017Eb8Ef41",
            "0xb95e7911c49195b05a6f83052db8000603691ef5", "0xb95e7911c49195b05a6f83052db8000603691ef5", "0xb95e7911c49195b05a6f83052db8000603691ef5", "0xb95e7911c49195b05a6f83052db8000603691ef5");

    // uat
    public static Web3jBean mGoerLiTestWeb3jBean = new Web3jBean(PLATFORM_GOERLI_TEST, "https://goerli.infura.io/v3/", 5, "0x14D5Ca5ac738f6d7D0012B5768A948017Eb8Ef41",
            "0xb95e7911c49195b05a6f83052db8000603691ef5", "0xb95e7911c49195b05a6f83052db8000603691ef5", "0xb95e7911c49195b05a6f83052db8000603691ef5", "0xb95e7911c49195b05a6f83052db8000603691ef5");


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
                .doOnError(throwable -> {
                    throwable.printStackTrace();

                });
        return flowable;
    }

    public static Flowable<BigInteger> getPendingNonceFlowable(Web3jBean web3jBean, String fromAddress) {
        Flowable<BigInteger> flowable = web3jBean.web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
                .flowable()
                .compose(RxUtils.rxSchedulerNewThread())
                .flatMap((io.reactivex.functions.Function<EthGetTransactionCount, Publisher<BigInteger>>) ethGetTransactionCount -> {
                    BigInteger nonce = ethGetTransactionCount.getTransactionCount();

                    Log.i("TAG", fromAddress + " , nonce: " + nonce);
                    return Flowable.just(nonce);
                })
                .doOnError(throwable -> {
                    throwable.printStackTrace();

                });
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
                    LogUtils.e(web3jBean.platform + " gasPrice: " + gasPrice + ", " + ethGasPrice.getResult() + " , rpcUrl: " + web3jBean.rpcUrl);

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

    public static void getErc20Balance(Web3jBean web3jBean, String fromAddress, String erc20Address) {
        if (StringUtils.isEmpty(fromAddress)) {
            return;
        }
        Disposable subscribe = getTokenSymbolFlowable(web3jBean, erc20Address)
                .subscribe(symbol -> {
                    Disposable subscribe1 = getErc20BalanceFlowable(web3jBean, fromAddress, erc20Address, symbol)
                            .subscribe(balance -> {

                            }, Throwable::printStackTrace);
                }, Throwable::printStackTrace);

    }

    public static Flowable<String> getTokenNameFlowable(Web3jBean web3jBean, String tokenAddress) {

        String methodName = "name";

        List<org.web3j.abi.datatypes.Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000000000", tokenAddress, data);

        Flowable<String> flowable = web3jBean.web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                .flowable()
                .compose(RxUtils.rxSchedulerNewThread())
                .flatMap((io.reactivex.functions.Function<EthCall, Publisher<String>>) ethCall -> {
                    String symbol = "";
                    if (ethCall == null || ethCall.getValue() == null || ethCall.getValue().isEmpty()) {
                        return Flowable.just(symbol);
                    }
                    List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                    if (null != results && results.size() > 0) {
                        symbol = results.get(0).getValue().toString();
                    }

                    return Flowable.just(symbol);
                })
                .doOnError(Throwable::printStackTrace);
        return flowable;
    }


    public static Flowable<BigDecimal> getAmountAFlowable(Web3jBean web3jBean, String tokenAddress) {

        String methodName = "_amountA";

        List<org.web3j.abi.datatypes.Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000000000", tokenAddress, data);

        Flowable<BigDecimal> flowable = web3jBean.web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                .flowable()
                .compose(RxUtils.rxSchedulerNewThread())
                .flatMap((io.reactivex.functions.Function<EthCall, Publisher<BigDecimal>>) ethCall -> {

                    BigDecimal balance = BigDecimal.ZERO;
                    if (ethCall == null || ethCall.getValue() == null || ethCall.getValue().isEmpty()) {
                        return Flowable.just(balance);
                    }
                    balance = erc20HexBalanceDecimalPointToDouble(ethCall.getValue(), 18);

                    StringBuilder sb = new StringBuilder();
                    sb.append(web3jBean.platform).append(", balance:").append(balance).append(" ").append(", tokenAddress:").append(tokenAddress);
                    LogUtils.i(sb.toString());

                    return Flowable.just(balance);
                })
                .doOnError(Throwable::printStackTrace);

        return flowable;
    }

    public static Flowable<BigDecimal> getAmountBFlowable(Web3jBean web3jBean, String tokenAddress) {

        String methodName = "_amountB";

        List<org.web3j.abi.datatypes.Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000000000", tokenAddress, data);


        Flowable<BigDecimal> flowable = web3jBean.web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                .flowable()
                .compose(RxUtils.rxSchedulerNewThread())
                .flatMap((io.reactivex.functions.Function<EthCall, Publisher<BigDecimal>>) ethCall -> {

                    BigDecimal balance = BigDecimal.ZERO;
                    if (ethCall == null || ethCall.getValue() == null || ethCall.getValue().isEmpty()) {
                        return Flowable.just(balance);
                    }
                    balance = erc20HexBalanceDecimalPointToDouble(ethCall.getValue(), 18);

                    StringBuilder sb = new StringBuilder();
                    sb.append(web3jBean.platform).append(", balance:").append(balance).append(" ").append(", tokenAddress:").append(tokenAddress);
                    LogUtils.i(sb.toString());

                    return Flowable.just(balance);
                })
                .doOnError(Throwable::printStackTrace);
        return flowable;
    }

    public static Flowable<String> getFlagFlowable(Web3jBean web3jBean, String tokenAddress) {

        String methodName = "flag";

        List<org.web3j.abi.datatypes.Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Bool> typeReference = new TypeReference<Bool>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000000000", tokenAddress, data);

        Flowable<String> flowable = web3jBean.web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                .flowable()
                .compose(RxUtils.rxSchedulerNewThread())
                .flatMap((io.reactivex.functions.Function<EthCall, Publisher<String>>) ethCall -> {
                    String symbol = "";
                    if (ethCall == null || ethCall.getValue() == null || ethCall.getValue().isEmpty()) {
                        return Flowable.just(symbol);
                    }
                    List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                    if (null != results && results.size() > 0) {
                        symbol = results.get(0).getValue().toString();
                    }

                    return Flowable.just(symbol);
                })
                .doOnError(Throwable::printStackTrace);
        return flowable;
    }


    public static Flowable<String> getTokenSymbolFlowable(Web3jBean web3jBean, String tokenAddress) {
        String tokenSymbol = DataLitePal.getInstance().getTokenSymbol(tokenAddress);
        if (!TextUtils.isEmpty(tokenSymbol)) {
            return Flowable.just(tokenSymbol);
        }

        String methodName = "symbol";
        List<org.web3j.abi.datatypes.Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000000000", tokenAddress, data);

        Flowable<String> flowable = web3jBean.web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                .flowable()
                .compose(RxUtils.rxSchedulerNewThread())
                .flatMap((io.reactivex.functions.Function<EthCall, Publisher<String>>) ethCall -> {
                    String symbol = "";
                    if (ethCall == null || ethCall.getValue() == null || ethCall.getValue().isEmpty()) {
                        return Flowable.just(symbol);
                    }
                    List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                    if (null != results && results.size() > 0) {
                        symbol = results.get(0).getValue().toString();
                    }

                    DataLitePal.getInstance().setTokenSymbol(tokenAddress, symbol);

                    return Flowable.just(symbol);
                })
                .doOnError(Throwable::printStackTrace);
        return flowable;
    }


    private static Flowable<BigDecimal> getErc20BalanceFlowable(Web3jBean web3jBean, String fromAddress, String tokenAddress, String symbol) {
        if (StringUtils.isEmpty(fromAddress) || StringUtils.isEmpty(tokenAddress)) {
            LogUtils.e("getErc20BalanceFlowable empty, " + fromAddress + ", " + tokenAddress);
            return Flowable.just(BigDecimal.ZERO);
        }
        Flowable<BigDecimal> flowable = getTokenDecimalFlowable(web3jBean, tokenAddress)
                .flatMap((io.reactivex.functions.Function<Integer, Publisher<BigDecimal>>) decimals -> {

                    String encode = FunctionEncoderUtils.getBalanceOfEncoder(fromAddress);

                    Transaction ethCallTransaction = Transaction.createEthCallTransaction(fromAddress, tokenAddress, encode);

                    Flowable<BigDecimal> flowable1 = web3jBean.web3j.ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST)
                            .flowable()
                            .compose(RxUtils.rxSchedulerNewThread())
                            .flatMap((io.reactivex.functions.Function<EthCall, Publisher<BigDecimal>>) ethCall -> {
                                if (ethCall == null || ethCall.getValue() == null || ethCall.getValue().isEmpty()) {
                                    return Flowable.just(BigDecimal.ZERO);
                                }

                                BigDecimal balance = erc20HexBalanceDecimalPointToDouble(ethCall.getValue(), decimals);

                                StringBuilder sb = new StringBuilder();
                                sb.append(web3jBean.platform).append(", fromAddress:").append(fromAddress).append(", balance:").append(balance).append(" ").append(symbol).append(", tokenAddress:").append(tokenAddress);
                                LogUtils.i(sb.toString());

                                return Flowable.just(balance);
                            });
                    return flowable1;
                });
        return flowable;
    }


    public static Flowable<Integer> getTokenDecimalFlowable(Web3jBean web3jBean, String tokenAddress) {
        int localDecimals = DataLitePal.getInstance().getTokenDecimals(tokenAddress);

        if (localDecimals >= 0) {
            return Flowable.just(localDecimals);
        }

        Function function = FunctionEncoderUtils.getDecimalsEncoder();
        String data = FunctionEncoder.encode(function);

        Transaction transaction = Transaction.createEthCallTransaction("0x0000000000000000000000000000000000000000", tokenAddress, data);

        Flowable<Integer> flowable = web3jBean.web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                .flowable()
                .compose(RxUtils.rxSchedulerNewThread())
                .flatMap((io.reactivex.functions.Function<EthCall, Publisher<Integer>>) ethCall -> {
                    int decimals = 18;
                    if (ethCall == null || ethCall.getValue().isEmpty()) {
                        LogUtils.e("getTokenDecimalFlowable empty, rpc: " + web3jBean.rpcUrl);
                        return Flowable.just(decimals);
                    }

                    List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                    if (!results.isEmpty()) {
                        decimals = Integer.parseInt(results.get(0).getValue().toString());
                    }

                    return Flowable.just(decimals);
                });
        return flowable;
    }

    public static BigDecimal erc20HexBalanceDecimalPointToDouble(String numHex, int erc20DecimalPoint) {
        String decStr = hexToDecStr(numHex);
        BigDecimal balance = new BigDecimal(decStr).divide(BigDecimal.TEN.pow(erc20DecimalPoint), erc20DecimalPoint + 2, BigDecimal.ROUND_HALF_UP);
        return balance;
    }

    public static String hexToDecStr(String hexValue) {
        if (hexValue == null || hexValue.isEmpty() || hexValue.equals("0x")) {
            return BigInteger.ZERO.toString();
        }

        BigInteger bigInteger = org.web3j.utils.Numeric.toBigInt(hexValue);
        return bigInteger.toString();
    }


}
