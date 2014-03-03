package jinvestor.jhouse.core.util;

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

import jinvestor.jhouse.core.query.QueryNode;
import jinvestor.jhouse.core.query.QueryPart;
import jinvestor.jhouse.core.House;

public final class HouseQueryUtil {
	private HouseQueryUtil(){}
	
	public static boolean matches(House house,String query) {
		QueryPart qp = QueryNode.parseNode(query).toQueryPart();
		return qp.passes(house);
	}
	
}