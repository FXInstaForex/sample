package net.corda.samples.example.flows;
import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.*;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.AppServiceHub;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.transactions.WireTransaction;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.identity.CordaX500Name;
import net.corda.samples.example.contracts.IOUContract;
import net.corda.samples.example.states.IOUState;

import java.security.SignatureException;
import java.util.Collections;
import java.util.stream.Collectors;

public class TradeAgreementFlow extends FlowLogic<Void>{
    @Override
    public Void call() throws FlowException {
        return null;
    }
}
// {
//    private final SignedTransaction signedTradeAgreement;
//    private ProgressTracker progressTracker= new ProgressTracker();
//    public TradeAgreementFlow(SignedTransaction signedTradeAgreement) {
//        this.signedTradeAgreement = signedTradeAgreement;
//    }
//
//    @Override
//    public ProgressTracker getProgressTracker() {
//        return progressTracker;
//    }
//
//    @Suspendable
//    @Override
//    public Void call() throws FlowException {
//        // Step 1: Validate the signed trade agreement
//        try {
//            verifySignedTransaction(signedTradeAgreement);
//        } catch (TransactionVerificationException | SignatureException e) {
//            throw new FlowException("Invalid trade agreement", e);
//        }
//
//        // Step 2: Notarize the trade agreement
//        try {
//            notarizeTransaction(signedTradeAgreement);
//        } catch (FlowException e) {
//            throw new FlowException("Failed to notarize the trade agreement", e);
//        }
//
//        return null;
//    }
//
//    private void verifySignedTransaction(SignedTransaction signedTransaction) throws TransactionVerificationException, AttachmentResolutionException, TransactionResolutionException, SignatureException {
//        LedgerTransaction ledgerTx = signedTransaction.toLedgerTransaction(getServiceHub(), false);
//        ContractState outputState = ledgerTx.getOutputStates().get(0);
//
//        // Add custom validation rules specific to your trade agreement contract
//        // For example, you can check if the parties involved are valid, the transaction amounts are correct, etc.
//
//        // Call the verify method on the contract state to perform contract-specific validation
//        //outputState.verify(getServiceHub());
//
//
//        // Call the verify method on the signed transaction to perform general validation
//        signedTransaction.verify(getServiceHub());
//    }
//
//    private void notarizeTransaction(SignedTransaction signedTransaction) throws FlowException {
//        AppServiceHub serviceHub = (AppServiceHub) getServiceHub();
//
//        // Get the notary
//        CordaX500Name notaryName = CordaX500Name.parse("Notary's name");
//        Party notary = serviceHub.getNetworkMapCache().getNotary(notaryName);
//
//        // Create a transaction builder and add the signed transaction as an input
//        TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
//                //.addInputState(signedTransaction.getTx())
//                .addCommand(new Command<>(new IOUContract.Commands.Notarize(),
//                        signedTransaction.getTx().getOutputStates().get(0).getParticipants()));
//
//        // Verify and sign the transaction builder
//        transactionBuilder.verify(serviceHub);
//        SignedTransaction notarizedTransaction = serviceHub.signInitialTransaction(transactionBuilder);
//
//        // Finalize the transaction and notarize it
//        subFlow(new FinalityFlow(notarizedTransaction, Collections.emptyList()));
//    }
//}
