package sk.upjs.cassandra_cql;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.cql.CqlTemplate;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

@Configuration
public class CassandraConfig {

	static String HOST = "nosql.gursky.sk";
	static String KEYSPACE = "ks_dzama";
	
	@Bean
	public Session getSession() {
		Cluster cluster = Cluster.builder().addContactPoint(HOST).build();
		return cluster.connect(KEYSPACE);
	}
	
	@Bean
	public CqlTemplate cqlTemplate(Session session) {
		return new CqlTemplate(session);
	}
	
}
