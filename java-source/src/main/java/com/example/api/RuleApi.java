package com.example.api;

import com.example.POJOs.RulePOJO;
import com.example.POJOs.ResponseMessage;
import com.example.flow.RuleFlow;
import com.example.schema.RuleSchema;
import com.example.state.RuleState;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.*;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static net.corda.core.node.services.vault.QueryCriteriaUtils.DEFAULT_PAGE_SIZE;


@Path("rule")
public class RuleApi {
    private final CordaRPCOps rpcOps;
    private final CordaX500Name myLegalName;

    private final List<String> serviceNames = ImmutableList.of("Notary");

    static private final Logger logger = LoggerFactory.getLogger(RuleApi.class);

    public RuleApi(CordaRPCOps rpcOps) {
        this.rpcOps = rpcOps;
        this.myLegalName = rpcOps.nodeInfo().getLegalIdentities().get(0).getName();
    }



    @GET
    @Path("rules")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StateAndRef<RuleState>> getRulees(@QueryParam("page") int page){
        if (page<1){
            page = 1;
        }

        return rpcOps.vaultQueryBy(
                new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED),
                new PageSpecification(page, DEFAULT_PAGE_SIZE),
                //per sortare dal piu recente al meno recente
                new Sort(ImmutableList.of(new Sort.SortColumn(new SortAttribute.Standard(Sort.CommonStateAttribute.STATE_REF),Sort.Direction.DESC))),
                RuleState.class).getStates();
    }


    @POST
    @Path("post")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postRule(RulePOJO rule){
        try {
            final SignedTransaction signedTx = rpcOps
                    .startTrackedFlowDynamic(RuleFlow.Initiator.class,
                            rule.getSpecie(),
                            rule.getcatchMethod(),
                            rule.getLatitude(),
                            rule.getLongitude(),
                            rule.getRadius(),
                            rule.getFromDate(),
                            rule.getToDate())
                    .getReturnValue()
                    .get();
            ResponseMessage resp = new ResponseMessage("SUCCESS","transaction "+signedTx.getId()+" committed to ledger.");
            return Response.status(CREATED).entity(resp).build();
        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            logger.error(ex.getMessage(), ex);
            ResponseMessage resp = new ResponseMessage("ERROR",ex.getMessage());
            return Response.status(BAD_REQUEST).entity(resp).build();
        }
    }


    @GET
    @Path("myRules")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyRulees(@QueryParam("page") int page) throws NoSuchFieldException {
        if (page<1){
            page = 1;
        }

        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
        Field owner = RuleSchema.PersistentRule.class.getDeclaredField("owner");
        CriteriaExpression ownerIndex = Builder.equal(owner, myLegalName.toString());
        QueryCriteria ownerCriteria = new QueryCriteria.VaultCustomQueryCriteria(ownerIndex);
        QueryCriteria criteria = generalCriteria.and(ownerCriteria);
        List<StateAndRef<RuleState>> results = rpcOps.vaultQueryBy(
                new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED),
                new PageSpecification(page, DEFAULT_PAGE_SIZE),
                new Sort(ImmutableList.of(new Sort.SortColumn(new SortAttribute.Standard(Sort.CommonStateAttribute.STATE_REF),Sort.Direction.DESC))),
                RuleState.class).getStates();


        return Response.status(OK).entity(results).build();
    }
}
