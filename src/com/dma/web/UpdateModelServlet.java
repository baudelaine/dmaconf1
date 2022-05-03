package com.dma.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
				List<QuerySubject> model = (List<QuerySubject>) Tools.fromJSON((String) parms.get("model"), new TypeReference<List<QuerySubject>>(){});

				Map<String, Map<String, Object>> tMap = new HashMap<String, Map<String, Object>>();
				
				for(QuerySubject qs: model) {
					String table = qs.getTable_name();
					List<Field> fields = qs.getFields();
					Map<String, Object> fMap = new HashMap<String, Object>();
					for(Field field: fields) {
						String fieldName = field.getField_name();
						fMap.put(fieldName, null);
					}
					tMap.put(table, fMap);
				}

				boolean isXML = false;
				Project project = (Project) request.getSession().getAttribute("currentProject");
				if(project != null) {
					Resource resource = project.getResource();
					if(resource.getJndiName().equalsIgnoreCase("XML")) {
						isXML = true;
					}
				}

				Map<String, List<Field>> newFields = new HashMap<String, List<Field>>();
				Map<String, List<Field>> fieldsToRemove = new HashMap<String, List<Field>>();
				
				if(isXML) {
					@SuppressWarnings("unchecked")
					Map<String, QuerySubject> qss = (Map<String, QuerySubject>) request.getSession().getAttribute("QSFromXML");
					
					for(Entry<String, QuerySubject> qs: qss.entrySet()) {
						String table = qs.getKey();
						if(tMap.containsKey(table)){
							List<Field> fields = qs.getValue().getFields();
							for(Field field: fields) {
								if(!tMap.get(table).containsKey(field.getField_name())) {
	
									if(!newFields.containsKey(table)) {
										newFields.put(table, new ArrayList<Field>());
									}
									
									Field newField = new Field();
									newField.set_id(field.getField_name() + field.getField_type());
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
					Connection con = (Connection) request.getSession().getAttribute("con");
					String schema = (String) request.getSession().getAttribute("schema");
					
				    DatabaseMetaData metaData = con.getMetaData();
				    
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
				    
				    ResultSet rstTables = metaData.getTables(con.getCatalog(), schema, "%", types);					    

				    while (rstTables.next()) {
				    	String table = rstTables.getString("TABLE_NAME");
				    	if(tMap.containsKey(table)) {
							ResultSet rstFields = metaData.getColumns(con.getCatalog(), schema, rstTables.getString("TABLE_NAME"), "%");
							while(rstFields.next()){
								if(!tMap.get(table).containsKey(rstFields.getString("COLUMN_NAME"))) {								

									if(!newFields.containsKey(table)) {
										newFields.put(table, new ArrayList<Field>());
									}
									
									Field newField = new Field();
									newField.set_id(rstFields.getString("COLUMN_NAME") + rstFields.getString("TYPE_NAME"));
									newField.setField_name(rstFields.getString("COLUMN_NAME"));
									newField.setField_type(rstFields.getString("TYPE_NAME"));
									newField.setNullable(rstFields.getString("IS_NULLABLE"));
									newField.setField_size(rstFields.getInt("COLUMN_SIZE"));
									newField.setDescription(rstFields.getString("REMARKS"));
									Map<String, String> langsMap = new HashMap<String, String>();
									for(String lang: langs) {
										langsMap.put(lang, "");
									}
									newField.setLabels(langsMap);
									newField.setDescriptions(langsMap);
									
									newFields.get(table).add(newField);
								}
								
							}
							rstFields.close();
				    	}

				    }		    
				    
				    rstTables.close();
				    
				    // Remove fields from QS if no longer exists in DB table and not custom
				    // set tableExists to true in QS  if DB table no longer exists
				    
					Map<String, Set<String>> fldMap = new HashMap<String, Set<String>>();
					Set<String> tblSet = new HashSet<String>();
					
					for(QuerySubject qs: model) {
						String table = qs.getTable_name();
						tblSet.add(table);
					}
				    
					for(String tbl: tblSet) {
						
						ResultSet rstFields = metaData.getColumns(con.getCatalog(), schema, tbl, "%");
						Set<String> fldSet = new HashSet<String>();
						while(rstFields.next()){
							fldSet.add(rstFields.getString("COLUMN_NAME"));
						}
						rstFields.close();
						if(fldSet.size() > 0) {
							fldMap.put(tbl, fldSet);
						}
					}
				    
					for(QuerySubject qs: model) {
						if(!tblSet.contains(qs.getTable_name())) {
//							System.out.println(qs.getTable_name() + " does not exists");
							qs.setTableExists(false);
						}
						else {
							Set<String> fldSet = fldMap.get(qs.getTable_name());
							List<Field> fldToRemove = new ArrayList<Field>();
							for(Field fld: qs.getFields()) {
								if(!fldSet.contains(fld.getField_name())) {
//									System.out.println(qs.getTable_name() + "." + fld.getField_name() + " does not exists");
									if(!fld.isCustom()) {
										fldToRemove.add(fld);
									}
								}
							}
							fieldsToRemove.put(qs.get_id(), fldToRemove);
							
						}
					}
					
				}
				
				Map<String, List<Field>> datas = new HashMap<String, List<Field>>();
				
				for(QuerySubject qs: model) {
					String table = qs.getTable_name();
					if(newFields.containsKey(table)) {
						datas.put(qs.get_id(), newFields.get(table));
						qs.getFields().addAll(newFields.get(table));
					}
					if(fieldsToRemove.containsKey(qs.get_id())) {
						List<Field> fldsToRemove = fieldsToRemove.get(qs.get_id());
						qs.getFields().removeAll(fldsToRemove);
					}
				}
				
				result.put("MODEL", model);
				result.put("REMOVED", fieldsToRemove);
				result.put("DATAS", datas);
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