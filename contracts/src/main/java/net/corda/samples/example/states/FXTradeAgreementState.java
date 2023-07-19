package net.corda.samples.example.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.example.contracts.FXTradeContract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@BelongsToContract(FXTradeContract.class)
public class FXTradeAgreementState implements LinearState {

    private final String tradeId;
    private final String sellCurrency;
    private final BigDecimal sellAmount;
    private final String buyCurrency;
    private final BigDecimal buyAmount;
    private final LocalDate settlementDate;
    private final List<String> regulatoryRequirements;
   private final Party bankA;
   private final Party bankB;
    private final UniqueIdentifier linearId;
    public String getTradeId() {
        return tradeId;
    }

    public String getSellCurrency() {
        return sellCurrency;
    }

    public BigDecimal getSellAmount() {
        return sellAmount;
    }

    public String getBuyCurrency() {
        return buyCurrency;
    }

    public BigDecimal getBuyAmount() {
        return buyAmount;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public List<String> getRegulatoryRequirements() {
        return regulatoryRequirements;
    }

    public Party getBankA() {
        return bankA;
    }

    public Party getBankB() {
        return bankB;
    }

    public FXTradeAgreementState(String tradeId, String sellCurrency, BigDecimal sellAmount, String buyCurrency, BigDecimal buyAmount, LocalDate settlementDate, List<String> regulatoryRequirements, Party bankA, Party bankB, UniqueIdentifier linearId) {
        this.tradeId = tradeId;
        this.sellCurrency = sellCurrency;
        this.sellAmount = sellAmount;
        this.buyCurrency = buyCurrency;
        this.buyAmount = buyAmount;
        this.settlementDate = settlementDate;
        this.regulatoryRequirements = regulatoryRequirements;
        this.bankA = bankA;
        this.bankB = bankB;
        this.linearId = linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(bankA,bankB);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }
}
