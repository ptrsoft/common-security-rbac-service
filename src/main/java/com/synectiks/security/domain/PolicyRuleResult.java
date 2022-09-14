/**
 * 
 */
package com.synectiks.security.domain;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.codehaus.jettison.json.JSONArray;

import com.synectiks.security.config.IDBConsts;

/**
 * Class to hold and translate Search Response into application class.
 * @author Rajesh
 */
@Entity
@Table(name = IDBConsts.Tbl_Result)
public class PolicyRuleResult extends PSqlEntity {

	private static final long serialVersionUID = 2016053745431745972L;

	private String scrollId;
	private long totalHits;
	private long tookInMillis;
	private long policyId;
	private long ruleId;
	@ElementCollection(targetClass = String.class)
	private List<String> hits;
	private boolean timeOut;
	private boolean terminatedEarly;

	public String getName() {
		return scrollId;
	}

	public long getPolicyId() {
		return policyId;
	}

	public void setPolicyId(long policyId) {
		this.policyId = policyId;
	}

	public long getRuleId() {
		return ruleId;
	}

	public void setRuleId(long ruleId) {
		this.ruleId = ruleId;
	}

	public String getScrollId() {
		return scrollId;
	}

	public void setScrollId(String scrollId) {
		this.scrollId = scrollId;
	}

	public long getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(long totalHits) {
		this.totalHits = totalHits;
	}

	public long getTookInMillis() {
		return tookInMillis;
	}

	public void setTookInMillis(long tookInMillis) {
		this.tookInMillis = tookInMillis;
	}

	public List<String> getHits() {
		return hits;
	}

	public void setHits(List<String> hits) {
		this.hits = hits;
	}

	public boolean isTimeOut() {
		return timeOut;
	}

	public void setTimeOut(boolean timeOut) {
		this.timeOut = timeOut;
	}

	public boolean isTerminatedEarly() {
		return terminatedEarly;
	}

	public void setTerminatedEarly(boolean terminatedEarly) {
		this.terminatedEarly = terminatedEarly;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		builder.append("\"id\": \"");
		builder.append(id);
		builder.append("\", ");
		if (scrollId != null) {
			builder.append("\"scrollId\": \"");
			builder.append(scrollId);
			builder.append("\", ");
		}
		builder.append("\"totalHits\": ");
		builder.append(totalHits);
		builder.append(", \"tookInMillis\": ");
		builder.append(tookInMillis);
		builder.append(", ");
		if (hits != null) {
			builder.append("\"hits\": ");
			builder.append(new JSONArray(hits).toString());
			builder.append(", ");
		}
		builder.append("\"policyId\": \"");
		builder.append(policyId);
		builder.append("\", ");
		builder.append("\"ruleId\": \"");
		builder.append(ruleId);
		builder.append("\", ");
		builder.append("\"timeOut\": ");
		builder.append(timeOut);
		builder.append(", \"terminatedEarly\": ");
		builder.append(terminatedEarly);
		builder.append(" }");
		return builder.toString();
	}
}
