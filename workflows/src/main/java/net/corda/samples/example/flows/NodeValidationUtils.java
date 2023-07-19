package net.corda.samples.example.flows;

import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.IdentityService;
import java.util.List;


public class NodeValidationUtils {

    public static boolean validateNodesAuthenticity(ServiceHub serviceHub, List<Party> nodes) {
        IdentityService identityService = serviceHub.getIdentityService();

        for (Party node : nodes) {
            if (!validateNodeAuthenticity(identityService, node)) {
                return false;
            }
        }

        return true;
    }

    private static boolean validateNodeAuthenticity(IdentityService identityService, Party node) {
        // Check if the node is a well-known identity in the network
        if (identityService.wellKnownPartyFromAnonymous(node) != null) {
            // The node's identity is known and trusted
            return true;
        } else {
            // The node's identity is not known or not trusted
            return false;
        }
    }
}