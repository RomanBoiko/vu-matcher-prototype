package boikoro.vumatcher;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class AjaxService {
	private JdbcTemplate jdbcTemplate;
	public List<String> activeExceptions(){
		List<Map<String, Object>> recordsToMatch = jdbcTemplate.queryForList(
				"select r.body " +
				"from record_active r");
		List<String> result = new LinkedList<String>();
		for (Map<String, Object> entry : recordsToMatch) {
			result.add(entry.get("body").toString());
		}
		return result;
	}
	
	public void setDatasource(DataSource datasource) {
		jdbcTemplate = new JdbcTemplate(datasource);
	}

}
