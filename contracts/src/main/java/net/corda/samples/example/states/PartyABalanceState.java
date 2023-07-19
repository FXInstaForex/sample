package net.corda.samples.example.states;

import net.corda.core.contracts.Amount;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.example.contracts.BalanceContractPartyA;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;

@BelongsToContract(BalanceContractPartyA.class)
public class PartyABalanceState implements ContractState {
    private final double  amount;
    private final Party owner;

    public PartyABalanceState(double amount, Party owner) {
        this.amount = amount;
        this.owner = owner;
    }

    public double getAmount() {
        return amount;
    }

    public Party getOwner() {
        return owner;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner);
    }
}
