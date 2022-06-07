package com.dma.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

public class Test28 {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		Path path = Paths.get("/home/fr054721/dmaconf/mod1.json");
		Path output = Paths.get("/home/fr054721/dmaconf/mod1-res.json");
//		String selectedQs = "POFinal";
//		String selectedQs = "POLINEFinal";
//		String selectedQs = "ASSETFinal";
		
		
		
		if(!Files.exists(path)) {
			System.err.println("ERROR: No file found !!!");
			System.exit(1);
		}
		
		List<QuerySubject> qssList = (List<QuerySubject>) Tools.fromJSON(path.toFile(), new TypeReference<List<QuerySubject>>(){});
		
		Map<String, QuerySubject> qss = new HashMap<String, QuerySubject>(); 
		Set<String> qssRestant = new HashSet<String>();
		Set<String> allQss = new HashSet<String>();
		Set<String> qssToRemove = new HashSet<String>();
		
		for(QuerySubject qs: qssList) {
			qss.put(qs.get_id(), qs);
			allQss.add(qs.get_id());
		}

		for(Entry<String, QuerySubject> qs: qss.entrySet()){
			
			
			if (qs.getValue().getType().equalsIgnoreCase("Final")){
				
				if(qs.getValue().isRoot()) {
					System.out.println(qs.getValue().get_id());
					qssRestant.add(qs.getValue().get_id());
					recurseFinal(qs.getValue(), qssRestant, qss);
				}
				
			}
			
		}
		System.out.println("***********************");
		System.out.println(qssRestant);
		System.out.println(qssRestant.size());
		qssToRemove.addAll(allQss);
		qssToRemove.removeAll(qssRestant);
		System.out.println(allQss);
		System.out.println(allQss.size());
		System.out.println(qssToRemove);
		System.out.println(qssToRemove.size());
		
		List<QuerySubject> querySubjects = new ArrayList<QuerySubject>();		
		List<QuerySubject> views  = new ArrayList<QuerySubject>();		

		for(String id: allQss) {
			querySubjects.add(qss.get(id));
		}
		
		
		Map<String, List<QuerySubject>> content = new HashMap<String, List<QuerySubject>>();
		content.put("querySubjects", querySubjects);
		content.put("views", views);
		
		Files.write(output, Arrays.asList(Tools.toJSON(content)), StandardCharsets.UTF_8);		
		
	}
	
	private static void recurseFinal(QuerySubject qs, Set<String> qssRestant, Map<String, QuerySubject> qss) {
		for(Relation rel: qs.getRelations()){
			String pkAlias = rel.getPktable_alias();
			if(rel.isFin()) {
				qssRestant.add(pkAlias + "Final");
				recurseFinal(qss.get(pkAlias + "Final"), qssRestant, qss);
			}
			Map<String, Integer> recurseCount = new HashMap<String, Integer>();
			
			for(Entry<String, QuerySubject> rcqs: qss.entrySet()){
	        	recurseCount.put(rcqs.getValue().getTable_alias(), 0);
	        }
			if(rel.isRef()) { 
				recurseRef(pkAlias, "Ref", qss, recurseCount, qssRestant);
			}
			if(rel.isSec()) { 
				recurseRef(pkAlias, "Sec", qss, recurseCount, qssRestant);
			}
			if(rel.isTra()) { 
				recurseRef(pkAlias, "Tra", qss, recurseCount, qssRestant);
			}
		}
	}

	private static void recurseRef(String qsAlias, String qSleftType, Map<String, QuerySubject> qss, Map<String, Integer> recurseCount, Set<String> qssRestant) {
		// TODO Auto-generated method stub
		
		Map<String, Integer> copyRecurseCount = new HashMap<String, Integer>();
		copyRecurseCount.putAll(recurseCount);
		
		QuerySubject query_subject;
		
		if (!qSleftType.equals("Final")) {
			
			query_subject = qss.get(qsAlias + qSleftType);
			
			int j = copyRecurseCount.get(qsAlias);
			if(j == query_subject.getRecurseCount()){
				return;
			}
			copyRecurseCount.put(qsAlias, j + 1);
		}
		
		query_subject = qss.get(qsAlias + qSleftType);
		qssRestant.add(query_subject.get_id());
		
		for(Relation rel: query_subject.getRelations()){
			String pkAlias = rel.getPktable_alias();
			
			if(rel.isRef()) { 
				qssRestant.add(pkAlias + "Ref");
				recurseRef(pkAlias, "Ref" ,qss, copyRecurseCount, qssRestant);	
			}
			if(rel.isSec()) { 
				qssRestant.add(pkAlias + "Sec");
				recurseRef(pkAlias, "Sec" ,qss, copyRecurseCount, qssRestant);	
			}
			if(rel.isTra()) { 
				qssRestant.add(pkAlias + "Tra");
				recurseRef(pkAlias, "Tra" ,qss, copyRecurseCount, qssRestant);	
			}
		}
		
	}

}
