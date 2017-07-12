/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.actors;

import java.util.UUID;

public interface PersistenceId {
	public UUID persistenceId();
}
