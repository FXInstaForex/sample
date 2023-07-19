package net.corda.samples.example.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public class FXTradeContract implements Contract {
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

    }
    public interface Commands extends CommandData {
        class Create implements FXTradeContract.Commands {}
        class Submit implements FXTradeContract.Commands {}
    }
}
