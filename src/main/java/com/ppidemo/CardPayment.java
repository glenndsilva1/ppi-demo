package com.ppidemo;

import java.io.IOException;
import java.util.List;

/**
 * Prepaid card payment at a shop: the wallet pays, the card scheme is
 * owed the money until it settles with the merchant.
 */
public final class CardPayment implements Entry {

    /**
     * Wallet.
     */
    private final Wallet wallet;

    /**
     * Settlement account of the card scheme.
     */
    private final Account scheme;

    /**
     * Shop name.
     */
    private final String merchant;

    /**
     * Amount, in paise.
     */
    private final long amount;

    /**
     * Ctor.
     * @param wlt Wallet
     * @param settlement Settlement account of the card scheme
     * @param shop Shop name
     * @param paise Amount, in paise
     */
    public CardPayment(final Wallet wlt, final Account settlement, final String shop,
        final long paise) {
        this.wallet = wlt;
        this.scheme = settlement;
        this.merchant = shop;
        this.amount = paise;
    }

    @Override
    public Operation operation() {
        return Operation.CARD;
    }

    @Override
    public String memo() {
        return String.format("At %s", this.merchant.trim());
    }

    @Override
    public List<Line> lines() throws IOException {
        return List.of(
            new Debit(this.wallet.account(), this.amount),
            new Credit(this.scheme, this.amount)
        );
    }

    @Override
    public List<Txn> reversed() {
        return List.of();
    }
}
