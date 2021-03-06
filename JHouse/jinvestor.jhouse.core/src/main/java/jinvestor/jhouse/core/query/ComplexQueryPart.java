package jinvestor.jhouse.core.query;

/*
 * #%L
 * jinvestor.parent
 * %%
 * Copyright (C) 2014 Michael Scott Knapp
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;

public class ComplexQueryPart implements QueryPart {
	private List<? extends QueryPart> subParts = new ArrayList<QueryPart>();
	private boolean and;
	
	private ComplexQueryPart(ComplexQueryPartBuilder builder) {
		List<QueryPart> sps = new ArrayList<QueryPart>();
		for (QueryPartBuilder b : builder.subParts) {
			sps.add(b.build());
		}
		this.subParts = Collections.unmodifiableList(sps);
		this.and = builder.and;
	}
	
	public static ComplexQueryPartBuilder builder() {
		return new ComplexQueryPartBuilder();
	}
	
	public static final class ComplexQueryPartBuilder implements QueryPartBuilder {
		private List<QueryPartBuilder> subParts = new ArrayList<QueryPartBuilder>();
		private boolean and;
		
		public ComplexQueryPartBuilder() {
			 subParts = new ArrayList<QueryPartBuilder>();
		}
		
		public ComplexQueryPartBuilder(boolean and) {
			 subParts = new ArrayList<QueryPartBuilder>();
			 this.and = and;
		}

		public ComplexQueryPartBuilder(ComplexQueryPartBuilder original) {
			this.subParts = new ArrayList<QueryPartBuilder>(original.subParts);
			this.and = original.and;
		}
		
		public ComplexQueryPartBuilder add(QueryPartBuilder part) {
			this.subParts.add(part);
			return this;
		}
		
		public ComplexQueryPartBuilder and(boolean and) {
			this.and=and;
			return this;
		}
		
		public ComplexQueryPart build() {
			return new ComplexQueryPart(this);
		}
	}
	
	public ComplexQueryPart(boolean and,List<? extends QueryPart> subParts) {
		this.and = and;
		this.subParts = subParts;
	}
	
	@Override
	public boolean passes(Object house) {
		int passCount = 0;
		int failCount = 0;
		for (QueryPart sp : subParts) {
			if (sp.passes(house)) {
				passCount++;
			} else {
				failCount++;
			}
		}
		if (and) {
			return failCount==0;
		} else { // or operation
			return passCount>0;
		}
	}
	
	public String toString() {
		List<String> ss = new ArrayList<String>();
		for (QueryPart qp : subParts) {
			ss.add(qp.toString());
		}
		return "("+StringUtils.join(ss,and?" and ":" or ")+")";
	}
	
	public Filter toFilter(byte[] family) {
		FilterList f = new FilterList();
		for (QueryPart p : this.subParts) {
			f.addFilter(p.toFilter(family));
		}
		return f;
	}
}