package net.corda.samples.example.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;
import net.corda.samples.example.states.IOUState;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static net.corda.core.contracts.ContractsDSL.requireThat;

@InitiatedBy(ExampleFlow.Initiator.class)
public  class Acceptor extends FlowLogic<SignedTransaction> {

    private final FlowSession otherPartySession;

    public Acceptor(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                super(otherPartyFlow, progressTracker);
            }

            protected void checkTransaction(SignedTransaction stx) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
                String formattedDate = sdf.format(date);
                System.out.println(formattedDate); // 12/01/2011 4:48:16 PM

                System.out.println("formattedDate.............inside acceptor check transaction start"+formattedDate);
                requireThat(require -> {
                    ContractState output = stx.getTx().getOutputs().get(0).getData();
                    require.using("This must be an IOU transaction.", output instanceof IOUState);
                    IOUState iou = (IOUState) output;
                    require.using("I won't accept IOUs with a value over 100.", iou.getValue() <= 100);
                    return null;
                });
            }
        }
        final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
        final SecureHash txId = subFlow(signTxFlow).getId();
// Extract the required bytes from the SecureHash
        byte[] hashBytes = txId.getBytes();

        // Convert the bytes to a ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(hashBytes);

        // Extract the most significant bits (8 bytes) to construct the UUID
        long mostSigBits = buffer.getLong();

        // Extract the least significant bits (8 bytes) to construct the UUID
        long leastSigBits = buffer.getLong();

        // Create the UUID instance
        UUID uuidTxID = new UUID(mostSigBits, leastSigBits);
         //subFlow(new SubmitTradeAgreementFlow(uuidTxID));

        return subFlow(new ReceiveFinalityFlow(otherPartySession, txId));
    }
}