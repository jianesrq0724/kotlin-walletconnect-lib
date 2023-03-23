package io.walletconnect.example.database;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import io.walletconnect.example.utils.EthUtils;
import io.walletconnect.example.utils.MapUtils;
import io.walletconnect.example.utils.StringUtils;

public class DataLitePal {

    public Map<String, BigDecimal> mMinGasPriceHashMap = new HashMap<>();
    public Map<String, String> symbolHashMap = new HashMap<>();

    public Map<String, Integer> decimalsHashMap = new HashMap<>();

    private static DataLitePal instance = new DataLitePal();

    public static DataLitePal getInstance() {
        return instance;
    }


    public void setMinGasPrice(String platform, BigDecimal gasPrice) {
        String key = platform;
        mMinGasPriceHashMap.put(key, gasPrice);
    }


    public BigDecimal getMinGasPrice(String platform) {
        String key = platform;
        BigDecimal minGasPrice = MapUtils.getMapValueBigDecimal(mMinGasPriceHashMap, key, BigDecimal.valueOf(2.25));
        // 2.25  扩大1.05倍  2.36
        minGasPrice = minGasPrice.multiply(BigDecimal.valueOf(1.05));

        return EthUtils.gasPriceDecimalPoint(platform, minGasPrice);

    }

    public void setTokenSymbol(String tokenAddress, String symbol) {
        if (StringUtils.isEmpty(tokenAddress) || StringUtils.isEmpty(symbol)) {
            return;
        }

        String localTokenSymbol = getTokenSymbol(tokenAddress);
        if (localTokenSymbol.equals(symbol)) {
            return;
        }

        // 设置本地的值
        String key = tokenAddress;
        symbolHashMap.put(key, symbol);

    }


    public String getTokenSymbol(String tokenAddress) {
        long lastTime = System.currentTimeMillis();

        if (StringUtils.isEmpty(tokenAddress)) {
            return "";
        }

        String mapValueString = MapUtils.getMapValueString(symbolHashMap, tokenAddress);

        return mapValueString;
    }

    public void setTokenDecimals(String tokenAddress, int decimals) {
        if (StringUtils.isEmpty(tokenAddress)) {
            return;
        }

        int localTokenDecimals = getTokenDecimals(tokenAddress);
        if (localTokenDecimals == decimals) {
            return;
        }

        // 设置本地的值
        String key = tokenAddress;
        decimalsHashMap.put(key, decimals);

    }

    public int getTokenDecimals(String tokenAddress) {
        int mapValueInt = MapUtils.getMapValueInt(decimalsHashMap, tokenAddress, -1);
        return mapValueInt;
    }

}
