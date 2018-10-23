package com.example.contract;

import com.example.schema.RuleSchema;
import com.example.state.FishState;
import com.example.state.RuleState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class RuleContract implements Contract {

    public static final String CONTRACT_ID = "com.example.contract.RuleContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        final CommandWithParties<RuleContract.Commands.Create> command = requireSingleCommand(tx.getCommands(), RuleContract.Commands.Create.class);
        requireThat(require -> {

            require.using("No inputs should be consumed",
                    tx.getInputs().isEmpty());
            require.using("Only one output state should be created.",
                    tx.getOutputs().size() == 1);
            require.using("the output must be a RuleState",
                    tx.getOutput(0) instanceof RuleState);
            final RuleState out = tx.outputsOfType(RuleState.class).get(0);
            require.using("All of the participants must be signers.",
                    command.getSigners().containsAll(out.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList())));

            // Fish specific constraints.
            require.using("The rule must have a valid specie",
                    out.getSpecie().length()>0);
            require.using("The rule must have a valid catchMethod",
                    out.getcatchMethod().length()>0);
            require.using("The rule must have a valid radius",
                    out.getRadius()>0);
            require.using("The rule must have a valid fromDate",
                    out.getFromDate()>0);
            require.using("The rule must have a valid toDate",
                    out.getToDate()>0);
            require.using("fromDate must be before toDate",
                    out.getFromDate()<out.getToDate());
            return null;
        });
    }

    public interface Commands extends CommandData {
        class Create implements RuleContract.Commands {}
    }
}
