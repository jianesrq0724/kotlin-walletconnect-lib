package io.walletconnect.example.util.eth;

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



}
