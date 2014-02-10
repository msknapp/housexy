package housexy.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

public class QueryNode {
	public QueryNode parent=null;
	public List<Object> subs = new ArrayList<Object>();

	public static QueryNode parseNode(String query) {
		Stack<StringBuilder> stack = new Stack<StringBuilder>();
		QueryNode rootNode = new QueryNode();
		QueryNode currentNode = rootNode;
		boolean inQuote=false;
		char quoteType='x';
		stack.add(new StringBuilder());
		for (int index =0;index<query.length();index++) {
			char c = query.charAt(index);
			if (inQuote) {
				if (quoteType==c) {
					inQuote=false;
				}
				stack.peek().append(c);
			} else {
				if ('('==c) {
					String textSoFar = stack.peek().toString();
					if (!textSoFar.isEmpty()) {
						currentNode.subs.add(textSoFar);
						stack.pop();
						stack.add(new StringBuilder());
					}
					stack.add(new StringBuilder());
					QueryNode qn = new QueryNode();
					currentNode.subs.add(qn);
					qn.parent=currentNode;
					currentNode = qn;
				} else if (')'==c) {
					StringBuilder sb = stack.pop();
					String text = sb.toString();
					currentNode.subs.add(text);
					currentNode = currentNode.parent;
				} else { 
					if ('"'==c) {
						inQuote=true;
						quoteType=c;
					} else if ('\''==c) {
						inQuote=true;
						quoteType=c;
					}
					stack.peek().append(c);
				}
			}
		}
		StringBuilder sb = stack.pop();
		String text = sb.toString();
		currentNode.subs.add(text);
		return rootNode;
	}
	
	public QueryPart toQueryPart() {
		List<QueryPart> queryParts = new ArrayList<QueryPart>();
		boolean andOverall = true;
		for (Object o : subs) {
			if (o instanceof String) {
				String s = (String)o;
				if (Pattern.matches(".*and\\s*", o.toString())) {
					andOverall=true;
					s = s.replaceAll("and\\s*$", "").trim();
				}
				if (Pattern.matches(".*or\\s*", o.toString())) {
					andOverall=false;
					s = s.replaceAll("or\\s*$", "").trim();
				}
				if (Pattern.matches("\\s*or.*", o.toString())) {
					andOverall=false;
					s = s.replaceAll("^\\s*or", "").trim();
				}
				if (Pattern.matches("\\s*and.*", o.toString())) {
					andOverall=true;
					s = s.replaceAll("^\\s*and", "").trim();
				}
				if (s.toLowerCase().contains("and")) {
					String[] parts = s.split("and");
					List<SimpleQueryPart> sqps = new ArrayList<>();
					for (String part : parts) {
						SimpleQueryPart sqp = new SimpleQueryPart(part);
						sqps.add(sqp);
					}
					ComplexQueryPart cqp = new ComplexQueryPart(true, sqps);
					queryParts.add(cqp);
				} else if (o.toString().toLowerCase().contains("or")) {
					String[] parts = s.split("or");
					List<SimpleQueryPart> sqps = new ArrayList<>();
					for (String part : parts) {
						SimpleQueryPart sqp = new SimpleQueryPart(part.trim());
						sqps.add(sqp);
					}
					ComplexQueryPart cqp = new ComplexQueryPart(false, sqps);
					queryParts.add(cqp);
				} else {
					queryParts.add(new SimpleQueryPart(s));
				}
			} else {
				queryParts.add(((QueryNode)o).toQueryPart());
			}
		}
		if (queryParts.size()==1) {
			return queryParts.get(0);
		}
		return new ComplexQueryPart(andOverall, queryParts);
	}
}