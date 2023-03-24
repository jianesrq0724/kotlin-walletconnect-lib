package io.walletconnect.example.bean;

import java.util.List;

public class RpcMethodBean {
    String method;
    List params;

    public RpcMethodBean(String method, List params) {
        this.method = method;
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List getParams() {
        return params;
    }

    public void setParams(List params) {
        this.params = params;
    }
}
