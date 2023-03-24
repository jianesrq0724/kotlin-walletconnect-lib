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

    public static RpcMethodBean getTokenNameBean() {

        String method = "eth_call";

        String methodName = "name";

        List<org.web3j.abi.datatypes.Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from", "0x0000000000000000000000000000000000000000");
            jsonObject.put("tokenAddress", "0x14D5Ca5ac738f6d7D0012B5768A948017Eb8Ef41");
            jsonObject.put("data", data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return new RpcMethodBean(method, Collections.<String>emptyList());

    }

}
