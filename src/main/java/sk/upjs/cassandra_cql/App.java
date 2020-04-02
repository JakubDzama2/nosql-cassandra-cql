package sk.upjs.cassandra_cql;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.cql.ResultSetExtractor;
import org.springframework.data.cassandra.core.cql.RowMapper;
import org.springframework.data.cassandra.core.cql.generator.CreateTableCqlGenerator;
import org.springframework.data.cassandra.core.cql.generator.DropTableCqlGenerator;
import org.springframework.data.cassandra.core.cql.keyspace.CreateTableSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DropTableSpecification;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;

import ch.qos.logback.classic.Level;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.setLevel(Level.INFO);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CassandraConfig.class);
		CqlTemplate template = context.getBean(CqlTemplate.class);
		Session session = context.getBean(Session.class);

//		template.execute("CREATE TABLE IF NOT EXISTS test (id uuid primary key, event text)");
//		long start = System.nanoTime();
//		for (int i = 0; i < 10; i++) {
//			template.execute("INSERT INTO test (id, event) VALUES (?, ?)", UUID.randomUUID(),
//					"udalosť číslo " + (int) (1000 * Math.random()));
//		}
//		System.out.println("prvy insert: " + (System.nanoTime() - start)/1000000.0 + " ms");
//		
//		template.query("SELECT * FROM test", new RowMapper<Void>() {
//
//			@Override
//			public Void mapRow(Row row, int rowNum) throws DriverException {
//				System.out.println("id: " + row.getUUID("id") + " , event: " + row.getString("event"));
//				return null;
//			}
//		});
//
//		template.execute("DROP TABLE test");

		DropTableSpecification dropTableSepcification = DropTableSpecification.dropTable("test2").ifExists();
		template.execute(DropTableCqlGenerator.toCql(dropTableSepcification));
		
		CreateTableSpecification specification = CreateTableSpecification.createTable("test2")
			.partitionKeyColumn("id_dept", DataType.bigint())
			.clusteredKeyColumn("name", DataType.text())
			.column("salary", DataType.decimal())
			.ifNotExists();
		
		template.execute(CreateTableCqlGenerator.toCql(specification));
		
		long start = System.nanoTime();
		PreparedStatement prepare = session.prepare("INSERT INTO test2 (id_dept, name, salary) VALUES (?, ?, ?)");
		for (int i = 0; i < 10; i++) {
			BoundStatement bind = prepare.bind((long) (Math.random()*3) + 1,
						"clovek" + (long) (Math.random()*10000) + 1,
						new BigDecimal(Math.random()*5000).setScale(2, RoundingMode.CEILING));
			template.execute(bind);
		}
		System.out.println("druhy insert: " + (System.nanoTime() - start)/1000000.0 + " ms");

		
		start = System.nanoTime();
		BatchStatement batchStatement = new BatchStatement();
		prepare = session.prepare("INSERT INTO test2 (id_dept, name, salary) VALUES (?, ?, ?)");
		for (int i = 0; i < 10; i++) {
			BoundStatement bind = prepare.bind((long) (Math.random()*3) + 1,
						"clovek" + (long) (Math.random()*10000) + 1,
						new BigDecimal(Math.random()*5000).setScale(2, RoundingMode.CEILING));
			batchStatement.add(bind);
		}
		template.execute(batchStatement);
		System.out.println("treti insert: " + (System.nanoTime() - start)/1000000.0 + " ms");
		
		template.query("SELECT * FROM test2", new ResultSetExtractor<Void>() {

			@Override
			public Void extractData(ResultSet resultSet) throws DriverException, DataAccessException {
				Iterator<Row> iterator = resultSet.iterator();
				while (iterator.hasNext()) {
					Row row = iterator.next();
					System.out.println("dept_id: " + row.getLong("id_dept")
									+ " name: " + row.getString("name")
									+ " salary: " + row.getDecimal("salary"));
				}
				return null;
			}
		});


		session.getCluster().close();
		context.close();
	}
}
