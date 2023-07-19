package net.corda.samples.example.flows;

import net.corda.samples.example.contracts.IOUContract;
import net.corda.samples.example.states.*;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.contracts.StateAndRef;

import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.corda.core.transactions.SignedTransaction;

@InitiatingFlow
@StartableByRPC
public class SubmitTradeAgreementFlow extends FlowLogic<UUID> {

    private ProgressTracker progressTracker= new ProgressTracker();

   /* public SubmitTradeAgreementFlow(UUID tradeAgreementId) {
        this.tradeAgreementId = tradeAgreementId;
    }*/
private final String secureHashStr;

    public SubmitTradeAgreementFlow(String secureHashStr) {
        this.secureHashStr = secureHashStr;
    }

    @Nullable
    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    //private final UUID tradeAgreementId;

    @Override
    public UUID call() throws FlowException {
        byte[] secureHashBytes = secureHashStr.getBytes(StandardCharsets.UTF_8);

        UUID tradeAgreementId = UUID.nameUUIDFromBytes(secureHashBytes);
        System.out.println("UUID: " + tradeAgreementId.toString());

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);
        System.out.println(formattedDate); // 12/01/2011 4:48:16 PM

        System.out.println("formattedDate.............inside SubmitTradeAgreementFlow starts"+formattedDate);
        // Retrieve the trade agreement state from the vault
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria()
                .withUuid(Collections.singletonList(tradeAgreementId));
        StateAndRef<IOUState> tradeAgreementStateRef = getServiceHub().getVaultService()
                .queryBy(IOUState.class,criteria).getStates().get(0);


        final Party notary= getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
        // Build the transaction
        IOUState tradeAgreementState = tradeAgreementStateRef.getState().getData();
        System.out.println("tradeAgreementState.............inside SubmitTradeAgreementFlow "+tradeAgreementState.toString());
        TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addInputState(tradeAgreementStateRef)
                .addOutputState(tradeAgreementState, IOUContract.ID)
                .addCommand(new IOUContract.Commands.Submit(), tradeAgreementState.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList()));

        // Verify and sign the transaction
        transactionBuilder.verify(getServiceHub());
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        // Collect signatures from required parties
        List<FlowSession> sessions = tradeAgreementState.getParticipants().stream()
                .map(this::initiateFlow)
                .collect(Collectors.toList());
        SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, sessions));


        // Finalize the transaction
        SignedTransaction finalSignedTransaction = subFlow(new FinalityFlow(fullySignedTransaction, sessions));

        // Return the trade agreement ID

        return null;
    }
}
