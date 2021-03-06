package com.dma.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuerySubject {
	
	String _id = "";
	String _ref = null;
	String table_name = "";
	String table_alias = "";
	String type = "";
	boolean visible = false;
	String filter = "";
	String secFilter = "";
	String Label = "";
	String description = "";
	boolean linker = false;
	Set<String> linker_ids =  new HashSet<String>();
	List<Field> fields = new ArrayList<Field>();
	List<Relation> relations = new ArrayList<Relation>();
	Map<String, Integer> relationCount = new HashMap<String, Integer>(); 
	int recurseCount = 1;
	long recCount= 0L;
	String folder = "";
	Map<String, String> labels = new HashMap<String, String>();
	Map<String, String> descriptions = new HashMap<String, String>();
	String merge = "";
	Map<String, Filter> filters = new HashMap<String, Filter>(); 
	String hiddenQuery = "SELECT $TABLE.$FIELD FROM $TABLE WHERE $TABLE.$FIELD IS NOT NULL AND $TABLE.$FIELD != '0'";
	boolean tableExists = true;
	boolean root = false;
	
	
	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	public Map<String, String> getLabels() {
		return labels;
	}
	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}
	public Map<String, String> getDescriptions() {
		return descriptions;
	}
	public void setDescriptions(Map<String, String> descriptions) {
		this.descriptions = descriptions;
	}
	public int getRecurseCount() {
		return recurseCount;
	}
	public void setRecurseCount(int recurseCount) {
		this.recurseCount = recurseCount;
	}
	public long getRecCount() {
		return recCount;
	}
	public void setRecCount(long recCount) {
		this.recCount = recCount;
	}
	public void incRelationCount(String qs_id){
		if(relationCount.get(qs_id) == null){
			relationCount.put(qs_id, new Integer(1));
		}
		else{
			int count = relationCount.get(qs_id);
			relationCount.put(qs_id, ++count);
		}
	}
	public Map<String, Integer> getRelationCount() {
		return relationCount;
	}
	public void setRelationCount(Map<String, Integer> relationCount) {
		this.relationCount = relationCount;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String get_ref() {
		return _ref;
	}
	public void set_ref(String _ref) {
		this._ref = _ref;
	}
	public String getTable_name() {
		return table_name;
	}
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}
	public String getTable_alias() {
		return table_alias;
	}
	public void setTable_alias(String table_alias) {
		this.table_alias = table_alias;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public String getSecFilter() {
		return secFilter;
	}
	public void setSecFilter(String secFilter) {
		this.secFilter = secFilter;
	}
	public String getLabel() {
		return Label;
	}
	public void setLabel(String label) {
		Label = label;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isLinker() {
		return linker;
	}
	public void setLinker(boolean linker) {
		this.linker = linker;
	}
	public Set<String> getLinker_ids() {
		return linker_ids;
	}
	public void setLinker_ids(Set<String> linker_ids) {
		this.linker_ids = linker_ids;
	}
	public void addLinker_id(String linker_id){
		this.linker_ids.add(linker_id);
	}
	public List<Field> getFields() {
		return fields;
	}
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	public void addField(Field field){
		this.fields.add(field);
	}
	public void addFields(List<Field> fields){
		this.fields.addAll(fields);
	}
	public List<Relation> getRelations() {
		return relations;
	}
	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	} 
	public void addRelation(String _id, Relation relation){
		this.relations.add(relation);
	}
	public void addRelations(List<Relation> relations){
		this.relations.addAll(relations);
	}
	public String getMerge() {
		return merge;
	}
	public void setMerge(String merge) {
		this.merge = merge;
	}
	public Map<String, Filter> getFilters() {
		return filters;
	}
	public void setFilters(Map<String, Filter> filters) {
		this.filters = filters;
	}
	public String getHiddenQuery() {
		return hiddenQuery;
	}
	public void setHiddenQuery(String hiddenQuery) {
		this.hiddenQuery = hiddenQuery;
	}
	public boolean isTableExists() {
		return tableExists;
	}
	public void setTableExists(boolean tableExists) {
		this.tableExists = tableExists;
	}
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
	
}
