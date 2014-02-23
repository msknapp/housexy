package stock.download;

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

import java.text.ParseException;
import java.util.List;

import org.apache.commons.collections4.IteratorUtils;
import org.junit.Assert;
import org.junit.Test;

import stock.core.TimeSpan;

public class TimeSpanIterableTest {

	
	@Test
	public void iterate() throws ParseException {
		TimeSpan ts = new TimeSpan("2000-01-01");
		TimeSpanIterable tsi = new TimeSpanIterable(ts, 60);
		List<TimeSpan> tss = IteratorUtils.toList(tsi.iterator());
		Assert.assertTrue(tss.size()>20);
//		for (TimeSpan t : tss) {
//			System.out.println(t);
//		}
	}
}
