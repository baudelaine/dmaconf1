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

		Path path = Paths.get("/home/fr054721/dmaconf/model-decoche.json");
		Path output = Paths.get("/home/fr054721/dmaconf/model-decoche-res.json");
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
		
		for(QuerySubject qs: qssList) {
			qss.put(qs.get_id(), qs);
			allQss.add(qs.get_id());
		}

		for(Entry<String, QuerySubject> qs: qss.entrySet()){
			
			
			if (qs.getValue().getType().equalsIgnoreCase("Final")){
				
				String qsAlias = qs.getValue().getTable_alias();  // table de gauche, celle ou tu es actuellement
				String gDirName = ""; // prefix qu'on cherche, il vaut cher
				String qsFinalName = qs.getValue().getTable_alias();   //CONSTANTE, nom du QS final auquel l'arbre ref est accroché, le tronc, on peut le connaitre à tout moment de f1
//				String table = qs.getValue().getTable_name();
				String qSleftType = "Final";
				
				for(Relation rel: qs.getValue().getRelations()){
					if(rel.isFin()) {
						String pkAlias = rel.getPktable_alias();
						System.out.println(pkAlias);
					}
				}
				
				Set<String> set = qs.getValue().getLinker_ids();
				for (String s : set) {
					if (s.equals("Root")) {
						System.out.println(qs.getValue().get_id() + " ids : " + s);
						qssRestant.add(qs.getValue().get_id());
						recurseFinal(qs.getValue(), qssRestant, qss);
					}
				}				
				
				
				

				
				
//				recurse0(qsAlias, gDirName, qsFinalName, qSleftType, qss, recurseCount, qssLeft);
				
				
			}
			
		}
		System.out.println("***********************");
		System.out.println(qssRestant);
		System.out.println(qssRestant.size());
		System.out.println(allQss);
		System.out.println(allQss.size());
		allQss.removeAll(qssRestant);
		System.out.println(allQss);
		System.out.println(allQss.size());
//		
//		List<QuerySubject> querySubjects = new ArrayList<QuerySubject>();		
//		List<QuerySubject> views  = new ArrayList<QuerySubject>();		
//
//		for(String id: qssLeft) {
//			querySubjects.add(qss.get(id));
//		}
//		
//		
//		Map<String, List<QuerySubject>> content = new HashMap<String, List<QuerySubject>>();
//		content.put("querySubjects", querySubjects);
//		content.put("views", views);
//		
//		Files.write(output, Arrays.asList(Tools.toJSON(content)), StandardCharsets.UTF_8);		
		
	}
	
	private static void recurseFinal(QuerySubject qs, Set<String> qssRestant, Map<String, QuerySubject> qss) {
		for(Relation rel: qs.getRelations()){
			String pkAlias = rel.getPktable_alias();
			if(rel.isFin()) {
				qssRestant.add(pkAlias + "Final");
				recurseFinal(qss.get(pkAlias + "Final"), qssRestant, qss);
			}
			if(rel.isRef()) { 
				Map<String, Integer> recurseCount = new HashMap<String, Integer>();
				
				for(Entry<String, QuerySubject> rcqs: qss.entrySet()){
		        	recurseCount.put(rcqs.getValue().getTable_alias(), 0);
		        }
				System.out.println(pkAlias + "" + qs.getType());
				recurseRef(pkAlias, "Ref", qss, recurseCount, qssRestant);
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
		
//				if(rel.getKey_type().equalsIgnoreCase("P") || rel.isNommageRep()){
//					System.out.println(pkAlias + qSleftType);
//					qss.get(pkAlias + qSleftType);
//				}
//				else{
//					System.out.println(rel.getAbove() + qSleftType);
//					qss.get(rel.getAbove() + qSleftType);
//				}					
				qssRestant.add(pkAlias + "Ref");
				recurseRef(pkAlias, "Ref" ,qss, copyRecurseCount, qssRestant);	
				
			}
		}
		
	}

}
