package com.dma.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet implementation class GetTablesServlet
 */
@WebServlet("/GetSQLQuery")
public class GetSQLQueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSQLQueryServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Connection con = null;
		Map<String, Object> results = new HashMap<String, Object>();
		String schema = null;
		@SuppressWarnings("unused")
		String dbEngine = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		
		try {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        Map<String, Object>	parms = new HashMap<String, Object>();
	        parms = mapper.readValue(br, new TypeReference<Map<String, Object>>(){});
		        
			con = (Connection) request.getSession().getAttribute("con");
			schema = (String) request.getSession().getAttribute("schema");
			dbEngine = (String) request.getSession().getAttribute("dbEngine");
//			if((dbEngine).equalsIgnoreCase("ORA")){
//				con.createStatement().execute("alter session set current_schema=" + schema);
//				con.createStatement().execute("ALTER SESSION SET NLS_SORT=BINARY_CI");
//				con.createStatement().execute("ALTER SESSION SET NLS_COMP=LINGUISTIC");
//			}
//
//			if((dbEngine).equalsIgnoreCase("DB2") || (dbEngine).equalsIgnoreCase("DB2400")){
//				con.createStatement().execute("set schema " + schema);
//			}
			
			String query = (String) parms.get("query");
			
			if(!query.toUpperCase().startsWith("SELECT ")) {
	            throw new Exception("Only SELECT queries are allowed.");
			}
			
			results.put("query", query);
			results.put("schema", schema);
			
			System.out.println("query=" + query);
			
			stmt = con.prepareStatement(query);
			stmt.setMaxRows(100);
			rst = stmt.executeQuery();
			ResultSetMetaData rsmd = rst.getMetaData();
			int colCount = rsmd.getColumnCount();

			List<String> column_names = new ArrayList<String>();
			List<Integer> column_sizes = new ArrayList<Integer>();
			List<String> column_types = new ArrayList<String>();
			
			for(int colid = 1; colid <= colCount; colid++){
				column_names.add(rsmd.getColumnLabel(colid));
				column_sizes.add(rsmd.getColumnDisplaySize(colid));
				column_types.add(rsmd.getColumnTypeName(colid));
			}
			
			results.put("columns", column_names);
			
			List<Object> recs = new ArrayList<Object>();
			while(rst.next()){
				Map<String, Object> rec = new HashMap<String, Object>();
				for(int colid = 1; colid <= colCount; colid++){
					if(!StringUtils.containsAny(column_types.get(colid -1), "BLOB", "XML", "SQLXML", "NCLOB", "CLOB")){
//						System.out.println(column_names.get(colid -1) + "->" + column_types.get(colid -1));
						rec.put(column_names.get(colid -1), rst.getObject(colid));
					}
				}
				recs.add(rec);
			}
			rst.close();
			stmt.close();					

			results.put("result", recs);
			results.put("STATUS", "OK");
			
		}
		catch (Exception e){
			results.put("STATUS", "KO");
            results.put("EXCEPTION", e.getClass().getName());
            results.put("MESSAGE", e.getMessage());
            results.put("TROUBLESHOOTING", "Check SQL syntax.");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            results.put("STACKTRACE", sw.toString());
            e.printStackTrace(System.err);		}
		
		finally {
		    response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(Tools.toJSON(results));
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
