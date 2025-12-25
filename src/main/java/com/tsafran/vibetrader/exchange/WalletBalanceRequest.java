package com.tsafran.vibetrader.exchange;

public record WalletBalanceRequest(
        ExchangeAccountType accountType
) {
    public WalletBalanceRequest {
        if (accountType == null) {
            accountType = ExchangeAccountType.UNIFIED;
        }
    }
}
