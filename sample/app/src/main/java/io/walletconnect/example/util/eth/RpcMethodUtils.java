package io.walletconnect.example.util.eth;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.walletconnect.example.bean.EthCallBean;
import io.walletconnect.example.bean.RpcMethodBean;

public class RpcMethodUtils {

    public static RpcMethodBean getNonceBean(String from) {

        String method = "eth_getTransactionCount";

        List<String> params = new ArrayList<>();
        params.add(from);
        // earliest、latest、pending
        params.add("latest");

        return new RpcMethodBean(method, params);

    }

    public static RpcMethodBean getGasPriceBean() {

        String method = "eth_gasPrice";

        return new RpcMethodBean(method, Collections.<String>emptyList());

    }

    // eth_estimateGas

    public static RpcMethodBean getTokenAmountABean(String tokenAddress) {

        String method = "eth_call";

        String methodName = "_amountA";

//        List<org.web3j.abi.datatypes.Type> inputParameters = new ArrayList<>();
//        List<TypeReference<?>> outputParameters = new ArrayList<>();
//        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {};
//        outputParameters.add(typeReference);
//        Function function = new Function(methodName, inputParameters, outputParameters);

        Function function = new Function(methodName, new ArrayList<>(), new ArrayList<>());
        String data = FunctionEncoder.encode(function);

        EthCallBean ethCallBean = new EthCallBean();
        ethCallBean.from = "0x0000000000000000000000000000000000000000";
        ethCallBean.to = tokenAddress;
        ethCallBean.data = data;

        List<EthCallBean> params = new ArrayList<>();
        params.add(ethCallBean);

        return new RpcMethodBean(method, params);

    }

}
