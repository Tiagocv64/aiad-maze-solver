import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Runner {

    public static void main(String[] args){
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");
        AgentContainer container = rt.createMainContainer(p);

        try {
            AgentController acSelfish = container.createNewAgent("Tiago", "SelfishAgent", null);
            AgentController acSupportive = container.createNewAgent("Rafa", "SupportiveAgent", null);
            AgentController acReasonable = container.createNewAgent("Sousa", "ReasonableAgent", null);
            acSelfish.start();
            acSupportive.start();
            acReasonable.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}