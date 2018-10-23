package com.example.contract;

import com.example.state.FishState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class FishContract implements Contract {
    public static final String CONTRACT_ID = "com.example.contract.FishContract";

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<FishContract.Commands.Create> command = requireSingleCommand(tx.getCommands(), FishContract.Commands.Create.class);
        requireThat(require -> {

            require.using("No inputs should be consumed",
                    tx.getInputs().isEmpty());
            require.using("Only one output state should be created.",
                    tx.getOutputs().size() == 1);
            require.using("the output must be a fishState",
                    tx.getOutput(0) instanceof FishState);
            final FishState out = tx.outputsOfType(FishState.class).get(0);
            require.using("The owner and the ruler cannot be the same entity.",
                    out.getOwner() != out.getRuler());
            require.using("All of the participants must be signers.",
                    command.getSigners().containsAll(out.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList())));

            // Fish specific constraints.
            require.using("The fish must have a valid vessel",
                    out.getVessel().length()>0);
            require.using("The fish must weight more than 0",
                    out.getWeight()>0);
            require.using("The fish must have a valid length",
                    out.getLength()>0);
            require.using("The fish must have a valid catchMetod",
                    out.getCatchMethod().length()>0);
            require.using("The fish must have a valid specie",
                    out.getSpecie().length()>0);
            require.using("The fish must have a valid dateFishCaught",
                    out.getDateFishCaught()>0);
            return null;
        });
    }

    /**
     * This contract only implements one command, Create.
     */
    public interface Commands extends CommandData {
        class Create implements FishContract.Commands {}
    }
}
