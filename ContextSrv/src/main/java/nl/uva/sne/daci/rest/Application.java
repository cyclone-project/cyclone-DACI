package nl.uva.sne.daci.rest;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import nl.uva.sne.daci.contextsvcimpl.Configuration;

@SpringBootApplication
//@EnableAutoConfiguration
//@ComponentScan("nl.uva.sne.daci.rest")
public class Application implements ApplicationRunner{

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	
        if (args.containsOption("redis_address")) 
        	Configuration.REDIS_SERVER_ADDRESS = args.getOptionValues("redis_address").get(0);
        if (args.containsOption("token_srv_port")) 
        	Configuration.TOKEN_SVC_PORT = args.getOptionValues("token_srv_port").get(0);
        if (args.containsOption("token_srv_address")) 
        	Configuration.TOKEN_SVC_URL = args.getOptionValues("token_srv_address").get(0) + ":" +
        								    Configuration.TOKEN_SVC_PORT + "/tokens";
        if (args.containsOption("domain")) 
        	Configuration.DOMAIN = args.getOptionValues("domain").get(0);
        
        /*System.out.println("redis_address="+Configuration.REDIS_SERVER_ADDRESS +
        				   " token_srv_port=" + Configuration.TOKEN_SVC_PORT +
        				   " token_srv_address=" + args.getOptionValues("token_srv_address").get(0)+
        				   " domain="+Configuration.DOMAIN);*/
    }
    
}