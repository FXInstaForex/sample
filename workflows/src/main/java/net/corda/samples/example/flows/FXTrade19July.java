package net.corda.samples.example.flows;

import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;

import java.security.PublicKey;
import java.util.*;

import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.IdentityService;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.example.contracts.BalanceContractPartyA;
import net.corda.samples.example.contracts.IOUContract;
import net.corda.samples.example.states.IOUState;
import net.corda.samples.example.states.PartyABalanceState;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;

import static java.util.Collections.emptyList;

@InitiatingFlow
@StartableByRPC
public class FXTrade19July extends FlowLogic<Void>  {

    private final  SignedTransaction signedTransaction;

    public FXTrade19July(SignedTransaction signedTransaction) {
        this.signedTransaction = signedTransaction;
    }

    @Override
    public Void call() throws FlowException {
        boolean flag = false;

        List<TransactionState<ContractState>> outputs = signedTransaction.getTx().getOutputs();
        int amount=0;
// Iterate over the outputs and extract the participants (nodes)
        List<AbstractParty> participants = null;
        for (TransactionState<ContractState> output : outputs) {
            participants = output.getData().getParticipants();

            ContractState state=output.getData();
            IOUState iouState = (IOUState) state;
            amount=iouState.getValue();
            System.out.println("output state..............//"+iouState.getValue());


        }

        System.out.println("participants" + participants.toString());
        flag=validateNodesAuthenticity(participants);
        //flag=validateNodesAuthenticity(nodes);
        System.out.println("flag after validateNodesAuthenticity called" + flag);

        if(flag){
            //sign transaction
            signTransaction(participants,amount);

        }else{
            //throw exception
        }



        double balanceAmt =getAmountfromVault();
        if (balanceAmt<=amount){
            System.out.println("Balance amount is less than the request amount ,Hence declining the transaction");
        }
        else
        {   balanceAmt=balanceAmt-amount;
            System.out.println("Updated Balance amount"+balanceAmt);
            // subFlow(new PartyAUpdateBalanceFlow(balanceAmt,getOurIdentity()));
            System.out.println("Balance amount is sufficient  for requested amount to lend ,Hence proceeding the transaction");
            updateLedger(balanceAmt,getOurIdentity());
        }


        return null;
    }

    public void signTransaction( List<AbstractParty> participants, int amount) throws TransactionVerificationException, AttachmentResolutionException, TransactionResolutionException {
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
        Party otherParty = null;
        // Stage 1.
        // Generate an unsigned transaction.
        Party me = getOurIdentity();
        for (AbstractParty node : participants) {
            if(!(participants.equals(me))){
                otherParty= (Party) node;
            }
        }
        System.out.println("Other party........"+otherParty);
        IOUState iouState = new IOUState(amount, me, otherParty, new UniqueIdentifier());
        final Command<IOUContract.Commands.Create> txCommand = new Command<>(
                new IOUContract.Commands.Create(),
                Arrays.asList(iouState.getLender().getOwningKey(), iouState.getBorrower().getOwningKey()));
        final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(iouState, IOUContract.ID)
                .addCommand(txCommand);

        // Stage 2.
        // Verify that the transaction is valid.
        txBuilder.verify(getServiceHub());

        // Stage 3.
        // Sign the transaction.
        final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

        System.out.println("partSignedTx..........."+partSignedTx);
    }
    public double getAmountfromVault(){
        double balanceAmt=0;
        System.out.println("------getAmountfromVault--------");


        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        //owner will be party initiating tansaction owner  is
        //.withExternalIds(Collections.singletonList(UUID.nameUUIDFromBytes(getOurIdentity().getOwningKey().getEncoded())));
        // System.out.println("----getServiceHub()------"+getServiceHub());
        //System.out.println("----getServiceHub().getVaultService() ContractState------"+getServiceHub().getVaultService().queryBy(ContractState.class));
        Vault.Page<PartyABalanceState> results =  getServiceHub().getVaultService().queryBy(PartyABalanceState.class);
        //System.out.println("results....................."+results);
        List<StateAndRef<PartyABalanceState>> states = results.getStates();
        //System.out.println("states....................."+states);
        //       IOUState partyAState=(IOUState)states;
        for (StateAndRef<PartyABalanceState> state : states) {
            PartyABalanceState iouState = state.getState().getData();
            System.out.println("Balance Amount"+iouState.getAmount());
            balanceAmt=iouState.getAmount();

        }

        return balanceAmt;

    }
    public boolean validateNodesAuthenticity(List<AbstractParty> nodes) {
        boolean validateNodeFlag=false;
        for (AbstractParty node : nodes) {
            if (isWellKnownNodeIdentity(node.nameOrNull())) {
                System.out.println("The node's identity is known and trusted"+node.nameOrNull());
                verifyNodeIntegrity(node.nameOrNull(), node.getOwningKey());
                validateNodeFlag=true;
            }

            else{
                System.out.println("The node's identity is not known or not trusted");
                throw new IllegalArgumentException("The node's identity is not known or not trusted");
            }
        }


        return validateNodeFlag;
    }

    private  boolean isWellKnownNodeIdentity( CordaX500Name identity) {
        for (NodeInfo nodeInfo : getServiceHub().getNetworkMapCache().getAllNodes()) {
            if (nodeInfo.getLegalIdentities().get(0).getName().equals(identity)) {
                return true;
            }
        }
        return false;
    }

    public boolean verifyNodeIntegrity(CordaX500Name nodeX500Name, PublicKey expectedPublicKey) {
        IdentityService identityService = getServiceHub().getIdentityService();
        boolean verifiedFlag=false;
        // Step 1: Retrieve the Party and Certificate for the node
        Party nodeParty = identityService.wellKnownPartyFromX500Name(nodeX500Name);
        if (nodeParty == null) {
            System.out.println("Node not found: " + nodeX500Name);
            verifiedFlag=false;
        }

        PartyAndCertificate nodeCertificate = identityService.certificateFromKey(nodeParty.getOwningKey());
        if (nodeCertificate == null) {
            System.out.println("Certificate not found for node: " + nodeX500Name);
            verifiedFlag=false;
        }else{
            verifiedFlag=true;
            System.out.println("Certificate  found for node: " + nodeX500Name+"......"+nodeCertificate.toString());
        }

        // Step 3: Verify the node's public key
        PublicKey actualPublicKey = nodeCertificate.getOwningKey();
        if (!actualPublicKey.equals(expectedPublicKey)) {
            System.out.println("Public key mismatch for node: " + nodeX500Name);

            verifiedFlag=false;

        }else{
            verifiedFlag=true;
            System.out.println("Public key matched for node: " + nodeX500Name);
        }
        // All checks passed, node integrity verified
        System.out.println("verifiedFlag" + verifiedFlag);
        if(verifiedFlag){
            verifiedFlag   =verifyKYCtransaction(nodeX500Name);
        }else{
            verifiedFlag=false;
            throw new IllegalArgumentException("Node integrity verification failed " + nodeX500Name);
        }
        System.out.println("verifiedFlag after KYC" + verifiedFlag);
        return  verifiedFlag;
    }
    public boolean verifyKYCtransaction(CordaX500Name nodeX500Name )  {
        System.out.println("I am inside verifyKYCtransaction "+ nodeX500Name.toString());
        boolean verifiedKYC=false;

        String[] tokens= (nodeX500Name.toString()).split(",");
        System.out.println("Value of nodesnames string "+tokens[0]+tokens[1]+tokens[2]);
        String[] conutrytoken=tokens[2].split("=");
        List<String> ctrylist = new ArrayList<String> ();
        ctrylist.add("IR");
        ctrylist.add("SA");


        if (ctrylist.contains(conutrytoken[1])) {
            verifiedKYC=false;

            System.out.println("ngCountry is blacklisted for trade hence rejecti for Trade"+conutrytoken[1]+"listof countries"+ctrylist);
            throw new IllegalArgumentException("BlackListed country"+conutrytoken[1]);
        }

        else
        {    System.out.println("Country is valid for trade "+conutrytoken[1]);
            verifiedKYC=true;


        }


        return verifiedKYC;
    }

    public void updateLedger(double amount,Party party)
    {
        System.out.println("Inside updateLedger" );
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
//        final QueryCriteria.LinearStateQueryCriteria linearStateQueryCriteria =
//                new QueryCriteria.LinearStateQueryCriteria()
//                        .withUuid(Collections.singletonList(linearId.getId()));
        QueryCriteria.VaultQueryCriteria queryCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        final StateAndRef<PartyABalanceState> iouStateAndRef = getServiceHub().getVaultService()
                .queryBy(PartyABalanceState.class, queryCriteria)
                .getStates().get(0);

        final PartyABalanceState oldIOUState = iouStateAndRef.getState().getData();
        System.out.println("oldIOUState" +oldIOUState);
        final PartyABalanceState newIOUState = new PartyABalanceState(amount, getOurIdentity());
        System.out.println("newIOUState" +newIOUState);

        final TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addInputState(iouStateAndRef)
                .addOutputState(newIOUState)
                .addCommand(new BalanceContractPartyA.Commands.UpdateBalance(), getOurIdentity().getOwningKey());

        // progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        // transactionBuilder.verify(getServiceHub());

        //progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        //progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
        try {
            subFlow(new FinalityFlow(signedTransaction, emptyList()));
        } catch (FlowException e) {
            throw new RuntimeException(e);
        }


    }


}
