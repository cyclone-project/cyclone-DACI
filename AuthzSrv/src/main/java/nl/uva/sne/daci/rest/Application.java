package nl.uva.sne.daci.rest;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import nl.uva.sne.daci.authzsvcimp.Configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

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
        if (args.containsOption("context_srv_port")) 
        	Configuration.CONTEXT_SVC_PORT = args.getOptionValues("context_srv_port").get(0);
        if (args.containsOption("context_srv_address")) 
        	Configuration.CONTEXT_SVC_URL = args.getOptionValues("context_srv_address").get(0) + ":"+
        								    Configuration.CONTEXT_SVC_PORT + "/contexts";
        if (args.containsOption("domain")) 
        	Configuration.DOMAIN = args.getOptionValues("domain").get(0);
        
        /*System.out.println("redis_address="+Configuration.REDIS_SERVER_ADDRESS +
        				   " context_srv_port=" + Configuration.CONTEXT_SVC_PORT +
        				   " context_srv_address=" + args.getOptionValues("context_srv_address").get(0)+
        				   " domain="+Configuration.DOMAIN);*/
    }
    
    
}