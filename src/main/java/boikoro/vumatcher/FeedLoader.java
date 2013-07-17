package boikoro.vumatcher;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.h2.tools.RunScript;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.io.Files;

public class FeedLoader {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private JdbcTemplate jdbcTemplate;
	private boolean dbCreated = false;
	
	public void loadFiles() throws IOException, JSONException, SQLException {

		createDbIfNotDone();
		log.debug("checking waiting area..");
		for (File inputFile : new File("load/waiting").listFiles()) {

			Long sourceKey = insertSource(inputFile);
			log.info("SOURCE_ID=" + sourceKey);
			
			File fileToProcess = new File("load/processed/" + inputFile.getName());
			log.info("input detected(" + inputFile.getAbsolutePath() + "), loading");
			Files.move(inputFile, fileToProcess);
			
			
			CSVReader reader = new CSVReader(new FileReader(fileToProcess));
			String[] header = reader.readNext();
			String[] tradeRow;
			while ((tradeRow = reader.readNext()) != null) {
				JSONObject tradeObject = tradeObject(header, tradeRow);
				insertTradeRecord(sourceKey, tradeObject);
			}
			log.info("input processed(" + fileToProcess.getAbsolutePath() + ")");
		}
	}

	private void createDbIfNotDone() throws SQLException {
		if(!dbCreated) {
			RunScript.execute(jdbcTemplate.getDataSource().getConnection(),
					new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("ddl.sql")));
			dbCreated = true;
		}
	}

	private JSONObject tradeObject(String[] header, String[] tradeRow) throws JSONException {
		JSONObject entry = new JSONObject();
		for (int i = 0; i < header.length; i++) {
			entry.put(header[i], tradeRow[i]);
		}
		return entry;
	}

	private void insertTradeRecord(Long sourceKey, JSONObject entry) {
		String jsonData = entry.toString();
		jdbcTemplate.update("insert into record_active(body, source_ref) values(?, ?)", jsonData, sourceKey);
		log.info("entry loaded: " + jsonData);
	}

	private Long insertSource(File inputFile) {
		Map<String, Object> sourceInsertData = new HashMap<String, Object>();
		sourceInsertData.put("uri", inputFile.getAbsolutePath());
		return new Long(new SimpleJdbcInsert(jdbcTemplate).withTableName("source").usingGeneratedKeyColumns("id").executeAndReturnKey(sourceInsertData).toString());
	}
	
	public void setDatasource(DataSource datasource) {
		jdbcTemplate = new JdbcTemplate(datasource);
	}

}
