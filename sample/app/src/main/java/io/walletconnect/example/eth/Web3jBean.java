package io.walletconnect.example.eth;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.ArrayList;
import java.util.List;

public class Web3jBean {
    public String platform;
    public Web3j web3j;
    public String rpcUrl;
    public int chainId;

    public String mdexAddress;
    public String wHTAddress;
    public String hecoUSDT;
    public String hecoHUSD = "";
    public String htLpAddress = "";


    public Web3jBean(String platform, String rpcUrl, int chainId, String mdexAddress, String wHTAddress, String hecoUSDT, String hecoHUSD, String htLpAddress) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
        this.platform = platform;
        this.rpcUrl = rpcUrl;
        this.chainId = chainId;
        this.mdexAddress = mdexAddress;
        this.wHTAddress = wHTAddress;
        this.hecoUSDT = hecoUSDT;
        this.hecoHUSD = hecoHUSD;
        this.htLpAddress = htLpAddress;
    }


    public boolean isTokenWHT(String toAddress) {
        return toAddress.equals(wHTAddress);
    }

    public boolean isTokenUSDT(String toAddress) {
        return toAddress.equals(hecoUSDT);
    }

    public boolean isTokenHUSD(String toAddress) {
        return toAddress.equals(hecoHUSD);
    }





}
