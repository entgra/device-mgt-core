package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "TicketingConfiguration")
public class TicketingConfiguration {

    private List<TicketingGateway> ticketingGateways;

    @XmlElementWrapper(name = "Gateways")
    @XmlElement(name = "Gateway")
    public List<TicketingGateway> getTicketingGateways() {
        return ticketingGateways;
    }

    public void setTicketingGateways(List<TicketingGateway> ticketingGateways) {
        this.ticketingGateways = ticketingGateways;
    }

    /**
     * Retrieve the default Ticketing Gateway as defined in the Ticketing configuration.
     * @return default {@link TicketingGateway}
     */
    public TicketingGateway getDefaultTicketingGateway() {
        for (TicketingGateway ticketingGateway : ticketingGateways) {
            if (ticketingGateway.isDefault()) {
                return ticketingGateway;
            }
        }
        return null;
    }

    /**
     * Retrieve Ticketing Gateway by the provided Gateway Name
     * @param gatewayName has the name of the Gateway to be retrieved
     * @return retrieved {@link TicketingGateway}
     */
    public TicketingGateway getTicketingGateway(String gatewayName) {
        for (TicketingGateway ticketingGateway : ticketingGateways) {
            if (gatewayName.equals(ticketingGateway.getName())) {
                return ticketingGateway;
            }
        }
        return null;
    }
}
