package stock.download.ph;

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

import java.io.IOException;

import stock.core.TimeSpan;
import stock.download.OutputWithMeta;

public interface PHOutputBuilder<T> {
	String determineUrl(T stockSymbol,
			TimeSpan timeSpan);
	OutputWithMeta buildOutputStream(T stockSymbol,
			TimeSpan timeSpan,String url) throws IOException;
	void handleFailure(T stockSymbol, TimeSpan timeSpan,
			String url);
}
