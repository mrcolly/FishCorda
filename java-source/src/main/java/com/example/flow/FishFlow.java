package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.contract.FishContract;
import com.example.schema.RuleSchema;
import com.example.state.FishState;
import com.example.state.RuleState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.resources.Haversine;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.UntrustworthyData;

import java.util.List;


import static com.example.contract.FishContract.CONTRACT_ID;
import static net.corda.core.node.services.vault.QueryCriteriaUtils.MAX_PAGE_SIZE;



public class FishFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final Party ruler;
        private final String vessel;
        private final double weight;
        private final double length;
        private final String catchMethod;
        private final String specie;
        private final String description;
        private final double latitude;
        private final double longitude;
        private final long dateFishCaught;
        private boolean isValid;

        private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new IOU.");
        private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
        private final ProgressTracker.Step GATHERING_SIGS = new ProgressTracker.Step("Gathering the counterparty's signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
        // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
        // function.
        private final ProgressTracker progressTracker = new ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        );

        public Initiator(Party ruler, String vessel, double weight, double length, String catchMethod, String specie, String description, double latitude, double longitude, long dateFishCaught) {
            this.ruler = ruler;
            this.vessel = vessel;
            this.weight = weight;
            this.length = length;
            this.catchMethod = catchMethod;
            this.specie = specie;
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
            this.dateFishCaught = dateFishCaught;
            this.isValid = true;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Obtain a reference to the notary we want to use.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            getLogger().info("GENERATING_TRANSACTION");
            // Generate an unsigned transaction.
            Party me = getOurIdentity();
            FlowSession rulerSession = initiateFlow(ruler);
            FishState fish = new FishState(me,
                    ruler,
                    new UniqueIdentifier(),
                    vessel,
                    weight,
                    length,
                    catchMethod,
                    specie,
                    description,
                    latitude,
                    longitude,
                    dateFishCaught,
                    isValid);

            getLogger().info("sending Fish to counterparty");
            rulerSession.send(fish);

            UntrustworthyData<Boolean> packet1 = rulerSession.receive(Boolean.class);
            Boolean valid = packet1.unwrap(data -> {
                // Perform checking on the object received.
                // T O D O: Check the received object.
                // Return the object.
                return data;
            });

            fish.setValid(valid);

            final Command<FishContract.Commands.Create> txCommand = new Command<>(
                    new FishContract.Commands.Create(),
                    ImmutableList.of(fish.getOwner().getOwningKey(), fish.getRuler().getOwningKey()));
            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(fish, CONTRACT_ID)
                    .addCommand(txCommand);

            // Stage 2.
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            getLogger().info("VERIFYING_TRANSACTION");
            // Verify that the transaction is valid.
            txBuilder.verify(getServiceHub());

            // Stage 3.
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            getLogger().info("SIGNING_TRANSACTION");
            // Sign the transaction.
            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Stage 4.
            progressTracker.setCurrentStep(GATHERING_SIGS);
            getLogger().info("GATHERING_SIGS");
            // Send the state to the counterparty, and receive it back with their signature.

            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, ImmutableSet.of(rulerSession), CollectSignaturesFlow.Companion.tracker()));

            // Stage 5.
            progressTracker.setCurrentStep(FINALISING_TRANSACTION);
            // Notarise and record the transaction in both parties' vaults.
            return subFlow(new FinalityFlow(fullySignedTx));
        }
    }

    @InitiatedBy(FishFlow.Initiator.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {

        private final FlowSession counterPartySession;

        public Acceptor(FlowSession counterPartySession) {
            this.counterPartySession = counterPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            FishState fish = counterPartySession.receive(FishState.class).unwrap(data -> data);
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            FieldInfo specie = null;
            FieldInfo catchMethod = null;
            FieldInfo fromDate = null;
            FieldInfo toDate = null;

            try {
                specie = new FieldInfo("specie", RuleSchema.PersistentRule.class);
                catchMethod = new FieldInfo("catchMethod", RuleSchema.PersistentRule.class);
                fromDate = new FieldInfo("fromDate", RuleSchema.PersistentRule.class);
                toDate = new FieldInfo("toDate", RuleSchema.PersistentRule.class);
            } catch (Exception e) {
                e.printStackTrace();
                throw new FlowException();
            }
            CriteriaExpression specieExpression = Builder.equal(specie, fish.getSpecie());
            CriteriaExpression catchMethodExpression = Builder.equal(catchMethod, fish.getCatchMethod());
            CriteriaExpression fromDateExpression = Builder.lessThan(fromDate, fish.getDateFishCaught());
            CriteriaExpression toDateExpression = Builder.greaterThan(toDate, fish.getDateFishCaught());

            QueryCriteria specieCriteria = new QueryCriteria.VaultCustomQueryCriteria(specieExpression);
            QueryCriteria catchMethodCriteria = new QueryCriteria.VaultCustomQueryCriteria(catchMethodExpression);
            QueryCriteria fromDateCriteria = new QueryCriteria.VaultCustomQueryCriteria(fromDateExpression);
            QueryCriteria toDateCriteria = new QueryCriteria.VaultCustomQueryCriteria(toDateExpression);

            QueryCriteria criteria = generalCriteria.and(specieCriteria).and(catchMethodCriteria).and(fromDateCriteria).and(toDateCriteria);

            List<StateAndRef<RuleState>> results = getServiceHub().getVaultService().queryBy(
                    RuleState.class,
                    criteria,
                    new PageSpecification(1, MAX_PAGE_SIZE),
                    new Sort(ImmutableList.of(new Sort.SortColumn(new SortAttribute.Standard(Sort.CommonStateAttribute.STATE_REF), Sort.Direction.DESC)))).getStates();

            getLogger().info("trovate " + results.size() + " regole");

            for (int i = 0; i < results.size(); i++) {
                RuleState rule = results.get(i).getState().getData();
                double distance = Haversine.distance(rule.getLatitude(), rule.getLongitude(), fish.getLatitude(), fish.getLongitude());
                if (rule.getRadius() > distance) {
                    getLogger().info("rule "+ rule.getLinearId().getId()+ " matching - distance "+distance+" - set isValid to false");
                    fish.setValid(false);
                }
            }

            getLogger().info("resending Valid to initiator");
            counterPartySession.send(fish.isValid());

            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession rulerFlow, ProgressTracker progressTracker) {
                    super(rulerFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {

                }
            }

            subFlow(new SignTxFlow(counterPartySession, SignTransactionFlow.Companion.tracker()));
            return null;
        }
    }
}
