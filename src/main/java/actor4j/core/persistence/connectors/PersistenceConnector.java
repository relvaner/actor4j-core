/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actor4j.core.persistence.connectors;

import actor4j.core.ActorSystem;

public abstract class PersistenceConnector {
	protected String host;
	protected int port; 
	protected String databaseName;
	
	public PersistenceConnector(String host, int port, String databaseName) {
		this.host = host;
		this.port = port;
		this.databaseName = databaseName;
	}
	
	public abstract void open();
	public abstract void close();
	
	
	public abstract PersistenceAdapter createAdapter(ActorSystem parent);
}
