package io.walletconnect.example.util.eth;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class FunctionEncoderUtils {

    public static String getTransferEncoder(String toAddress, String numValue, int decimalPoint) {

        BigInteger value = new BigDecimal(numValue).multiply(BigDecimal.TEN.pow(decimalPoint)).toBigInteger();

        Function function = new Function(
                "transfer",
                Arrays.asList(new Address(toAddress), new Uint256(value)),
                new ArrayList<>());

        return FunctionEncoder.encode(function);

    }

    public static String getInitAddLiquidityEncoder() {

        Function function = new Function(
                "initAddLiquidity",
                new ArrayList<>(),
                new ArrayList<>());

        return FunctionEncoder.encode(function);

    }

    public static String getAddLiquidityEncoder(String token1Value, String token2Value) {

        int decimalPoint1 = 18;
        int decimalPoint2 = 18;

        BigInteger token1ValueBigInteger = new BigDecimal(token1Value).multiply(BigDecimal.TEN.pow(decimalPoint1)).toBigInteger();
        BigInteger token2ValueBigInteger = new BigDecimal(token2Value).multiply(BigDecimal.TEN.pow(decimalPoint2)).toBigInteger();

        Function function = new Function(
                "addLiquidity",
                Arrays.asList(new Uint256(token1ValueBigInteger), new Uint256(token2ValueBigInteger)),
                new ArrayList<>());

        return FunctionEncoder.encode(function);


    }

    public static String getRemoveLiquidityEncoder(String liquidity) {

        int decimalPoint = 18;
        BigInteger token1ValueBigInteger = new BigDecimal(liquidity).multiply(BigDecimal.TEN.pow(decimalPoint)).toBigInteger();

        Function function = new Function(
                "removeLiquidity",
                Arrays.asList(new Uint256(token1ValueBigInteger)),
                new ArrayList<>());

        return FunctionEncoder.encode(function);

    }

    public static String getPlaceOrderBuyEncoder(String numValue) {
        return getPlaceOrderEncoder(numValue, "1");
    }

    public static String getPlaceOrderSellEncoder(String numValue) {
        return getPlaceOrderEncoder(numValue, "2");
    }

    public static String getPlaceOrderEncoder(String numValue, String side) {
        int decimalPoint = 18;

        BigInteger token1ValueBigInteger = new BigDecimal(numValue).multiply(BigDecimal.TEN.pow(decimalPoint)).toBigInteger();

        Function function = new Function(
                "placeOrder",
                Arrays.asList(new Uint256(token1ValueBigInteger), new Uint256(new BigDecimal(side).toBigInteger())),
                new ArrayList<>());

        return FunctionEncoder.encode(function);

    }


    public static String getBalanceOfEncoder(String fromAddress) {

        Function function = new Function("balanceOf",
                Collections.singletonList(new Address(fromAddress)),
                new ArrayList<>());

        return FunctionEncoder.encode(function);

    }

    public static Function getDecimalsEncoder() {

        Function function = new Function("decimals",
                new ArrayList<>(),
                new ArrayList<>());

        return function;

    }


}
