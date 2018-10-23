package com.example.api;

import com.example.POJOs.ResponseMessage;
import com.example.flow.FishFlow;
import com.example.schema.FishSchema;
import com.example.POJOs.FishPOJO;
import com.example.state.FishState;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static net.corda.core.node.services.vault.QueryCriteriaUtils.DEFAULT_PAGE_SIZE;

@Path("fish")
public class FishApi {

    private final CordaRPCOps rpcOps;
    private final CordaX500Name myLegalName;

    private final List<String> serviceNames = ImmutableList.of("Notary");

    static private final Logger logger = LoggerFactory.getLogger(FishApi.class);

    public FishApi(CordaRPCOps rpcOps) {
        this.rpcOps = rpcOps;
        this.myLegalName = rpcOps.nodeInfo().getLegalIdentities().get(0).getName();
    }



    @GET
    @Path("fishes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StateAndRef<FishState>> getFishes(@QueryParam("page") int page){
        if (page<1){
            page = 1;
        }

        return rpcOps.vaultQueryBy(
                new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED),
                new PageSpecification(page, DEFAULT_PAGE_SIZE),
                //per sortare dal piu recente al meno recente
                new Sort(ImmutableList.of(new Sort.SortColumn(new SortAttribute.Standard(Sort.CommonStateAttribute.STATE_REF),Sort.Direction.DESC))),
                FishState.class).getStates();
    }

    @POST
    @Path("post")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postFish(FishPOJO fish){
        try {

            final Party ruler = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(fish.getRuler()));

            if (ruler == null) {
                ResponseMessage resp = new ResponseMessage("ERROR","cannot find ruler " + fish.getRuler());
                return Response.status(BAD_REQUEST).entity(resp).build();
            }

            final SignedTransaction signedTx = rpcOps
                    .startTrackedFlowDynamic(FishFlow.Initiator.class,
                            ruler,
                            fish.getVessel(),
                            fish.getWeight(),
                            fish.getLength(),
                            fish.getCatchMethod(),
                            fish.getSpecie(),
                            fish.getDescription(),
                            fish.getLatitude(),
                            fish.getLongitude(),
                            fish.getDateFishCaught())
                    .getReturnValue()
                    .get();
            ResponseMessage resp = new ResponseMessage("SUCCESS","transaction "+signedTx.toString()+" committed to ledger.");
            return Response.status(CREATED).entity(resp).build();
        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            logger.error(ex.getMessage(), ex);
            ResponseMessage resp = new ResponseMessage("ERROR",ex.getMessage());
            return Response.status(BAD_REQUEST).entity(resp).build();
        }
    }


    @GET
    @Path("myFishes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyFishes(@QueryParam("page") int page) throws NoSuchFieldException {
        if (page<1){
            page = 1;
        }

        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
        Field owner = FishSchema.PersistentFish.class.getDeclaredField("owner");
        CriteriaExpression ownerIndex = Builder.equal(owner, myLegalName.toString());
        QueryCriteria ownerCriteria = new QueryCriteria.VaultCustomQueryCriteria(ownerIndex);
        QueryCriteria criteria = generalCriteria.and(ownerCriteria);
        List<StateAndRef<FishState>> results = rpcOps.vaultQueryBy(
                new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED),
                new PageSpecification(page, DEFAULT_PAGE_SIZE),
                new Sort(ImmutableList.of(new Sort.SortColumn(new SortAttribute.Standard(Sort.CommonStateAttribute.STATE_REF),Sort.Direction.DESC))),
                FishState.class).getStates();


        return Response.status(OK).entity(results).build();
    }
}
