package io.walletconnect.example.database;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import io.walletconnect.example.utils.EthUtils;
import io.walletconnect.example.utils.MapUtils;

public class DataLitePal {

    public Map<String, BigDecimal> mMinGasPriceHashMap = new HashMap<>();

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
}
