package boikoro.vumatcher;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class Matcher {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private JdbcTemplate jdbcTemplate;
	
	public void matchRecords() throws IOException, JSONException, SQLException {

		log.debug("matching..");
		List<Map<String, Object>> recordsToMatch = jdbcTemplate.queryForList(
				"select r.id rid, r.body, e.id eid " +
				"from record_active r left outer join exception_active e " +
				"on e.record_ref = r.id");
		
		Set<Long> matched = new HashSet<Long>();
		for (Map<String, Object> activeRecord : recordsToMatch) {
			Long activeId = recordId(activeRecord);
			for (Map<String, Object> potentialMatch : recordsToMatch) {
				Long potentialId = recordId(potentialMatch);
				if((!activeId.equals(potentialId)) && (!matched.contains(activeId)) && (!matched.contains(potentialId))) {
					JSONObject activeObject = new JSONObject(new JSONTokener(activeRecord.get("BODY").toString()));
					JSONObject potentialObject = new JSONObject(new JSONTokener(potentialMatch.get("BODY").toString()));
					if(MatchingRules.isMatchedPair(activeObject, potentialObject)) {
						log.info("Records "+activeId+" and "+potentialId+" matched");
						matched.add(activeId);
						matched.add(potentialId);
						markAsMatched(activeRecord, potentialMatch);
						break;
					}
				}
			}
		}

		for (Map<String, Object> activeRecord : recordsToMatch) {
			if(null == exceptionId(activeRecord) && (!matched.contains(recordId(activeRecord)))) {
				insertException(recordId(activeRecord));
			}
		}
		
	}

	

	private Object exceptionId(Map<String, Object> activeRecord) {
		return activeRecord.get("EID");
	}

	private Long recordId(Map<String, Object> record) {
		return new Long(record.get("RID").toString());
	}

	private void markAsMatched(Map<String, Object> record1, Map<String, Object> record2) {
		Long matchId = createMatch();
		moveActiveRecordToMatched(record1, matchId);
		moveActiveRecordToMatched(record2, matchId);
	}
	

	private void moveActiveRecordToMatched(Map<String, Object> activeRecord, Long matchId) {
		Object activeExceptionId = exceptionId(activeRecord);
		Long matchedRecordId = createMatchedRecord(activeRecord, matchId);
		if(null != activeExceptionId) {
			closeException(activeExceptionId, matchedRecordId);
		}
		jdbcTemplate.update("delete from record_active where id=?", recordId(activeRecord));
	}
	
	private void closeException(Object activeExceptionId, Long matchedRecordId) {
		jdbcTemplate.update("insert into exception_closed(name, record_ref) values (?, ?)", "Record matched", matchedRecordId);
		jdbcTemplate.update("delete from exception_active where id=?", activeExceptionId);
		
	}

	private Long createMatchedRecord(Map<String, Object> activeRecord, Long matchId) {
		Map<String, Object> sourceInsertData = new HashMap<String, Object>();
		sourceInsertData.put("body", activeRecord.get("body"));
		sourceInsertData.put("source_ref", activeRecord.get("source_ref"));
		sourceInsertData.put("match_ref", matchId);
		return new Long(new SimpleJdbcInsert(jdbcTemplate).withTableName("record_matched").usingGeneratedKeyColumns("id").executeAndReturnKey(sourceInsertData).toString());
	}

	private Long createMatch() {
		Map<String, Object> sourceInsertData = new HashMap<String, Object>();
		sourceInsertData.put("name", "One2OneMatch");
		log.info("Match One2OneMatch created");
		return new Long(new SimpleJdbcInsert(jdbcTemplate).withTableName("match").usingGeneratedKeyColumns("id").executeAndReturnKey(sourceInsertData).toString());
	}
	


	private void insertException(Long recordKey) {
		jdbcTemplate.update("insert into exception_active(name, record_ref) values(?, ?)", "No record to match", recordKey);
		log.info("active exception created for record " + recordKey);
	}

	public void setDatasource(DataSource datasource) {
		jdbcTemplate = new JdbcTemplate(datasource);
	}
}
