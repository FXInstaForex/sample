package net.corda.samples.example.contracts;
import net.corda.samples.example.states.PartyABalanceState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;

import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public class BalanceContractPartyA implements Contract {
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
//        final CommandData command = tx.getCommands().get(0).getValue();
//
//        if (command instanceof Commands.UpdateBalance) {
//            final PartyABalanceState balanceOutput = tx.outputsOfType(PartyABalanceState.class).get(0);
//            final double balanceAmount = balanceOutput.getAmount();
//
//           // RequirementsKt.require(balanceAmount.getQuantity() > 0, "The balance amount must be positive");
//           // RequirementsKt.require(balanceOutput.getOwner() instanceof Party, "The balance owner must be a party");
//        }
    }



    public interface Commands extends CommandData {
        class UpdateBalance implements Commands {
        }
    }
}
