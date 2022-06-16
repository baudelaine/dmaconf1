package com.dma.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Servlet implementation class AppendSelectionsServlet
 */
@WebServlet(name = "UpdateModel", urlPatterns = { "/UpdateModel" })
public class UpdateModelServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateModelServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		Map<String, Object> result = new HashMap<String, Object>();

		try {
			
			result.put("CLIENT", request.getRemoteAddr() + ":" + request.getRemotePort());
			result.put("SERVER", request.getLocalAddr() + ":" + request.getLocalPort());
			
			result.put("FROM", this.getServletName());
			
			String user = request.getUserPrincipal().getName();
			result.put("USER", user);

			result.put("JSESSIONID", request.getSession().getId());
			
			Path wks = Paths.get(getServletContext().getRealPath("/datas") + "/" + user);			
			result.put("WKS", wks.toString());
			
			Path prj = Paths.get((String) request.getSession().getAttribute("projectPath"));
			result.put("PRJ", prj.toString());
			
			Map<String, Object> parms = Tools.fromJSON(request.getInputStream());

			
			if(parms != null) {

				
				@SuppressWarnings("unchecked")
				List<String> langs = (List<String>) Tools.fromJSON((String) parms.get("langs"), new TypeReference<List<String>>(){});
				
				@SuppressWarnings("unchecked")
				List<QuerySubject> qss = (List<QuerySubject>) Tools.fromJSON((String) parms.get("model"), new TypeReference<List<QuerySubject>>(){});

				Map<String, List<Field>> fieldsToRemove = new HashMap<String, List<Field>>();
				Map<String, List<Field>> fieldsToAdd = new HashMap<String, List<Field>>();
				Map<String, List<Field>> fieldsToUpdate = new HashMap<String, List<Field>>();
				Map<String, List<Field>> customFields = new HashMap<String, List<Field>>();
				
				Map<String, Map<String, Field>> modelMap = new HashMap<String, Map<String, Field>>();
				Map<String, Map<String, Field>> dbMap = new HashMap<String, Map<String, Field>>();
				Map<String, Integer> colCountMap = new HashMap<String, Integer>();
				
				// Start build modelMap and put custom fields in customFields
				for(QuerySubject qs: qss) {
					String table = qs.getTable_name();
					List<Field> fields = qs.getFields();
					Map<String, Field> fMap = new HashMap<String, Field>();
					List<Field> customFList = new ArrayList<Field>();
					for(Field field: fields) {
						String fieldName = field.getField_name();
						if(!field.isCustom()) {
							fMap.put(fieldName, field);
						}
						else {
							customFList.add(field);					
						}
					}
					modelMap.put(table, fMap);
					customFields.put(table, customFList);
				}
				// End build modelMap and put custom fields in customFields

				boolean isXML = false;
				Project project = (Project) request.getSession().getAttribute("currentProject");
				if(project != null) {
					Resource resource = project.getResource();
					if(resource.getJndiName().equalsIgnoreCase("XML")) {
						isXML = true;
					}
				}

				
				if(isXML) {
					@SuppressWarnings("unchecked")
					Map<String, QuerySubject> qssXML = (Map<String, QuerySubject>) request.getSession().getAttribute("QSFromXML");
					Map<String, List<Field>> newFields = new HashMap<String, List<Field>>();
					
					for(Entry<String, QuerySubject> qs: qssXML.entrySet()) {
						String table = qs.getKey();
						if(modelMap.containsKey(table)){
							List<Field> fields = qs.getValue().getFields();
							for(Field field: fields) {
								if(!modelMap.get(table).containsKey(field.getField_name())) {
	
									if(!newFields.containsKey(table)) {
										newFields.put(table, new ArrayList<Field>());
									}
									
									Field newField = new Field();
									newField.set_id(field.getField_name());
									newField.setField_name(field.getField_name());
									newField.setField_type(field.getField_type());
									Map<String, String> langsMap = new HashMap<String, String>();
									for(String lang: langs) {
										langsMap.put(lang, "");
									}
									newField.setLabels(langsMap);
									newField.setDescriptions(langsMap);
									
									newFields.get(table).add(newField);
								}
							}
						}
					}
				}
				else {

					// Add fields in QS if new in DB table
					Connection conn = (Connection) request.getSession().getAttribute("con");
					String schema = (String) request.getSession().getAttribute("schema");
					
				    DatabaseMetaData metaData = conn.getMetaData();
				    
//				    String[] types = {"TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"};
				    String[] types = {"TABLE"}; 
				    		
				    if(project != null) {
					    String tableTypes = project.getResource().getTableTypes();
					    List<String> typesList = new ArrayList<String>();
					    switch(tableTypes.toUpperCase()) {
					    	case "TABLE":
					    		typesList.add("TABLE");
					    		break;
					    	case "VIEW":
					    		typesList.add("VIEW");
					    		break;
					    	case "BOTH":
					    		typesList.add("TABLE");
					    		typesList.add("VIEW");
					    		break;
					    }
					    types = typesList.stream().toArray(String[]::new);
				    }				    
				    
					// Start build dbMap and colCountMap
				    ResultSet rstTables = metaData.getTables(conn.getCatalog(), schema, "%", types);					    
				    while (rstTables.next()) {
				    	String table = rstTables.getString("TABLE_NAME");
				    	if(modelMap.containsKey(table)) {
					    	System.out.println(table);
							ResultSet rstFields = metaData.getColumns(conn.getCatalog(), schema, table, "%");
							ResultSetMetaData rsmd = rstTables.getMetaData();
							colCountMap.put(table, rsmd.getColumnCount());
							Map<String, Field> fMap = new HashMap<String, Field>();
							while(rstFields.next()){
								Field field = new Field();
								String column = rstFields.getString("COLUMN_NAME");
								System.out.println(column);
								field.set_id(rstFields.getString("COLUMN_NAME"));
								field.setField_name(rstFields.getString("COLUMN_NAME"));
								field.setField_type(rstFields.getString("TYPE_NAME"));
								field.setNullable(rstFields.getString("IS_NULLABLE"));
								field.setField_size(rstFields.getInt("COLUMN_SIZE"));
								field.setDescription(rstFields.getString("REMARKS"));
								field.setFieldPos(rstFields.getInt("ORDINAL_POSITION"));
								Map<String, String> langsMap = new HashMap<String, String>();
								for(String lang: langs) {
									langsMap.put(lang, "");
								}
								field.setLabels(langsMap);
								field.setDescriptions(langsMap);
								fMap.put(column, field);
							}
							dbMap.put(table, fMap);
							rstFields.close();
				    	}
				    }
				    rstTables.close();
					// End build dbMap and colCountMap
				    
				    System.out.println(modelMap);
				    System.out.println(dbMap);
				    
				    // Start Update ORDINAL_POSITION (fieldPos) of existing field in model and put fields that does not exists in db in fieldsToRemove 
				    for(Entry<String, Map<String, Field>> model: modelMap.entrySet()) {
				    	String modelTable = model.getKey();
				    	Map<String, Field> modelFieldsMap = model.getValue();
			    		List<Field> fieldsToRemoveList = new ArrayList<Field>();
			    		List<Field> fieldsToUpdateList = new ArrayList<Field>();
				    	for(Entry<String, Field> field: modelFieldsMap.entrySet()) {
				    		String modelColumn = field.getKey();
				    		Field modelField = field.getValue();
					    	if(dbMap.get(modelTable).get(modelColumn) != null) {
					    		int dbFieldPos = dbMap.get(modelTable).get(modelColumn).getFieldPos();
					    		modelField.setFieldPos(dbFieldPos);
					    		fieldsToUpdateList.add(modelField);
//					    		System.out.println(modelColumn + " exists in DB (" + modelField.getFieldPos() + ")");
					    	}
					    	else {
//					    		System.out.println(modelColumn + " DOES NOT exists in DB");
					    		fieldsToRemoveList.add(modelField);
					    	}
				    	}
				    	if(!fieldsToRemoveList.isEmpty()) {
				    		fieldsToRemove.put(modelTable, fieldsToRemoveList);
				    	}
				    	if(!fieldsToUpdateList.isEmpty()) {
				    		fieldsToUpdate.put(modelTable, fieldsToUpdateList);
				    	}
				    }
				    // End Update ORDINAL_POSITION (fieldPos) of existing field in model and put fields that does not exists in db in fieldsToRemove 
				    
				    // Start Adding field in db that does not exists in model in fieldsToAdd
				    for(Entry<String, Map<String, Field>> db: dbMap.entrySet()) {
				    	String dbTable = db.getKey();
				    	Map<String, Field> dbFieldsMap = db.getValue();
				    	List<Field> fieldsToAddList = new ArrayList<Field>();
				    	for(Entry<String, Field> field: dbFieldsMap.entrySet()) {
				    		String dbColumn = field.getKey();
				    		Field dbField = field.getValue();
				    		if(modelMap.get(dbTable).get(dbColumn) == null) {
				    			fieldsToAddList.add(dbField);
//					    		System.out.println(dbColumn + " DOES NOT exists in model (" + dbField.getFieldPos() + ")");
				    		}
				    	}
				    	if(!fieldsToAddList.isEmpty()) {
				    		fieldsToAdd.put(dbTable, fieldsToAddList);
				    	}
				    }
				    // End Adding field in db that does not exists in model in fieldsToAdd
				    
				    
				    
//				    System.out.println(modelMap);
//				    System.out.println(dbMap);
//				    System.out.println(fieldsToRemove);
//				    System.out.println(fieldsToAdd);
//				    System.out.println(fieldsToUpdate);
//				    System.out.println(customFields);
				    
					for(QuerySubject qs: qss) {
						List<Field> fields = new ArrayList<Field>();
						if(fieldsToUpdate.get(qs.getTable_name()) != null) {
							fields.addAll(fieldsToUpdate.get(qs.getTable_name()));
						}
						if(fieldsToAdd.get(qs.getTable_name()) != null) {
							fields.addAll(fieldsToAdd.get(qs.getTable_name()));
						}
						int fieldPos = colCountMap.get(qs.getTable_name());
//						System.out.println(qs.getTable_name());
//						System.out.println(fieldPos);
						if(customFields.get(qs.getTable_name()) != null) {
							for(Field customField: customFields.get(qs.getTable_name())) {
								customField.setFieldPos(fieldPos++);
								fields.add(customField);
							}
						}
						qs.setFields(fields);
					}
				}
				
				result.put("MODEL", qss);
				result.put("REMOVED", fieldsToRemove);
				result.put("ADDED", fieldsToAdd);
				result.put("STATUS", "OK");
			}
			else {
				result.put("STATUS", "KO");
				result.put("ERROR", "Input parameters are not valid.");
				throw new Exception();
			}			
		}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			result.put("STATUS", "KO");
			result.put("EXCEPTION", e.getClass().getName());
			result.put("MESSAGE", e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			result.put("STACKTRACE", sw.toString());
			e.printStackTrace(System.err);
		}

		finally{
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(Tools.toJSON(result));
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}