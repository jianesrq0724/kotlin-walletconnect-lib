package io.walletconnect.example.bean;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class Web3jBean {
    public String platform;
    public Web3j web3j;
    public String rpcUrl;
    public int chainId;

    public String mdexAddress;
    public String wHTAddress;
    public String USDT;
    public String USDC = "";
    public String htLpAddress = "";


    public Web3jBean(String platform, String rpcUrl, int chainId, String mdexAddress, String wHTAddress, String USDT, String USDC, String htLpAddress) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
        this.platform = platform;
        this.rpcUrl = rpcUrl;
        this.chainId = chainId;
        this.mdexAddress = mdexAddress;
        this.wHTAddress = wHTAddress;
        this.USDT = USDT;
        this.USDC = USDC;
        this.htLpAddress = htLpAddress;
    }


    public boolean isTokenWHT(String toAddress) {
        return toAddress.equals(wHTAddress);
    }

    public boolean isTokenUSDT(String toAddress) {
        return toAddress.equals(USDT);
    }

    public boolean isTokenHUSD(String toAddress) {
        return toAddress.equals(USDC);
    }





}
